import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.io.IOException;



public class TestRelation {
	public static void main(String [] args) throws IOException {
		
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
		
		System.out.println("Nom de la table : " + relation.getNomRelation());
		System.out.println("Nombre de colonne : " + relation.getNbCol());
		
		ArrayList<ColInfo> colonnes = relation.getTableCols();
		for(int i=0; i<relation.getNbCol(); i++) {
			System.out.println("Nome de colonne : " + colonnes.get(i).getNameCol());
	        System.out.println("Type de colonne : " + colonnes.get(i).getTypeCol());
	        System.out.println("Taille de colonne : " + colonnes.get(i).getLengthChar());
	        
		}
		
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
			
			
		 ByteBuffer buffer = ByteBuffer.allocate(100);
		int bytesEcrite = relation.writeToBuffer(record, buffer, 0);
		System.out.println("Bytes ecrite : " + bytesEcrite);
		
		int byteslus = relation.readFromBuffer(record, buffer, 0);
		System.out.println("Bytes lus : " + byteslus);
		ArrayList<Record> listesRecord = relation.getAllRecords();
		System.out.println(listesRecord);
		
	}

}
