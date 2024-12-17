public class SelectOperator implements IRecordIterator {

    private IRecordIterator iterateurFils;  // L'itérateur fils qui parcourt les enregistrements
    private Condition conditionSelection;   // La condition de sélection
    private Record recordActuel;

    public SelectOperator(IRecordIterator iterateurFils, Condition condition) {
        this.iterateurFils = iterateurFils;
        this.conditionSelection = condition;
    }

    @Override
    public Record GetNextRecord() {
        while (true) {
            recordActuel = iterateurFils.GetNextRecord();
            if (recordActuel == null) {
                return null;  // Plus d'enregistrements
            }
            if (conditionSelection.evaluate(recordActuel)) {
                return recordActuel;  // Si l'enregistrement correspond à la condition
            }
        }
    }

    @Override
    public void Close() {
        if (iterateurFils != null) {
            iterateurFils.Close();
        }
    }

    @Override
    public void Reset() {
        iterateurFils.Reset();
    }
}


