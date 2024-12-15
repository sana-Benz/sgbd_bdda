import java.nio.ByteBuffer;
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

	private Buffer selectbufferToReplace() {
		Buffer bufferToReplace=null;
		if(currentPolicy.equals("LRU")) {
			bufferToReplace=bufferPool.getFirst();
		}else if (currentPolicy.equals("MRU")) {
			bufferToReplace=bufferPool.getLast();
		}
		//si le buffer est modifié, on l'écrit sur le disque
		if(bufferToReplace.getDirty()) {
			diskManager.WritePage(bufferToReplace.getPageId(), bufferToReplace.getData());
		}
		return bufferToReplace;// Retourne le buffer qui doit etre remplacé
	}

	/**
	 *Cette méthode retourne un des buffers gérés par le BufferManager, rempli avec le contenu de la page
	 * désignée par l’argument pageId.
	 * @param pageId id de la page à charger
	 * @return ByteBuffer qui contient le contenu de la page chargée
	 */
	public ByteBuffer GetPage(PageId pageId) {
		// Recherche si la page est déjà chargée dans un buffer
		for (Buffer buffer : bufferPool) {
			if (buffer.getPageId().equals(pageId)) {
				buffer.incrementPinCount();
				updateBufferOrder(buffer);
				return buffer.getData(); // Retourne le buffer
			}
		}

		//si la page n'est pas dans les buffers et que le pool est plein
		if(bufferPool.size()>=config.getBm_buffercount()) {
			Buffer bufferToReplace= selectbufferToReplace();

			if(bufferToReplace.getDirty()) {
				diskManager.WritePage(bufferToReplace.getPageId(), bufferToReplace.getData());
			}

			bufferToReplace.setPageId(pageId);
			ByteBuffer newData= loadPageFromDisk(pageId);
			bufferToReplace.setData(newData);// on met à jour les données du buffer
			bufferToReplace.reset();//rénitialise le pin_count et le dirty flag
			updateBufferOrder(bufferToReplace);

			return bufferToReplace.getData();
		}
		//Si le pool n'est pas vide, on charge la page depuis le disque
		ByteBuffer newBuffer = loadPageFromDisk(pageId);
		Buffer newBufferObj= new Buffer(pageId, newBuffer);
		bufferPool.add(newBufferObj);
		//updateBufferOrder(newBufferObj);
		newBufferObj.incrementPinCount();

		return newBufferObj.getData();

	}

	private ByteBuffer loadPageFromDisk(PageId pageId) {
		ByteBuffer buffer = ByteBuffer.allocate(config.getPageSize());
		diskManager.ReadPage(pageId, buffer);// on utilise diskManager pour lire la  page
		buffer.flip();// réinitialise la position
		return buffer;
	}

	private void updateBufferOrder(Buffer buffer) {
		if(currentPolicy.equals("LRU")) {
			bufferPool.remove(buffer);//supprime l'ancien
			bufferPool.addLast(buffer);// ajoute a la fin pour dire qu'il a été utilisé récemment
		}else if (currentPolicy.equals("MRU")) {
			bufferPool.remove(buffer);// supprime l'ancien
			bufferPool.addFirst(buffer);//ajoute au début et devient le plus récemment utilisé
		}
	}

	/**
	 * Cette méthode décrémente le pin_count et actualise le flag dirty (et aussi
	 * potentiellement actualise des informations concernant la politique de remplacement).
	 * @param pageId
	 * @param valdirty
	 * @throws Exception
	 */
	public void FreePage(PageId pageId, boolean valdirty) throws Exception {
		// Rechercher si la page est déjà chargée dans un buffer
		for (Buffer buffer : bufferPool) {
			if (buffer.getPageId().equals(pageId)) {
				// Décrémenter le pin_count
				buffer.decrementPinCount();

				// Si le pin_count devient inférieur à 0, lever une exception
				if (buffer.getPinCount() < 0) {
					throw new Exception("Pin count ne peut pas être négatif.");
				}

				// Mettre à jour le flag dirty directement
				if (valdirty) {
					buffer.setDirty(true); // Modifie directement l'attribut dirty
				}

				// Si le pin_count est à 0, cette page peut être remplacée
				return;
			}
		}

		// Si la page n'est pas trouvée dans le buffer pool, lever une exception
		throw new Exception("Page " + pageId + " non trouvée dans le buffer pool.");
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
	public void flushBuffers() {
		for (Buffer buffer : bufferPool) {
			if (buffer.getDirty()) {
				diskManager.WritePage(buffer.getPageId(), buffer.getData());
			}
			if (buffer.getPinCount() == 0) {
				buffer.reset();
			}
		}
	}
}
