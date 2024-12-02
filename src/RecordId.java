
public class RecordId {
	
	    private PageId pageId; // Identifiant de la page où le record est stocké
	    private int slotIdx;   // L'index du slot dans la page

	    // Constructeur simple pour initialiser le RecordId avec un PageId et un index de slot
	    public RecordId(PageId pageId, int slotIdx) {
	        this.pageId = pageId;
	        this.slotIdx = slotIdx;
	    }

	    // Getter pour l'identifiant de la page
	    public PageId getPageId() {
	        return pageId;
	    }

	    // Getter pour l'index du slot
	    public int getSlotIdx() {
	        return slotIdx;
	    }

	    // Méthode toString pour afficher RecordId sous forme lisible
	    @Override
	    public String toString() {
	        return "RecordId [PageId=" + pageId + ", SlotIdx=" + slotIdx + "]";
	    }
	}