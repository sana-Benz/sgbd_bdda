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
				System.out.println("Traitement de la colonne " + i + ": " + colInfo.toString() + " avec valeur : " + value);

				// Switch basé sur le type de la colonne
				switch (colInfo.getTypeCol()) {
					case INT:
						int intValue = Integer.parseInt(value);
						buff.putInt(intValue); // Écrire la valeur entière dans le buffer
						totalSize += 4; // Un INT occupe 4 octets
						System.out.println("Écrit INT : " + intValue);
						break;

					case FLOAT:
						BigDecimal floatValue = new BigDecimal(value); // Convertir la valeur à BigDecimal
						byte[] floatBytes = floatValue.toString().getBytes(); // Convertir BigDecimal en bytes (chaîne)
						buff.putInt(floatBytes.length); // Écrire la longueur de la chaîne
						buff.put(floatBytes); // Écrire les bytes de la chaîne
						totalSize += 4 + floatBytes.length; // Ajouter la longueur totale (4 octets pour la taille + contenu)
						System.out.println("Écrit FLOAT (en BigDecimal) : " + floatValue);
						break;

					case CHAR:
						String charValue = value;
						int charLength = colInfo.getLengthChar(); // Obtenir la longueur fixe
						byte[] charBytes = new byte[charLength]; // Créer un tableau de bytes de longueur fixe
						byte[] charValueBytes = charValue.getBytes(); // Convertir la chaîne en tableau de bytes

						// Copier les bytes de la chaîne dans le tableau de longueur fixe (troncature si nécessaire)
						System.arraycopy(charValueBytes, 0, charBytes, 0, Math.min(charValueBytes.length, charLength));
						buff.put(charBytes); // Écrire les bytes de longueur fixe dans le buffer
						totalSize += charLength; // Ajouter la longueur du champ CHAR à la taille totale
						System.out.println("Écrit CHAR : " + charValue);
						break;

					case VARCHAR:
						int varcharLength = value.length(); // Obtenir la longueur de la chaîne
						buff.putInt(varcharLength); // Écrire d'abord la longueur de la chaîne (4 octets)
						byte[] varcharBytes = value.getBytes(); // Convertir la chaîne en tableau de bytes
						buff.put(varcharBytes); // Écrire les bytes de la chaîne dans le buffer
						totalSize += 4 + varcharLength; // Ajouter 4 octets pour la longueur et la longueur de la chaîne à la taille totale
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
						int valeur_int = buff.getInt(); // lit 4 octs et les interpter comme un entier et avance la pos du
						// tampon de 4 octs
						record.getValeursRec().add(Integer.toString(valeur_int));
						totalSize += 4;
						break;
					case FLOAT:
						int floatBytesLength = buff.getInt(); // Lire la longueur des bytes
						byte[] floatBytes = new byte[floatBytesLength];
						buff.get(floatBytes); // Lire les bytes correspondant à la chaîne
						String floatString = new String(floatBytes); // Convertir les bytes en chaîne
						BigDecimal valeur_float = new BigDecimal(floatString); // Convertir la chaîne en BigDecimal
						record.getValeursRec().add(valeur_float.toString()); // Ajouter au record sous forme de chaîne
						totalSize += 4 + floatBytesLength; // Longueur totale (taille + contenu)
						System.out.println("Lu FLOAT (en BigDecimal) : " + valeur_float);
						break;


					case CHAR:
						int charLength = colInfo.getLengthChar();
						byte[] charBytes = new byte[charLength]; // Créer un tableau de bytes pour stocker les données lues
						// depuis le tampon
						buff.get(charBytes); // Lire les bytes correspondant à la longueur de la chaîne CHAR
						String valeur_char = new String(charBytes).trim(); // remove spaces or extra-padding
						record.getValeursRec().add(valeur_char); // Ajouter la valeur lue (chaîne) dans la liste des valeurs
						totalSize += charLength;
						break;
					case VARCHAR:
						int varCharLength = buff.getInt();
						byte[] varCharBytes = new byte[varCharLength];
						buff.get(varCharBytes);
						String varCharValue = new String(varCharBytes);
						record.getValeursRec().add(varCharValue);
						totalSize += 4 + varCharLength;
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
	 * Cette méthode écrit l’enregistrement record dans la page de données identifiée par pageId, et
	 *  renvoie son RecordId.
	 *  On suppose que la page dispose d’assez d’espace disponible pour l’insertion.
	 * @param record
	 * @param pageId
	 * @return RecordId du record écrit dans la dataPage
	 */
	public RecordId writeRecordToDataPage(Record record, PageId pageId) {
		try {
			// Charger la dataPage via BufferManager
			ByteBuffer dataPageBuffer = buffer.GetPage(pageId);
			Buffer buffDataPage = new Buffer(pageId,dataPageBuffer);

			// Lire les métadonnées de la page
			int posDebutLibre = dataPageBuffer.getInt(config.getPageSize() - 4); // Position début espace libre
			int nbSlots = dataPageBuffer.getInt(config.getPageSize() - 8); // Nombre de slots

			// Écrire le record dans le buffer à partir de la position libre
			int sizeRecord = writeToBuffer(record, dataPageBuffer, posDebutLibre);

			// Mettre à jour les métadonnées : position début libre
			posDebutLibre += sizeRecord;
			dataPageBuffer.putInt(config.getPageSize() - 4, posDebutLibre); // Met à jour pos début libre

			// Mettre à jour le nombre de slots et écrire dans le slot directory
			int slotIdx = nbSlots;
			nbSlots++;
			dataPageBuffer.putInt(config.getPageSize() - 8, nbSlots); // Met à jour nb slots

			// Calcul de la position pour le slot directory et mise à jour
			int slotOffset = config.getPageSize() - 8 - 8 * nbSlots; // 8 octets par slot (position + taille)
			dataPageBuffer.position(slotOffset);
			dataPageBuffer.putInt(posDebutLibre - sizeRecord); // Position du début du record
			dataPageBuffer.putInt(sizeRecord); // Taille du record

			// Marquer la page comme modifiée
			buffDataPage.setDirty(true);

			// Mettre à jour headerPage
			updateFreeSpaceInHeader(pageId, -sizeRecord);

			// écriture sur disque
			buffer.FreePage(pageId, true);
			buffer.FreePage(headerPageId, true); //peut etre modifie

			// Retourner un RecordId
			return new RecordId(pageId, slotIdx);

		} catch (Exception e) {
			throw new RuntimeException("Erreur lors de l'écriture du record dans la page de données : " + e.getMessage(), e);
		}
	}

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
			//Recupere la headerPage avecBufferManager
			ByteBuffer ByteBuffHeaderPage=buffer.GetPage(headerPageId);
			Buffer buffHeaderPage = new Buffer(headerPageId,ByteBuffHeaderPage);
			// allocation d'une nouvelle page pour l'ajouter au headerpage
			PageId newPageId = disk.AllocPage();
			System.out.println("Nouvelle page allouée : FileIdx = " + newPageId.getFileIdx() + ", PageIdx = " + newPageId.getPageIdx());
			// On incrèmente le nombre de pages de headerpage
			int nbPages = ByteBuffHeaderPage.getInt(0);
			System.out.println("Nombre actuel de pages dans la Header Page avant ajout de la nouvelle page: " + nbPages);
			nbPages++;
			ByteBuffHeaderPage.putInt(0, nbPages);
			// on ajoute dans la headerpage l'id de la page et le nombre d'octets vides
			ByteBuffHeaderPage.position(4 + 12 * (nbPages - 1));
			ByteBuffHeaderPage.putInt(newPageId.getFileIdx());
			ByteBuffHeaderPage.putInt(newPageId.getPageIdx());
			ByteBuffHeaderPage.putInt(config.getPageSize() - 8);
			//on mets la headerpage dirty pour stocker les infos sur disque
			buffHeaderPage.setDirty(true);

			//initialisation des données de la page qu'on vient d'ajouter
			//à la headerpage
			ByteBuffer byteBuffDataPage = buffer.GetPage(newPageId);
			Buffer buffDataPage = new Buffer(newPageId,byteBuffDataPage);
			byteBuffDataPage.position(0);
			byteBuffDataPage.putInt(8);
			byteBuffDataPage.putInt(0);
			//pour stocker les infos de datapage
			buffDataPage.setDirty(true);
			buffer.FreePage(headerPageId, true);
			buffer.FreePage(newPageId, true);
			System.out.println("ajout de page dans addDatapage avec succés");
		} catch (Exception e) {
			System.err.println("Error in addDataPage: " + e.getMessage());
			e.printStackTrace();
		}
	}

















}