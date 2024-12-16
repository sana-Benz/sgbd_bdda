import java.nio.ByteBuffer;

public class DiskManagerTests {
	public static void main(String[] args) {
		try {
			// Initialize the configuration and DiskManager
			DBConfig config = new DBConfig("../DB", 4096, 8192, 100, "LRU");
			DiskManager dm = new DiskManager(config);

			// Test writing and reading pages
			for (int i = 0; i < 5; i++) {
				// Allocate a page
				PageId pageId = dm.AllocPage();
				System.out.println("Allocated Page: File " + pageId.getFileIdx() + ", Page " + pageId.getPageIdx());

				// Prepare data to write
				ByteBuffer writeBuffer = ByteBuffer.allocate(config.getPageSize());
				String dataToWrite = "Test data for page " + i;
				writeBuffer.put(dataToWrite.getBytes());


				// Write data to the page
				dm.WritePage(pageId, writeBuffer);
				System.out.println("Written data to Page: " + pageId);

				// Prepare buffer to read back the data
				ByteBuffer readBuffer = ByteBuffer.allocate(config.getPageSize());
				dm.ReadPage(pageId, readBuffer);
				readBuffer.flip(); // Prepare buffer for reading

				// Convert read data to string for comparison
				byte[] readDataBytes = new byte[readBuffer.remaining()];
				readBuffer.get(readDataBytes);
				String readData = new String(readDataBytes).trim(); // Convert to string and trim

				// Assert that the written data matches the read data
				assert readData.equals(dataToWrite) : "Data mismatch! Expected: " + dataToWrite + ", but got: " + readData;
				System.out.println("Data verified successfully for Page: " + pageId);
			}

			System.out.println("All tests completed successfully.");
		} catch (Exception e) {
			System.err.println("An error occurred: " + e.getMessage());
		}
	}
}