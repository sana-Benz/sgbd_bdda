import java.util.ArrayList;
import java.nio.ByteBuffer;

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
}