import java.util.List;

public class PageDirectoryIterator {
	
    private List<PageId> pageIds; // Liste des pages à parcourir
    private int currentIndex;    // Index courant dans la liste
    private BufferManager bufferManager;

    // Constructeur
    public PageDirectoryIterator(List<PageId> pageIds, BufferManager bufferManager) {
        this.pageIds = pageIds;
        this.currentIndex = 0; // On commence au début
        this.bufferManager = bufferManager; 
    }

    // Retourne le prochain PageId ou null s'il n'y en a plus
    public PageId GetNextDataPageId() {
        if (currentIndex >= pageIds.size()) {
            return null; // Fin de la liste
        }
        PageId currentPage = pageIds.get(currentIndex);
        currentIndex++; // Passe à la page suivante

        // Libérer les ressources liées à la page si nécessaire
        try {
            bufferManager.FreePage(currentPage, false); // Pas de modification à sauvegarder
        } catch (Exception e) {
            System.err.println("Erreur lors de la libération de la page : " + e.getMessage());
        }

        return currentPage;
    }

    // Réinitialise l'itérateur pour repartir depuis le début
    public void Reset() {
        currentIndex = 0;
    }

    // Ferme l'itérateur et libère les ressources
    public void Close() {
        pageIds = null; // Libération de la liste
    }
}
