import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.nio.ByteBuffer;

public class DiskManagerTest {
    private DBConfig config;
    private DiskManager diskManager;
    private String testFileName = "F0"; // Nom de fichier de test

    @Before
    public void setUp() throws Exception {
        // Initialiser la configuration pour les tests
        config = new DBConfig(1024, 10240, "dbpath"); // pageSize = 1024, maxFilesize = 10240
        diskManager = new DiskManager(config, testFileName);
    }

    @Test
    public void testAllocPage() {
        PageId pageId = diskManager.AllocPage();
        assertNotNull("La page doit être allouée", pageId);
        assertEquals("L'index de fichier doit être correct", 0, pageId.getFileIdx());
        assertEquals("L'index de page doit être 0", 0, pageId.getPageIdx());
    }

    @Test
    public void testDeallocPage() {
        PageId pageId = diskManager.AllocPage();
        diskManager.DeallocPage(pageId);
        // Vérifier que la page est ajoutée à la liste des pages libres
        assertTrue("La page doit être libérée", diskManager.pagesLibres.contains(pageId.getPageIdx()));
    }

    @Test
    public void testWriteAndReadPage() {
        PageId pageId = diskManager.AllocPage();
        ByteBuffer buff = ByteBuffer.allocate(config.getPageSize());
        buff.put("Test Data".getBytes());
        buff.flip();
        
        diskManager.WritePage(pageId, buff);
        
        ByteBuffer readBuff = ByteBuffer.allocate(config.getPageSize());
        diskManager.ReadPage(pageId, readBuff);
        
        readBuff.flip();
        byte[] data = new byte[readBuff.remaining()];
        readBuff.get(data);
        
        assertEquals("Les données lues doivent correspondre", "Test Data", new String(data));
    }

    @Test
    public void testSaveAndLoadState() {
        // Simuler des allocations de pages
        diskManager.AllocPage();
        diskManager.AllocPage();
        
        // Sauvegarder l'état
        diskManager.SaveState();

        // Créer un nouveau DiskManager pour tester le chargement
        DiskManager newDiskManager = new DiskManager(config, testFileName);
        newDiskManager.LoadState();

        // Vérifier que les pages libres sont correctement chargées
        assertFalse("La liste des pages libres ne doit pas être vide", newDiskManager.pagesLibres.isEmpty());
    }

    // Nettoyer après les tests
    @After
    public void tearDown() {
        // Supprimer le fichier de test
        File file = new File(testFileName);
        if (file.exists()) {
            file.delete();
        }
    }
}
