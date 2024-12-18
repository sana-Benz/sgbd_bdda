public class RecordPrinter {
    private IRecordIterator iterator;

    public RecordPrinter(IRecordIterator iterator) {
        this.iterator = iterator;
    }

    public void printRecords() {
        System.out.println("Début de l'affichage des enregistrements");
        Record record;
        while ((record = iterator.GetNextRecord()) != null) {
            System.out.println(record.toString()); // Affiche chaque record
        }
        System.out.println("Fin de l'affichage des enregistrements");
        iterator.Close();  // Fermer l'itérateur après utilisation
    }
}