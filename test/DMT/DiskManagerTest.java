package DMT;

import DM.DBConfig;
import DM.DiskManager;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DiskManagerTest {
    private static DBConfig config;
    private static DiskManager diskManager;
    private static String dbTestPath = "./DBTest"; // Répertoire temporaire pour les tests

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



    //test du constructeur
    @Test
    public void testConstructor_CreatesDirectories() {
        // Lire le chemin depuis le fichier de conf
        String dbPath = config.getDbpath();
        File dbDirectory = new File(dbPath);

        assertTrue(dbDirectory.exists(), "Le répertoire DB n'a pas été créé.");

        File binDataDirectory = new File(dbDirectory, "Bin_Data");
        assertTrue(binDataDirectory.exists(), "Le répertoire Bin_Data n'a pas été créé.");


    }




}