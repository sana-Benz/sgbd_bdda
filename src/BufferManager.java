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
		System.out.println("On demande de mettre la page"+pageId+"dans le bufferPool");

		for (Buffer buffer : bufferPool) {
			if (buffer.getPageId().equals(pageId)) {
				buffer.incrementPinCount();
				updateBufferOrder(buffer);
				System.out.println("la page demandée est déjà dans le bufferpool "+ buffer);
				return buffer.getData(); // Retourne le buffer
			}
		}
		System.out.println("La page demandée"+pageId+" n'est pas dans le bufferPool");
		// on cherche si on trouve un buffer invalide on le met comme victime
		for (Buffer buffer : bufferPool) {
			if (buffer.getPageId().equals(pageId)&& !buffer.isValid()) {
				System.out.println("On a choisi comme victime ce buffer invalide "+ buffer);
				ByteBuffer newData= loadPageFromDisk(pageId);
				buffer.setData(newData);
				buffer.incrementPinCount();
				buffer.setValid(true);
				buffer.setDirty(false);
				updateBufferOrder(buffer);
				return buffer.getData();


			}
		}

		//sinon si le bufferpool est plein avec des buffers valides

		//si la page n'est pas dans les buffers et que le pool est plein avec des buffers valides on doit choisir une victime
		if(bufferPool.size()>=config.getBm_buffercount()) {
			System.out.println("le bufferPool est plein avec des buffers valides");
			System.out.println("la page demandée n'est pas dans bufferPool et le pool est plein");
			Buffer bufferToReplace= selectbufferToReplace();

			if(bufferToReplace.getDirty()) {
				System.out.println("la page à écraser dans le bufferpool est modifiée");
				diskManager.WritePage(bufferToReplace.getPageId(), bufferToReplace.getData());
				System.out.println("Fin de l'écriture de la page à écraser "+bufferToReplace.getPageId()+"dans le disque");
			}

			bufferToReplace.setPageId(pageId);
			System.out.println("la page à écraser dans le bufferpool n'a pas été modifiée "+bufferToReplace.getPageId());
			ByteBuffer newData= loadPageFromDisk(pageId);
			bufferToReplace.setData(newData);// on met à jour les données du buffer
			bufferToReplace.reset();//rénitialise le pin_count et le dirty flag et met le buffer comme invalide
			updateBufferOrder(bufferToReplace);

			return bufferToReplace.getData();
		}
		System.out.println("le pool n'est pas plein");
		// Si le pool n'est pas vide, on charge la page depuis le disque
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
				System.out.println("La page "+ pageId+" a été libérée. pincount= "+buffer.getPinCount()+"dirty= "+buffer.getDirty());
				return;
			}
		}

		// Si la page n'est pas trouvée dans le buffer pool, lever une exception
		throw new Exception("Page " + pageId + " non trouvée dans le buffer pool. Impossible de la libérer");
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
				System.out.println("écriture de la page "+buffer.getPageId()+"car son dirty bit est "+buffer.getDirty());
				buffer.setDirty(false);
			}
			if (buffer.getPinCount() == 0) {
				System.out.println("la page n'est plus en cours d'utilisation "+buffer.getPageId() +" flush de la page");
				buffer.reset();
			} else {
				System.out.println("La page"+buffer.getPageId()+" est encore en cours d'utilisation "+buffer.getPinCount());
				System.out.println("Il faut libérer la page "+buffer.getPageId()+" pour l'enlever du bufferPool");
			}
		}
	}

	public Buffer getBufferByPageId(PageId pageId) {
		for (Buffer buffer : bufferPool) {
			if (buffer.getPageId().equals(pageId)) {
				return buffer;
			}
		}
		return null;

	}

	public void bufferPoolState() {
		for (Buffer buffer : bufferPool) {
			System.out.println("Voici l'état du bufferPool");
			System.out.println(buffer);
			System.out.println("Fin de l'état du bufferPool");

		}


	}
}