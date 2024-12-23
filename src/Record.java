import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Record {
	private Relation relation;
	private ArrayList<String> valeursRec;
	private RecordId recordId;
    private ArrayList<Field> fields; // Liste des champs de l'enregistrement


	public Record(Relation relation, RecordId recordId) {
		this.relation = relation;
		this.valeursRec = new ArrayList<>();
		this.recordId = recordId;
        this.fields = new ArrayList<>(); // Initialiser la liste des champs

	}
	
	// Méthode pour ajouter un champ
    public void addField(Field field) {
        fields.add(field);
    }

    // Méthode pour obtenir la liste des champs
    public ArrayList<Field> getFields() {
        return fields;
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
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Record [");

		// Ajouter l'ID du record s'il existe
		if (recordId != null) {
			sb.append("Record ID: ").append(recordId).append(", ");
		}

		// Ajouter les valeurs des colonnes
		sb.append("Valeurs: ");
		for (int i = 0; i < valeursRec.size(); i++) {
			sb.append(valeursRec.get(i));
			if (i < valeursRec.size() - 1) {
				sb.append(", ");
			}
		}

		sb.append("]");
		return sb.toString();
	}

}