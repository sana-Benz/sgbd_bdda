import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DBManager {
   
    private Map<String, Map<String, Relation>> databases = new HashMap<>();
    private DBConfig config;
	private String currentDatabase; // Variable pour stocker la base de données courante


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

	public void SetCurrentDatabase(String nomBdd) {
        // Vérifier si la base de données existe
        if (databases.containsKey(nomBdd)) {
            currentDatabase = nomBdd; // Mettre à jour la base de données courante
            System.out.println("Base de données courante définie sur : " + nomBdd);
        } else {
            System.out.println("Erreur : La base de données '" + nomBdd + "' n'existe pas.");
        }
    }

	public void RemoveTableFromCurrentDatabase(String nomTable) {
		// Vérifier si une base de données courante est définie
		if (currentDatabase == null) {
			System.out.println("Erreur : Aucune base de données courante n'est définie.");
			return;
		}
	
		// Vérifier si la table existe dans la base de données courante
		Map<String, Relation> tables = databases.get(currentDatabase);
		if (tables.containsKey(nomTable)) {
			tables.remove(nomTable); // Supprimer la table
			System.out.println("Table '" + nomTable + "' supprimée avec succès de la base de données '" + currentDatabase + "'.");
		} else {
			System.out.println("Erreur : La table '" + nomTable + "' n'existe pas dans la base de données courante.");
		}
	}
	
	public void RemoveDatabases() {
		// Vider la map des bases de données
		databases.clear(); // Supprime toutes les entrées de la map
		currentDatabase = null; // Réinitialiser la base de données courante
		System.out.println("Toutes les bases de données ont été supprimées avec succès.");
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

}