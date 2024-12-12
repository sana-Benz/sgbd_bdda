import java.util.ArrayList;
import java.util.List;

public class Database {
    private String nom;
    private List<Relation> relations;

    public Database(String nom) {
        this.nom = nom;
        this.relations = new ArrayList<Relation>();
    }

    public String getNom() {
        return nom;
    }
}
