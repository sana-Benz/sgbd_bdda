import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class DiskManagerTests {

	public static void main(String[] args) {
		try {
			// Initialize configuration and DiskManager
			DBConfig config = new DBConfig("../DB", 4096, 8192, 100, "LRU");
			DiskManager diskManager = new DiskManager(config);

			// Test 1: Allocate and write pages
			System.out.println("Test 1: Allocate and write pages");
			ArrayList<PageId> allocatedPages = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				PageId pageId = diskManager.AllocPage();
				allocatedPages.add(pageId);
				ByteBuffer buffer = ByteBuffer.allocate(config.getPageSize());
				buffer.put(("Test data for page " + i).getBytes());
				diskManager.WritePage(pageId, buffer);
				System.out.println("Allocated and wrote to Page: " + pageId);
			}


			// Test 2: Read back the pages
			System.out.println("Test 2: Read back the pages");
			for (PageId pageId : allocatedPages) {
				ByteBuffer readBuffer = ByteBuffer.allocate(config.getPageSize());
				diskManager.ReadPage(pageId, readBuffer);
				readBuffer.flip(); // Prepare buffer for reading
				String data = new String(readBuffer.array()).trim();
				System.out.println("Read from Page " + pageId + ": " + data);
			}

			// Test 3: Deallocate a page
			System.out.println("Test 3: Deallocate a page");
			PageId pageToDeallocate = allocatedPages.get(2); // Deallocate the third page
			diskManager.DeallocPage(pageToDeallocate);
			System.out.println("Deallocated Page: " + pageToDeallocate);

			// Test 4: Attempt to read the deallocated page
			System.out.println("Test 4: Attempt to read the deallocated page");
			ByteBuffer readBuffer = ByteBuffer.allocate(config.getPageSize());
			diskManager.ReadPage(pageToDeallocate, readBuffer);
			System.out.println("Read from deallocated Page " + pageToDeallocate + ": " + new String(readBuffer.array()).trim());

			// Test 5: Save and load state
			System.out.println("Test 5: Save and load state");
			diskManager.SaveState();
			diskManager.LoadState();
			System.out.println("State loaded successfully.");

			// Test 6: Verify the state after loading
			System.out.println("Test 6: Verify the state after loading");
			for (PageId pageId : allocatedPages) {
				if (pageId.equals(pageToDeallocate)) {
					System.out.println("Page " + pageId + " should be deallocated.");
				} else {
					ByteBuffer verifyBuffer = ByteBuffer.allocate(config.getPageSize());
					diskManager.ReadPage(pageId, verifyBuffer);
					verifyBuffer.flip();
					String data = new String(verifyBuffer.array()).trim();
					System.out.println("Page " + pageId + " contains: " + data);
				}
			}

			// Test 7: Clean up by deallocating remaining pages
			System.out.println("Test 7: Clean up by deallocating remaining pages");
			for (PageId pageId : allocatedPages) {
				if (!pageId.equals(pageToDeallocate)) {
					diskManager.DeallocPage(pageId);
					System.out.println("Deallocated Page: " + pageId);
				}
			}

			System.out.println("All tests completed successfully!");

		} catch (IOException e) {
			System.err.println("IOException occurred: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("An error occurred: " + e.getMessage());
		}
	}
}