import org.json.simple.parser.ParseException;
import java.io.IOException;

public class DBManagerTests {
    public static void main(String[] args) {
        // Test : Chargement de la configuration de la base de données à partir du fichier JSON
        System.out.println("Test : Chargement de la configuration de la base de données");
        try {
            DBConfig config = DBConfig.loadDBConfig("./src/data/infos.json");
            System.out.println("Configuration chargée avec succès.");
            System.out.println("Chemin de la base de données : " + config.getDbpath());
            System.out.println("Taille des pages : " + config.getPageSize());
            System.out.println("Taille maximale des fichiers : " + config.getDm_maxfilesize());
            System.out.println("Nombre de buffers : " + config.getBm_buffercount());
            System.out.println("Politique du buffer manager : " + config.getBm_policy());
        } catch (IOException | ParseException e) {
            System.out.println("Échec du chargement de la configuration : " + e.getMessage());
        }

        // Initialisation avec les paramètres de configuration chargés
        DBConfig config = null;
        try {
            config = DBConfig.loadDBConfig("./src/data/infos.json");
        } catch (IOException | ParseException e) {
            System.out.println("Impossible de charger la configuration : " + e.getMessage());
            return; // Arrêt des tests si la configuration échoue
        }
        
        DBManager dbManager = new DBManager(config);

        // Test : Création d'une base de données
        System.out.println("Test : Création d'une base de données");
        try {
            dbManager.CreateDatabase("TestDB");
            if (dbManager.databases.containsKey("TestDB")) {
                System.out.println("Base de données 'TestDB' créée avec succès.");
            } else {
                System.out.println("La base de données 'TestDB' n'existe pas après sa création.");
            }
        } catch (Exception e) {
            System.out.println("Échec de la création de la base de données : " + e.getMessage());
        }

        // Test : Activation d'une base de données
        System.out.println("Test : Activation d'une base de données");
        try {
            dbManager.currentDatabase = "TestDB";
            if ("TestDB".equals(dbManager.currentDatabase)) {
                System.out.println("Base de données 'TestDB' activée avec succès.");
            } else {
                System.out.println("La base de données 'TestDB' n'est pas activée.");
            }
        } catch (Exception e) {
            System.out.println("Échec de l'activation de la base de données : " + e.getMessage());
        }

        // Test : Ajout d'une table
        System.out.println("Test : Ajout d'une table à la base courante");
        try {
            Relation relation = new Relation("TestTable");
            dbManager.databases.get("TestDB").put("TestTable", relation);

            if (dbManager.databases.get("TestDB").containsKey("TestTable")) {
                System.out.println("Table 'TestTable' ajoutée avec succès.");
            } else {
                System.out.println("La table 'TestTable' n'a pas été ajoutée.");
            }
        } catch (Exception e) {
            System.out.println("Échec de l'ajout de la table : " + e.getMessage());
        }

        // Test : Suppression d'une table
        System.out.println("Test : Suppression d'une table");
        try {
            dbManager.RemoveTableFromCurrentDatabase("TestTable");
            if (!dbManager.databases.get("TestDB").containsKey("TestTable")) {
                System.out.println("Table 'TestTable' supprimée avec succès.");
            } else {
                System.out.println("La table 'TestTable' existe toujours après suppression.");
            }
        } catch (Exception e) {
            System.out.println("Échec de la suppression de la table : " + e.getMessage());
        }

        // Test : Suppression d'une base de données
        System.out.println("Test : Suppression d'une base de données");
        try {
            dbManager.databases.remove("TestDB");
            if (!dbManager.databases.containsKey("TestDB")) {
                System.out.println("Base de données 'TestDB' supprimée avec succès.");
            } else {
                System.out.println("La base de données 'TestDB' existe toujours après suppression.");
            }
        } catch (Exception e) {
            System.out.println("Échec de la suppression de la base de données : " + e.getMessage());
        }

        // Test : Liste des bases de données
        System.out.println("Test : Liste des bases de données");
        try {
            dbManager.ListDatabases(); // Devrait afficher toutes les bases existantes
            System.out.println("Les bases de données ont été listées.");
        } catch (Exception e) {
            System.out.println("Échec de la liste des bases de données : " + e.getMessage());
        }

        // Test : Sauvegarde de l'état
        System.out.println("Test : Sauvegarde de l'état");
        try {
            dbManager.SaveState();
            System.out.println("État sauvegardé avec succès.");
        } catch (Exception e) {
            System.out.println("Échec de la sauvegarde de l'état : " + e.getMessage());
        }

        // Test : Chargement de l'état
        System.out.println("Test : Chargement de l'état");
        try {
            dbManager.LoadState();
            System.out.println("État chargé avec succès.");
        } catch (Exception e) {
            System.out.println("Échec du chargement de l'état : " + e.getMessage());
        }
    }
}



