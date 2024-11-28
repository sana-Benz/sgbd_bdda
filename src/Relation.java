import java.nio.ByteBuffer;
import java.util.ArrayList;

import java.io.Serializable;


public class Relation implements Serializable {
	private String nomRelation;
	private int nbCol;
	private ArrayList<ColInfo> tableCols;
	private PageId headerPageId;
	private DiskManager diskManager;
	private BufferManager bufferManager;
	private DBConfig config;


	public Relation(String nomRelation, int nbCol, ArrayList<ColInfo> tableCols) {
		this.nomRelation = nomRelation;
		this.nbCol = nbCol;
		this.tableCols = tableCols;

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
