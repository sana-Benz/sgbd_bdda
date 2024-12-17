import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class TestRelation {
	public static void main(String[] args) {
		try {
			System.out.println("\n Étape 1 : Initialisation des composants");
			DBConfig config = new DBConfig("../DB", 8192, 24576, 2, "LRU");
			DiskManager diskManager = new DiskManager(config);
			BufferManager bufferManager = new BufferManager(config, diskManager);

			System.out.println("\n Étape 2 : Définition du schéma de la relation");
			ArrayList<ColInfo> tableCols = new ArrayList<>();
			tableCols.add(new ColInfo("ID", ColmType.INT, 0));
			tableCols.add(new ColInfo("Name", ColmType.CHAR, 20));

			System.out.println("\nÉtape 3 : Création de la relation");
			Relation relation = new Relation("SimpleRelation", 2, tableCols, config, diskManager, bufferManager);
			bufferManager.bufferPoolState();
			System.out.println("Relation créée avec succès : " + relation);

			System.out.println("\nÉtape 4 : Ajout d'une page de données");
			relation.addDataPage();
			System.out.println("Une page de données a été ajoutée avec succès.");
			bufferManager.bufferPoolState();

			System.out.println("\nÉtape 5 : Récupération de la première page de données");
			ArrayList<PageId> dataPages = relation.getDataPages();
			if (dataPages.isEmpty()) {
				System.err.println("Erreur : aucune page de données trouvée.");
				return;
			}
			PageId firstDataPage = dataPages.get(0);

			System.out.println("voici l'id de la headerPAGE "+ relation.getHeaderPageId() + " id de la premiere page de donnees "+ firstDataPage);

			System.out.println("\nÉtape 6 : Création d'un enregistrement");
			Record record = new Record(relation, null);
			ArrayList<String> recordValues = new ArrayList<>(Arrays.asList("1", "Alice"));
			record.setValeursRec(recordValues);

			System.out.println("\nÉtape 7 : Écriture de l'enregistrement dans la page de données");
			System.out.println("voici l'id de la datapage" + firstDataPage);
			System.out.println("voici le record qu'on va ecrire "+ record);
			RecordId recordId = relation.writeRecordToDataPage(record, firstDataPage);
			if (recordId != null) {
				System.out.println("Enregistrement inséré avec RecordId : " + recordId);
			} else {
				System.err.println("Échec de l'insertion de l'enregistrement.");
			}

			System.out.println("\nÉtape 8 : Lecture de tous les enregistrements");
			System.out.println("Enregistrements dans la relation :");
			ArrayList<Record> allRecords = relation.getRecordsInDataPage(firstDataPage);
			for (Record rec : allRecords) {
				System.out.println("Enregistrement : " + rec.getValeursRec());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
