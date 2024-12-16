import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Record {
	private Relation relation;
	private ArrayList<String> valeursRec;
	private RecordId recordId;

	public Record(Relation relation, RecordId recordId) {
		this.relation = relation;
		this.valeursRec = new ArrayList<>();
		this.recordId = recordId;
	}

	
	public Relation getRelation() {
		return relation;
	}


	public RecordId getRecordId() {
		return recordId;
	}


	public ArrayList<String> getValeursRec() {
		return valeursRec;
	}

	public void setValeursRec(ArrayList<String> valeursRec) {
		this.valeursRec = valeursRec;
	}

	public String valeurRec(int indexCol) {
		if(indexCol < 0 || indexCol > valeursRec.size())
			throw new IndexOutOfBoundsException("Indice Invalide");
		return valeursRec.get(indexCol);
	}

}