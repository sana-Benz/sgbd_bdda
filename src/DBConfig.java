import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;

import java.io.IOException;
import java.io.FileNotFoundException;
import org.json.simple.parser.ParseException;

public class DBConfig {
    private String dbpath;
    private int pagesize;
    private int dm_maxfilesize;
    private int bm_buffercount;
    private String  bm_policy;

    public DBConfig(String dbpath, int pagesize, int dm_maxfilesize, int bm_buffercount, String  bm_policy ) {
        this.dbpath = dbpath;
        this.pagesize = pagesize;
        this.dm_maxfilesize = dm_maxfilesize;
        this.bm_buffercount = bm_buffercount;
        this.bm_policy = bm_policy;
    }

    public String getDbpath() {
        return dbpath;
    }
    public int getPageSize() {
        return pagesize ;
    }
    public int getDm_maxfilesize() {
        return dm_maxfilesize ;
    }
    public int getBm_buffercount() {return bm_buffercount ;}
    public String getBm_policy() { return bm_policy ;}

    public static DBConfig loadDBConfig(String fichierConfig) throws IOException, ParseException{
        JSONParser parser = new JSONParser();
        FileReader reader = new FileReader("./src/data/infos.json");
        Object obj = parser.parse(reader); // convertir json file --> java object
        JSONObject jsonObject = (JSONObject) obj; // convertir java object -->JSON object

        String dbpath = (String) jsonObject.get("dbpath");
        // les valeurs récupérées du fichier JSON sont traitées comme des objets de type Long
        // convertir Long --> int
        int pagesize = ((Long) jsonObject.get("pagesize")).intValue();
        int dm_maxfilesize = ((Long) jsonObject.get("dm_maxfilesize")).intValue();
        int bm_buffercount = ((Long) jsonObject.get("bm_buffercount")).intValue();
        String bm_policy = (String) jsonObject.get("bm_policy");
        return new DBConfig(dbpath, pagesize, dm_maxfilesize, bm_buffercount, bm_policy );
    }

}

