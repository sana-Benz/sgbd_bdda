import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DiskManager {
    private DBConfig config;
    private List<Integer> pagesLibres; // Liste des pages libres d'un fichier
    private List<PageId> accessedPages; // Suivi des accès des pages (LRU/MRU)
    private int nbMaxPages; // nombre maximal de pages dans un fichier
    private RandomAccessFile fichier;
    private String nomFichier;
    private ReplacementPolicy currentPolicy; // Politique de remplacement courante

    public DiskManager(DBConfig config, String nomFichier) throws IOException {
        this.config = config;
        this.pagesLibres = new ArrayList<>();
        this.accessedPages = new ArrayList<>();
        this.nbMaxPages = config.getDm_maxfilesize() / config.getPageSize();
        this.fichier = new RandomAccessFile(nomFichier, "rw");
        this.currentPolicy = ReplacementPolicy.LRU; // Par défaut, LRU
    }

    public void SetCurrentReplacementPolicy(ReplacementPolicy policy) {
        this.currentPolicy = policy;
        System.out.println("Politique de remplacement actuelle définie sur : " + policy);
    }

    public int getIndexFichier() {
        if (nomFichier.startsWith("F")) {
            String indexString = nomFichier.substring(1); // Prendre tous les chiffres après F du nomfichier
            return Integer.parseInt(indexString);
        } else {
            throw new IllegalArgumentException("Nom de fichier invalide : " + nomFichier);
        }
    }

    public int nbPagesFichier() {
        try {
            return (int) (fichier.length() / config.getPageSize());
        } catch (IOException e) {
            System.out.println("Erreur de calcul du nombre de pages du fichier " + e.getMessage());
            return 0;
        }
    }

    public PageId AllocPage() {
        if (!pagesLibres.isEmpty()) {
            Integer indicePageLibre = pagesLibres.remove(pagesLibres.size() - 1);
            PageId pageId = new PageId(getIndexFichier(), indicePageLibre);
            accessedPages.add(pageId); // Suivre l'accès pour la politique de remplacement
            return pageId;
        }

        if (nbPagesFichier() < nbMaxPages) {
            PageId pageId = new PageId(getIndexFichier(), nbPagesFichier());
            ByteBuffer newPage = ByteBuffer.allocate(config.getPageSize());
            WritePage(pageId, newPage);
            accessedPages.add(pageId); // Suivre l'accès pour la politique de remplacement
            return pageId;
        } else {
            // Appliquer la politique de remplacement
            PageId pageToReplace = null;
            if (currentPolicy == ReplacementPolicy.LRU) {
                pageToReplace = accessedPages.remove(0); // LRU : première page accédée
            } else if (currentPolicy == ReplacementPolicy.MRU) {
                pageToReplace = accessedPages.remove(accessedPages.size() - 1); // MRU : dernière page accédée
            }

            // Réutiliser la page à remplacer (écraser ses données)
            ByteBuffer newPage = ByteBuffer.allocate(config.getPageSize());
            WritePage(pageToReplace, newPage);
            accessedPages.add(pageToReplace); // Ajouter comme la plus récemment accédée
            return pageToReplace;
        }
    }

    public void DeallocPage(PageId pageId) {
        pagesLibres.add(pageId.getPageIdx());
    }

    private int calculOffset(int pageIdx) {
        return pageIdx * config.getPageSize();
    }

    public void ReadPage(PageId pageId, ByteBuffer buff) {
        try {
            int offset = calculOffset(pageId.getPageIdx());
            fichier.seek(offset);
            byte[] pageData = new byte[config.getPageSize()];
            int bytesRead = fichier.read(pageData);
            buff.clear();
            buff.put(pageData, 0, bytesRead);
            buff.flip();

            accessedPages.remove(pageId); // Mettre à jour l'ordre d'accès
            accessedPages.add(pageId);    // Ajouter comme page récemment accédée

        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture de la page : " + e.getMessage());
        }
    }

    public void WritePage(PageId pageId, ByteBuffer buff) {
        int offset = calculOffset(pageId.getPageIdx());
        try {
            fichier.seek(offset);
            fichier.write(buff.array(), buff.position(), buff.remaining());
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture de la page : " + e.getMessage());
        }
    }

    public void SaveState() {
        String cheminFichier = config.getDbpath() + "/dm.save";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cheminFichier))) {
            for (Integer pageIdx : pagesLibres) {
                writer.write(pageIdx.toString());
                writer.newLine();
            }
            System.out.println("L'état a été sauvegardé avec succès dans " + cheminFichier);
        } catch (IOException e) {
            System.out.println("Erreur lors de la sauvegarde de l'état : " + e.getMessage());
        }
    }

    public void LoadState() {
        String cheminFichier = config.getDbpath() + "/dm.save";
        File fichier = new File(cheminFichier);
        if (!fichier.exists()) {
            System.out.println("Aucun fichier de sauvegarde trouvé à l'emplacement : " + cheminFichier);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                int pageIdx = Integer.parseInt(ligne.trim());
                pagesLibres.add(pageIdx);
            }
            System.out.println("L'état a été chargé avec succès depuis " + cheminFichier);
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement de l'état : " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Erreur de format dans le fichier de sauvegarde : " + e.getMessage());
        }
    }
}
