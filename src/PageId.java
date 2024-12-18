import java.util.*;



public class PageId {
    private int FileIdx;
    private int PageIdx;
    
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
