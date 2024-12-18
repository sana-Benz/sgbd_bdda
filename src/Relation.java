import java.io.IOException;
import java.math.BigDecimal;
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
	public Relation(String nomRelation, int nbCol, ArrayList<ColInfo> tableCols, DBConfig config, DiskManager disk, BufferManager buffer) {
		this.nomRelation = nomRelation;
		this.nbCol = nbCol;
		this.tableCols = tableCols;
		this.buffer = buffer;  // Initialiser BufferManager
		this.disk = disk;
		this.config = config;
		//creer une headerPage pour la nouvelle relation
		try{
			this.headerPageId = disk.AllocPage();
			ByteBuffer headerData = buffer.GetPage(headerPageId);
			headerData.clear(); // Clear the buffer before writing
			headerData.putInt(0); // Write the initial value (0 pages)
			//Buffer headerBuffer = new Buffer(headerPageId, headerData);
			buffer.FreePage(headerPageId, true);
			buffer.flushBuffers();

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

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
	
	public ArrayList<String> getAllColumnNames() {
	    ArrayList<String> nomsColonnes = new ArrayList<>();
	    for (ColInfo colInfo : tableCols) {
	        nomsColonnes.add(colInfo.getNameCol()); // Supposant que ColInfo possède une méthode getNomCol()
	    }
	    return nomsColonnes;
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

	// * Méthode writeToBuffer qui écrit l'enregistrement dans un tampon. Elle gère
	 //* les types de colonnes tels que INT, FLOAT, CHAR (longueur fixe) et VARCHAR
	// * (longueur variable).
    //* @param record : un Record (dont les valeurs sont remplies correctement par l’appelant)
	// * @param buff : ByteBuffer - le tampon dans lequel écrire.
	// * @param pos  : int - la position de départ dans le tampon.
	 //* @return int : la taille totale de l'enregistrement en octets, ou -1 en cas
	 //*         d'erreur.

	 public int writeToBuffer(Record record, ByteBuffer buff, int pos) {
		try {
			// Définir la position du buffer à la valeur spécifiée
			buff.position(pos);
			int totalSize = 0; // Variable pour suivre la taille totale du record
			ArrayList<String> recValues = record.getValeursRec(); // Obtenir les valeurs du Record

			System.out.println("Écriture dans le buffer à la position : " + pos);
			System.out.println("Valeurs du record : " + recValues);

			// Parcourir toutes les colonnes du schéma de la table
			for (int i = 0; i < getNbCol(); i++) {
				String value = recValues.get(i); // Récupérer la valeur actuelle pour la colonne
				ColInfo colInfo = tableCols.get(i);
				System.out.println(
						"Traitement de la colonne " + i + ": " + colInfo.toString() + " avec valeur : " + value);
			System.out.println("Position avant écriture (writeToBuffer): " + buff.position());

				// Switch basé sur le type de la colonne
				switch (colInfo.getTypeCol()) {
					case INT:
						int intValue = Integer.parseInt(value);
						System.out.println("Position avant écriture (writeToBuffer) : " + buff.position());

						buff.putInt(intValue); // Écrire la valeur entière dans le buffer
						System.out.println("Position après écriture (writeToBuffer): " + buff.position());

						totalSize += 4; // Un INT occupe 4 octets
						System.out.println("Écrit INT : " + intValue);
						break;

					case FLOAT:
						BigDecimal floatValue = new BigDecimal(value); // Convertir la valeur à BigDecimal
						byte[] floatBytes = floatValue.toString().getBytes(); // Convertir BigDecimal en bytes (chaîne)
						buff.putInt(floatBytes.length); // Écrire la longueur de la chaîne
						System.out.println("Position avant écriture (writeToBuffer) : " + buff.position());

						buff.put(floatBytes); // Écrire les bytes de la chaîne
						System.out.println("Position après écriture (writeToBuffer): " + buff.position());

						totalSize += 4 + floatBytes.length; // Ajouter la longueur totale (4 octets pour la taille +
															// contenu)
						System.out.println("Écrit FLOAT (en BigDecimal) : " + floatValue);
						break;

					case CHAR:
						String charValue = value;
						int charLength = colInfo.getLengthChar(); // Obtenir la longueur fixe
						byte[] charBytes = new byte[charLength]; // Créer un tableau de bytes de longueur fixe
						byte[] charValueBytes = charValue.getBytes(); // Convertir la chaîne en tableau de bytes

						// Copier les bytes de la chaîne dans le tableau de longueur fixe (troncature si
						// nécessaire)
						System.arraycopy(charValueBytes, 0, charBytes, 0, Math.min(charValueBytes.length, charLength));
						// Remplir le reste avec des espaces
						for (int j = charValueBytes.length; j < charLength; j++) {
							charBytes[j] = ' '; // Remplir avec des espaces
						}
						System.out.println("Position avant écriture (writeToBuffer) : " + buff.position());

						buff.put(charBytes); // Écrire les bytes de longueur fixe dans le buffer
						System.out.println("Position après écriture (writeToBuffer): " + buff.position());

						totalSize += charLength; // Ajouter la longueur du champ CHAR à la taille totale
						System.out.println("Écrit CHAR : " + charValue);
						break;

					case VARCHAR:
						int varcharLength = value.length(); // Obtenir la longueur de la chaîne
						buff.putInt(varcharLength); // Écrire d'abord la longueur de la chaîne (4 octets)
						byte[] varcharBytes = value.getBytes(); // Convertir la chaîne en tableau de bytes
						System.out.println("Position avant écriture (writeToBuffer) : " + buff.position());

						buff.put(varcharBytes); // Écrire les bytes de la chaîne dans le buffer
						System.out.println("Position après écriture (writeToBuffer): " + buff.position());

						totalSize += 4 + varcharLength; // Ajouter 4 octets pour la longueur et la longueur de la chaîne
														// à la taille totale
						System.out.println("Écrit VARCHAR : " + value + " (longueur : " + varcharLength + ")");
						break;

					default:
						throw new IllegalArgumentException("le type de la colonne invalide !!");
				}
			}

			System.out.println("Taille totale écrite : " + totalSize + " octets");
			return totalSize;
		} catch (Exception e) {
			System.err.println("Erreur dans writeToBuffer : " + e.getMessage());
			e.printStackTrace(); // Afficher la trace de la pile pour plus de détails
			return -1; // Retourner -1 en cas d'erreur
		}
	}

	/**
	 * Cette méthode rend comme résultat la taille totale (=le nombre d’octets) lus
	 * depuis le buffer.
	 * Elle lit les valeurs du Record depuis le buffer à partir de pos, en supposant
	 * que le
	 * Record a été écrit avec writeToBuffer.
	 * 
	 * @param record : dont la liste de valeurs est vide et sera remplie par cette
	 *               méthode
	 * @param buff
	 * @param pos    : un entier correspondant à une position dans le buffer
	 * @return int le nombre d’octets lus depuis le buffer
	 */
	public int readFromBuffer(Record record, ByteBuffer buff, int pos) {
		try {
			record.getValeursRec().clear();
			buff.position(pos);
			int totalSize = 0;

			for (int i = 0; i < getNbCol(); i++) {
				ColInfo colInfo = tableCols.get(i);
				System.out.println("Position avant lecture (readFromBuffer): " + buff.position());

				switch (colInfo.getTypeCol()) {
					case INT:
					System.out.println("Position avant lecture (readFromBuffer) : " + buff.position());

						int valeur_int = buff.getInt(); // lit 4 octs et les interpter comme un entier et avance la pos
														// du
						// tampon de 4 octs
						System.out.println("Position après lecture (readFromBuffer): " + buff.position());

						record.getValeursRec().add(Integer.toString(valeur_int));
						totalSize += 4;
						System.out.println("Lu INT: " + valeur_int + " (taille totale jusqu'à présent: " + totalSize + ")");

						break;
					case FLOAT:
						int floatBytesLength = buff.getInt(); // Lire la longueur des bytes
						byte[] floatBytes = new byte[floatBytesLength];
						System.out.println("Position avant lecture (readFromBuffer) : " + buff.position());

						buff.get(floatBytes); // Lire les bytes correspondant à la chaîne
						System.out.println("Position après lecture (readFromBuffer): " + buff.position());

						String floatString = new String(floatBytes); // Convertir les bytes en chaîne
						BigDecimal valeur_float = new BigDecimal(floatString); // Convertir la chaîne en BigDecimal
						record.getValeursRec().add(valeur_float.toString()); // Ajouter au record sous forme de chaîne
						totalSize += 4 + floatBytesLength; // Longueur totale (taille + contenu)
						System.out.println("Lu FLOAT (en BigDecimal) : " + valeur_float);
						System.out.println("Lu FLOAT: " + valeur_float + " (taille totale jusqu'à présent: " + totalSize + ")");

						break;

					case CHAR:
						int charLength = colInfo.getLengthChar();
						byte[] charBytes = new byte[charLength]; // Créer un tableau de bytes pour stocker les données
																	// lues
						// depuis le tampon
						System.out.println("Position avant lecture (readFromBuffer) : " + buff.position());

						buff.get(charBytes); // Lire les bytes correspondant à la longueur de la chaîne CHAR
						System.out.println("Position après lecture (readFromBuffer): " + buff.position());

						String valeur_char = new String(charBytes).trim(); // remove spaces or extra-padding
						// Vérification de la taille lue par rapport à la taille attendue
   						 if (valeur_char.length() != charLength) { 
     					   System.err.println("Problème de taille CHAR : attendue " + charLength + " mais lue " + valeur_char.length());
   						 }
						record.getValeursRec().add(valeur_char); // Ajouter la valeur lue (chaîne) dans la liste des
																	// valeurs
						totalSize += charLength;	
						System.out.println("Lu CHAR: " + valeur_char + " (taille totale jusqu'à présent: " + totalSize + ")");

						break;
					case VARCHAR:
						int varCharLength = buff.getInt();
						byte[] varCharBytes = new byte[varCharLength];
						System.out.println("Position avant lecture (readFromBuffer) : " + buff.position());

						buff.get(varCharBytes);
						System.out.println("Position après lecture (readFromBuffer): " + buff.position());

						String varCharValue = new String(varCharBytes);
						record.getValeursRec().add(varCharValue);
						totalSize += 4 + varCharLength;		
						System.out.println("Lu VARCHAR: " + varCharValue + " (taille totale jusqu'à présent: " + totalSize + ")");

						break;
					default:
						throw new IllegalArgumentException("le type de votre colonne invalide !!");
				}
			}
			return totalSize;
		} catch (Exception e) {
			System.err.println("Error in readFromBuffer: " + e.getMessage());
			return -1; // Or another error value as needed
		}
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
			ByteBuffer buff = buffer.GetPage(pageId);
			Buffer buffDataPage = new Buffer(pageId, buff);

			// Ajoutez ces logs pour déboguer
			System.out.println("offset pour slot directory: " + (config.getPageSize() - 8));
			System.out.println(buff.position() == 0);
			// Lire la position de début de l'espace libre
			int posDebutLibre = buff.getInt(config.getPageSize() - 4);
			System.out.println("Position début espace libre : " + posDebutLibre);

			// Vérifier si l'espace libre est suffisant pour le record
			buff.position(posDebutLibre);
			int sizeRecord = writeToBuffer(record, buff, posDebutLibre);
			System.out.println("Taille du record à écrire : " + sizeRecord);
			if (sizeRecord < 0 || posDebutLibre + sizeRecord > config.getPageSize()) {
				throw new RuntimeException("Pas assez d'espace pour écrire le record dans la page de données.");
			}

			// Écrire le record dans le buffer à partir de la position libre
			//buff.position(posDebutLibre);
			//writeToBuffer(record, buff, posDebutLibre); // Écrire le record

			// Mettre à jour la position de début d'espace libre
			posDebutLibre += sizeRecord;
			buff.putInt(buff.capacity() - 4, posDebutLibre); // Met à jour pos début libre

			// Mettre à jour le nombre de slots et écrire dans le slot directory
			int nbSlots = buff.getInt(buff.capacity() - 8);
			int slotIdx = nbSlots;
			nbSlots++;
			buff.putInt(buff.capacity() - 8, nbSlots); // Met à jour nb slots

			// Calcul de la position pour le slot directory et mise à jour
			int slotOffset = buff.capacity() - 8 - 8 * nbSlots; // 8 octets par slot (position + taille)
			if (slotOffset < 0) {
				throw new RuntimeException("Erreur de calcul de l'offset pour le slot directory.");
			}
			buff.position(slotOffset);
			buff.putInt(posDebutLibre - sizeRecord); // Position du début du record
			buff.putInt(sizeRecord); // Taille du record

			// Marquer la page comme modifiée
			buffDataPage.setDirty(true); // Ensure this method exists in your Buffer class

			// Mettre à jour l'espace libre dans l'en-tête
			updateFreeSpaceInHeader(pageId, -sizeRecord); // Ensure this method is defined

			// Écriture sur disque
			buffer.FreePage(pageId,true);
			buffer.flushBuffers();

			// Retourner un RecordId
			return new RecordId(pageId, slotIdx);

		} catch (Exception e) {
			System.err.println("Erreur lors de l'écriture du record sur la page de données : " + e.getMessage());
			throw new RuntimeException("Erreur lors de l'écriture du record sur la page de données", e);
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
			System.out.println("Number of data pages in header: " + numPages);

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
			int offsetM = pageSize - 8; // Offset pour M (nombre de slots)
			int M = bufferPage.getInt(offsetM);

			System.out.println("Nombre de slots (M) : " + M);

			int DebutSlotDirectory = offsetM - (M * 8); // Chaque slot a 8 octets (position + taille)

			// Valider la position du slot directory
			if (DebutSlotDirectory < 0 || DebutSlotDirectory >= pageSize) {
				throw new RuntimeException("Erreur de calcul de l'offset pour le slot directory. Offset invalide.");
			}

			for (int slotIdx = 0; slotIdx < M; slotIdx++) {
				int slotOffset = DebutSlotDirectory + (slotIdx * 8); // Position du slot
				if (slotOffset < 0 || slotOffset + 8 > pageSize) {
					throw new RuntimeException("Offset de slot invalide : " + slotOffset);
				}

				// Lire la position de début et la taille du record
				int recordStart = bufferPage.getInt(slotOffset);
				int recordSize = bufferPage.getInt(slotOffset + 4);

				System.out.println("Slot " + slotIdx + ": Start = " + recordStart + ", Size = " + recordSize);

				// Vérifier si le record est valide
				if (recordSize > 0 && recordStart >= 0 && recordStart + recordSize <= DebutSlotDirectory) {
					RecordId rid = new RecordId(pageId, slotIdx);
					Record record = new Record(this, rid);
					int bytesRead = readFromBuffer(record, bufferPage, recordStart);
					if (bytesRead != recordSize) {
						throw new IllegalStateException("Taille de record incohérente : attendue " + recordSize + ", lue " + bytesRead);
					}
					listeRecords.add(record);
				} else {
					System.out.println("Record invalide : Start = " + recordStart + ", Size = " + recordSize);
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
			System.out.println("load headerPage pour ajouter une page");
			ByteBuffer headerBuffer = buffer.GetPage(headerPageId);
			Buffer headerPageBUFFER = new Buffer(headerPageId,headerBuffer);
			int numPages = headerBuffer.getInt(0); // Read the current number of data pages
			System.out.println("Nombre actuel de pages de données dans headerpage avant ajout : " + numPages);
			System.out.println("allouer une nouvelle datapage");
			PageId newPageId = disk.AllocPage();
			ByteBuffer dataPageBuffer = buffer.GetPage(newPageId);
			Buffer dataPageBUFFER = new Buffer(newPageId,dataPageBuffer);
			dataPageBuffer.clear();
			dataPageBuffer.putInt(dataPageBuffer.capacity()-4,0); // Free space starts at 0
			dataPageBuffer.putInt(dataPageBuffer.capacity()-8,0); // Number of records (M) initialized to 0
			System.out.println("Nouvelle page de données initialisée : " + newPageId + "espace vide commence à "+ dataPageBuffer.getInt(dataPageBuffer.capacity()-4)
					+ " et nb slots est " + dataPageBuffer.getInt(dataPageBuffer.capacity()-8));

			System.out.println("Mise à jour de la headerPage après ajout de datapage ");
			headerBuffer.position(4 + numPages * 12); // Move to the correct position for the new page
			headerBuffer.putInt(newPageId.getFileIdx());
			headerBuffer.putInt(newPageId.getPageIdx());
			headerBuffer.putInt(dataPageBuffer.capacity() - 8); // Initial free space
			int numPagesIncremente = numPages + 1;
			// Update the number of pages in the header
			headerBuffer.putInt(0, numPagesIncremente); // Increment the number of pages
			System.out.println("Nombre de pages de données incrémenté à : " + numPagesIncremente);
			System.out.println("nouvelle position pour la prochaine page dans headerPage "+ (4 + numPagesIncremente * 12) );


			// Free the pages after use
			buffer.FreePage(headerPageId, true);
			buffer.FreePage(newPageId, true);
			System.out.println("Pages libérées : HeaderPage (" + headerPageId + ") et DataPage (" + newPageId + ")");

			buffer.flushBuffers();
			disk.WritePage(headerPageId,headerBuffer);
			disk.WritePage(newPageId,dataPageBuffer);

			System.out.println("je récupére les pages que je viens d'ajouter");
			ByteBuffer headerpage = buffer.GetPage(headerPageId);
			ByteBuffer datapage = buffer.GetPage(newPageId);
			System.out.println("voici ce que g ecrit dans header page "+ " nb pages dans headerpage :" + headerpage.get(0));
			System.out.println("voici ce que g ecrit dans data page qui est vide apres "+ " position début espace dispo" +datapage.get(config.getPageSize()-4));


		} catch (Exception e) {
			System.err.println("Error in addDataPage: " + e.getMessage());
		}
	}

	/**
	 * Cette méthode écrit l’enregistrement record dans la page de données identifiée par pageId, et
	 *  renvoie son RecordId.
	 *  On suppose que la page dispose d’assez d’espace disponible pour l’insertion.
	 //* @param record
	 * @param pageId
	 * @return RecordId du record écrit dans la dataPage
	 */
	//il faut peut etre vérifier que le record n'existe pas dans la datapage
	//pour éviter les doublons
	/*public RecordId writeRecordToDataPage(Record record, PageId pageId) {
		try {
			ByteBuffer buff = buffer.GetPage(pageId);
			Buffer buffDataPage = new Buffer(pageId, buff);

			// Ajoutez ces logs pour déboguer
			System.out.println("offset pour slot directory: " + (config.getPageSize() - 8));
			System.out.println(buff.position() == 0);
			// Lire la position de début de l'espace libre
			int posDebutLibre = buff.getInt(config.getPageSize() - 4);
			System.out.println("Position début espace libre : " + posDebutLibre);

			// Vérifier si l'espace libre est suffisant pour le record
			buff.position(posDebutLibre);
			int sizeRecord = writeToBuffer(record, buff, posDebutLibre);
			System.out.println("Taille du record à écrire : " + sizeRecord);
			if (sizeRecord < 0 || posDebutLibre + sizeRecord > config.getPageSize()) {
				throw new RuntimeException("Pas assez d'espace pour écrire le record dans la page de données.");
			}

			// Écrire le record dans le buffer à partir de la position libre
			//buff.position(posDebutLibre);
			//writeToBuffer(record, buff, posDebutLibre); // Écrire le record

			// Mettre à jour la position de début d'espace libre
			posDebutLibre += sizeRecord;
			buff.putInt(buff.capacity() - 4, posDebutLibre); // Met à jour pos début libre

			// Mettre à jour le nombre de slots et écrire dans le slot directory
			int nbSlots = buff.getInt(buff.capacity() - 8);
			int slotIdx = nbSlots;
			nbSlots++;
			buff.putInt(buff.capacity() - 8, nbSlots); // Met à jour nb slots

			// Calcul de la position pour le slot directory et mise à jour
			int slotOffset = buff.capacity() - 8 - 8 * nbSlots; // 8 octets par slot (position + taille)
			if (slotOffset < 0) {
				throw new RuntimeException("Erreur de calcul de l'offset pour le slot directory.");
			}
			buff.position(slotOffset);
			buff.putInt(posDebutLibre - sizeRecord); // Position du début du record
			buff.putInt(sizeRecord); // Taille du record

			// Marquer la page comme modifiée
			buffDataPage.setDirty(true); // Ensure this method exists in your Buffer class

			// Mettre à jour l'espace libre dans l'en-tête
			updateFreeSpaceInHeader(pageId, -sizeRecord); // Ensure this method is defined

			// Écriture sur disque
			buffer.FreePage(pageId,true);
			buffer.flushBuffers();

			// Retourner un RecordId
			return new RecordId(pageId, slotIdx);

		} catch (Exception e) {
			System.err.println("Erreur lors de l'écriture du record sur la page de données : " + e.getMessage());
			throw new RuntimeException("Erreur lors de l'écriture du record sur la page de données", e);
		}
	}*/



	//cette fonction parcourt les entrées des dataPages dans headerpage
	// Lorsqu'elle trouve une correspondance avec la page spécifiée , elle met à jour l'espace libre
	// (tailleLibre) en ajoutant la valeur de delta, marque la page d'en-tête comme dirty
	private void updateFreeSpaceInHeader(PageId pageId, int delta) {
		try {
			ByteBuffer buff = buffer.GetPage(headerPageId);
			Buffer buffHeaderPage = new Buffer(headerPageId, buff);
			int nbDataPages = buff.getInt(0);

			for (int i = 0; i < nbDataPages; i++) {
				buff.position(4 + i * 12);
				int fileIdx = buff.getInt();
				int pageIdx = buff.getInt();

				if (fileIdx == pageId.getFileIdx() && pageIdx == pageId.getPageIdx()) {
					int tailleLibre = buff.getInt();
					buff.putInt(4 + i * 12 + 8, tailleLibre + delta);
					buffHeaderPage.setDirty(true);
					break;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

      
	
	//addRecord : Cette méthode vérifie d'abord si une page de données libre est disponible pour insérer 
	//le nouvel enregistrement.
	//Si aucune page n'est disponible, elle appelle addDataPage pour en allouer une nouvelle.
	public void addRecord(Record record) {
	    // Obtenir la taille du record à insérer
	    int recordSize = calculateRecordSize(record); // Implémentez cette méthode pour calculer la taille

	    // Obtenir une page de données libre
	    PageId freePageId = getFreeDataPageId(recordSize);
	    if (freePageId == null) {
	        // Si aucune page libre n'est disponible, ajoutez une nouvelle page
	        addDataPage();
	        freePageId = getFreeDataPageId(recordSize); // Réessayez d'obtenir une page libre
	    }

	    if (freePageId != null) {
	        // Écrire le record dans la page de données
	        RecordId recordId = writeRecordToDataPage(record, freePageId);
	        System.out.println("Record ajouté avec succès avec RecordId : " + recordId);
	    } else {
	        System.out.println("Erreur : Pas assez d'espace pour ajouter le record.");
	    }
	}

	// Méthode pour calculer la taille d'un record
	private int calculateRecordSize(Record record) {
	    int size = 0;
	    for (int i = 0; i < record.getValeursRec().size(); i++) {
	        String value = record.getValeursRec().get(i);
	        ColInfo colInfo = tableCols.get(i);
	        switch (colInfo.getTypeCol()) {
	            case INT:
	                size += 4; // Un INT occupe 4 octets
	                break;
	            case FLOAT:
	                size += 4; // Un FLOAT occupe 4 octets
	                break;
	            case CHAR:
	                size += colInfo.getLengthChar(); // Longueur fixe
	                break;
	            case VARCHAR:
	                size += 4 + value.length(); // 4 octets pour la longueur + longueur de la chaîne
	                break;
	            default:
	                throw new IllegalArgumentException("Type de colonne invalide : " + colInfo.getTypeCol());
	        }
	    }
	    return size;
	}

	public RecordId allocateNextRecordId() {
		try {
			// Parcourir les pages existantes pour trouver un emplacement libre
			ArrayList<PageId> dataPages = getDataPages();
			for (PageId pageId : dataPages) {
				ByteBuffer pageBuffer = buffer.GetPage(pageId);
				int slotCount = pageBuffer.getInt(pageBuffer.capacity() - 8); // Nombre de slots
				int freeSpaceStart = pageBuffer.getInt(pageBuffer.capacity() - 4); // Position libre

				// Parcourir les slots pour vérifier s'il y a un espace libre
				for (int slotIdx = 0; slotIdx < slotCount; slotIdx++) {
					int slotOffset = pageBuffer.capacity() - 8 - 8 * (slotIdx + 1); // Offset du slot
					int recordStart = pageBuffer.getInt(slotOffset);
					if (recordStart == -1) { // Slot libre trouvé
						buffer.FreePage(pageId, false);
						return new RecordId(pageId, slotIdx);
					}
				}

				// Vérifier s'il y a encore de l'espace libre dans la page
				if (freeSpaceStart < (pageBuffer.capacity() - 8 - slotCount * 8)) {
					buffer.FreePage(pageId, false);
					return new RecordId(pageId, slotCount);
				}

				buffer.FreePage(pageId, false);
			}

			// Si aucune page existante n'a d'espace, ajouter une nouvelle page
			addDataPage();
			PageId newPageId = getDataPages().get(getDataPages().size() - 1);
			return new RecordId(newPageId, 0); // Retourner le premier slot de la nouvelle page

		} catch (Exception e) {
			throw new RuntimeException("Erreur dans allocateNextRecordId : " + e.getMessage());
		}
	}

}