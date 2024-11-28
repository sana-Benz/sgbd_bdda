//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
//package projet_BDDA;

import java.io.IOException;

	
	public class Main {
	public static void main(String [] args) throws org.json.simple.parser.ParseException {
	
	        try {
	                DBConfig config = DBConfig.loadDBConfig("../../infos.json");
	                System.out.println(config.getDbpath());
	                System.out.println(config.getPageSize());
	                System.out.println(config.getDm_maxfilesize());
	        }catch(IOException e) {
	                System.out.println("erreur: " + e.getMessage());
	        }
	        }
	
	}
