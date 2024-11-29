import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Relation {
    private DBConfig config;  // Déclaration de config
    private String nomRelation;
    private int nbCol;
    private ArrayList<ColInfo> tableCols;
    private PageId headerPageId;
    private DiskManager disk;  // Référence à DiskManager
    private BufferManager buffer;  // Référence à BufferManager

    // Modifiez le constructeur pour accepter config et buffer en paramètre
    public Relation(String nomRelation, int nbCol, ArrayList<ColInfo> tableCols, DBConfig config, PageId headerPageId, DiskManager disk, BufferManager buffer) {
        this.nomRelation = nomRelation;
        this.nbCol = nbCol;
        this.tableCols = tableCols;
        this.config = config; // Initialiser config dans Relation
        this.headerPageId = headerPageId;
        this.disk = disk;  // Initialiser DiskManager
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
        try {
            // Charger la page via le BufferManager
            ByteBuffer dataPage = buffer.GetPage(pageId);

            // Lire les métadonnées de la page
            int pageSize = config.getPageSize(); // Taille totale de la page
            int freePosOffset = pageSize - 4;    // Offset de la position libre
            int slotCountOffset = pageSize - 8;  // Offset du nombre de slots

            // Lire les positions et le nombre de slots
            int freePos = dataPage.getInt(freePosOffset); // Position actuelle de l'espace libre
            int slotCount = dataPage.getInt(slotCountOffset); // Nombre actuel de slots

            // Écrire le record dans la page
            int recordSize = record.writeToBuffer(dataPage, freePos); // Retourne la taille du record
            int requiredSpace = recordSize + 8; // Taille totale nécessaire (record + slot directory)

            // Vérifier si la page a assez d'espace
            if (freePos + requiredSpace > slotCountOffset) {
                // Si espace insuffisant, allouer une nouvelle page
                PageId newPageId = disk.AllocPage(); // Allouer une nouvelle page via DiskManager
                if (newPageId == null) {
                    throw new Exception("Impossible d'allouer une nouvelle page.");
                }
                dataPage = buffer.GetPage(newPageId); // Charger la nouvelle page
                freePos = 0; // Réinitialiser les métadonnées
                slotCount = 0;
            }

            // Ajouter une entrée dans le slot directory
            int slotDirectoryOffset = slotCountOffset - (slotCount + 1) * 8; // Calcul de l'offset pour ce slot
            dataPage.putInt(slotDirectoryOffset, freePos); // Position du record
            dataPage.putInt(slotDirectoryOffset + 4, recordSize); // Taille du record

            // Mettre à jour les métadonnées
            freePos += recordSize; // Avancer la position libre
            dataPage.putInt(freePosOffset, freePos); // Mettre à jour la position libre
            dataPage.putInt(slotCountOffset, slotCount + 1); // Incrémenter le nombre de slots

            // Sauvegarder la page sur le disque
            disk.WritePage(pageId, dataPage);

            // Retourner un RecordId avec la page et l'index du slot
            return new RecordId(pageId, slotCount); 
        } catch (Exception e) {
            e.printStackTrace();
            return null; // En cas d'erreur, retourner null
        }
    }

    /**
     * Cette méthode liste tous les records
     * @return ArrayList de Record
     */
    public ArrayList<Record> getAllRecords (){
    	
    	ArrayList <Record> listRecords=new ArrayList<>();  //list of record
        ArrayList<PageId> Pages=getDataPages();  //list of pages inside a relation
        
        for(int i=0;i<listPages.size();i++){
            ArrayList<Record> listRecords = getRecordsInDataPage(Pages.get(i)); //get the record of a page
            
           
            for(Record record:listRecords){
                listRecords.add(record);
            }
        }
        return listRecords;
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
