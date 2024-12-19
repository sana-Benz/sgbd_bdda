import java.util.ArrayList;
import java.util.List;

public class PageOrientedJoinOperator implements IRecordIterator {
    private PageDirectoryIterator pageDirIter1;
    private PageDirectoryIterator pageDirIter2;
    private DataPageHoldRecordIterator pageIter1;
    private DataPageHoldRecordIterator pageIter2;
    private Record currentRecord1;
    private Record currentRecord2;
    private List<Condition> conditions;  // Liste des conditions de jointure
    private BufferManager bufferManager1; // Buffer pour la première relation
    private BufferManager bufferManager2; // Buffer pour la deuxième relation

    
    public PageOrientedJoinOperator(PageDirectoryIterator pageDirIter1, PageDirectoryIterator pageDirIter2, 
                                     List<Condition> conditions, BufferManager bufferManager1, BufferManager bufferManager2) {
        this.pageDirIter1 = pageDirIter1;
        this.pageDirIter2 = pageDirIter2;
        this.conditions = conditions;
        this.bufferManager1 = bufferManager1;
        this.bufferManager2 = bufferManager2;
    }

    @Override
    public Record GetNextRecord() {
        // Lecture des pages pour les deux relations en utilisant deux buffers différents
        while (true) {
            // Chargement de la prochaine page de la première relation
            if (pageIter1 == null || !pageIter1.hasNext()) {
                PageId pageId1 = pageDirIter1.GetNextDataPageId();
                if (pageId1 == null) {
                    return null;  // Plus de pages pour la première relation
                }
                pageIter1 = new DataPageHoldRecordIterator(pageId1, bufferManager1);
            }
            currentRecord1 = pageIter1.GetNextRecord();  // Récupération du prochain record de la relation 1

            // Chargement de la première page de la seconde relation
            if (pageIter2 == null || !pageIter2.hasNext()) {
                PageId pageId2 = pageDirIter2.GetNextDataPageId();
                if (pageId2 == null) {
                    return null;  // Plus de pages pour la seconde relation
                }
                pageIter2 = new DataPageHoldRecordIterator(pageId2, bufferManager2);
            }
            currentRecord2 = pageIter2.GetNextRecord();  // Récupération du prochain record de la relation 2

            // Vérification des conditions de jointure
            if (joinConditionsSatisfied(currentRecord1, currentRecord2)) {
            	// Combiner les valeurs des deux records
            	ArrayList<String> combinedValues = new ArrayList<>();
            	combinedValues.addAll(currentRecord1.getValeursRec());  // Valeurs du premier record
            	combinedValues.addAll(currentRecord2.getValeursRec());  // Valeurs du second record

            	// Créer un nouveau record avec la relation et l'ID du premier record
            	Record joinedRecord = new Record(currentRecord1.getRelation(), currentRecord1.getRecordId());
            	joinedRecord.setValeursRec(combinedValues); 

            	return joinedRecord;
            }
            }
    }

    @Override
    public void Reset() {
        pageDirIter1.Reset();
        pageDirIter2.Reset();
    }

    @Override
    public void Close() {
        if (pageIter1 != null) pageIter1.Close();
        if (pageIter2 != null) pageIter2.Close();
    }

    // Méthode pour vérifier si les conditions de jointure sont satisfaites
  private boolean joinConditionsSatisfied(Record record1, Record record2) {
    for (Condition condition : conditions) {
        if (!condition.evaluate(record1) || !condition.evaluate(record2)) {
            return false; 
        }
    }
    return true;  
}
}

