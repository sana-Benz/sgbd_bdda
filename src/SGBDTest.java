import java.io.IOException;
import org.json.simple.parser.ParseException;
import java.util.*;

public class SGBDTest {
    public static void main(String[] args) {
        try {
            // Charger la configuration
            DBConfig config = DBConfig.loadDBConfig("../../infos.json");
            SGBD sgbd = new SGBD(config);
            
            // Vérifiez que les instances sont initialisées
            assert sgbd.getDiskManager() != null : "DiskManager non initialisé";
            assert sgbd.getBufferManager() != null : "BufferManager non initialisé";
            assert sgbd.getDbManager() != null : "DBManager non initialisé";
            System.out.println("Test de constructeur réussi !");
            
            // Test de création de base de données
            String dbName = "testDB";
            sgbd.ProcessCreateDatabaseCommand(dbName);
            assert sgbd.getDbManager().databases.contains(dbName) : "La base de données n'a pas été créée";
            System.out.println("Test de création de base de données réussi !");
            
            // Test de définition de la base de données courante
            sgbd.ProcessSetDatabaseCommand(dbName);
            assert sgbd.getDbManager().getCurrentDatabase().getNom().equals(dbName) : "La base de données courante n'est pas définie correctement";
            System.out.println("Test de définition de la base de données courante réussi !");
            
            // Test de création de table
            String createTableCommand = "create table testTable Colonne1 INT Colonne2 FLOAT";
            sgbd.ProcessCreateTableCommand(createTableCommand);
            assert sgbd.getDbManager().getCurrentDatabase().getTable("testTable") != null : "La table n'a pas été créée";
            System.out.println("Test de création de table réussi !");
            
            // Test de suppression de table
            sgbd.ProcessDropTableCommand("testTable");
            assert sgbd.getDbManager().getCurrentDatabase().getTable("testTable") == null : "La table n'a pas été supprimée";
            System.out.println("Test de suppression de table réussi !");
            
            // Test de suppression de base de données
            sgbd.ProcessDropDatabaseCommand(dbName);
            assert !sgbd.getDbManager().databases.contains(dbName) : "La base de données n'a pas été supprimée";
            System.out.println("Test de suppression de base de données réussi !");
            
        } catch (IOException | ParseException e) {
            System.err.println("Erreur lors du test : " + e.getMessage());
        } catch (AssertionError e) {
            System.err.println("Échec du test : " + e.getMessage());
        }
    }
}