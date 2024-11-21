
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

    public ByteBuffer GetPage(PageId pageId) {
        // Recherche si la page est déjà chargée dans un buffer
        for (Buffer buffer : bufferPool) {
            if (buffer.getPageId().equals(pageId)) {
                System.out.println("La page existe dans le bufferPool ");
                buffer.incrementPinCount();
                updateBufferOrder(buffer);
                return buffer.getData(); // Retourne le buffer
            }
        }

        //si la page n'est pas dans les buffers et que le pool est plein
        if(bufferPool.size()>=config.getBm_buffercount()) {
            System.out.println("La page existe dans le bufferPool mais il est plein ");
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
        //Si le pool n'est pas plein, on charge la page depuis le disque
        System.out.println("La page est chargée depuis le disque  ");
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


    public void SetCurrentReplacementPolicy(String policy) {
        if (policy.equals("LRU") || policy.equals("MRU")) {
            currentPolicy = policy;
            System.out.println("Politique de remplacement changée à : " + policy);
        } else {
            throw new IllegalArgumentException("Politique invalide : " + policy + ". Utilisez 'LRU' ou 'MRU'.");
        }
    }
    public void freePageId(PageId pageId, boolean valdirty){
        for (Buffer buffer : bufferPool) {
            if((buffer.getPageId() != null) && (buffer.getPageId().getPageIdx() == pageId.getPageIdx()) &&(buffer.getPageId().getFileIdx() == pageId.getFileIdx()) ){
                buffer.setDirty();
                buffer.decrementerLePinCount();
            }

        }
    }

    public void flushBuffers() throws Exception{
        for (Buffer buffer : bufferPool) {
            if (buffer.getPinCount()>0){
                throw new Exception("Le flush des pages est interrompu : La page "+buffer.getPageId().getPageIdx() +" du fichier " +
                        buffer.getPageId().getFileIdx()+ " est encore en cours d'utilisation");
            }
            if (buffer.getDirty()) {
                diskManager.WritePage(buffer.getPageId(), buffer.getData());
            }
            buffer.reset();
            System.out.println("Le flush a bien été effectué");
        }
    }
}

