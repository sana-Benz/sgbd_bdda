public interface IRecordIterator {
    Record GetNextRecord();
    void Close();        
    void Reset();           
}
