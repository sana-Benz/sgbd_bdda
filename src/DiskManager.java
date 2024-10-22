import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class DiskManager {
    private DBConfig config;
    private List<Integer> pagesLibres= new ArrayList<>(); // Liste des pages libres d'un fichier
    private int nbMaxPages; // nombre maximal de pages dans un fichier
    private static int indexFichierCourant;
    private int nbPagesAllouees; // Nombre de pages allouées dans le fichier courant


    public DiskManager(DBConfig config) throws IOException {
        this.config = config;
        this.nbMaxPages = config.getDm_maxfilesize() / config.getPageSize();

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

        // Vérifier si le fichier de sauvegarde existe avant de charger l'état
        File saveFile = new File(dbDirectory, "dm.save");
        if (saveFile.exists()) {
            LoadState();
        } else {
            indexFichierCourant=0;
            nbPagesAllouees=0;
        }
    }

    public String construireNomFichier(int index){
        return "F" + index +".rsdb" ;
    }

    public String construireCheminFichier(int index){
        return config.getDbpath()+"/Bin_Data/"+construireNomFichier(index);
    }



    public PageId AllocPage() throws IOException{
        RandomAccessFile fichier = null;
        try {
            fichier = new RandomAccessFile(construireCheminFichier(indexFichierCourant), "rw");
        }catch(IOException e){
            System.out.println("Erreur d'ouverture du fichier pour l'allocation de page" + e.getMessage());
            return null;
        }
        if (!pagesLibres.isEmpty()) {
            Integer indicePageLibre = pagesLibres.remove(pagesLibres.size() - 1); //retourne l'indice de la derniere page vide
            System.out.println("Page libre utilisée : " + indicePageLibre);
            PageId pageId = new PageId(indexFichierCourant, indicePageLibre);
            return pageId;
        }
        //je dois savoir si j'ai encore de l'espace pour ajouter une page dans le fichier courant
        if (nbPagesAllouees < nbMaxPages ) {
            int nextPageIdx = nbPagesAllouees; // Calcule l'indice avant d'écrire la page
            ByteBuffer newPage = ByteBuffer.allocate(config.getPageSize());
            nbPagesAllouees++;
            //System.out.println("Nouvelle page allouée : Fichier " + indexFichierCourant + ", Page " + nextPageIdx);
            PageId pageId = new PageId(indexFichierCourant, nextPageIdx); // Utilise cet indice pour PageId
            return pageId;
        }else{ //sinon, je cree un nouveau fichier et cree la premiere page
            indexFichierCourant ++;
            try {
                // Créer un nouveau fichier RandomAccessFile
                RandomAccessFile nouveauFichier = new RandomAccessFile(construireCheminFichier(indexFichierCourant), "rw");
                ByteBuffer newPage = ByteBuffer.allocate(config.getPageSize()); // Crée la première page
                //nouveauFichier.write(newPage.array());// on écrit qlq chose dans la nouvelle page
                nbPagesAllouees = 1;
                //System.out.println("Création d'un nouveau fichier : " + indexFichierCourant + " avec la première page.");
                PageId pageId = new PageId(indexFichierCourant, 0);
                return pageId;
            } catch (IOException e) {
                System.out.println("Erreur lors de la création du nouveau fichier pour l'allocation d'une nouvelle page: " + e.getMessage());
                return null;
            }
        }
    }

    private int calculOffset(int pageIdx) { // Calcule l'offset d'une page dans le fichier
        return pageIdx * config.getPageSize();
    }

    public void ReadPage (PageId pageId, ByteBuffer buff) {
        RandomAccessFile fichier = null;
        try {
            fichier = new RandomAccessFile(construireCheminFichier(pageId.getFileIdx()), "r");
            int offset = calculOffset(pageId.getPageIdx());
            fichier.seek(offset);
            // Pour transférer des données dans un ByteBuffer, il faut d'abord lire dans un tableau de bytes
            byte[] pageData = new byte[config.getPageSize()]; // Créer un tableau pour contenir les données de la page
            int bytesRead = fichier.read(pageData); // Lire les données à partir de l'offset

            if (bytesRead == -1) {
                throw new IOException("La page est vide");
            }

            // Remplir le ByteBuffer avec les données de pageData
            buff.clear();
            buff.put(pageData, 0, bytesRead);
            buff.flip();

        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture de la page : " + e.getMessage());
        } finally {
            // Fermer le fichier à la fin
            if (fichier != null) {
                try {
                    fichier.close();
                } catch (IOException e) {
                    System.out.println("Erreur lors de la fermeture du fichier après lecture: " + e.getMessage());
                }
            }
        }
    }

    public void WritePage (PageId pageId, ByteBuffer buff){
        //Cette méthode copie le contenu de l’argument buff dans le fichier et à la position indiquée par l’argument pageId.
        RandomAccessFile fichier = null;
        try {
            fichier = new RandomAccessFile(construireCheminFichier(pageId.getFileIdx()), "rw");
            int offset = calculOffset(pageId.getPageIdx());
            fichier.seek(offset); // Positionne le curseur à l'endroit où la nouvelle page sera écrite
            fichier.write(buff.array(),0,buff.limit()); // Écrit le contenu de buff dans le fichier
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture de la nouvelle page : " + e.getMessage());
        }finally {
            // Fermer le fichier à la fin
            if (fichier != null) {
                try {
                    fichier.close();
                } catch (IOException e) {
                    System.out.println("Erreur lors de la fermeture du fichier après écriture: " + e.getMessage());
                }
            }
        }
    }

    public void DeallocPage (PageId pageId){
        //Cette méthode doit désallouer une page, et la rajouter dans la liste des pages «libres»
        pagesLibres.add(pageId.getPageIdx());
        SaveState();

    }
    public void SaveState() {
        //on sauvegarde la liste des pages vides
        String cheminFichier = config.getDbpath() + "/dm.save"; //Le fichier s’appellera dm.save et sera placé à la racine dossier dbpath
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cheminFichier))) {
            writer.write("FichierCourant:" + indexFichierCourant);
            writer.newLine();
            writer.write("NbPagesAllouees:" + nbPagesAllouees); // Sauvegarder le nombre de pages allouées dans le fichier
            writer.newLine();
            if (pagesLibres.isEmpty()) {
                writer.write("pageLibres est vide");
            }else{
                for (Integer pageIdx : pagesLibres) {
                    writer.write(pageIdx.toString());
                    writer.newLine();
                }
                //System.out.println("L'état a été sauvegardé avec succès dans " + cheminFichier);
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de la sauvegarde de l'état : " + e.getMessage());
        }
    }


    public void LoadState() {
        //Cette méthode devra charger la liste des pages libres depuis le fichier dm.save
        String cheminFichier = config.getDbpath() + "/dm.save";
        File fichier = new File(cheminFichier);
        if (!fichier.exists()) {
            System.out.println("Aucun fichier de sauvegarde trouvé à l'emplacement : " + cheminFichier);
            return;
        }



        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {
            // Lire l'indice du fichier courant
            String ligne = reader.readLine();
            if (ligne != null && ligne.startsWith("FichierCourant:")) {
                String[] parts = ligne.split(":");
                if (parts.length == 2) {
                    indexFichierCourant = Integer.parseInt(parts[1].trim());
                    //System.out.println("indice du fichier courant chargé avec succès : " + indexFichierCourant);
                }
            }

            // Lire le nombre de pages allouées dans le fichier
            ligne = reader.readLine(); // Lire la deuxième ligne
            if (ligne != null && ligne.startsWith("NbPagesAllouees:")) {
                String[] parts = ligne.split(":");
                if (parts.length == 2) {
                    try {
                        nbPagesAllouees = Integer.parseInt(parts[1].trim());
                        //System.out.println("Nombre de pages allouées chargé avec succès : " + nbPagesAllouees);
                    } catch (NumberFormatException e) {
                        System.out.println("Erreur de format pour le nombre de pages allouées : " + e.getMessage());
                    }
                }
            }


            // Lire la liste des pages libres
            while ((ligne = reader.readLine()) != null) {
                int pageIdx = Integer.parseInt(ligne.trim());
                pagesLibres.add(pageIdx);
            }
            //System.out.println("Pages libres chargées : " + pagesLibres);
            //System.out.println("L'état a été chargé avec succès depuis " + cheminFichier);
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement de l'état : " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Erreur de format dans le fichier de sauvegarde : " + e.getMessage());
        }
    }







}
