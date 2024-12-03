package Projet_SGBD;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.io.IOException;
import Projet_SGBD.DBConfig;
import Projet_SGBD.DiskManager;
import Projet_SGBD.Relation;
import Projet_SGBD.PageId;



public class TestaddDataPage{
    public static void main(String[] args) throws IOException {
        // Configuration de la base de données et initialisation des gestionnaires
        DBConfig config = new DBConfig(4096, "/path/to/database", 10485760); // Exemple de configuration
        DiskManager diskManager = new DiskManager(config);
        BufferManager bufferManager = new BufferManager(config, diskManager);

        // Créer une relation avec un nom, 3 colonnes, et un DiskManager et BufferManager pour l'accès aux pages
        Relation relation = new Relation("MaRelation", 3, 
            List.of("Col1", "Col2", "Col3"), 
            List.of("int", "string", "float"), diskManager, bufferManager);

        // Initialisation de la Header Page
        PageId headerPageId = new PageId(0, 0); // Supposons que la Header Page a l'ID (0, 0)
        relation.setHeaderPageId(headerPageId);

        // Ajouter des pages de données
        System.out.println("Ajout de pages de données...");
        relation.addDataPage(); // Appel à la méthode à tester
        relation.addDataPage(); // Appel à la méthode à tester
        relation.addDataPage(); // Appel à la méthode à tester

        // Vérification du résultat
        System.out.println("Pages de données après ajout :");
        List<PageId> dataPages = relation.getDataPages(); // Récupérer les PageIds des pages de données
        for (PageId pageId : dataPages) {
            System.out.println(pageId); // Afficher chaque PageId pour vérifier qu'elles ont été ajoutées correctement
        }

        // Vérification de la Header Page : on devrait voir que le nombre de pages a été mis à jour
        ByteBuffer headerBuffer = bufferManager.GetPage(headerPageId);
        int numPages = headerBuffer.getInt(0); // Le nombre de pages devrait être 3 après ajout
        System.out.println("Nombre de pages après ajout dans la Header Page : " + numPages);
    }
}
