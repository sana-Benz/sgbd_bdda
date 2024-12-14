import java.io.IOException;
import org.json.simple.parser.ParseException;
import java.util.*;

public class SGBD {
    private DBConfig config;
    private DiskManager diskManager;
    private BufferManager bufferManager;
    private DBManager dbManager;


    // Constructeur qui prend en argument une instance de DBConfig
    public SGBD(DBConfig config) {
        this.config = config;
        try {
            this.diskManager = new DiskManager(config);
            this.bufferManager = new BufferManager(config, diskManager);
            this.dbManager = new DBManager(config, diskManager, bufferManager); // Passer les gestionnaires ici
            
            // Charger l'état des gestionnaires
            diskManager.LoadState();
            dbManager.LoadState();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'initialisation du SGBD : " + e.getMessage());
        }
    }
    // Getters
    public DBConfig getConfig() {
        return config;
    }

    public DiskManager getDiskManager() {
        return diskManager;
    }

    public BufferManager getBufferManager() {
        return bufferManager;
    }

    public DBManager getDbManager() {
        return dbManager;
    }

    public void ProcessQuitCommand() {
        diskManager.SaveState();
        dbManager.SaveState();
        bufferManager.flushBuffers();
    }
    
    public void ProcessCreateDatabaseCommand(String nomBdd) {
        dbManager.CreateDatabase(nomBdd);
    }
    
    // Méthode pour définir la base de données courante
    public void ProcessSetDatabaseCommand(String nomBdd) {
        // Vérifiez si la base existe
    	if (dbManager.databases.contains(nomBdd)) {
            dbManager.setCurrentDatabase(nomBdd); // Assurez-vous d'avoir une méthode pour définir la base courante
            System.out.println("Base de données courante définie sur : " + nomBdd);
        } else {
            System.out.println("La base de données " + nomBdd + " n'existe pas.");
        }
    }
    
    // Méthode pour supprimer une base de données
    public void ProcessDropDatabaseCommand(String nomBdd) {
        if (dbManager.databases.contains(nomBdd)) {
            dbManager.RemoveDatabase(nomBdd); // Assurez-vous d'avoir une méthode pour supprimer la base
            System.out.println("Base de données " + nomBdd + " supprimée.");
        } else {
            System.out.println("La base de données " + nomBdd + " n'existe pas.");
        }
    }
    
    // Méthode pour supprimer toutes les bases de données
    public void ProcessDropDatabasesCommand() {
        dbManager.RemoveAllDatabases();
        System.out.println("Toutes les bases de données ont été supprimées avec succès !");
    }
    
    // Méthode pour lister les bases de données
    public void ProcessListDatabasesCommand() {
        dbManager.ListDatabases();
    }
    
 // Méthode pour créer une table
    public void ProcessCreateTableCommand(String commande) {
        String[] parts = commande.split(" ");
        
        // Vérifiez que la commande contient suffisamment d'arguments
        if (parts.length < 3) {
            System.out.println("Commande invalide : " + commande);
            return;
        }

        String nomTable = parts[2]; // Nom de la table
        ArrayList<ColInfo> colonnes = new ArrayList<>();

        // Commencez à partir de l'index 3 pour les colonnes
        for (int i = 3; i < parts.length; i += 2) { // Incrémentez de 2 pour obtenir le nom et le type
            // Vérifiez que nous avons un nom de colonne et un type
            if (i + 1 >= parts.length) {
                System.out.println("Définition de colonne invalide : " + parts[i]);
                return;
            }

            String nomCol = parts[i]; // Nom de la colonne
            ColmType typeCol;

            // Vérifiez que le type de colonne est valide
            try {
                typeCol = ColmType.valueOf(parts[i + 1].toUpperCase()); // Utilisez parts[i + 1] pour le type
            } catch (IllegalArgumentException e) {
                System.out.println("Type de colonne invalide : " + parts[i + 1]);
                return;
            }

            colonnes.add(new ColInfo(nomCol, typeCol, 0));
        }

        dbManager.CreateTable(nomTable, colonnes);
        System.out.println("Table " + nomTable + " créée avec succès !");
    }
    
 // Méthode pour supprimer une table
    public void ProcessDropTableCommand(String nomTable) {
        if (dbManager.tableExists(nomTable)) {
            dbManager.RemoveTable(nomTable);
            diskManager.DeallocPagesForTable(nomTable);
            System.out.println("Table " + nomTable + " supprimée avec succès !");
        } else {
            System.out.println("La table " + nomTable + " n'existe pas.");
        }
    }
    
 // Méthode pour supprimer toutes les tables
    public void ProcessDropTablesCommand() {
        dbManager.RemoveAllTables();
        System.out.println("Toutes les tables ont été supprimées avec succès !");
    }

    // Méthode pour lister les tables
    public void ProcessListTablesCommand() {
        dbManager.ListTables();
    }

    
 // Méthode Run
    public void Run() {
        Scanner scanner = new Scanner(System.in);
        String commande;

        while (true) {
            System.out.print("SGBD> ");
            commande = scanner.nextLine();

            if (commande.equalsIgnoreCase("quit")) {
                ProcessQuitCommand();
                break;
            } else if (commande.startsWith("create database")) {
                ProcessCreateDatabaseCommand(commande);
            } else if (commande.startsWith("set database")) {
                ProcessSetDatabaseCommand(commande.split(" ")[2]);
            } else if (commande.startsWith("drop database")) {
                ProcessDropDatabaseCommand(commande.split(" ")[2]);
            } else if (commande.equalsIgnoreCase("drop databases")) {
                ProcessDropDatabasesCommand();
            } else if (commande.equalsIgnoreCase("list databases")) {
                ProcessListDatabasesCommand();
            } else if (commande.startsWith("create table")) {
                ProcessCreateTableCommand(commande);
            } else if (commande.startsWith("drop table")) {
                ProcessDropTableCommand(commande.split(" ")[2]);
            } else if (commande.equalsIgnoreCase("drop tables")) {
                ProcessDropTablesCommand();
            } else if (commande.equalsIgnoreCase("list tables")) {
                ProcessListTablesCommand();
            } else {
                System.out.println("Commande non reconnue.");
            }
        }
        scanner.close();
    }


    // Méthode main
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java SGBD <chemin_vers_fichier_config>");
            return;
        }

        String cheminConfig = args[0];
        try {
            // Construire l'objet DBConfig à partir du chemin
            DBConfig config = DBConfig.loadDBConfig(cheminConfig);
            // Créer une instance de SGBD
            SGBD sgbd = new SGBD(config);
            // Appeler la méthode Run
            sgbd.Run();
        } catch (IOException | ParseException e) {
            System.err.println("Erreur lors de la configuration du SGBD : " + e.getMessage());
        }
    }
    
}