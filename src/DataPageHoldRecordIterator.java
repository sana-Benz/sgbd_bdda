import java.nio.ByteBuffer;
import java.util.List;

public class DataPageHoldRecordIterator implements IRecordIterator {
    private PageId pageId;         // Page à lire
    private BufferManager bufferManager;
    private ByteBuffer buffer;     // Buffer pour la page
    private List<Record> records;  // Liste des records
    private int currentIndex;      // Index courant dans les records

    // Constructeur : Initialise avec une page et le BufferManager
    public DataPageHoldRecordIterator(PageId pageId, BufferManager bufferManager) {
        this.pageId = pageId;
        this.bufferManager = bufferManager;
        this.buffer = bufferManager.GetPage(pageId); // Charge la page en mémoire
        this.currentIndex = 0;
    }
 
    // Retourne le prochain record ou null s'il n'y en a plus
    @Override
    public Record GetNextRecord() {
        if (currentIndex >= records.size()) {
            return null; // Plus de records
        }
        return records.get(currentIndex++); // Retourne le record courant et avance
    }
    public boolean hasNext() {
        return currentIndex < records.size(); // Vérifie s'il reste des records à lire
    }

    // Réinitialise l'itérateur pour repartir depuis le début
    @Override
    public void Reset() {
        currentIndex = 0;
    }
 
    // Ferme l'itérateur et libère le buffer
    @Override
    public void Close() {
        if (buffer != null) {
            try {
                bufferManager.FreePage(pageId, false); // Libération du buffer sans modification
            } catch (Exception e) {
                System.err.println("Erreur lors de la libération du buffer : " + e.getMessage());
            }
            buffer = null; // Réinitialise le buffer
        }
    }

}
