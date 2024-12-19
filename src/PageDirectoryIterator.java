import java.util.List;

public class PageDirectoryIterator {

    private List<PageId> pageIds;
    private int currentIndex;
    private BufferManager bufferManager;
    private PageId currentPage;

    public PageDirectoryIterator(List<PageId> pageIds, BufferManager bufferManager) {
        this.pageIds = pageIds;
        this.currentIndex = 0;
        this.bufferManager = bufferManager;
        this.currentPage = null;
    }

    public PageId GetNextDataPageId() {
        if (currentPage != null) {
            try {
                bufferManager.FreePage(currentPage, false);
            } catch (Exception e) {
                System.err.println("Erreur lors de la libération de la page : " + e.getMessage());
            }
        }

        if (currentIndex >= pageIds.size()) {
            return null;
        }

        currentPage = pageIds.get(currentIndex);
        currentIndex++;

        return currentPage;
    }

    public void Reset() {
        currentIndex = 0;
        currentPage = null;
    }

    public void Close() {
        if (currentPage != null) {
            try {
                bufferManager.FreePage(currentPage, false);
            } catch (Exception e) {
                System.err.println("Erreur lors de la libération finale de la page : " + e.getMessage());
            }
        }
        currentPage = null;
        pageIds = null;
    }
}


