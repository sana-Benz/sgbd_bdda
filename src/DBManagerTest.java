import java.io.IOException;

public class DBManagerTest {


	public static void main(String[] args) throws IOException {
		DBConfig config = new DBConfig("../DB", 4096, 8192, 100, "LRU");
        DiskManager dm = new DiskManager(config); 
        BufferManager bufferManager = new BufferManager(config, dm);
        
		DBManager manager = new DBManager(config, dm, bufferManager);
		  
        manager.CreateDatabase("NouvelleBase");

     // Essayer de créer une base avec le même nom
     try {
         manager.CreateDatabase("NouvelleBase");
     } catch (IllegalArgumentException e) {
         System.out.println(e.getMessage());
     }

     // Créer une autre base
     manager.CreateDatabase("DeuxiemeBase");
     //manager.RemoveDatabase("NouvelleBase");
     //manager.ListTablesInCurrentDatabase();
		// Charger l'état au démarrage
		/*manager.loadState();
	
		// Créer une nouvelle base
		manager.createDatabase("TestDB");

		// Sauvegarder l'état actuel
		manager.saveState();
*/
	}

}
