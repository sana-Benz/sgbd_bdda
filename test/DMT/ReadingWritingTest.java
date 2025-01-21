package DMT;

import DM.DBConfig;
import DM.DiskManager;
import DM.PageId;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.ByteBuffer;

public class ReadingWritingTest {

    private static DBConfig config;
    private static DiskManager diskManager;
    private static String dbTestPath = "./DBTest"; // Répertoire pour les tests

    @BeforeAll
    public static void setUp() throws IOException, ParseException {
        // Charger la configuration du SGBD à partir du fichier de configuration infos.json
        config = DBConfig.loadDBConfig("./data/infos.json");
        config.setDbpath(dbTestPath); // Répertoire pour les tests
        diskManager = new DiskManager(config);
    }

    @BeforeEach
    public void setUpTestDirectory() throws IOException {
        // Réinitialiser les fichiers de test avant chaque test
        File testDir = new File(dbTestPath);
        if (testDir.exists()) {
            diskManager.cleanDirectory(testDir); // Nettoyer les fichiers existants
        }
        testDir.mkdir(); // Recréer le répertoire
        diskManager = new DiskManager(config);
    }

    @AfterEach
    public void clearTestDirectory() {
        // Nettoyer après chaque test
        File testDir = new File(dbTestPath);
        diskManager.cleanDirectory(testDir);
    }

    @Test
    public void testWriteAndReadPage() {
        try {
            for (int i = 0; i < 5; i++) {
                PageId pageId = diskManager.AllocPage();

                // Préparer les données à écrire
                ByteBuffer writeBuffer = ByteBuffer.allocate(config.getPageSize());
                String dataToWrite = "Test data for page " + i;
                writeBuffer.put(dataToWrite.getBytes());

                // Écrire les données dans la page
                //writeBuffer.flip(); // Préparer le buffer pour l'écriture
                diskManager.WritePage(pageId, writeBuffer);

                // Préparer un buffer pour lire les données
                ByteBuffer readBuffer = ByteBuffer.allocate(config.getPageSize());
                diskManager.ReadPage(pageId, readBuffer);
                readBuffer.flip(); // Préparer le buffer pour la lecture

                // Convertir les données lues en chaîne de caractères pour comparaison
                byte[] readDataBytes = new byte[readBuffer.remaining()];
                readBuffer.get(readDataBytes);
                String readData = new String(readDataBytes).trim(); // Convertir en chaîne et retirer les espaces

                // Vérifier que les données écrites et lues correspondent
                assertEquals(dataToWrite, readData, "Les données lues et écrites doivent correspondre.");
            }
        } catch (Exception e) {
            fail("Une erreur s'est produite pendant le test: " + e.getMessage());
        }
    }
}
