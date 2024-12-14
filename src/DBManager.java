import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    public List<String> databases;         // List of database names
    private DBConfig config;                // Configuration for database path
    private Database currentDatabase;       // Currently active database
	private DiskManager diskManager;        // Instance of DiskManager
    private BufferManager bufferManager;    // Instance of BufferManager

 // Constructor taking DBConfig and DiskManager instances
    public DBManager(DBConfig config, DiskManager diskManager, BufferManager bufferManager) {
        this.config = config;
        this.diskManager = diskManager; // Initialize diskManager
        this.bufferManager = bufferManager; // Initialize bufferManager
        this.databases = new ArrayList<>();
    }

    // Method to create a new database
    public void CreateDatabase(String nomBdd) {
        // Check if the database already exists
        if (databases.contains(nomBdd)) {
            System.out.println("La base de données " + nomBdd + " existe déjà !");
            return;
        }

        // Add the new database
        databases.add(nomBdd);
        System.out.println("Base de données " + nomBdd + " créée avec succès !");
    }
    
    public Database getCurrentDatabase() {
		return currentDatabase; // Retourne la base de données courante
	}

	// Method to set the current database
    public void setCurrentDatabase(String nomBdd) {
        if (databases.contains(nomBdd)) {
            // Créez une nouvelle instance de Database pour la base de données courante
            currentDatabase = new Database(nomBdd, new ArrayList<>()); // Vous pouvez passer les relations si nécessaire
            System.out.println("Base de données courante définie sur : " + nomBdd);
        } else {
            System.out.println("La base de données " + nomBdd + " n'existe pas.");
        }
    }

	 // Method to remove a database
	 public void RemoveDatabase(String nomBdd) {
        if (databases.remove(nomBdd)) {
            System.out.println("Base de données " + nomBdd + " supprimée avec succès !");
            // Si la base de données supprimée était la courante, réinitialisez currentDatabase
            if (currentDatabase != null && currentDatabase.getNom().equals(nomBdd)) {
                currentDatabase = null;
            }
        } else {
            System.out.println("La base de données " + nomBdd + " n'existe pas.");
        }
    }
	// Method to remove all databases
	public void RemoveAllDatabases() {
		if (databases.isEmpty()) {
			System.out.println("Aucune base de données à supprimer.");
			return;
		}

		// Supprimer toutes les bases de données
		databases.clear();
		currentDatabase = null; // Réinitialiser la base de données courante
		System.out.println("Toutes les bases de données ont été supprimées avec succès !");
	}

    // Method to list all databases
    public void ListDatabases() {
        if (databases.isEmpty()) {
            System.out.println("Aucune base de données n'existe encore.");
            return;
        }

        System.out.println("Bases de données existantes :");
        for (String nomBdd : databases) {
            System.out.println("- " + nomBdd);
        }
    }

    // Method to load the database state from a file
    public void LoadState() {
        File fichierSauvegarde = new File(config.getDbpath() + "/databases.save");

        // Check if the backup file exists
        if (!fichierSauvegarde.exists()) {
            System.out.println("Aucun fichier de sauvegarde trouvé. Aucune base chargée.");
            return;
        }

        try (BufferedReader lecteur = new BufferedReader(new FileReader(fichierSauvegarde))) {
            String ligne;

            // Read each line to load databases
            while ((ligne = lecteur.readLine()) != null) {
                String nomBdd = ligne.trim();
                if (!databases.contains(nomBdd)) {
                    databases.add(nomBdd);
                }
            }

            System.out.println("État chargé avec succès !");
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement de l'état : " + e.getMessage());
        }
    }

    // Method to save the current state of databases and tables
    public void SaveState() {
        File fichierSauvegarde = new File(config.getDbpath() + "/databases.save");
        try (BufferedWriter ecrivain = new BufferedWriter(new FileWriter(fichierSauvegarde))) {
            for (String databaseName : databases) {
                ecrivain.write("DATABASE:" + databaseName);
                ecrivain.newLine();
            }
            System.out.println("État sauvegardé avec succès !");
        } catch (IOException e) {
            System.out.println("Erreur lors de la sauvegarde de l'état : " + e.getMessage());
        }
    }

    // Method to get a table from the current active database
    public Relation GetTableFromCurrentDatabase(String nomTable) {
        if (currentDatabase == null) {
            throw new IllegalStateException("Aucune base de données active.");
        }
        Relation table = currentDatabase.getTable(nomTable);
        if (table == null) {
            throw new IllegalArgumentException("La table " + nomTable + " n'existe pas dans la base active.");
        }
        return table;
    }

   /*  // Method to remove a table from the current active database
    public void RemoveTableFromCurrentDatabase(String nomTable) {
        if (currentDatabase == null) {
            throw new IllegalStateException("Aucune base de données active.");
        }
        boolean removed = currentDatabase.removeTable(nomTable);
        if (!removed) {
            throw new IllegalArgumentException("La table " + nomTable + " n'existe pas dans la base active.");
        }
        System.out.println("Table " + nomTable + " supprimée avec succès de la base " + currentDatabase.getNom() + ".");
    }*/

	// Method to create a table in the current active database
