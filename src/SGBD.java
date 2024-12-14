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
    
    public void ProcessCreateDatabaseCommand(String commande) {
        String[] parts = commande.split(" "); // Diviser la commande en parties
        if (parts.length < 3) {
            System.out.println("Commande invalide.");
            return;
        }
        String nomBdd = parts[2]; // Récupérer le nom de la base de données
        dbManager.CreateDatabase(nomBdd); // Passer uniquement le nom à la méthode
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
        System.out.println("Nombre de bases de données avant suppression : " + dbManager.databases.size());
        if (dbManager.databases.isEmpty()) {
            System.out.println("Aucune base de données à supprimer.");
            return;
        }

        // Supprimer toutes les bases de données
        dbManager.RemoveAllDatabases();
        System.out.println("Toutes les bases de données ont été supprimées avec succès !");
    }
    
    // Méthode pour lister les bases de données
    public void ProcessListDatabasesCommand() {
        dbManager.ListDatabases();
    }
    
 // Méthode pour créer une table
    public void ProcessCreateTableCommand(String commande) {
        // Vérifiez que la commande commence par "create table"
        if (!commande.startsWith("create table")) {
            System.out.println("Commande invalide : " + commande);
            return;
        }

        // Extraire le nom de la table et les colonnes
        String[] parts = commande.split("\\s+"); // Diviser par espaces
        String nomTable = parts[2]; // Nom de la table

        // Vérifiez que la commande contient des colonnes
        if (parts.length < 4 || !commande.contains("(")) {
            System.out.println("Définition de colonne invalide : " + commande);
            return;
        }

        // Extraire la partie entre parenthèses
        String colonnesPart = commande.substring(commande.indexOf("(") + 1, commande.indexOf(")"));
        String[] colonnesDef = colonnesPart.split(","); // Diviser par virgule

        ArrayList<ColInfo> colonnes = new ArrayList<>();

        // Analyser chaque définition de colonne
        for (String colDef : colonnesDef) {
            String[] colParts = colDef.trim().split("\\s+"); // Diviser par espaces
            if (colParts.length < 2) {
                System.out.println("Définition de colonne invalide : " + colDef);
                return;
            }

            String nomCol = colParts[0]; // Nom de la colonne
            ColmType typeCol;

            // Vérifiez que le type de colonne est valide
            try {
                typeCol = ColmType.valueOf(colParts[1].toUpperCase()); // Utilisez colParts[1] pour le type
            } catch (IllegalArgumentException e) {
                System.out.println("Type de colonne invalide : " + colParts[1]);
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
            }  else if (commande.startsWith("set database")) {
                String[] parts = commande.split(" ");
                if (parts.length < 3) { // Vérifiez que l'index 2 existe
                    System.out.println("Commande invalide : " + commande);
                    continue; // Passez à la prochaine itération
                }
                ProcessSetDatabaseCommand(parts[2]);
            } else if (commande.startsWith("drop database")) {
                String[] parts = commande.split(" ");
                if (parts.length < 3) { // Vérifiez que l'index 2 existe
                    System.out.println("Commande invalide : " + commande);
                    continue; // Passez à la prochaine itération
                }
                ProcessDropDatabaseCommand(parts[2]);
            } else if (commande.equalsIgnoreCase("DROP DATABASES")) { //faut lecrire en majuscule
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