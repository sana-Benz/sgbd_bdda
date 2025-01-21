package DMT;

import DM.DBConfig;
import DM.DiskManager;
import DM.PageId;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AllocationTest {
    private static DBConfig config;
    private static DiskManager diskManager;
    private static String dbTestPath = "./DBTest";

    @BeforeAll
    public static void setUp() throws IOException, ParseException {
        // Charger la configuration du SGBD à partir du fichier de configuration infos.json
        config = DBConfig.loadDBConfig("./data/infos.json");
        config.setDbpath("./DBTest"); // Répertoire de tests
        diskManager = new DiskManager(config);
    }

    @BeforeEach
    public void setUpTestDirectory() throws IOException {
        // Réinitialiser les fichiers de test avant chaque test
        File testDir = new File(dbTestPath);
        if (testDir.exists()) {
            diskManager.cleanDirectory(testDir);  // Nettoyer les fichiers existants
        }
        testDir.mkdir();  // Recréer le répertoire
        diskManager = new DiskManager(config);  // Réinitialiser DiskManager
    }


    @AfterEach
    public void clearTestDirectory() {
        File testDir = new File(dbTestPath);
        diskManager.cleanDirectory(testDir);  // Supprimer tous les fichiers et répertoires après chaque test
    }

    @Test
    public void testAllocPageInExistingFile() throws IOException {
        PageId pageId = diskManager.AllocPage();
        PageId pageId2 = diskManager.AllocPage();
        assertNotNull(pageId2);
        assertEquals(pageId.getFileIdx(), pageId2.getFileIdx()); //vérifier que les 2 pages sont dans le même fichier

    }


    @Test
    public void testReuseFreePage() throws IOException {
        diskManager.getPagesLibres().add(3); // Simuler une page libre
        PageId pageId = diskManager.AllocPage();
        assertNotNull(pageId);
        assertEquals(3, pageId.getPageIdx());
    }

    @Test
    public void testCreateNewFileWhenCurrentIsFull() throws IOException {
        // Simuler un fichier plein
        for (int i = 0; i < config.getDm_maxfilesize() / config.getPageSize(); i++) {
            diskManager.AllocPage();
        }
        PageId pageId = diskManager.AllocPage();
        assertNotNull(pageId);
        assertEquals(1, pageId.getFileIdx()); // Nouveau fichier
        assertEquals(0, pageId.getPageIdx()); // Première page du nouveau fichier
        System.out.println(new File(".").getAbsolutePath());
    }
}
