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

    /**
     * Retourne l'index du fichier.
     *
     * @return L'index du fichier (FileIdx).
     */
    public int getFileIdx() {
        return FileIdx;
    }

    /**
     * Retourne l'index de la page.
     *
     * @return L'index de la page (PageIdx).
     */
    public int getPageIdx() {
        return PageIdx;
    }

    /**
     * Vérifie si un autre PageId est égal à celui-ci.
     * Deux PageId sont considérés égaux si leurs FileIdx et PageIdx respectifs sont identiques.
     *
     * @param pageId L'objet PageId à comparer avec l'instance courante.
     * @return {@code true} si les deux PageId sont égaux, {@code false} sinon.
     */
    public boolean equals(PageId pageId) {
        if (this.FileIdx == pageId.getFileIdx() && this.PageIdx == pageId.getPageIdx()) {
            return true;
        }
        return false;
    }

    /**
     * Vérifie si le PageId est valide.
     * Un PageId est considéré valide si les deux indices (FileIdx et PageIdx) sont supérieurs ou égaux à zéro.
     *
     * @return {@code true} si le PageId est valide, {@code false} sinon.
     */
    public boolean estValid() {
        if (this.FileIdx < 0 || this.PageIdx < 0) {
            return false;
        }
        return true;
    }
}
