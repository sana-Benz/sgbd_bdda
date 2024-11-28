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
    private static int indexFichierCourant = 0;

 
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

    } 

    public String construireNomFichier(int index){
        return "F" + index +".rsdb" ;
    }

    public String construireCheminFichier(int index){ 
        return config.getDbpath()+"/Bin_Data/"+construireNomFichier(index);
    }
 
    public int nbPagesFichier(RandomAccessFile fichier) { // Calcule le nombre de pages qui existent/sont allouées dans le fichier
        try {
            int nbPagesFichier = (int) (fichier.length() / config.getPageSize());
            return(nbPagesFichier);
        } catch (IOException e) {
            System.out.println("Erreur de calcul du nombre de pages du fichier " + e.getMessage());
            return 0;
        }
    } 

     public PageId AllocPage() throws IOException{
         RandomAccessFile fichier = null;
         try {
             fichier = new RandomAccessFile(construireCheminFichier(indexFichierCourant), "rw");
         }catch(IOException e){
             System.out.println("Erreur d'ouverture du fichier pour l'allocation de page" + e.getMessage());
         }
         if (!pagesLibres.isEmpty()) {
             Integer indicePageLibre = pagesLibres.remove(pagesLibres.size() - 1); //retourne l'indice de la derniere page vide
             PageId pageId = new PageId(indexFichierCourant, indicePageLibre);
             return pageId; 
         }
         //je dois savoir si j'ai encore de l'espace pour ajouter une page dans le fichier courant
         if (nbPagesFichier(fichier) < nbMaxPages ) {
             ByteBuffer newPage = ByteBuffer.allocate(config.getPageSize());
             fichier.write(newPage.array());
             PageId pageId = new PageId(indexFichierCourant, nbPagesFichier(fichier) );
             return pageId;
         }else{ //sinon, je cree un nouveau fichier et cree la premiere page
             indexFichierCourant ++;  
             try {
                 // Créer un nouveau fichier RandomAccessFile
                 RandomAccessFile nouveauFichier = new RandomAccessFile(construireNomFichier(indexFichierCourant), "rw");
                 ByteBuffer newPage = ByteBuffer.allocate(config.getPageSize()); // Crée la première page
                 nouveauFichier.write(newPage.array());// on écrit qlq chose dans la nouvelle page
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
        String cheminFichier = config.getDbpath() + "/dm.save"; //Le fichier s’appellera dm.save et sera placé à la racine dossier dbpath .?
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
        //Cette méthode devra charger la liste des pages libres depuis le fichier dm.save
        String cheminFichier = config.getDbpath() + "/dm.save";
        File fichier = new File(cheminFichier);
        if (!fichier.exists()) {
            System.out.println("Aucun fichier de sauvegarde trouvé à l'emplacement : " + cheminFichier);
            return; 
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
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
}
