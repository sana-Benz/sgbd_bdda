import java.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Database {
    private String nom;
    private ArrayList<Relation> relations;

    public Database(String nom, ArrayList<Relation> relations) {
        this.nom = nom;
        this.relations = relations;
    }

    public String getNom() {
        return nom;
    }

    public ArrayList<Relation> getRelations() {
        return relations;
    }

    // Method to add a table to the database
    public void addTable(Relation table) {
        relations.add(table);
    }

    // Retourne une table (Relation) par son nom
    public Relation getTable(String nomTable) {
        for (Relation relation : relations) {
            if (relation.getNomRelation().equals(nomTable)) {
                return relation;
            }
        }
        return null;
    }

    // Supprime une table (Relation) par son nom
    public boolean removeTable(String nomTable) {
        for (int i = 0; i < relations.size(); i++) {
            if (relations.get(i).getNomRelation().equals(nomTable)) {
                relations.remove(i);
                return true;
            }
        }
        return false;
    }
    // Method to remove all tables from the database
    public void removeAllTables() {
        if (relations.isEmpty()) {
            System.out.println("Aucune table à supprimer.");
            return;
        }
        relations.clear();
        System.out.println("Toutes les tables ont été supprimées avec succès !");
    }
    // Method to list all tables in the database
    public void listTables() {
        if (relations.isEmpty()) {
            System.out.println("Aucune table dans la base de données " + nom + ".");
            return;
        }
        System.out.println("Tables dans la base de données " + nom + " :");
        for (Relation table : relations) {
            System.out.println("- " + table.getNomRelation());
        }
    }

}
