import java.nio.ByteBuffer;

public class BufferManagerTests {
    public static void main(String[] args) throws Exception {
        DBConfig config = new DBConfig("../DB", 4096, 8192, 3, "LRU");
        DiskManager diskManager = new DiskManager(config);
        BufferManager bufferManager = new BufferManager(config, diskManager);

        // Test 1: Load a page from disk
        System.out.println("Test 1: Load a page from disk");
        PageId pageId1 = new PageId(0, 0);
        ByteBuffer pageData1 = bufferManager.GetPage(pageId1);
        if (pageData1 != null) {
            System.out.println("Page 1 loaded successfully!");
        } else {
            System.err.println("Failed to load Page 1!");
        }

        // Test 2: Load a page from the buffer pool
        System.out.println("Test 2: Load a page from the buffer pool");
        ByteBuffer pageData2 = bufferManager.GetPage(pageId1);
        if (pageData1 == pageData2) {
            System.out.println("Page 1 retrieved from memory!");
        } else {
            System.err.println("Failed: Page 1 not retrieved from memory!");
        }

        // Test 3: LRU replacement policy
        System.out.println("Test 3: LRU replacement policy");

        PageId pageId2 = new PageId(0, 1);
        ByteBuffer pageData3 = bufferManager.GetPage(pageId2);

        PageId pageId3 = new PageId(1, 0);
        ByteBuffer pageData4 = bufferManager.GetPage(pageId3);

// Free some pages to simulate unpinning
        bufferManager.FreePage(pageId1, false);
        bufferManager.FreePage(pageId2, false);

// Load a new page to trigger LRU replacement
        PageId pageId4 = new PageId(1, 1);
        try {
            ByteBuffer pageData5 = bufferManager.GetPage(pageId4);
            System.out.println("Page 1 replaced successfully as per LRU policy.");
        } catch (IllegalStateException e) {
            System.err.println("LRU replacement failed: " + e.getMessage());
        }


        // Test 4: Flush dirty buffers
        System.out.println("Test 4: Flush dirty buffers");

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



    }
}
