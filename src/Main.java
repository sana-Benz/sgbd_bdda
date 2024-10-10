//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
//package projet_BDDA;

import java.io.IOException;

import org.json.simple.parser.ParseException;


public class Main {
        //Test DBConfig
        public static void main(String [] args) {

        try {
                DBConfig config = DBConfig.loadDBConfig("../../infos.json");
                System.out.println(config.getDbpath());
                System.out.println(config.getPageSize());
                System.out.println(config.getDm_maxfilesize());
                System.out.println(config.getBm_buffercount());
                System.out.println(config.getBm_policy());

        }catch(IOException e) {
                System.out.println("erreur: " + e.getMessage());
        }catch(ParseException e) {
                System.out.println("erreur: " + e.getMessage());
        }
        }

}
