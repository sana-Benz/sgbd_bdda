import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class TestRelation {
	public static void main(String [] args) throws IOException {
		// Initialisation de la configuration et du DiskManager et du BufferManager
		DBConfig config = new DBConfig("../DB", 4096, 8192, 100, "LRU");
		DiskManager diskManager = new DiskManager(config);
		BufferManager bufferManager = new BufferManager(config, diskManager);

		// Allocation d'une Header Page et initialisation
		PageId headerPageId = diskManager.AllocPage();
		ByteBuffer headerPage = ByteBuffer.allocate(config.getPageSize());
		headerPage.putInt(1); // Une seule page de données pour commencer
		headerPage.putInt(0); // FileIdx de la première page
		headerPage.putInt(0); // PageIdx de la première page
		headerPage.putInt(config.getPageSize() - 8); // Espace libre initial
		diskManager.WritePage(headerPageId, headerPage);


		// Création de la relation
		ArrayList<ColInfo> tableCols = new ArrayList<>();
		tableCols.add(new ColInfo("Colonne1", ColmType.INT, 0));
		tableCols.add(new ColInfo("Colonne2", ColmType.FLOAT, 0));
		tableCols.add(new ColInfo("Colonne3", ColmType.CHAR, 15));
		tableCols.add(new ColInfo("Colonne4", ColmType.VARCHAR, 25));
		Relation relation = new Relation("table1", 4, tableCols,config,headerPageId,diskManager,bufferManager);

		System.out.println("affichage des informations liées à la table");
		System.out.println("Nom de la table : " + relation.getNomRelation());
		System.out.println("Nombre de colonnes : " + relation.getNbCol());
		
		ArrayList<ColInfo> colonnes = relation.getTableCols();
		for(int i=0; i<relation.getNbCol(); i++) {
			System.out.println("Nom de la colonne : " + colonnes.get(i).getNameCol());
	        System.out.println("Type de la colonne : " + colonnes.get(i).getTypeCol());
	        System.out.println("Taille de la colonne : " + colonnes.get(i).getLengthChar());
	        
		}
		System.out.println(relation.toString());

		// Test 6: addDataPage
		System.out.println("Test 6: Ajouter une Data Page");
		relation.addDataPage();
		System.out.println("Data Pages après ajout : " + relation.getDataPages().size());

		// Test 7: getFreeDataPageId
		System.out.println("Test 7: Recherche de page avec espace libre");
		PageId freePage = relation.getFreeDataPageId(100);
		if (freePage != null) {
			System.out.println("Page trouvée : FileIdx = " + freePage.getFileIdx() + ", PageIdx = " + freePage.getPageIdx());
		} else {
			System.out.println("Aucune page avec assez d'espace libre");
		}

		// Test 8: Écriture et lecture de record
		System.out.println("Test 8: Écriture et lecture de record");
		PageId dataPageId = diskManager.AllocPage(); // Allocation d'une Data Page
		ByteBuffer dataPage = ByteBuffer.allocate(config.getPageSize());
		diskManager.WritePage(dataPageId, dataPage);

		// Création d'un RecordId et d'un Record
		RecordId recordId = new RecordId(dataPageId, 0); // Création d'un RecordId avec slotIdx = 0
		Record record = new Record(relation, recordId); // Création du Record avec RecordId

		// Ajout des valeurs au Record
		ArrayList<String> valeursRec = new ArrayList<>();
		valeursRec.add("123");
		valeursRec.add("45.67");
		valeursRec.add("TestCHAR");
		valeursRec.add("TestVARCHAR");
		record.setValeursRec(valeursRec);

		// Écriture du record dans la Data Page
		int bytesWritten = relation.writeToBuffer(record, dataPage, 8); // Offset 8 pour laisser l'espace des métadonnées
		System.out.println("Bytes écrits : " + bytesWritten);
		diskManager.WritePage(dataPageId, dataPage);

		// Lecture du record
		Record recordRead = new Record(relation, new RecordId(dataPageId, 0)); // Utilisation du RecordId pour la lecture
		diskManager.ReadPage(dataPageId, dataPage);
		int bytesRead = relation.readFromBuffer(recordRead, dataPage, 8);
		System.out.println("Bytes lus : " + bytesRead);
		System.out.println("Valeurs lues : " + recordRead.getValeursRec());

		// Test 9: getAllRecords
		System.out.println("Test 9: getAllRecords");
		ArrayList<Record> allRecords = relation.getAllRecords();
		System.out.println("Nombre de records : " + allRecords.size());

		// Test 10: getRecordsInDataPage
		System.out.println("Test 10: Records dans une Data Page");
		ArrayList<Record> recordsInPage = relation.getRecordsInDataPage(dataPageId);
		System.out.println("Nombre de records dans la page : " + recordsInPage.size());
		
	}

}
