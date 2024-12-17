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
	  // Méthode addAttribut pour ajouter un attribut à l'enregistrement
    public void addAttribut(String value) {
        this.valeursRec.add(value); // Ajouter un attribut à la liste de valeurs
    }
	// Récupère l'ID du record
	public RecordId getRecordId() {
	    return recordId; 
	}

	// Récupère la relation associée au record
	public Relation getRelation() {
	    return relation;
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