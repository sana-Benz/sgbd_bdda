import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
	private List<Database> databases;
	private DBConfig config;
	private Database currentDatabase;

	// Constructeur prenant une instance de DBConfig
	public DBManager(DBConfig config, List<Database> databases, Database currentDatabase) {
		this.config = config;
		this.databases = databases;
		this.currentDatabase = currentDatabase;
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
		if (currentDatabase == null || !databases.contains(currentDatabase)) {
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
		if (currentDatabase == null || !databases.contains(currentDatabase)) {
			throw new IllegalStateException("Aucune base de données active ou inexistante.");
		}
		boolean removed = currentDatabase.removeTable(nomTable);
		if (!removed) {
			throw new IllegalArgumentException("La table " + nomTable + " n'existe pas dans la base active.");
		}
		System.out.println("Table " + nomTable + " supprimée avec succès de la base " + currentDatabase.getNom() + ".");
	}

	/**
	 * Sauvegarde l'état actuel des bases de données et des tables dans un fichier.
	 */
	public void SaveState() {
		File fichierSauvegarde = new File(config.getDbpath() + "/databases.save");
		try (BufferedWriter ecrivain = new BufferedWriter(new FileWriter(fichierSauvegarde))) {
			for (Database database : databases) {
				ecrivain.write("DATABASE:" + database.getNom());
				ecrivain.newLine();
				List<Relation> tables = database.getRelations();
				if (tables.isEmpty()) {
					ecrivain.write("  PAS_DE_TABLES"); // Indiquer que la base n'a pas de tables
					ecrivain.newLine();
				} else {
					for (Relation relation : tables) {
						if (relation == null) {
							throw new IllegalStateException("Relation invalide dans la base " + database.getNom());
						}
						ecrivain.write("TABLE:" + relation.getNomRelation() + ":HeaderPageId=" + relation.getHeaderPageId());
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
