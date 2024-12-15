import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class TestRelation {
	public static void main(String[] args) {
		try {
			// Étape 1 : Initialisation des composants
			DBConfig config = new DBConfig("../DB", 8192, 24576, 100, "LRU");
			DiskManager diskManager = new DiskManager(config);
			BufferManager bufferManager = new BufferManager(config, diskManager);

			// Étape 2 : Définition du schéma de la relation
			ArrayList<ColInfo> tableCols = new ArrayList<>();
			tableCols.add(new ColInfo("ID", ColmType.INT, 0));
			tableCols.add(new ColInfo("Name", ColmType.CHAR, 20));

			// Étape 3 : Création de la relation
			Relation relation = new Relation("SimpleRelation", 2, tableCols, config, diskManager, bufferManager);
			System.out.println("Relation créée avec succès : " + relation);

			// Étape 4 : Ajout d'une page de données
			relation.addDataPage();
			System.out.println("Une page de données a été ajoutée avec succès.");

			// Étape 5 : Récupération de la première page de données
			ArrayList<PageId> dataPages = relation.getDataPages();
			if (dataPages.isEmpty()) {
				System.err.println("Erreur : aucune page de données trouvée.");
				return;
			}
			PageId firstDataPage = dataPages.get(0);

			// Étape 6 : Création d'un enregistrement
			Record record = new Record(relation, null);
			ArrayList<String> recordValues = new ArrayList<>(Arrays.asList("1", "Alice"));
			record.setValeursRec(recordValues);

			// Étape 7 : Écriture de l'enregistrement dans la page de données
			RecordId recordId = relation.writeRecordToDataPage(record, firstDataPage);
			if (recordId != null) {
				System.out.println("Enregistrement inséré avec RecordId : " + recordId);
			} else {
				System.err.println("Échec de l'insertion de l'enregistrement.");
			}

			// Étape 8 : Lecture de tous les enregistrements
			System.out.println("\nEnregistrements dans la relation :");
			ArrayList<Record> allRecords = relation.getRecordsInDataPage(firstDataPage);
			for (Record rec : allRecords) {
				System.out.println("Enregistrement : " + rec.getValeursRec());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
