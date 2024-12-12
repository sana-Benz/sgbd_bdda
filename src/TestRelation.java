import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class TestRelation {
	public static void main(String[] args) throws IOException {
		// Initialisation des composants de base
		DBConfig config = new DBConfig("../DB", 4096, 8192, 100, "LRU");
		DiskManager diskManager = new DiskManager(config);
		BufferManager bufferManager = new BufferManager(config, diskManager);

		// Allocation et initialisation de la Header Page
		PageId headerPageId = diskManager.AllocPage();
		System.out.println("Page allouée : Fichier " + headerPageId.getFileIdx() + ", Page " + headerPageId.getPageIdx());
		ByteBuffer headerPage = ByteBuffer.allocate(config.getPageSize());
		headerPage.putInt(0); // Initialisation : 0 pages
		diskManager.WritePage(headerPageId, headerPage);

		// Création de la relation avec des colonnes
		ArrayList<ColInfo> tableCols = new ArrayList<>();
		tableCols.add(new ColInfo("Colonne1", ColmType.INT, 0));
		tableCols.add(new ColInfo("Colonne2", ColmType.FLOAT, 0));
		tableCols.add(new ColInfo("Colonne3", ColmType.CHAR, 15));
		tableCols.add(new ColInfo("Colonne4", ColmType.VARCHAR, 25));
		Relation relation = new Relation("table1", 4, tableCols, config, headerPageId, diskManager, bufferManager);

		// Test 1 : Ajout de pages de données
		System.out.println("Test 1: Ajout de Data Pages");
		relation.addDataPage();
		relation.addDataPage();
		ArrayList<PageId> dataPages = relation.getDataPages();
		System.out.println("Pages de données après ajout : " + dataPages.size());

		// Test 2 : Insertion de records
		System.out.println("Test 2: Insertion de records");
		Record record = new Record(relation, null);
		record.getValeursRec().add("42");        // INT
		record.getValeursRec().add("3.14");      // FLOAT
		record.getValeursRec().add("HelloWorld"); // CHAR (15)
		record.getValeursRec().add("DynamicString"); // VARCHAR (25)

		// Recherche d'une page avec de l'espace libre
		PageId freePage = relation.getFreeDataPageId(100);
		if (freePage != null) {
			RecordId recordId = relation.writeRecordToDataPage(record, freePage);
			System.out.println("Record inséré avec succès à : FileIdx = " + recordId.getPageId().getFileIdx() + ", PageIdx = " + recordId.getPageId().getPageIdx());
		} else {
			System.out.println("Aucune page avec assez d'espace libre pour insérer le record.");
		}

		// Test 3 : Récupération de tous les records
		System.out.println("Test 3: Récupération de tous les records");
		ArrayList<Record> allRecords = relation.getAllRecords();
		System.out.println("Nombre total de records : " + allRecords.size());
		for (Record rec : allRecords) {
			System.out.println("Record récupéré : " + rec.getValeursRec());
		}

		// Test 4 : Récupération des records dans une page spécifique
		System.out.println("Test 4: Récupération des records dans une page spécifique");
		if (!dataPages.isEmpty()) {
			ArrayList<Record> recordsInPage = relation.getRecordsInDataPage(dataPages.get(0));
			System.out.println("Nombre de records dans la page : " + recordsInPage.size());
			for (Record rec : recordsInPage) {
				System.out.println("Record dans la page : " + rec.getValeursRec());
			}
		} else {
			System.out.println("Aucune page de données pour tester la récupération des records.");
		}

		// Test 5 : Ajout et validation de nouvelles pages
		System.out.println("Test 5: Ajout de nouvelles pages et validation");
		relation.addDataPage();
		dataPages = relation.getDataPages();
		System.out.println("Nombre total de pages après ajout : " + dataPages.size());
	}
}
