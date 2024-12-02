import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Relation {
    private String nomRelation;
    private int nbCol;
    private ArrayList<ColInfo> tableCols;
    private PageId headerPageId;
    private BufferManager buffer;  // Référence à BufferManager

    
  
	// Modifiez le constructeur pour accepter config et buffer en paramètre
    public Relation(DBConfig config, String nomRelation, int nbCol, ArrayList<ColInfo> tableCols, PageId headerPageId, DiskManager disk, BufferManager buffer) {
        this.nomRelation = nomRelation;
        this.nbCol = nbCol;
        this.tableCols = tableCols;
        this.headerPageId = headerPageId;
        this.buffer = buffer;  // Initialiser BufferManager
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
			
			// Parcourir toutes les colonnes du schéma de la table
			for (int i = 0; i < getNbCol(); i++) {
				String value = recValues.get(i); // Récupérer la valeur actuelle pour la colonne
				ColInfo colInfo = tableCols.get(i);
				// Switch basé sur le type de la colonne
				switch (colInfo.getTypeCol()) {

				// Cas pour les colonnes de type INT
				case INT:
					int intValue = Integer.parseInt(value);
					buff.putInt(intValue); // Écrire la valeur entière dans le buffer
					totalSize += 4; // Un INT occupe 4 octets
					break;

				// Cas pour les colonnes de type FLOAT
				case FLOAT:
					float floatValue = Float.parseFloat(value);
					buff.putFloat(floatValue); // Écrire la valeur flottante dans le buffer
					totalSize += 4; // Un FLOAT occupe 4 octets
					break;

				// Cas pour les chaînes de caractères de longueur fixe (CHAR)
				case CHAR:
					// Écrire une valeur CHAR de longueur fixe
					String charValue = value;
					int charLength = colInfo.getLengthChar(); // Obtenir la longueur fixe
					byte[] charBytes = new byte[charLength]; // Créer un tableau de bytes de longueur fixe
					byte[] charValueBytes = charValue.getBytes(); // Convertir la chaîne en tableau de bytes

					// Copier les bytes de la chaîne dans le tableau de longueur fixe (troncature si
					// nécessaire)
					System.arraycopy(charValueBytes, 0, charBytes, 0, Math.min(charValueBytes.length, charLength));

					buff.put(charBytes); // Écrire les bytes de longueur fixe dans le buffer
					totalSize += charLength; // Ajouter la longueur du champ CHAR à la taille totale
					break;

				// Cas pour les chaînes de caractères de longueur variable (VARCHAR)
				case VARCHAR:
					// Écrire une valeur VARCHAR de longueur variable
					int varcharLength = value.length(); // Obtenir la longueur de la chaîne
					buff.putInt(varcharLength); // Écrire d'abord la longueur de la chaîne (4 octets)
					byte[] varcharBytes = value.getBytes(); // Convertir la chaîne en tableau de bytes
					buff.put(varcharBytes); // Écrire les bytes de la chaîne dans le buffer
					totalSize += 4 + varcharLength; // Ajouter 4 octets pour la longueur et la longueur de la chaîne à
													// la taille totale
					break;

				// Gérer les types de colonnes non pris en charge
				default:
					throw new IllegalArgumentException("le type de la colonne invalide !!");
				}
			}

			// Retourner la taille totale du record en octets
			return totalSize;
		} catch (Exception e) {
			System.err.println("Erreur dans writeToBuffer : " + e.getMessage());
			return -1; // Retourner -1 en cas d'erreur
		}
	}

	/**
	 *  Cette méthode rend comme résultat la taille totale (=le nombre d’octets) lus depuis le buffer.
	 * Elle lit les valeurs du Record depuis le buffer à partir de pos, en supposant que le
	 * Record a été écrit avec writeToBuffer.
	 * @param Record : dont la liste de valeurs est vide et sera remplie par cette méthode
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
					float valeur_float = buff.getFloat();
					record.getValeursRec().add(Float.toString(valeur_float));
					totalSize += 4;
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
            // Charger la Header Page depuis le BufferManager.
            ByteBuffer headerPage = buffer.GetPage(headerPageId);

            int numPages = headerPage.getInt(0); // Lire le nombre total de pages dans l'en-tête.
            for (int i = 0; i < numPages; i++) {
                int offset = 4 + i * 12; // Calculer l'emplacement des informations sur la page.
                int pageIdx = headerPage.getInt(offset); // Lire l'indice de la page.
                int freeSpace = headerPage.getInt(offset + 8); // Lire l'espace libre.

                if (freeSpace >= recordSize) {
                    int fileIdx = headerPageId.getFileIdx(); // Identifiant du fichier, souvent constant.
                    return new PageId(fileIdx, pageIdx); // Crée un PageId valide avec FileIdx et PageIdx.
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Gérer les erreurs si nécessaires.
        }
        return null; // Aucune page disponible avec assez d'espace.
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
    	
        // Charger la page via BufferManager
        ByteBuffer dataPageBuffer = buffer.GetPage(pageId);

        try {
            // Lire les métadonnées de la page
            int freeSpaceOffset = dataPageBuffer.getInt(0); // Position de l'espace libre
            int numSlots = dataPageBuffer.getInt(4);        // Nombre de slots

            // Construire les données du record directement
            StringBuilder recordString = new StringBuilder();
            for (String value : record.getValeursRec()) {
                recordString.append(value).append("\0"); // Séparer les valeurs par un null
            }
            byte[] recordData = recordString.toString().getBytes();

            // Vérifier l'espace disponible
            if (recordData.length > dataPageBuffer.capacity() - freeSpaceOffset) {
                throw new IllegalArgumentException("Pas assez d'espace pour insérer le record !");
            }

            // Insérer le record dans l'espace libre
            dataPageBuffer.position(freeSpaceOffset);
            dataPageBuffer.put(recordData);

            // Mettre à jour le slot directory
            int slotOffset = dataPageBuffer.capacity() - 8 * (numSlots + 1);
            dataPageBuffer.putInt(slotOffset, freeSpaceOffset);        // Position du record
            dataPageBuffer.putInt(slotOffset + 4, recordData.length); // Taille du record

            // Mettre à jour les métadonnées
            freeSpaceOffset += recordData.length;
            dataPageBuffer.putInt(0, freeSpaceOffset); // Mise à jour de l'espace libre
            dataPageBuffer.putInt(4, numSlots + 1);   // Mise à jour du nombre de slots

            // Retourner un RecordId
            return new RecordId(pageId, numSlots);
        } finally {
            // Marquer la page comme modifiée et libérer via BufferManager
            buffer.FreePage(pageId, true);
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
		// il faut passer Dm et Bm et Config dans le constructeur
		// ce passage est provisoire
		bufferManager = new BufferManager(config, diskManager);
		ByteBuffer bufferPage = bufferManager.GetPage(pageId);
		ArrayList<Record> listeRecords = new ArrayList<>();

		if (bufferPage == null) {
			throw new IllegalArgumentException("La page demandée n'a pas pu être chargée.");
		}

		try {
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
				if (recordSize > 0) {
					RecordId rid = new RecordId(pageId, slotIdx);
					Record record = new Record(this, rid);
					int bytesRead = record.readFromBuffer(bufferPage, recordStart);
					if (bytesRead != recordSize) {
						throw new IllegalStateException("Erreur : taille de record incohérente.");
					}
					listeRecords.add(record);
				}
			}
		} finally {
			// Libérer la page après utilisation
			try {
				bufferManager.FreePage(pageId, false);
			} catch (Exception e) {
				System.out.println("Erreur lors de la libération de page après lecture dans getRecordsInDataPage : " + e.getMessage());
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
			// Charger la header page via le BufferManager
			headerBuffer = bufferManager.GetPage(headerPageId);

			// Lire N (nombre de pages de données)
			int N = headerBuffer.getInt(0);

			// Parcourir les N cases dans la header page
			for (int i = 0; i < N; i++) {
				int offset = 4 + (i * 12); // 12 octets par case (8 pour idDataPage et 4 pour le nb d'octets dispo)
				// pour lire PageId stocké sur 8o : 2 entiers consécutifs (fileIdx et pageIdx)
				int fileIdx = headerBuffer.getInt(offset);      // Premier entier (fileIdx)
				int pageIdx = headerBuffer.getInt(offset + 4);  // Deuxième entier (pageIdx)

				PageId dataPageId = new PageId(fileIdx, pageIdx);
				dataPages.add(dataPageId);
			}
		} finally {
			// Libérer la header page après lecture
			try{
				bufferManager.FreePage(headerPageId, false);
			}catch (Exception e) {
				System.out.println("Erreur lors de la libération de page après lecture dans getDataPages : " + e.getMessage());
			}
		}
		return dataPages;
	}

}
