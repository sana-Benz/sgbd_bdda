import java.io.IOException;

import java.nio.ByteBuffer;

public class DiskManagerTests {

	public static void main(String[] args) throws IOException {
		//test modifié

		DBConfig config = new DBConfig("../DB", 4096, 8192, 100, "LRU");
		DiskManager dm = new DiskManager(config);

		// Boucle pour créer 3 fichiers avec 2 pages dans chacun
		for (int pageNum = 0; pageNum < 6; pageNum++) {
			// Allouer une page
			PageId pageId = dm.AllocPage();
			System.out.println("Page allouée : Fichier " + pageId.getFileIdx() + ", Page " + pageId.getPageIdx());

			// Écriture dans la page
			ByteBuffer buffer = ByteBuffer.allocate(config.getPageSize());
			buffer.put("Test d'écriture".getBytes());
			dm.WritePage(pageId, buffer);
			System.out.println("Page " + pageNum + " écrite avec succès.");

			// Sauvegarder l'état après l'écriture
			dm.SaveState();


		}
		// test pour désallouer une page et la mettre dans pagesLibres
		PageId pageid = new PageId(2,1);
		dm.DeallocPage (pageid);
		dm.SaveState();


		System.out.println("Test terminé : 3 fichiers créés avec 2 pages écrites dans chacun.");
	}
}