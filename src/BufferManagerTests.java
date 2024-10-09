import org.junit.Test;
import static org.junit.Assert.*;

public class BufferManagerTests {
    
    @Test
    public void testSetCurrentReplacementPolicyValid() {
        // Crée une instance de DBConfig avec une politique par défaut
        DBConfig config = new DBConfig("LRU", 5, 4096, 50000, "/path/to/db");
        DiskManager diskManager = new DiskManager(config);
        
        // Crée une instance de BufferManager
        BufferManager bufferManager = new BufferManager(config, diskManager);
        
        // Vérifie que la politique initiale est bien LRU
        assertEquals("LRU", bufferManager.getCurrentPolicy());

        // Change la politique à MRU
        bufferManager.SetCurrentReplacementPolicy("MRU");
        
        // Vérifie que la politique a bien changé à MRU
        assertEquals("MRU", bufferManager.getCurrentPolicy());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCurrentReplacementPolicyInvalid() {
        // Crée une instance de DBConfig avec une politique par défaut
        DBConfig config = new DBConfig("LRU", 5, 4096, 50000, "/path/to/db");
        DiskManager diskManager = new DiskManager(config);
        
        // Crée une instance de BufferManager
        BufferManager bufferManager = new BufferManager(config, diskManager);
        
        // Tente de changer la politique avec une valeur invalide
        bufferManager.SetCurrentReplacementPolicy("INVALID_POLICY");
    }
}
