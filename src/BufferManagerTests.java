import java.nio.ByteBuffer;

public class BufferManagerTests {
    public static void main(String[] args) {
        try {
            // Configuration et initialisation
            DBConfig config = new DBConfig("../DB", 4096, 8192, 3, "LRU");
            DiskManager diskManager = new DiskManager(config);
            BufferManager bufferManager = new BufferManager(config, diskManager);

            // Étape 1: Allocation et écriture de données
            System.out.println("\n[Étape 1] Allocation et écriture de données");
            PageId pageId = diskManager.AllocPage(); // marche
            ByteBuffer writeBuffer = ByteBuffer.allocate(config.getPageSize());
            String data = "Données de test pour la page";
            writeBuffer.put(data.getBytes());
            diskManager.WritePage(pageId, writeBuffer); //marche
            System.out.println("Page allouée et données écrites: " + pageId);

            // Étape 2: Lecture et vérification des données
            System.out.println("\n[Étape 2] Lecture et vérification des données");
            ByteBuffer readBuffer = bufferManager.GetPage(pageId);
            bufferManager.bufferPoolState();
            byte[] readData = new byte[data.length()];
            readBuffer.get(readData);
            System.out.println("Données lues depuis la page: " + new String(readData));

            // Étape 3: Modification, libération et vérification
            System.out.println("\n[Étape 3] Modification, libération et vérification");
            readBuffer.position(0);
            readBuffer.put((byte) 1); // Modification des données
            bufferManager.FreePage(pageId, true); // Libérer la page avec dirty bit
            Buffer buffer = bufferManager.getBufferByPageId(pageId);

            assert buffer.getDirty() : "Le dirty bit n'est pas correctement défini!";
            assert buffer.getPinCount() == 0 : "Le pin count devrait être 0 après la libération.";

            System.out.println("état du buffer pool après freepage");
            bufferManager.bufferPoolState();

            // Étape 4: Test de flushBuffers
            System.out.println("\n[Étape 4] Test de flushBuffers");
            bufferManager.flushBuffers();
            System.out.println("Buffers flushés avec succès.");
            bufferManager.bufferPoolState();

            //probably le flush n'ecrit pas correctement les pages modifiées

            // Vérification finale: Lecture après flush
            System.out.println("\n[Vérification finale] Lecture après flush");
            ByteBuffer finalBuffer = ByteBuffer.allocate(config.getPageSize());
            diskManager.ReadPage(pageId, finalBuffer);
            byte[] finalData = new byte[data.length()];
            finalBuffer.get(finalData);
            System.out.println("Données finales après flush: " + new String(finalData));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
