public interface IRecordIterator {
    Record GetNextRecord(); // Retourne le prochain record, ou null s'il n'y a plus de records
    void Close();           // Libère les ressources
    void Reset();           // Remet le curseur au début
}
