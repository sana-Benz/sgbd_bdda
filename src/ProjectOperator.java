import java.util.List;

public class ProjectOperator implements IRecordIterator {

    private IRecordIterator iterateurFils; 
    private List<String> attributsAConserver;  // Liste des attributs à conserver
    private Record recordActuel;

    public ProjectOperator(IRecordIterator iterateurFils, List<String> attributsAConserver) {
        this.iterateurFils = iterateurFils;
        this.attributsAConserver = attributsAConserver;
    }

    @Override
    public Record GetNextRecord() {
        recordActuel = iterateurFils.GetNextRecord();
        if (recordActuel == null) {
            return null;  // Plus d'enregistrements à traiter
        }

        // Créer un nouvel enregistrement avec uniquement les attributs spécifiés
        Record recordProjete = new Record(recordActuel.getRelation(), recordActuel.getRecordId());
        for (String attribut : attributsAConserver) {
            recordProjete.addAttribut(attribut);  // Ajouter l'attribut à l'enregistrement projeté
        }

        return recordProjete;
    }

    @Override
    public void Close() {
        if (iterateurFils != null) {
            iterateurFils.Close();
        }
    }

    @Override
    public void Reset() {
        iterateurFils.Reset();
    }
}





