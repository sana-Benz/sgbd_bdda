import java.nio.ByteBuffer;

	public class Buffer {
	    private PageId pageId;
	    private ByteBuffer data;
	    private int pinCount;
	    private boolean dirty;

	    public Buffer(PageId pageId, ByteBuffer data) {
	        this.pageId = pageId;
	        this.data = data;
	        this.pinCount = 0;
	        this.setDirty(false);
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

	 // Incrémente le pin_count
	    public void incrementPinCount() {
	        this.pinCount++;
	    } 

	    // Décrémente le pin_count
	    public void decrementPinCount() {
	        if (this.pinCount > 0) {
	            this.pinCount--;
	        }
	    }
	    //setters
	    public void setPageId(PageId pageId) {
	       this.pageId=pageId;
	    }
	    public void setData(ByteBuffer data) {
	        this.data = data;
	    }

	    public void setPinCount(int pinCount) {
	        this.pinCount = pinCount;
	    }

	    public void reset(){
	        pinCount=0;
	        setDirty(false);
	        this.data.clear();//vider les données du buffer 
	    }
		public boolean getDirty() {
			return dirty; 
		}
		public void setDirty(boolean dirty) {
			this.dirty = dirty;
		}
	}


