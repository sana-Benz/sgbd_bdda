import java.io.IOException;
import java.nio.ByteBuffer;

	public class DiskManagerTests { 
		
		    public static void main(String[] args) throws IOException {
		        DBConfig config = new DBConfig("../DB", 4096, 8192, 100, "LRU");
		        DiskManager dm = new DiskManager(config); 

 
		        // Test allocation de page  
		        PageId pageId = dm.AllocPage();   
		        System.out.println("Page allouée : Fichier " + pageId.getFileIdx() + ", Page " + pageId.getPageIdx());
 
		        // Test écriture dans une page
		        ByteBuffer buffer = ByteBuffer.allocate(config.getPageSize()); 
		        buffer.put("Test d'écriture".getBytes());
		        dm.WritePage(pageId, buffer);
				System.out.println("Page écrite avec succès."); 
	  
				// Test lecture de la page
				ByteBuffer readBuffer = ByteBuffer.allocate(config.getPageSize());
				dm.ReadPage(pageId, readBuffer);
				System.out.println("Contenu lu : " + new String(readBuffer.array()));
				 
				// Sauvegarder l'état
		        dm.SaveState();   

		        // Lire la page pour vérifier le contenu
		        dm.ReadPage(pageId, readBuffer);
		        System.out.println("Contenu lu : " + new String(readBuffer.array()).trim()); // Utilise trim() pour enlever les espaces

		        // Effacer le contenu et charger l'état
		        dm.DeallocPage(pageId); // Désalloue la page pour simuler une "suppression"

		        // Charger l'état 
		        dm.LoadState();
 
		        // Lire à nouveau la page après le chargement
		        dm.ReadPage(pageId, readBuffer);
		        System.out.println("Contenu lu après chargement : " + new String(readBuffer.array()).trim()); // Vérifie si le contenu est toujours présent
		    }
		    
		} 
	
