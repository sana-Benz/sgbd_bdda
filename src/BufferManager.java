public class BufferManager {
    private DBConfig config;
    private DiskManager diskManager;
    private String currentPolicy;
    private List<Buffer> buffers;

    public BufferManager(DBConfig config, DiskManager diskManager) {
        this.config = config;
        this.diskManager = diskManager;
        this.currentPolicy = config.getBmPolicy(); // Défini la politique initiale
        this.buffers = new ArrayList<>(config.getBmBuffercount()); // Initialise les buffers selon la config
    }
    public String getCurrentPolicy() {
        return currentPolicy;
    }

    public void SetCurrentReplacementPolicy(String policy) {
        if (policy.equals("LRU") || policy.equals("MRU")) {
            currentPolicy = policy;
            System.out.println("Politique de remplacement changée à : " + policy);
        } else {
            throw new IllegalArgumentException("Politique invalide : " + policy + ". Utilisez 'LRU' ou 'MRU'.");
        }
    }

}
