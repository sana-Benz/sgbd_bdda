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
	
	public String getValeurByNomCol(String nomCol) {
	    int indexCol = relation.getColIndex(nomCol);
	    if (indexCol == -1) {
	        throw new IllegalArgumentException("Nom de colonne invalide : " + nomCol);
	    }
	    return valeurRec(indexCol);
	}

}