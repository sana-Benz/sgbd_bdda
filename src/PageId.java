/**
 * Classe représentant l'identifiant unique d'une page dans un fichier.
 * Chaque page est identifiée par deux valeurs : un index de fichier et un index de page.
 */
public class PageId {
    private int FileIdx; // Index du fichier auquel appartient la page
    private int PageIdx; // Index de la page dans le fichier

    /**
     * Constructeur de la classe PageId.
     *
     * @param FileIdx L'index du fichier auquel appartient la page.
     * @param PageIdx L'index de la page dans le fichier.
     */
    public PageId(int FileIdx, int PageIdx) {
        this.FileIdx = FileIdx;
        this.PageIdx = PageIdx;
    }
    public int getFileIdx() {return FileIdx;}
    public int getPageIdx() {return PageIdx;}
    public String toString(){
        return "page : "+PageIdx+" fichier :"+FileIdx;
    }
}
