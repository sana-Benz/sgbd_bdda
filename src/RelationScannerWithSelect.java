public class RelationScannerWithSelect implements IRecordIterator {
    private IRecordIterator relationIterator;  // L'itérateur pour parcourir les enregistrements de la relation
    private Condition selectionCondition;     // Condition de sélection
    private Record currentRecord;             // Enregistrement courant

    public RelationScannerWithSelect(IRecordIterator relationIterator, Condition selectionCondition) {
        this.relationIterator = relationIterator;
        this.selectionCondition = selectionCondition;
    }

    @Override
    public Record GetNextRecord() {
        while (true) {
            currentRecord = relationIterator.GetNextRecord();  // Récupérer l'enregistrement suivant
            if (currentRecord == null) {
                return null;  // Aucun enregistrement suivant
            }
            if (selectionCondition.evaluate(currentRecord)) {
                return currentRecord;  // L'enregistrement respecte la condition
            }
        }
    }

    @Override
    public void Close() {
        if (relationIterator != null) {
            relationIterator.Close();  // Fermer l'itérateur de la relation
        }
    }

    @Override
    public void Reset() {
        relationIterator.Reset();  // Réinitialiser l'itérateur de la relation
    }
}

