import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DBManager {
   
    private Map<String, Map<String, Relation>> databases = new HashMap<>();
    
    public Map<String, Map<String, Relation>> getDatabases() {
		return databases;
	}


	private DBConfig config;
    private Database currentDatabase;

    // Constructeur prenant une instance de DBConfig
    public DBManager(DBConfig config) {
        this.config = config;
    }
	
	public void CreateDatabase(String nomBdd) {
	    // Vérifier si la base existe déjà
	    if (databases.containsKey(nomBdd)) {
	        System.out.println("La base de données " + nomBdd + " existe déjà !");
	        return;
	    }

	    // Ajouter la base de données
	    databases.put(nomBdd, new HashMap<>()); // Une base contient une liste de tables
	    System.out.println("Base de données " + nomBdd + " créée avec succès !");
	}

	public void ListDatabases() {
	    if (databases.isEmpty()) {
	        System.out.println("Aucune base de données n'existe encore.");
	        return;
	    }

	    System.out.println("Bases de données existantes :");
	    for (String nomBdd : databases.keySet()) {
	        System.out.println("- " + nomBdd);
	    }
	}
	
	public void LoadState() {
	    File fichierSauvegarde = new File(config.getDbpath() + "/databases.save");

	    // Vérifier si le fichier de sauvegarde existe
	    if (!fichierSauvegarde.exists()) {
	        System.out.println("Aucun fichier de sauvegarde trouvé. Aucune base chargée.");
	        return;
	    }

	    try (BufferedReader lecteur = new BufferedReader(new FileReader(fichierSauvegarde))) {
	        String ligne;

	        // Lire chaque ligne pour récupérer les bases de données
	        while ((ligne = lecteur.readLine()) != null) {
	            String nomBdd = ligne.trim();
	            databases.put(nomBdd, new HashMap<>()); // Ajouter une base vide
	        }

	        System.out.println("État chargé avec succès !");
	    } catch (IOException e) {
	        System.out.println("Erreur lors du chargement de l'état : " + e.getMessage());
	    }
	} 
	/**
	 * Retourne une instance de Classes.Relation correspondant à une table spécifique
	 * dans la base de données active.
	 * @param nomTable        Nom de la table à récupérer.
	 * @return L'instance de Classes.Relation correspondant à la table demandée.
	 * @throws IllegalStateException    Si aucune base active n'est sélectionnée.
	 * @throws IllegalArgumentException Si la table demandée n'existe pas.
	 */
	public Relation GetTableFromCurrentDatabase(String nomTable) {
		if (currentDatabase == null || !databases.containsKey(currentDatabase)) {
			throw new IllegalStateException("Aucune base de données active ou inexistante.");
		}
		Map<String, Relation> tables = databases.get(currentDatabase);
		if (!tables.containsKey(nomTable)) {
			throw new IllegalArgumentException("La table " + nomTable + " n'existe pas dans la base active.");
		}
		return tables.get(nomTable);
	}


	/**
	 * Supprime une table spécifique dans la base de données active.
	 * @param nomTable Nom de la table à supprimer.
	 * @throws IllegalStateException    Si aucune base active n'est sélectionnée.
	 * @throws IllegalArgumentException Si la table demandée n'existe pas.
	 */
	public void RemoveTableFromCurrentDatabase(String nomTable) {
		if (currentDatabase == null || !databases.containsKey(currentDatabase)) {
			throw new IllegalStateException("Aucune base de données active ou inexistante.");
		}
		Map<String, Relation> tables = databases.get(currentDatabase);
		if (!tables.containsKey(nomTable)) {
			throw new IllegalArgumentException("La table " + nomTable + " n'existe pas dans la base active.");
		}
		tables.remove(nomTable);
		System.out.println("Table " + nomTable + " supprimée avec succès de la base " + currentDatabase + ".");
	}


	/**
	 * Sauvegarde l'état actuel des bases de données et des tables dans un fichier.
	 */
	public void SaveState() {
		File fichierSauvegarde = new File(config.getDbpath() + "/databases.save");

		try (BufferedWriter ecrivain = new BufferedWriter(new FileWriter(fichierSauvegarde))) {
			for (String nomBdd : databases.keySet()) {
				ecrivain.write("DATABASE:" + nomBdd);
				ecrivain.newLine();

				Map<String, Relation> tables = databases.get(nomBdd);
				if (tables.isEmpty()) {
					ecrivain.write("  EMPTY_TABLES"); // Indiquer que la base n'a pas de tables
					ecrivain.newLine();
				} else {
					for (String nomTable : tables.keySet()) {
						Relation relation = tables.get(nomTable);
						if (relation == null) {
							throw new IllegalStateException("Classes.Relation invalide pour la table " + nomTable);
						}
						ecrivain.write("TABLE:" + nomTable + ":HeaderPageId=" + relation.getHeaderPageId());
						ecrivain.newLine();
					}
				}
			}
			System.out.println("État sauvegardé avec succès !");
		} catch (IOException e) {
			System.out.println("Erreur lors de la sauvegarde de l'état : " + e.getMessage());
		}
	}
}