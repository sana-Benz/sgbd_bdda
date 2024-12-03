import java.nio.ByteBuffer;
import java.util.ArrayList;
public class TestRecord {
	public static void main( String[] args) throws Exception{
		// Initialisation de la configuration et du DiskManager et du BufferManager
		DBConfig config = new DBConfig("../DB", 4096, 8192, 100, "LRU");
		DiskManager diskManager = new DiskManager(config);
		BufferManager bufferManager = new BufferManager(config, diskManager);

		//allocation d'une datapage
		PageId dataPageId = diskManager.AllocPage();
		ByteBuffer dataPage = ByteBuffer.allocate(config.getPageSize());
		diskManager.WritePage(dataPageId, dataPage);

		//allocation d'une headerPage
		PageId headerPageId = diskManager.AllocPage();
		ByteBuffer headerPage = ByteBuffer.allocate(config.getPageSize());
		headerPage.putInt(1); // nombre de dataPages
		headerPage.putInt(dataPageId.getFileIdx()); // FileIdx de la premiere DataPage
		headerPage.putInt(dataPageId.getPageIdx()); // PageIdx
		headerPage.putInt(config.getPageSize() - 8); // espace libre
		diskManager.WritePage(headerPageId, headerPage);

		//création de la relation table 1
		ArrayList<ColInfo> tableCols = new ArrayList<>();
		tableCols.add(new ColInfo("Colonne1", ColmType.INT, 0));
		tableCols.add(new ColInfo("Colonne2", ColmType.FLOAT, 0));
		tableCols.add(new ColInfo("Colonne3", ColmType.CHAR, 15));
		tableCols.add(new ColInfo("Colonne4", ColmType.VARCHAR, 25));
		Relation relation = new Relation("table1", 4, tableCols, config, headerPageId,diskManager,bufferManager);

		//création d'un record
		RecordId recordId = new RecordId(dataPageId,0);
		Record record = new Record(relation,recordId);
		ArrayList<String> valeursRec = new ArrayList<>();
		valeursRec.add("2457");
		valeursRec.add("15.75");
		valeursRec.add("LionelMessi");
		valeursRec.add("ProjetBDDAL3");
		record.setValeursRec(valeursRec);


		//ByteBuffer buffer = ByteBuffer.allocate(100);

		//ecriture du record dans la dataPage
		System.out.println("test 1 : écriture d'un record dans un buffer");
		int bytesEcrite = relation.writeToBuffer(record, dataPage, 8);
		System.out.println("Bytes ecrite : " + bytesEcrite);

		//sauvegarde de la dataPage dans le disque
		diskManager.WritePage(dataPageId, dataPage);

		//lecture du record à partir de la dataPage
		System.out.println("test 2 : lecture d'un record d'un buffer");
		Record recordRead = new Record(relation, new RecordId(dataPageId, 0));
		diskManager.ReadPage(dataPageId, dataPage);
		int byteslus = relation.readFromBuffer(recordRead, dataPage, 8);
		System.out.println("Bytes lus : " + byteslus);

		//validation des valeurs
		ArrayList<String> valeursLues = recordRead.getValeursRec();
		for (int i = 0; i < valeursLues.size(); i++) {
			System.out.println("Colonne" + (i + 1) + ": " + valeursLues.get(i));
			if (!valeursRec.get(i).equals(valeursLues.get(i))) {
				System.err.println("Mismatch at Colonne" + (i + 1));
			}
		}
		
		
	}

}
