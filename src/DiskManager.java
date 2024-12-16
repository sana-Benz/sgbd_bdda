import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiskManager {
    private DBConfig config;
    private List<Integer> pagesLibres = new ArrayList<>(); // Liste des pages libres d'un fichier
    private int nbMaxPages; // nombre maximal de pages dans un fichier
    private static int indexFichierCourant = 0;
    private Map<String, List<PageId>> pagesAllouees; // Map pour suivre les pages allouées par table

    public DiskManager(DBConfig config) throws IOException {
        this.config = config;
        this.nbMaxPages = config.getDm_maxfilesize() / config.getPageSize();
        this.pagesAllouees = new HashMap<>(); // Initialiser la map

        // Création du dossier DB s'il n'existe pas
        File dbDirectory = new File(config.getDbpath());
        if (!dbDirectory.exists()) {
            dbDirectory.mkdir(); // Créer le dossier DB
        }
        // Création du dossier Bin_Data s'il n'existe pas
        File binDataDirectory = new File(dbDirectory, "Bin_Data");
        if (!binDataDirectory.exists()) {
            binDataDirectory.mkdir(); // Créer le dossier Bin_Data
        }

    }

    private String construireNomFichier(int index) {
        return "F" + index + ".rsdb";
    }

    private String construireCheminFichier(int index) {
        return config.getDbpath() + "/Bin_Data/" + construireNomFichier(index);
    }

    private int nbPagesFichier(RandomAccessFile fichier) { // Calcule le nombre de pages qui existent/sont allouées dans
                                                           // le fichier
        try {
            int nbPagesFichier = (int) (fichier.length() / config.getPageSize());
            return (nbPagesFichier);
        } catch (IOException e) {
            System.out.println("Erreur de calcul du nombre de pages du fichier " + e.getMessage());
            return 0;
        }
    }

    /**
     * Cette méthode doit allouer une page, c’est à dire réserver une nouvelle page
     * à la demande
     * d’une des couches au-dessus. Elle retourne un PageId correspondant à la page
     * nouvellement rajoutée.
     * 
     * @return PageId
     * @throws IOException
     */
    public PageId AllocPage() throws IOException {
        try (RandomAccessFile fichier = new RandomAccessFile(construireCheminFichier(indexFichierCourant), "rw")) {
            if (!pagesLibres.isEmpty()) {
                Integer indicePageLibre = pagesLibres.remove(pagesLibres.size() - 1);
                PageId pageId = new PageId(indexFichierCourant, indicePageLibre);
                return pageId;
            }
            if (nbPagesFichier(fichier) < nbMaxPages) {
                PageId pageId = new PageId(indexFichierCourant, nbPagesFichier(fichier));
                initializePage(pageId); // Ensure the page is properly initialized
                return pageId;
            }
            indexFichierCourant++;
            try (RandomAccessFile nouveauFichier = new RandomAccessFile(construireCheminFichier(indexFichierCourant),
                    "rw")) {
                ByteBuffer newPage = ByteBuffer.allocate(config.getPageSize());
                nouveauFichier.write(newPage.array());
                PageId pageId = new PageId(indexFichierCourant, 0);
                initializePage(pageId); // Ensure the page is properly initialized
                return pageId;
            } catch (IOException e) {
                System.out.println("Erreur lors de la création du nouveau fichier : " + e.getMessage());
                return null;
            }
        } catch (IOException e) {
            System.out.println("Erreur d'ouverture du fichier pour l'allocation de page : " + e.getMessage());
        }
        return null;
    }

    private int calculOffset(int pageIdx) { // Calcule l'offset d'une page dans le fichier

        return pageIdx * config.getPageSize();
    }

    /**
     * Cette méthode remplit l’argument buff en copiant dans ce buffer le contenu
     * disque de la
     * page identifiée par l’argument pageId.
     * 
     * @param pageId
     * @param buff
     */
    public void ReadPage(PageId pageId, ByteBuffer buff) {
        try (RandomAccessFile file = new RandomAccessFile(construireCheminFichier(pageId.getFileIdx()), "r")) {
            int offset = pageId.getPageIdx() * config.getPageSize();
            System.out.println("Lecture de la page : " + pageId);

            file.seek(offset); // Se déplacer à la bonne position
            byte[] pageData = new byte[config.getPageSize()];
            int bytesRead = file.read(pageData);

            if (bytesRead < config.getPageSize()) {
                throw new IOException("Lecture incomplète pour PageId: " + pageId);
            }

            buff.clear(); // Préparer le buffer pour l'écriture
            buff.put(pageData); // Copier les octets lus dans le buffer
            buff.flip(); // Préparer le buffer pour la lecture
            System.out.println("Buffer Après lecture: " + Arrays.toString(Arrays.copyOf(buff.array(), 16)));
        } catch (IOException e) {
            System.err.println("Error while reading page: " + e.getMessage());
        }
    }

    /**
     * Cette méthode remplit l’argument buff en copiant dans ce buffer le contenu
     * disque de la
     * page identifiée par l’argument pageId.
     * 
     * @param pageId
     * @param buff
     */
    public void WritePage(PageId pageId, ByteBuffer buff) {
        try (RandomAccessFile file = new RandomAccessFile(construireCheminFichier(pageId.getFileIdx()), "rw")) {
            int offset = pageId.getPageIdx() * config.getPageSize();
            file.seek(offset);

            // Assurez-vous que le buffer est prêt à être écrit
            buff.position(0); // Réinitialiser la position du buffer avant d'écrire
            byte[] pageData = new byte[config.getPageSize()];
            int bytesToWrite = Math.min(buff.remaining(), config.getPageSize()); // Limiter à la taille de la page
            buff.get(pageData, 0, bytesToWrite); // Lire les données du buffer
            file.write(pageData); // Écrire les données dans le fichier

            // Validation de l'écriture
            ByteBuffer validationBuffer = ByteBuffer.allocate(config.getPageSize());
            file.seek(offset);
            file.read(validationBuffer.array());
            if (!Arrays.equals(validationBuffer.array(), pageData)) {
                System.err
                        .println("Erreur d'écriture de la page dans le disque : la data ne se correspond pas" + pageId);
            } else {
                System.out.println("Page écrite avec succès dans le disque " + pageId);
            }
        } catch (IOException e) {
            System.err.println("Error writing page: " + e.getMessage());
        }
    }

    /**
     *
     * Cette méthode désalloue une page, et la rajoute dans la liste des pages
     * «libres».
     * 
     * @param pageId
     */
    public void DeallocPage(PageId pageId) {
        // effacer le contenu de la page
        ByteBuffer emptyBuffer = ByteBuffer.allocate(config.getPageSize()); // Buffer vide rempli de zéros
        WritePage(pageId, emptyBuffer);
        pagesLibres.add(pageId.getPageIdx());
        SaveState();

    }

    // Méthode pour désallouer les pages pour une table
    public void DeallocPagesForTable(String nomTable) {
        List<PageId> pages = pagesAllouees.get(nomTable);
        if (pages == null || pages.isEmpty()) {
            System.out.println("Aucune page à désallouer pour la table " + nomTable + ".");
            return;
        }

        for (PageId pageId : pages) {
            DeallocPage(pageId); // Appel de la méthode DeallocPage pour chaque page
        }

        pagesAllouees.remove(nomTable); // Supprimer les entrées de la map
        System.out.println("Désallocation des pages pour la table " + nomTable + " effectuée avec succès !");
    }

    /**
     * Cette méthode sauvegarde dans un fichier la liste des pages
     * libres du gestionnaire disque. Le fichier s’appellera dm.save et sera placé à
     * la racine du
     * dossier dbpath.
     */
    public void SaveState() {
        // on sauvegarde la liste des pages vides
        String cheminFichier = config.getDbpath() + "/dm.save"; // Le fichier s’appellera dm.save et sera placé à la
                                                                // racine dossier dbpath .?
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cheminFichier))) {
            writer.write("IndexFichierCourant:" + indexFichierCourant);
            writer.newLine();
            for (Integer pageIdx : pagesLibres) {
                writer.write(pageIdx.toString());
                writer.newLine();
            }
            System.out.println("L'état a été sauvegardé avec succès dans " + cheminFichier);
        } catch (IOException e) {
            System.out.println("Erreur lors de la sauvegarde de l'état : " + e.getMessage());
        }
    }

    /**
     * Cette méthode charge la liste des pages libres depuis le fichier dm.save
     */
    public void LoadState() {
        // Cette méthode devra charger la liste des pages libres depuis le fichier
        // dm.save
        String cheminFichier = config.getDbpath() + "/dm.save";
        File fichier = new File(cheminFichier);
        if (!fichier.exists()) {
            System.out.println("Aucun fichier de sauvegarde trouvé à l'emplacement : " + cheminFichier);
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;

            if ((ligne = reader.readLine()) != null) {
                // Extraire index fichier courant
                if (ligne.startsWith("IndexFichierCourant:")) {
                    String value = ligne.split(":")[1].trim();
                    indexFichierCourant = Integer.parseInt(value);
                }
            }

            while ((ligne = reader.readLine()) != null) {
                int pageIdx = Integer.parseInt(ligne.trim()); // Convertir la ligne en entier
                pagesLibres.add(pageIdx); // Ajouter l'indice de la page libre à la liste

            }
            System.out.println("L'état a été chargé avec succès depuis " + cheminFichier);
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement de l'état : " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Erreur de format dans le fichier de sauvegarde : " + e.getMessage());
        }
    }

    private void initializePage(PageId pageId) {
        // Allocate a buffer and initialize it with zeros
        ByteBuffer pageBuffer = ByteBuffer.allocate(config.getPageSize());
        Arrays.fill(pageBuffer.array(), (byte) 0); // Fill the buffer with zeros

        // Write the page index as the first integer for validation purposes
        pageBuffer.putInt(0, pageId.getPageIdx()); // Write PageIdx as the first value
        pageBuffer.position(0); // Reset position before writing to ensure all bytes are written

        // Call the WritePage method of DiskManager to persist the initialized page
        WritePage(pageId, pageBuffer); // Persist to disk
        System.out.println(
                "La page a été initialisée: FileIdx = " + pageId.getFileIdx() + ", PageIdx = " + pageId.getPageIdx());
    }

}
