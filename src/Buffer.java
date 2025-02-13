import java.nio.ByteBuffer;

	public class Buffer {
	    private PageId pageId;
	    private ByteBuffer data;
	    private int pinCount;
	    private boolean dirty;
		private boolean isValid;

	    public Buffer(PageId pageId, ByteBuffer data) {
	        this.pageId = pageId;
	        this.data = data;
	        this.pinCount = 0;
	        this.setDirty(false);
			this.isValid = true; // Initialement valide
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

		public boolean isValid() {
			return isValid;
		}

		public void setValid(boolean valid) {
			isValid = valid;
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

		//cette méthode est utilisée quand je veux effacer une page du bufferpool (avec flushBuffer)
		//ce buffer sera alors la première victime si on veut remplacer une page
	    public void reset(){
	        pinCount=0;
	        setDirty(false);
	        this.data.clear();//vider les données du buffer
			isValid = false; // Marquer le buffer comme non valide
	    }
		public boolean getDirty() {
			return dirty; 
		}
		public void setDirty(boolean dirty) {
			this.dirty = dirty;
		}

		public String toString(){
			return "Le buffer contient la page  "+pageId+" qui a pincount= "+pinCount+" et dirtyBit "+dirty;
		}


	}