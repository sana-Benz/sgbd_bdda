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

}
