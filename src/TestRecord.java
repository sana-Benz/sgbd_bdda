import java.io.IOException;
import java.util.ArrayList;
public class TestRecord {
	public static void main( String[] args) throws IOException{
		
		// Initialisation de la configuration et du DiskManager
		DBConfig config = new DBConfig("../DB", 4096, 8192, 100, "LRU");
		DiskManager diskManager = new DiskManager(config);
		// Initialisation du BufferManager avec la politique LRU par défaut
        BufferManager bufferManager = new BufferManager(config, diskManager);
        // allocation de la page depuis le disk  
        PageId headerPageId = diskManager.AllocPage();   
        System.out.println("Page allouée : Fichier " + headerPageId.getFileIdx() + ", Page " + headerPageId.getPageIdx());

		ArrayList<ColInfo> tableCols = new ArrayList<>();
		tableCols.add(new ColInfo("Colonne1", ColmType.INT, 0));
		tableCols.add(new ColInfo("Colonne2", ColmType.FLOAT, 0));
		tableCols.add(new ColInfo("Colonne3", ColmType.CHAR, 15));
		tableCols.add(new ColInfo("Colonne4", ColmType.VARCHAR, 25));
		Relation relation = new Relation(config, "table1", 4, tableCols, headerPageId, diskManager, bufferManager);
		
		int slotIndex = 0;
		RecordId recordId = new RecordId(headerPageId, slotIndex);
		Record record = new Record(relation, recordId);
		 System.out.println("Record inséré avec RecordId : " + recordId.toString());
		 
		 ArrayList<String> valeursRec = new ArrayList<>();
			valeursRec.add("2457");
			valeursRec.add("15.75");
			valeursRec.add("LionelMessi");
			valeursRec.add("ProjetBDDAL3");
			record.setValeursRec(valeursRec);
		
		
		ArrayList<String> valeursLus = record.getValeursRec();
		for(int i = 0; i < valeursLus.size(); i++) {
			System.out.println("Colonne" + (i+1) + " : "+ valeursLus.get(i));
		}
		
		
	}

}
