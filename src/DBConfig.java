import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;

import java.io.IOException;
import java.io.FileNotFoundException;
import org.json.simple.parser.ParseException;

public class DBConfig {
    private String dbpath;
    DBConfig(String dbpath) {
        this.dbpath = dbpath;
    }

    public String getDbpath() {
        return dbpath;
    }

    public static DBConfig loadDBConfig(String fichierConfig) throws IOException, ParseException{
        JSONParser parser = new JSONParser();
        FileReader reader = new FileReader("./data/infos.json");
        Object obj = parser.parse(reader); // convertir json file --> java object
        JSONObject jsonObject = (JSONObject) obj; // convertir java object -->JSON object

        String dbpath = (String) jsonObject.get("dbpath");
        return new DBConfig(dbpath);
    }

}