public void CreateTable(String nomTable, ArrayList<ColInfo> colonnes) {
    if (currentDatabase == null) {
        throw new IllegalStateException("Aucune base de données active.");
    }

    try {
        // Créer une nouvelle Header Page pour la table
        PageId headerPageId = diskManager.AllocPage(); // Allouer une nouvelle page pour l'en-tête
        ByteBuffer headerPage = ByteBuffer.allocate(config.getPageSize());
        headerPage.putInt(1); // Nombre de pages de données initial
        headerPage.putInt(headerPageId.getFileIdx()); // FileIdx de la première DataPage
        headerPage.putInt(0); // PageIdx de la première DataPage
        headerPage.putInt(config.getPageSize() - 8); // Espace libre initial
        diskManager.WritePage(headerPageId, headerPage); // Écrire la Header Page sur le disque

        // Créer la relation (table)
        Relation relation = new Relation(nomTable, colonnes.size(), colonnes, config, headerPageId, diskManager, bufferManager);
        currentDatabase.addTable(relation); // Ajouter la table à la base de données courante
        System.out.println("Table " + nomTable + " ajoutée à la base de données " + currentDatabase.getNom() + ".");
    } catch (IOException e) {
        System.out.println("Erreur lors de l'allocation de la page : " + e.getMessage());
    }
}

    // Method to check if la table existe dans la base de données courante
    public boolean tableExists(String nomTable) {
        if (currentDatabase == null) {
            throw new IllegalStateException("Aucune base de données active.");
        }
        return currentDatabase.getTable(nomTable) != null;
    }

    // Method to remove a table from the current active database
    public void RemoveTable(String nomTable) {
        if (currentDatabase == null) {
            throw new IllegalStateException("Aucune base de données active.");
        }
        boolean removed = currentDatabase.removeTable(nomTable);
        if (removed) {
            System.out.println("Table " + nomTable + " supprimée avec succès de la base " + currentDatabase.getNom() + ".");
        } else {
            System.out.println("La table " + nomTable + " n'existe pas dans la base active.");
        }
    }

    // Method to deallocate pages for a table
    public void DeallocPagesForTable(String nomTable) {
        // Implémentez la logique pour désallouer les pages associées à la table
        System.out.println("Pages désallouées pour la table " + nomTable + ".");
    }

    // Method to remove all tables from the current active database
    public void RemoveAllTables() {
        if (currentDatabase == null) {
            throw new IllegalStateException("Aucune base de données active.");
        }
        currentDatabase.removeAllTables();
    }

    // Method to list all tables in the current active database
    public void ListTables() {
        if (currentDatabase == null) {
            throw new IllegalStateException("Aucune base de données active.");
        }
        currentDatabase.listTables(); // Assurez-vous que cette méthode est définie dans la classe Database
    }
}

