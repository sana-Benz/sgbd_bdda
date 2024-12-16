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

	// Constructeur prenant une instance de DBConfig
    public DBManager(DBConfig config, DiskManager diskManager, BufferManager bufferManager) {
        this.config = config;
        this.diskManager = diskManager; // Initialize diskManager
        this.bufferManager = bufferManager; // Initialize bufferManager
        this.databases = new ArrayList<>();
    }

	
	public PageId getHeaderPageId(String nomTable) {
		Relation colInfo = currentDatabase.getTable(nomTable);
		return colInfo.getHeaderPageId();
	}
    
    public Database getCurrentDatabase() {
		return currentDatabase; // Retourne la base de données courante
	}

	// Method to set the current database
    
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
    
	/**
	 * Retourne une instance de Relation correspondant à une table spécifique
	 * dans la base de données active.
	 * @param nomTable        Nom de la table à récupérer.
	 * @return L'instance de Relation correspondant à la table demandée.
	 * @throws IllegalStateException    Si aucune base active n'est sélectionnée.
	 * @throws IllegalArgumentException Si la table demandée n'existe pas.
	 */
	public Relation GetTableFromCurrentDatabase(String nomTable) {
		if (currentDatabase == null || !databases.contains(currentDatabase.getNom())) {
			throw new IllegalStateException("Aucune base de données active ou inexistante.");
		}
		Relation table = currentDatabase.getTable(nomTable);
		if (table == null) {
			throw new IllegalArgumentException("La table " + nomTable + " n'existe pas dans la base active.");
		}
		return table;
	}

	/**
	 * Supprime une table spécifique dans la base de données active.
	 * @param nomTable Nom de la table à supprimer.
	 * @throws IllegalStateException    Si aucune base active n'est sélectionnée.
	 * @throws IllegalArgumentException Si la table demandée n'existe pas.
	 */
	public void RemoveTableFromCurrentDatabase(String nomTable) {
		if (currentDatabase == null || !databases.contains(currentDatabase.getNom())) {
			throw new IllegalStateException("Aucune base de données active ou inexistante.");
		}
		boolean removed = currentDatabase.removeTable(nomTable);
		if (!removed) {
			throw new IllegalArgumentException("La table " + nomTable + " n'existe pas dans la base active.");
		}
		System.out.println("Table " + nomTable + " supprimée avec succès de la base " + currentDatabase.getNom() + ".");
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

	/**
	 * Sauvegarde l'état actuel des bases de données et des tables dans un fichier.
	 */
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

    
	/**
	 * Crée une nouvelle base de données avec le nom spécifié.
	 * 
	 * @param nomBdd Nom de la base de données à créer.
	 * @throws IllegalArgumentException Si une base de données avec ce nom existe déjà.
	 */
	public void CreateDatabase(String nomBdd) {
	    // Vérifie si le nom est valide
	    if (nomBdd == null || nomBdd.trim().isEmpty()) {
	        throw new IllegalArgumentException("Le nom de la base de données ne peut pas être vide ou nul.");
	    }

	    // Vérifie si une base avec ce nom existe déjà
	    for (String db : databases) {
	        if (db.equalsIgnoreCase(nomBdd)) {
	            throw new IllegalArgumentException("Une base de données avec le nom <" + nomBdd + "> existe déjà.");
	        }
	    }

	   
	    databases.add(nomBdd);

	    System.out.println("Base de données \"" + nomBdd + "\" créée avec succès.");
	}

	/**
	 * Ajoute une table (Relation) à la base de données actuellement active.
	 *
	 * @param tab L'instance de Relation représentant la table à ajouter.
	 * @throws IllegalStateException    Si aucune base de données active n'est sélectionnée.
	 * @throws IllegalArgumentException Si une table avec le même nom existe déjà.
	 */
	public void AddTableToCurrentDatabase(Relation tab) {
	    // Vérifie si une base de données active est sélectionnée
	    if (currentDatabase == null || !databases.contains(currentDatabase.getNom())) {
	        throw new IllegalStateException("Aucune base de données active ou inexistante.");
	    }

	    // Vérifie si l'instance de la table est valide
	    if (tab == null || tab.getNomRelation() == null || tab.getNomRelation().trim().isEmpty()) {
	        throw new IllegalArgumentException("La table à ajouter est invalide.");
	    }

	    // Vérifie si une table avec le même nom existe déjà dans la base active
	    for (Relation relation : currentDatabase.getRelations()) {
	        if (relation.getNomRelation().equalsIgnoreCase(tab.getNomRelation())) {
	            throw new IllegalArgumentException(
	                "Une table avec le nom <" + tab.getNomRelation() + "> existe déjà dans la base active.");
	        }
	    }

	    // Ajoute la table à la liste de relations de la base active
	    currentDatabase.getRelations().add(tab);
	    System.out.println("Table <" + tab.getNomRelation() + "> ajoutée avec succès à la base de données <"
	            + currentDatabase.getNom() + ">.");
	}

	/**
	 * Supprime une base de données par son nom.
	 *
	 * @param nomBdd Le nom de la base de données à supprimer.
	 * @throws IllegalArgumentException Si la base de données n'existe pas.
	 */
	public void RemoveDatabase(String nomBdd) {
	    if (nomBdd == null || nomBdd.trim().isEmpty()) {
	        throw new IllegalArgumentException("Le nom de la base de données est invalide.");
	    }

	    // Cherche la base de données dans la liste
	    String databaseToRemove = null;
	    for (String db : databases) {
	        if (db.equalsIgnoreCase(nomBdd)) {
	            databaseToRemove = db;
	            break;
	        }
	    }

	    // Vérifie si la base de données existe
	    if (databaseToRemove == null) {
	        throw new IllegalArgumentException("La base <" + nomBdd + "> n'existe pas.");
	    }

	    // Supprime la base de données
	    databases.remove(databaseToRemove);

	    // Vérifie si la base supprimée était la base active
	    if (currentDatabase.getNom() == databaseToRemove) {
	        currentDatabase = null; // Réinitialise la base active
	        System.out.println("La base de données active <" + nomBdd + "> a été supprimée.");
	    }

	    System.out.println("Base de données <" + nomBdd + "> supprimée avec succès.");
	}

	/**
	 * Affiche les noms de toutes les bases de données existantes, une par ligne.
	 * Si aucune base n'existe, affiche un message approprié.
	 */
	public void ListDatabases() {
	    if (databases.isEmpty()) {
	        System.out.println("Aucune base de données existante.");
	    } else {
	        System.out.println("Bases de données existantes :");
	        for(String db : databases) {
	        	db.toString();
	        }
	    }
	}
	
	/**
	 * Affiche les noms et les schmas (noms et types des colonnes) de toutes les tables
	 * de la base de donnes couramment active.
	 * Si aucune base n'est active, affiche un message appropri.
	 */
	public void ListTablesInCurrentDatabase() {
	    // vérifier si une base de donnes active est slectionne
	    if (currentDatabase == null) {
	        System.out.println("Aucune base de donn\u00e9es active.");
	        return;
	    }

	    ArrayList<Relation> relations = currentDatabase.getRelations();
	    if (relations.isEmpty()) {
	        System.out.println("La base de donnée " + currentDatabase.getNom() + "\" ne contient aucune table.");
	        return;
	    }

	    System.out.println("Tables dans la base de données" + currentDatabase.getNom() + " : ");
	    for (Relation relation : relations) {
	        // Récupérer le schéma (colonnes et types) de la table
	        StringBuilder schema = new StringBuilder();
	        for (ColInfo col : relation.getTableCols()) {
	            schema.append(col.getNameCol())
	                  .append(":")
	                  .append(col.getTypeCol());
	            if (col.getTypeCol().equals("CHAR")) {
	                schema.append("(").append(col.getLengthChar()).append(")");
	            }
	            schema.append(", ");
	        }

	        // Supprimer la virgule et l'espace en trop la fin du schma
	        if (schema.length() > 0) {
	            schema.setLength(schema.length() - 2);
	        }

	        // Affichage dans le format demand
	        System.out.println("CREATE TABLE " + relation.getNomRelation() + " (" + schema + ")");
	    }
	}

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
	        ArrayList<ColInfo>  tableCols = new ArrayList<>(); 
	        // Créer la relation (table)
	        Relation relation = new Relation(config, "table1", 0, tableCols, headerPageId, diskManager, bufferManager);
	        currentDatabase.addTable(relation); // Ajouter la table à la base de données courante
	        System.out.println("Table " + nomTable + " ajoutée à la base de données " + currentDatabase.getNom() + ".");
	    } catch (IOException e) {
	        System.out.println("Erreur lors de l'allocation de la page : " + e.getMessage());
	    }
	}

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
    
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (String db : databases) {
			builder.append(db);
		}
		return builder.toString();
	}
	
	

	   

	
}