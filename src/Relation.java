import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class Relation {
	private String nomRelation;
	private int nbCol;
	private ArrayList<ColInfo> tableCols;
	private PageId headerPageId;
	private BufferManager buffer;  // Référence à BufferManager
	private DiskManager disk;
	private DBConfig config;

	// Modifiez le constructeur pour accepter config et buffer en paramètre
	public Relation(String nomRelation, int nbCol, ArrayList<ColInfo> tableCols, DBConfig config, PageId headerPageId, DiskManager disk, BufferManager buffer) {
		this.nomRelation = nomRelation;
		this.nbCol = nbCol;
		this.tableCols = tableCols;
		this.headerPageId = headerPageId;
		this.buffer = buffer;  // Initialiser BufferManager
		this.disk = disk;
		this.config = config;
	}

	public String getNomRelation() {
		return nomRelation;
	}

	public int getNbCol() {
		return nbCol;
	}

	public ArrayList<ColInfo> getTableCols() {
		return tableCols;
	}

	public PageId getHeaderPageId() {
		return headerPageId;
	}

	@Override
	public String toString() {
		String TableInfo = "--" + nomRelation + "--";
		for (ColInfo col : tableCols) {
			TableInfo += col.toString();
		}
		TableInfo += "-- nombre de colonnes : " + nbCol + "|";
		return TableInfo;
	}

	public int getColIndex(String colName) {
		for (int i = 0; i < tableCols.size(); i++) {
			if (tableCols.get(i).getNameCol().equals(colName)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Méthode writeToBuffer qui écrit l'enregistrement dans un tampon. Elle gère
	 * les types de colonnes tels que INT, FLOAT, CHAR (longueur fixe) et VARCHAR
	 * (longueur variable).
	 * @param record : un Record (dont les valeurs sont remplies correctement par l’appelant)
	 * @param buff : ByteBuffer - le tampon dans lequel écrire.
	 * @param pos  : int - la position de départ dans le tampon.
	 * @return int : la taille totale de l'enregistrement en octets, ou -1 en cas
	 *         d'erreur.
	 */
	public int writeToBuffer(Record record, ByteBuffer buff, int pos) {
		try {
			buff.position(pos);
			int totalSize = 0;
			ArrayList<String> recValues = record.getValeursRec();

			for (int i = 0; i < getNbCol(); i++) {
				String value = recValues.get(i);
				ColInfo colInfo = tableCols.get(i);

				switch (colInfo.getTypeCol()) {
					case INT:
						int intValue = Integer.parseInt(value);
						buff.putInt(intValue);
						totalSize += 4;
						break;

					case FLOAT:
						float floatValue = Float.parseFloat(value);
						buff.putFloat(floatValue); // Write float to the buffer
						totalSize += 4;
						break;

					case CHAR:
						String charValue = value;
						int charLength = colInfo.getLengthChar();
						byte[] charBytes = new byte[charLength];
						byte[] charValueBytes = charValue.getBytes();
						System.arraycopy(charValueBytes, 0, charBytes, 0, Math.min(charValueBytes.length, charLength));
						buff.put(charBytes);
						totalSize += charLength;
						break;

					case VARCHAR:
						int varcharLength = value.length();
						buff.putInt(varcharLength);
						byte[] varcharBytes = value.getBytes();
						buff.put(varcharBytes);
						totalSize += 4 + varcharLength;
						break;

					default:
						throw new IllegalArgumentException("Invalid column type!");
				}
			}
			return totalSize;
		} catch (Exception e) {
			System.err.println("Error in writeToBuffer: " + e.getMessage());
			return -1;
		}
	}


	/**
	 *  Cette méthode rend comme résultat la taille totale (=le nombre d’octets) lus depuis le buffer.
	 * Elle lit les valeurs du Record depuis le buffer à partir de pos, en supposant que le
	 * Record a été écrit avec writeToBuffer.
	 * @param record : dont la liste de valeurs est vide et sera remplie par cette méthode
	 * @param buff
	 * @param pos : un entier correspondant à une position dans le buffer
	 * @return int  le nombre d’octets lus depuis le buffer
	 */
	public int readFromBuffer(Record record, ByteBuffer buff, int pos) {
		try {
			record.getValeursRec().clear();
			buff.position(pos);
			int totalSize = 0;

			for (int i = 0; i < getNbCol(); i++) {
				ColInfo colInfo = tableCols.get(i);

				switch (colInfo.getTypeCol()) {
					case INT:
						int valeur_int = buff.getInt();
						record.getValeursRec().add(Integer.toString(valeur_int));
						totalSize += 4;
						break;

					case FLOAT:
						float valeur_float = buff.getFloat();
						record.getValeursRec().add(String.format("%.2f", valeur_float)); // Format float consistently
						totalSize += 4;
						break;

					case CHAR:
						int charLength = colInfo.getLengthChar();
						byte[] charBytes = new byte[charLength];
						buff.get(charBytes);
						String valeur_char = new String(charBytes).trim(); // Remove padding spaces
						record.getValeursRec().add(valeur_char);
						totalSize += charLength;
						break;

					case VARCHAR:
						int varcharLength = buff.getInt();
						byte[] varCharBytes = new byte[varcharLength];
						buff.get(varCharBytes);
						String varCharValue = new String(varCharBytes);
						record.getValeursRec().add(varCharValue);
						totalSize += 4 + varcharLength;
						break;

					default:
						throw new IllegalArgumentException("Invalid column type!");
				}
			}
			return totalSize;
		} catch (Exception e) {
			System.err.println("Error in readFromBuffer: " + e.getMessage());
			return -1;
		}
	}



	/**
	 * Cette méthode retourne le PageId d’une page de données sur laquelle il reste assez de place
	 * pour insérer le record ; si une telle page n’existe pas, la méthode retournera null.
	 * @param recordSize : un entier
	 * correspondant à la taille du record à insérer.
	 * @return PageId d'une page disponible, null sinon
	 */
	// Utiliser buffer pour obtenir la Header Page
	public PageId getFreeDataPageId(int recordSize) {
		try {
			ByteBuffer headerBuffer = buffer.GetPage(headerPageId); // Load the header page
			headerBuffer.position(0); // Reset buffer position

			// Read the total number of data pages
			int numPages = headerBuffer.getInt(0);
			System.out.println("Number of data pages: " + numPages);

			for (int i = 0; i < numPages; i++) {
				int offset = 4 + i * 12; // Offset for each data page entry
				int fileIdx = headerBuffer.getInt(offset);
				int pageIdx = headerBuffer.getInt(offset + 4);
				int freeSpace = headerBuffer.getInt(offset + 8);

				if (freeSpace >= recordSize) {
					return new PageId(fileIdx, pageIdx); // Return the page if it has enough space
				}
			}
		} catch (Exception e) {
			System.err.println("Error in getFreeDataPageId: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				buffer.FreePage(headerPageId, false); // Release the header page
			} catch (Exception e) {
				System.err.println("Error freeing header page: " + e.getMessage());
			}
		}
		return null; // No page found
	}



	/**
	 * Cette méthode écrit l’enregistrement record dans la page de données identifiée par pageId, et
	 *  renvoie son RecordId.
	 *  On suppose que la page dispose d’assez d’espace disponible pour l’insertion.
	 * @param record
	 * @param pageId
	 * @return RecordId du record écrit dans la dataPage
	 */
	public RecordId writeRecordToDataPage(Record record, PageId pageId) {
		try {
			// Load the data page buffer
			ByteBuffer dataPageBuffer = buffer.GetPage(pageId);

			// Read metadata from the data page
			int freeSpacePointer = dataPageBuffer.getInt(0); // Free space pointer
			int slotCount = dataPageBuffer.getInt(4);        // Slot count

			// Calculate the size of the record to be written
			int recordSize = writeToBuffer(record, ByteBuffer.allocate(0), 0); // Use a temporary buffer to calculate size
			if (recordSize <= 0) {
				throw new IllegalStateException("Failed to calculate record size.");
			}

			// Validate free space
			if (freeSpacePointer + recordSize > dataPageBuffer.capacity()) {
				throw new IllegalStateException("Not enough free space in data page.");
			}

			// Write record and update metadata
			int recordStartOffset = freeSpacePointer;
			writeToBuffer(record, dataPageBuffer, recordStartOffset); // Write the record to the data page
			dataPageBuffer.putInt(0, freeSpacePointer + recordSize);  // Update free space pointer
			dataPageBuffer.putInt(4, slotCount + 1);                 // Increment slot count

			// Update slot directory
			int slotOffset = dataPageBuffer.capacity() - (slotCount + 1) * 8;
			dataPageBuffer.putInt(slotOffset, recordStartOffset); // Write the start offset of the record
			dataPageBuffer.putInt(slotOffset + 4, recordSize);    // Write the size of the record

			// Mark the page as dirty and release it
			buffer.MarkDirty(pageId);
			buffer.FreePage(pageId, true);

			// Return the RecordId for the inserted record
			return new RecordId(pageId, slotCount);
		} catch (Exception e) {
			System.err.println("Error in writeRecordToDataPage: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}





	/**
	 * Cette méthode liste tous les records
	 * @return ArrayList de Record
	 */
	public ArrayList<Record> getAllRecords (){

		ArrayList <Record> Records=new ArrayList<>();  //list of record
		ArrayList<PageId> Pages=getDataPages();  //list of pages inside a relation

		for(int i=0;i<Pages.size();i++){
			ArrayList<Record> listRecords = getRecordsInDataPage(Pages.get(i)); //get the record of a page


			for(Record record:listRecords){
				Records.add(record);
			}
		}
		return Records;
	}

	/**
	 * Cette méthode renvoie la liste des records stockés dans une page de type HeapFile
	 * qui veut dire un ensemble de tuples sauvegardées dans un ordre aléatoire.
	 * @param pageId : identifiant d'une page
	 * @return la liste des records stockés dans la page identifiée par pageId
	 */
	ArrayList<Record> getRecordsInDataPage(PageId pageId) {
		ArrayList<Record> listeRecords = new ArrayList<>();
		ByteBuffer bufferPage = null;

		try {
			// Charger la page via BufferManager
			bufferPage = buffer.GetPage(pageId);

			if (bufferPage == null) {
				throw new IllegalArgumentException("La page demandée n'a pas pu être chargée.");
			}

			int pageSize = bufferPage.capacity(); // Taille totale de la page
			int offsetM = pageSize - 4; // Offset de M
			int M = bufferPage.getInt(offsetM);
			int DebutSlotDirectory = offsetM - (M * 8); // Chaque slot a 8 octets (4 pour position, 4 pour taille)

			for (int slotIdx = 0; slotIdx < M; slotIdx++) {
				int slotOffset = DebutSlotDirectory + (slotIdx * 8); // Position du slot

				// Lire la position de début et la taille du record
				int recordStart = bufferPage.getInt(slotOffset);       // la position du record (1er 4o)
				int recordSize = bufferPage.getInt(slotOffset + 4);   // la taille du record (2eme 4o)

				// Vérifier si le record est valide (taille > 0)
				if (recordSize > 0 && recordStart != -1) {
					RecordId rid = new RecordId(pageId, slotIdx);
					Record record = new Record(this, rid);
					int bytesRead = readFromBuffer(record, bufferPage, recordStart);
					if (bytesRead != recordSize) {
						throw new IllegalStateException("Erreur : taille de record incohérente.");
					}
					listeRecords.add(record);
				}
			}
		} catch (Exception e) {
			System.err.println("Erreur lors de la récupération des records : " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Libérer la page après utilisation
			try {
				if (bufferPage != null) {
					buffer.FreePage(pageId, false);
				}
			} catch (Exception e) {
				System.err.println("Erreur lors de la libération de la page : " + e.getMessage());
			}
		}
		return listeRecords;
	}



	/**
	 * Cette méthode retourne un ArrayList qui contient les identifiants des pages contenues dans
	 * la Header Page de la relation.
	 * @return la liste des PageIds des pages de données.
	 */
	public ArrayList<PageId> getDataPages() {
		ArrayList<PageId> dataPages = new ArrayList<>();
		ByteBuffer headerBuffer = null;

		try {
			// Fetch the header page
			headerBuffer = buffer.GetPage(headerPageId);
			headerBuffer.position(0);

			// Read the number of data pages
			int pageCount = headerBuffer.getInt(0); // First 4 bytes store the count
			System.out.println("Nombre de pages dans la Header Page : " + pageCount);

			// Retrieve each data page's metadata
			for (int i = 0; i < pageCount; i++) {
				int offset = 4 + i * 12; // 4 bytes for count + 12 bytes per entry
				int fileIdx = headerBuffer.getInt(offset);
				int pageIdx = headerBuffer.getInt(offset + 4);
				dataPages.add(new PageId(fileIdx, pageIdx));
			}
		} catch (Exception e) {
			System.err.println("Erreur lors de la récupération des pages de données : " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (headerBuffer != null) {
				try {
					buffer.FreePage(headerPageId, false);
				} catch (Exception e) {
					System.err.println("Erreur lors de la libération de la page d'en-tête : " + e.getMessage());
				}
			}
		}
		return dataPages;
	}









	/**
	 * Cette méthode devra rajouter une page de données « vide » au Heap File correspondant à la relation.
	 * Pour cela, elle devra :
	 * allouer une nouvelle page via AllocPage du DiskManager
	 * actualiser le Page Directory en prenant en compte cette page
	 */
	public void addDataPage() {
		try {
			// Step 1: Allocate a new data page
			PageId newPageId = disk.AllocPage();
			System.out.println("New data page allocated: FileIdx = " + newPageId.getFileIdx() + ", PageIdx = " + newPageId.getPageIdx());

			// Step 2: Initialize the new data page
			ByteBuffer dataPageBuffer = ByteBuffer.allocate(config.getPageSize());
			dataPageBuffer.putInt(0, 8); // Free space pointer (initially 8 bytes)
			dataPageBuffer.putInt(4, 0); // Slot count (initially 0 slots)
			disk.WritePage(newPageId, dataPageBuffer);

			// Step 3: Load and update the header page
			ByteBuffer headerBuffer = buffer.GetPage(headerPageId);
			headerBuffer.position(0); // Ensure position is reset

			int pageCount = headerBuffer.getInt(); // Read current page count
			System.out.println("Current page count: " + pageCount);

			// Validate there is space for the new entry
			int offset = 4 + pageCount * 12;
			if (offset + 12 > config.getPageSize()) {
				throw new IllegalStateException("No space available in the header page to add a new data page.");
			}

			// Step 4: Write new page metadata
			headerBuffer.putInt(offset, newPageId.getFileIdx());
			headerBuffer.putInt(offset + 4, newPageId.getPageIdx());
			headerBuffer.putInt(offset + 8, config.getPageSize() - 8); // Free space available initially

			// Increment and update page count
			headerBuffer.putInt(0, pageCount + 1);

			// Step 5: Mark header page as dirty and release
			buffer.MarkDirty(headerPageId);
			buffer.FreePage(headerPageId, true);

			System.out.println("Header page updated successfully. New page count: " + (pageCount + 1));

		} catch (Exception e) {
			System.err.println("Error in addDataPage: " + e.getMessage());
			e.printStackTrace();
		}
	}

















}