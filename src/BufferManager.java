import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;

public class BufferManager {
    private final DBConfig config;
    private final DiskManager diskManager;
    private final LinkedList<Buffer> bufferPool; // Liste des buffers
    private String currentPolicy;

    public BufferManager(DBConfig config, DiskManager diskManager) {
        this.config = config;
        this.diskManager = diskManager;
        this.bufferPool= new LinkedList<>();
        this.currentPolicy = config.getBm_policy(); // Récupération de la politique depuis DBConfig
    }

	private Buffer selectBufferToReplace() {
		for (Buffer buffer : bufferPool) {
			if (buffer.getPinCount() == 0) {
				logBufferPoolState();
				return buffer;
			}
		}
		throw new IllegalStateException("No buffer available for replacement. All buffers are pinned.");
	}



	/**
	 *Cette méthode retourne un des buffers gérés par le BufferManager, rempli avec le contenu de la page
	 * désignée par l’argument pageId.
	 * @param pageId id de la page à charger
	 * @return ByteBuffer qui contient le contenu de la page chargée
	 */
	public ByteBuffer GetPage(PageId pageId) throws Exception {
		// Log the page access attempt
		System.out.println("Getting Page: FileIdx = " + pageId.getFileIdx() + ", PageIdx = " + pageId.getPageIdx());

		// Search for the page in the buffer pool
		for (Buffer buffer : bufferPool) {
			if (buffer.getPageId().equals(pageId)) {
				buffer.incrementPinCount(); // Increment pin count for buffer
				updateBufferOrder(buffer);  // Update buffer order for replacement policy
				ByteBuffer existingBuffer = buffer.getData();
				existingBuffer.position(0); // Reset position for reading
				existingBuffer.limit(existingBuffer.capacity()); // Ensure the limit is correct
				System.out.println("Buffer Retrieved Content: " + Arrays.toString(Arrays.copyOf(existingBuffer.array(), 16)));
				logBufferPoolState(); // Log the buffer pool state for debugging
				return existingBuffer; // Return the existing buffer
			}
		}

		// If the page is not in the buffer pool and the pool is full
		if (bufferPool.size() >= config.getBm_buffercount()) {
			Buffer bufferToReplace = selectBufferToReplace(); // Select a buffer for replacement

			// Check if a buffer is available for replacement
			if (bufferToReplace == null) {
				throw new IllegalStateException("No buffer available for replacement.");
			}

			// If the buffer is dirty, write its content back to disk
			if (bufferToReplace.getDirty()) {
				System.out.println("Writing dirty buffer to disk: " + bufferToReplace.getPageId());
				diskManager.WritePage(bufferToReplace.getPageId(), bufferToReplace.getData());
			}

			// Replace the buffer with the new page
			bufferToReplace.setPageId(pageId);
			ByteBuffer newData = loadPageFromDisk(pageId); // Load the new page from disk
			bufferToReplace.setData(newData);             // Update buffer data
			bufferToReplace.reset();                      // Reset pin_count and dirty flag
			updateBufferOrder(bufferToReplace);           // Update buffer order for replacement policy
			System.out.println("Buffer Retrieved Content (New Replacement): " + Arrays.toString(Arrays.copyOf(newData.array(), 16)));
			logBufferPoolState(); // Log the buffer pool state for debugging
			return bufferToReplace.getData(); // Return the new buffer's data
		}

		// If the pool is not full, load the page from disk
		ByteBuffer newBuffer = loadPageFromDisk(pageId);
		Buffer newBufferObj = new Buffer(pageId, newBuffer);
		bufferPool.add(newBufferObj); // Add the new buffer to the pool
		updateBufferOrder(newBufferObj); // Update buffer order for replacement policy
		newBufferObj.incrementPinCount(); // Increment pin count for the new buffer
		System.out.println("Buffer Retrieved Content (New Addition): " + Arrays.toString(Arrays.copyOf(newBuffer.array(), 16)));
		logBufferPoolState(); // Log the buffer pool state for debugging
		return newBufferObj.getData(); // Return the new buffer's data
	}





