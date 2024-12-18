import java.nio.ByteBuffer;

public class BufferManagerTests {
    public static void main(String[] args) throws Exception {
        DBConfig config = new DBConfig("../DB", 4096, 8192, 2, "LRU");
        DiskManager diskManager = new DiskManager(config);
        BufferManager bufferManager = new BufferManager(config, diskManager);

        // Test 1: Load a page from disk
        System.out.println("Test 1: Load a page from disk");
        PageId pageId1 = new PageId(0, 0);
        ByteBuffer pageData1 = bufferManager.GetPage(pageId1);
        PageId pageId2 = new PageId(0, 1);
        ByteBuffer pageData2 = bufferManager.GetPage(pageId2);
        /*PageId pageId3 = new PageId(1, 0);
        ByteBuffer pageData3 = bufferManager.GetPage(pageId3);*/
        
        if (pageData1 != null) {
            System.out.println("Page 1 loaded successfully!");
        } else {
            System.err.println("Failed to load Page 1!");
        }
        bufferManager.printBufferPool();
        try {
        	 PageId pageId4 = new PageId(1, 1);
        	bufferManager.GetPage(pageId4);
        	System.out.println("page " +pageId1.getPageIdx() + " remplacée avec succès selon la politique LRU");
        }catch(IllegalStateException e) {
            System.err.println("LRU replacement failed: " + e.getMessage());
        }
        System.out.println("-----");
        System.out.println("Etat final du buffer pool :");
        
        bufferManager.printBufferPool();
// Free some pages to simulate unpinning
      // bufferManager.FreePage(pageId3, false);
        /*bufferManager.FreePage(pageId2, false);*/
// Load a new page to trigger LRU replacement
        //PageId pageId4 = new PageId(1, 1);
        
        /*try {
            ByteBuffer pageData5 = bufferManager.GetPage(pageId4);
            System.out.println("Page 1 replaced successfully as per LRU policy.");
        } catch (IllegalStateException e) {
            System.err.println("LRU replacement failed: " + e.getMessage());
        }*/
        

        // Test 4: Flush dirty buffers
        /*System.out.println("Test 4: Flush dirty buffers");

// Modify a page to mark it as dirty
        if (pageData4 != null) {
            pageData4.clear(); // Reset position to allow writing
            pageData4.put((byte) 1); // Modify the buffer
        }

// Free pages in the correct order
        bufferManager.FreePage(pageId3, true); // Mark as dirty and free
        bufferManager.FreePage(pageId4, false); // Ensure valid PageId from LRU replacement
        bufferManager.FreePage(pageId1, false); // Ensure PageId is still in the buffer pool

// Attempt to flush dirty buffers
        try {
            bufferManager.flushBuffers();
            System.out.println("Dirty buffers flushed successfully!");
        } catch (Exception e) {
            System.err.println("Flush failed: " + e.getMessage());
        }


*/
    }
}
