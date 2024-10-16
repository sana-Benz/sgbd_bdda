import java.nio.ByteBuffer;

public class Buffer {
    private PageId pageId;
    private ByteBuffer data;
    private int pinCount;
    private boolean dirty;

    public Buffer(PageId pageId, ByteBuffer data) {
        this.pageId = pageId;
        this.data = data;
        this.pinCount = 1;
        this.dirty = false;
    }
    //getters
    public PageId getPageId() {
        return pageId;
    }

    public ByteBuffer getData() {
        return data;
    }

    public int getPinCount() {
        return pinCount;
    }
    public boolean isDirty(){
        return dirty;
    }

    //setters
    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public void setPinCount(int pinCount) {
        this.pinCount = pinCount;
    }

    public void reset(){
        pinCount=1;
        dirty= false;
    }
    public void incrementPinCount(){
        pinCount++;
    }
    public void setDirty(){
        if(!this.dirty)
            this.dirty = true;
    }

    public void decrementerLePinCount(){
        if(pinCount > 0)
            pinCount--;

    }
}

