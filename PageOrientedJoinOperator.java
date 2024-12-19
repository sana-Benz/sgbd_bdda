public class PageOrientedJoinOperator implements IRecordIterator {

    /*private PageId leftPageId;
    private PageId rightPageId;
    private BufferManager bufferManager;
    private IRecordIterator leftIterator;  // Itérateur pour les records de la page gauche
    private IRecordIterator rightIterator; // Itérateur pour les records de la page droite
    private Record currentLeftRecord;      // Record actuel de la page gauche
    private Record currentRightRecord;     // Record actuel de la page droite
    private Condition joinCondition;       // Condition de jointure

    public PageOrientedJoinOperator(PageId leftPid, PageId rightPid, BufferManager buffer, String condition) {
        this.leftPageId = leftPid;
        this.rightPageId = rightPid;
        this.bufferManager = buffer;
        this.joinCondition = new Condition(condition);

        // Initialiser les itérateurs pour parcourir les pages des relations
        this.leftIterator = new DataPageHoldRecordIterator(leftPageId, bufferManager);
        this.rightIterator = new DataPageHoldRecordIterator(rightPageId, bufferManager);
    }

    @Override
    public Record GetNextRecord() {
        // Cherche le prochain enregistrement qui satisfait la condition de jointure
        if (findNextMatchingRecord()) {
            return combineRecords(currentLeftRecord, currentRightRecord);
        }
        return null;  // Aucun match trouvé
    }

    private boolean findNextMatchingRecord() {
        // Parcours des records à gauche
        while (true) {
            currentLeftRecord = leftIterator.GetNextRecord();
            if (currentLeftRecord == null) {
                return false;  // Aucun record à gauche à traiter
            }

            rightIterator.Reset();  // Réinitialiser l'itérateur de la page droite

            // Parcours des records à droite
            while (true) {
                currentRightRecord = rightIterator.GetNextRecord();
                if (currentRightRecord == null) {
                    break;  // Plus de records à droite
                }

                // Vérifier la condition de jointure
                if (checkJoinCondition(currentLeftRecord, currentRightRecord)) {
                    return true;  // Une correspondance a été trouvée
                }
            }
        }
    }

    boolean checkJoinCondition(Record left, Record right) {
        return joinCondition.evaluate(left) && joinCondition.evaluate(right);
    }

    private Record combineRecords(Record left, Record right) {
        Record combined = new Record(left.getRelation(), left.getRecordId());
        combined.getValeursRec().addAll(left.getValeursRec());
        combined.getValeursRec().addAll(right.getValeursRec());
        return combined;
    }

    @Override
    public void Reset() {
        leftIterator.Reset();
        rightIterator.Reset();
    }

    @Override
    public void Close() {
        if (leftIterator != null) {
            leftIterator.Close();
        }
        if (rightIterator != null) {
            rightIterator.Close();
        }
    }*/
}
