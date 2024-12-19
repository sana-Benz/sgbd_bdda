import java.util.List;

public class RelationScanner implements IRecordIterator {

    private Relation relation;
    private List<Record> enregistrements;
    private int indexActuel;

    public RelationScanner(Relation relation) {
        this.relation = relation;
        this.enregistrements = relation.getAllRecords();  // Récupère tous les enregistrements de la relation
        this.indexActuel = 0;
    }

    @Override
    public Record GetNextRecord() {
        if (indexActuel >= enregistrements.size()) {
            return null;  
        }
        return enregistrements.get(indexActuel++);  // Retourne l'enregistrement suivant et incrémente l'index
    }

    @Override
    public void Close() {
        enregistrements = null;
        indexActuel = 0;
    }

    @Override
    public void Reset() {
        indexActuel = 0;  // Réinitialise l'index
    }
}


