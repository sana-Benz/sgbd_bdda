import java.nio.ByteBuffer;

public class BufferManager {
    private ByteBuffer buffer;
    private PageId pageId;
    private int pinCount;
    private int dirty;

    public BufferManager(PageId pageId) {
        this.pageId = pageId;
    }

    public BufferManager(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public BufferManager(int pinCount) {
        this.pinCount = pinCount;
    }
    public BufferManager(int dirty){
        this.dirty = dirty;
    }

    public int getDirty() {
        return dirty;
    }

    public void setDirty(int dirty) {
        this.dirty = dirty;
    }
    public void freePage(PageId pageId, int valdirty){

    }
}
