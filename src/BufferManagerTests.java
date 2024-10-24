import java.nio.ByteBuffer;

public class BufferManagerTests{

    public static void main(String[] args) throws Exception {
        // Initialisation de la configuration et du DiskManager
        DBConfig config = new DBConfig("../DB", 4096, 8192, 3, "LRU");
        DiskManager diskManager = new DiskManager(config);

        // Initialisation du BufferManager avec la politique LRU par défaut
        BufferManager bufferManager = new BufferManager(config, diskManager);

        // Test 1 : Charger une page depuis le disque
        System.out.println("Test 1 : Charger une page depuis le disque");
        PageId pageId1 = new PageId(0,0);
        ByteBuffer pageData1=bufferManager.GetPage(pageId1);
        if (pageData1 != null) {
            System.out.println("Page 1 chargée avec succès !");
        } else {
            System.out.println("Échec du chargement de la page 1 !");
        }

        // Test 2 : Charger une page depuis le bufferPool
        System.out.println("Test 2 : Charger une page du bufferPool");
        ByteBuffer pageData2 = bufferManager.GetPage(pageId1);
        if (pageData1 == pageData2) {
            System.out.println("La page 1 est bien récupérée depuis la mémoire !");
        } else {
            System.out.println("Échec : la page 1 n'a pas été récupérée depuis la mémoire !");
        }

        // Test 3 : Politique de remplacement LRU
        System.out.println("Test 3 : Politique de remplacement LRU");
        PageId pageId2 = new PageId(0,1);
        ByteBuffer pageData3 = bufferManager.GetPage(pageId2);
        if (pageData3 != null) {
            System.out.println("Page 2 chargée avec succès !");
        } else {
            System.out.println("Échec du chargement de la page 2 !");
        }


        PageId pageId3 = new PageId(1,0);
        ByteBuffer pageData4 = bufferManager.GetPage(pageId3);
        if (pageData4 != null) {
            System.out.println("Page 3 chargée avec succès !");
        } else {
            System.out.println("Échec du chargement de la page 3 !");
        }

        // Charger une nouvelle page qui devrait remplacer la première
        PageId pageId4 = new PageId(1,1);
        ByteBuffer pageData5 = bufferManager.GetPage(pageId4);

        if (pageData1 != bufferManager.GetPage(pageId4)) {
            System.out.println("La page 1 a été remplacée conformément à la politique LRU.");
        } else {
            System.out.println("Échec : la page 1 n'a pas été remplacée !");
        }


        // Test 4 : Flusher les buffers modifiés (dirty)
        System.out.println(" Test 4 : Flusher les buffers modifiés");

        pageData5.put(0, (byte) 1); // donc modification du buffer qui contient la page 1 1
        //ici même si je modifie la page la seule fonction qui met le flag à dirty c'est freePage

        //bufferManager.flushBuffers(); // cela doit relever une exception ici car la page est en cours d'utilisation

        //ici free page pour décrémenter pin_count
        bufferManager.FreePage(pageId4, true);
        bufferManager.FreePage(pageId2, false);
        bufferManager.FreePage(pageId3, false);

        bufferManager.flushBuffers(); // celui là devra marcher



    }
}