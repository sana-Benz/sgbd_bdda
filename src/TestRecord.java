import java.nio.ByteBuffer;
import java.util.ArrayList;
public class TestRecord {
	public static void main( String[] args) {
		ArrayList<ColInfo> tableCols = new ArrayList<>();
		tableCols.add(new ColInfo("Colonne1", ColmType.INT, 0));
		tableCols.add(new ColInfo("Colonne2", ColmType.FLOAT, 0));
		tableCols.add(new ColInfo("Colonne3", ColmType.CHAR, 15));
		tableCols.add(new ColInfo("Colonne4", ColmType.VARCHAR, 25));
		Relation relation = new Relation("table1", 4, tableCols);
		
		Record record = new Record(relation);
		ArrayList<String> valeursRec = new ArrayList<>();
		valeursRec.add("2457");
		valeursRec.add("15.75");
		valeursRec.add("LionelMessi");
		valeursRec.add("ProjetBDDAL3");
		record.setValeursRec(valeursRec);
		
		
		
		
		
		ByteBuffer buffer = ByteBuffer.allocate(100);
		int bytesEcrite = record.writeToBuffer(buffer, 0);
		System.out.println("Bytes ecrite : " + bytesEcrite);
		
		Record record2 = new Record(relation);
		int byteslus = record2.readFromBuffer(buffer, 0);
		System.out.println("Bytes lus : " + byteslus);
		/*
		ArrayList<String> valeursLus = record2.getValeursRec();
		for(int i = 0; i < valeursLus.size(); i++) {
			System.out.println("Colonne" + (i+1) + " : "+ valeursLus.get(i));
		}
		*/
		
	}

}
