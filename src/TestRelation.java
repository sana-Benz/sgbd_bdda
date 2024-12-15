import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class TestRelation {
	public static void main(String[] args) {
		try {
			// Step 1: Initialize components
			DBConfig config = new DBConfig("../DB", 8192, 24576, 100, "LRU");
			DiskManager diskManager = new DiskManager(config);
			BufferManager bufferManager = new BufferManager(config, diskManager);


			// Step 3: Define the relation schema
			ArrayList<ColInfo> tableCols = new ArrayList<>();
			tableCols.add(new ColInfo("ID", ColmType.INT, 0));
			tableCols.add(new ColInfo("Name", ColmType.CHAR, 20));
			tableCols.add(new ColInfo("Age", ColmType.INT, 0));
			tableCols.add(new ColInfo("Salary", ColmType.FLOAT, 0));

			// Step 4: Create the relation
			Relation relation = new Relation("Employee", 4, tableCols, config, diskManager, bufferManager);
			System.out.println("Relation created successfully: " + relation);

			// Step 5: Test addDataPage
			System.out.println("\nTesting addDataPage:");
			relation.addDataPage();
			ArrayList<PageId> dataPages = relation.getDataPages();
			if (dataPages == null || dataPages.isEmpty()) {
				System.err.println("No data pages found after calling addDataPage.");
				return;
			} else {
				System.out.println("Data pages after adding a page: " + dataPages);
			}

			// Debugging the header page content
			ByteBuffer debugBuffer = ByteBuffer.allocate(config.getPageSize());
			diskManager.ReadPage(relation.getHeaderPageId(), debugBuffer);
			System.out.println("Debug Header Page Content: " + Arrays.toString(debugBuffer.array()));

			// Step 6: Test writeToBuffer and readFromBuffer
			System.out.println("\nTesting writeToBuffer and readFromBuffer:");
			Record record = new Record(relation, null);
			ArrayList<String> recordValues = new ArrayList<>(Arrays.asList("1", "Alice", "30", "50000.50"));
			record.setValeursRec(recordValues);

			ByteBuffer buffer = ByteBuffer.allocate(config.getPageSize());
			int recordSize = relation.writeToBuffer(record, buffer, 0);
			System.out.println("Record written to buffer: " + recordValues + " (Size: " + recordSize + " bytes)");

			Record readRecord = new Record(relation, null);
			int bytesRead = relation.readFromBuffer(readRecord, buffer, 0);
			System.out.println("Record read from buffer: " + readRecord.getValeursRec() + " (Bytes Read: " + bytesRead + ")");
			if (recordValues.equals(readRecord.getValeursRec())) {
				System.out.println("Write and Read to/from buffer validated successfully.");
			} else {
				System.err.println("Mismatch in buffer write/read validation.");
				System.err.println("Expected: " + recordValues);
				System.err.println("Actual: " + readRecord.getValeursRec());
			}

			// Step 7: Test writing multiple records to the first data page
			System.out.println("\nTesting multiple record insertions:");
			for (int i = 2; i <= 5; i++) {
				Record multiRecord = new Record(relation, null);
				ArrayList<String> multiRecordValues = new ArrayList<>(Arrays.asList(
						String.valueOf(i), "Name" + i, String.valueOf(20 + i), String.valueOf(1000.0 * i)
				));
				multiRecord.setValeursRec(multiRecordValues);

				RecordId recordId = relation.writeRecordToDataPage(multiRecord, dataPages.get(0));
				if (recordId != null) {
					System.out.println("Record written with RecordId: " + recordId);
				} else {
					System.err.println("Failed to write record: " + multiRecordValues);
				}
			}

			// Step 8: Test free space in data page after insertions
			ByteBuffer dataPageBuffer = bufferManager.GetPage(dataPages.get(0));
			int freeSpace = dataPageBuffer.getInt(0); // Free space pointer
			System.out.println("Free space after insertions: " + freeSpace + " bytes");
			bufferManager.FreePage(dataPages.get(0), false);

			// Step 9: Test getRecordsInDataPage
			System.out.println("\nTesting getRecordsInDataPage:");
			if (!dataPages.isEmpty()) {
				ArrayList<Record> recordsInPage = relation.getRecordsInDataPage(dataPages.get(0));
				if (recordsInPage.isEmpty()) {
					System.err.println("No records found in the data page. Check writeRecordToDataPage.");
				} else {
					for (Record rec : recordsInPage) {
						System.out.println("Record in data page: " + rec.getValeursRec());
					}
				}
			} else {
				System.err.println("No data pages available to test getRecordsInDataPage.");
			}

			// Step 10: Test getAllRecords
			System.out.println("\nTesting getAllRecords:");
			ArrayList<Record> allRecords = relation.getAllRecords();
			if (allRecords.isEmpty()) {
				System.err.println("No records found in the relation. Check getAllRecords and record insertion logic.");
			} else {
				for (Record rec : allRecords) {
					System.out.println("Record in relation: " + rec.getValeursRec());
				}
			}

			// Step 11: Test edge case: Large record insertion
			System.out.println("\nTesting edge case: Large record insertion:");
			ArrayList<String> largeRecordValues = new ArrayList<>(Arrays.asList(
					"999", "VeryLongNameForTestingEdgeCases", "99", "999999.99"
			));
			Record largeRecord = new Record(relation, null);
			largeRecord.setValeursRec(largeRecordValues);

			RecordId largeRecordId = relation.writeRecordToDataPage(largeRecord, dataPages.get(0));
			if (largeRecordId != null) {
				System.out.println("Large record written successfully with RecordId: " + largeRecordId);
			} else {
				System.err.println("Failed to write large record.");
			}

			System.out.println("\nTestRelation completed successfully!");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