	private ByteBuffer loadPageFromDisk(PageId pageId) {
		ByteBuffer buffer = ByteBuffer.allocate(config.getPageSize());
		diskManager.ReadPage(pageId, buffer);// on utilise diskManager pour lire la  page 
		buffer.flip();// réinitialise la position
		return buffer;  
	}

	public void updateBufferOrder(Buffer buffer) {
		bufferPool.remove(buffer);
		bufferPool.add(buffer); // Move to the end (LRU policy)
		System.out.println("Buffer pool updated for PageId: " + buffer.getPageId());
	}




	/**
	 * Cette méthode décrémente le pin_count et actualise le flag dirty (et aussi
	 * potentiellement actualise des informations concernant la politique de remplacement).
	 * @param pageId
	 * @param valdirty
	 * @throws Exception
	 */
	public void FreePage(PageId pageId, boolean valdirty) throws Exception {
		// Search for the page in the buffer pool
		for (Buffer buffer : bufferPool) {
			if (buffer.getPageId().equals(pageId)) {
				// Decrement pin count
				buffer.decrementPinCount();

				// Pin count cannot go below 0
				if (buffer.getPinCount() < 0) {
					throw new Exception("Pin count cannot be negative for page: " + pageId);
				}

				// Update the dirty flag if needed
				if (valdirty) {
					buffer.setDirty(true);
				}
				System.out.println("Page unpinned successfully: " + pageId);
				logBufferPoolState();
				return;
			}
		}

		// If the page is not found, indicate it was not in the buffer pool
		throw new Exception("Page " + pageId + " not found in the buffer pool.");
	}




	/**
	 * Cette méthode change la politique de remplacement courante, et a la priorité par
	 * rapport à la politique spécifiée par la DBConfig passée au constructeur.
	 * @param policy
	 */
	public void SetCurrentReplacementPolicy(String policy) {
        if (policy.equals("LRU") || policy.equals("MRU")) {
            currentPolicy = policy;
            System.out.println("Politique de remplacement changée à : " + policy);
        } else {
            throw new IllegalArgumentException("Politique invalide : " + policy + ". Utilisez 'LRU' ou 'MRU'.");
        }
    }

	/**
	 * Cette méthode s'occupe de l’écriture de toutes les pages dont le flag dirty = 1 sur disque en utilisant le
	 * DiskManager.
	 *
	 * Elle s'occupe aussi de la remise à 0 de tous les flags et contenus des buffers. Après appel de cette
	 * méthode, le BufferManager repart avec des buffers où il n’y a aucun contenu de chargé
	 * comme dans son état initial après appel du constructeur.
	 */
	public void flushBuffers() throws Exception {
		for (Buffer buffer : bufferPool) {
			if (buffer.getDirty() && buffer.getPinCount() == 0) {
				diskManager.WritePage(buffer.getPageId(), buffer.getData());
				buffer.setDirty(false);
				System.out.println("Flushed buffer for PageId: " + buffer.getPageId());
				logDirtyBuffersState();
			} else if (buffer.getPinCount() > 0) {
				System.err.println("Cannot flush buffer for PageId: " + buffer.getPageId() + " - still pinned.");
				throw new Exception("Cannot flush buffers: Page " + buffer.getPageId() + " is still pinned.");
			}
		}
	}




	public void MarkDirty(PageId pageId) {
		for (Buffer buffer : bufferPool) {
			if (buffer.getPageId().equals(pageId)) {
				buffer.setDirty(true);
				System.out.println("Page marked as dirty: " + pageId);
				return;
			}
		}
		throw new IllegalStateException("Buffer for PageId not found: " + pageId);
	}

	private void logBufferPoolState() {
		System.out.println("Current Buffer Pool State:");
		for (Buffer buffer : bufferPool) {
			System.out.println("PageId: " + buffer.getPageId() + ", PinCount: " + buffer.getPinCount() + ", Dirty: " + buffer.getDirty());
		}
		System.out.println("End of Buffer Pool State.");
	}

	private void logDirtyBuffersState() {
		System.out.println("Dirty Buffers State:");
		for (Buffer buffer : bufferPool) {
			if (buffer.getDirty()) {
				System.out.println("PageId: " + buffer.getPageId() + ", PinCount: " + buffer.getPinCount());
			}
		}
		System.out.println("End of Dirty Buffers State.");
	}

} 

