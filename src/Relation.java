import java.util.ArrayList;
import java.nio.ByteBuffer;

public class Relation {
    private String nomRelation;
    private int nbCol;
    private ArrayList<ColInfo> tableCols;
    private PageId headerPageId;
    private BufferManager buffer;  // Référence à BufferManager

    // Modifiez le constructeur pour accepter config et buffer en paramètre
    public Relation(String nomRelation, int nbCol, ArrayList<ColInfo> tableCols, DBConfig config, PageId headerPageId, DiskManager disk, BufferManager buffer) {
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
   }
 