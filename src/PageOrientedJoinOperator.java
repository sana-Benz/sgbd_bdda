import java.util.ArrayList;

public class PageOrientedJoinOperator implements IRecordIterator {
    private PageDirectoryIterator pageDirectoryIterator1; // Itérateur pour la première relation
    private PageDirectoryIterator pageDirectoryIterator2; // Itérateur pour la deuxième relation
    private DataPageHoldRecordIterator dataPageIterator1; // Itérateur pour les records de la première page
    private DataPageHoldRecordIterator dataPageIterator2; // Itérateur pour les records de la deuxième page
    private Record currentRecord1; // Record courant de la première relation
    private Record currentRecord2; // Record courant de la deuxième relation

    // Constructeur qui prend les itérateurs des pages et initialise les itérateurs de records
    public PageOrientedJoinOperator(PageDirectoryIterator pageDirectoryIterator1, PageDirectoryIterator pageDirectoryIterator2) {
        this.pageDirectoryIterator1 = pageDirectoryIterator1;
        this.pageDirectoryIterator2 = pageDirectoryIterator2;
        this.currentRecord1 = null;
        this.currentRecord2 = null;
    }

    // Retourne le prochain record qui résulte de la jointure
    @Override
    public Record GetNextRecord() {
        while (true) {
            // Si on n'a pas encore de record dans la première relation, on charge la première page
            if (currentRecord1 == null) {
                PageId pageId1 = pageDirectoryIterator1.GetNextDataPageId();
                if (pageId1 == null) {
                    return null; // Plus de pages dans la première relation
                }
                dataPageIterator1 = new DataPageHoldRecordIterator(pageId1, pageDirectoryIterator1.bufferManager);
                currentRecord1 = dataPageIterator1.GetNextRecord();
            }

            // Si on n'a pas encore de record dans la deuxième relation, on charge la deuxième page
            if (currentRecord2 == null) {
                PageId pageId2 = pageDirectoryIterator2.GetNextDataPageId();
                if (pageId2 == null) {
                    return null; // Plus de pages dans la deuxième relation
                }
                dataPageIterator2 = new DataPageHoldRecordIterator(pageId2, pageDirectoryIterator2.bufferManager);
                currentRecord2 = dataPageIterator2.GetNextRecord();
            }

            // Si on a des records dans les deux relations, on fait la jointure
            if (currentRecord1 != null && currentRecord2 != null) {
                // Si les conditions de jointure sont satisfaites, on retourne les records
                if (joinCondition(currentRecord1, currentRecord2)) {
                    Record joinedRecord = createJoinedRecord(currentRecord1, currentRecord2);
                    currentRecord2 = dataPageIterator2.GetNextRecord(); // Passe au record suivant dans la deuxième relation
                    return joinedRecord;
                } else {
                    // Sinon, on passe au record suivant dans la deuxième relation
                    currentRecord2 = dataPageIterator2.GetNextRecord();
                }
            }

            // Si aucun record n'est valide pour la jointure, on passe au record suivant dans la première relation
            if (currentRecord2 == null) {
                currentRecord2 = null; // Réinitialisation du record2
                currentRecord1 = dataPageIterator1.GetNextRecord(); // Passe au record suivant dans la première relation
            }
        }
    }

    // Vérifie si les records respectent la condition de jointure
    private boolean joinCondition(Record record1, Record record2) {
       //???????????????????????????????????????????????
    }

    // Crée un nouveau record en combinant les deux records
    private Record createJoinedRecord(Record record1, Record record2) {
       //????????????????????????????????????????????
    }

    // Réinitialise l'itérateur
    @Override
    public void Reset() {
        pageDirectoryIterator1.Reset();
        pageDirectoryIterator2.Reset();
        currentRecord1 = null;
        currentRecord2 = null;
    }

    // Ferme les itérateurs et libère les ressources
    @Override
    public void Close() {
        if (dataPageIterator1 != null) {
            dataPageIterator1.Close();
        }
        if (dataPageIterator2 != null) {
            dataPageIterator2.Close();
        }
    }
}
