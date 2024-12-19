import java.util.List;

public class PageDirectoryIterator {

    /*private List<PageId> pageIds;   // Liste des pages à parcourir
    private int currentIndex;       // Index courant dans la liste
    private BufferManager bufferManager;
    private PageId currentPage;     // Page actuellement utilisée

    // Constructeur
    public PageDirectoryIterator(List<PageId> pageIds, BufferManager bufferManager) {
        this.pageIds = pageIds;
        this.currentIndex = 0; // On commence au début
        this.bufferManager = bufferManager;
        this.currentPage = null;
    }

    // Retourne le prochain PageId ou null s'il n'y en a plus
    public PageId GetNextDataPageId() {
        // Libérer la page actuelle avant de passer à la suivante
        if (currentPage != null) {
            try {
                bufferManager.FreePage(currentPage, false); // Pas de modification à sauvegarder
            } catch (Exception e) {
                System.err.println("Erreur lors de la libération de la page : " + e.getMessage());
            }
        }

        // Vérifier s'il reste des pages à parcourir
        if (currentIndex >= pageIds.size()) {
            return null; // Fin de la liste
        }

        // Passer à la page suivante
        currentPage = pageIds.get(currentIndex);
        currentIndex++;

        return currentPage;
    }

    // Réinitialise l'itérateur pour repartir depuis le début
    public void Reset() {
        currentIndex = 0;
        currentPage = null;
    }

    // Ferme l'itérateur et libère toutes les ressources
    public void Close() {
        // Libérer la dernière page utilisée (si nécessaire)
        if (currentPage != null) {
            try {
                bufferManager.FreePage(currentPage, false);
            } catch (Exception e) {
                System.err.println("Erreur lors de la libération finale de la page : " + e.getMessage());
            }
        }

        // Nettoyer les ressources
        currentPage = null;
        pageIds = null;
    }*/
}

