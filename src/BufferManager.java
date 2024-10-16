import java.awt.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

public class BufferManager {
    private DBConfig config;
    private DiskManager diskManager;
    private String currentPolicy;
    private List<Buffer> bufferPool; // Liste des buffers

    public BufferManager(DBConfig config, DiskManager diskManager) {
        this.config = config;
        this.diskManager = diskManager;
        this.bufferPool = new ArrayList<>();
    }


    public void flushBuffers() {
        for (Buffer buffer : bufferPool) {
            if (buffer.isDirty()) {
                diskManager.WritePage(buffer.getPageId(), buffer.getData());
            }
            buffer.reset();
        }
    }

}
