import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            writeBuffer.put(dataBytes); // Écrire les données dans le buffer
            writeBuffer.flip(); // Réinitialiser la position avant d'écrire sur le disque
            diskManager.WritePage(pageId, writeBuffer); // marche
            System.out.println("Page allouée et données écrites: " + pageId);

            // Étape 2: Lecture et vérification des données
            System.out.println("\n[Étape 2] Lecture et vérification des données");
            ByteBuffer readBuffer = bufferManager.GetPage(pageId);
            bufferManager.bufferPoolState();
            byte[] readData = new byte[dataBytes.length]; // Assurez-vous que la taille est correcte
            readBuffer.get(readData); // Lire les données du buffer
            String readString = new String(readData, StandardCharsets.UTF_8); // Convertir en chaîne
            System.out.println("Données lues depuis la page: " + readString);

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

            // Vérification finale: Lecture après flush
            System.out.println("\n[Vérification finale] Lecture après flush");
            ByteBuffer finalBuffer = ByteBuffer.allocate(config.getPageSize());
            diskManager.ReadPage(pageId, finalBuffer);
            byte[] finalData = new byte[dataBytes.length];
            finalBuffer.get(finalData);
            String finalString = new String(finalData, StandardCharsets.UTF_8); // Convertir en chaîne
            System.out.println("Données finales après flush: " + finalString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}