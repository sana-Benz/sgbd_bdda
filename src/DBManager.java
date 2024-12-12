import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBManager {
	private List<Database> databases;
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

}