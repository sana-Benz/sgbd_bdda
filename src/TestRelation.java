import java.util.ArrayList;

public class TestRelation {
	public static void main(String [] args) {
		ArrayList<ColInfo> tableCols = new ArrayList<>();
		tableCols.add(new ColInfo("Colonne1", ColmType.INT, 0));
		tableCols.add(new ColInfo("Colonne2", ColmType.FLOAT, 0));
		tableCols.add(new ColInfo("Colonne3", ColmType.CHAR, 15));
		tableCols.add(new ColInfo("Colonne4", ColmType.VARCHAR, 25));
		
		Relation relation = new Relation("table1", 4, tableCols);
		
		System.out.println("Nom de la table : " + relation.getNomRelation());
		System.out.println("Nombre de colonne : " + relation.getNbCol());
		
		ArrayList<ColInfo> colonnes = relation.getTableCols();
		for(int i=0; i<relation.getNbCol(); i++) {
			System.out.println("Nome de colonne : " + colonnes.get(i).getNameCol());
	        System.out.println("Type de colonne : " + colonnes.get(i).getTypeCol());
	        System.out.println("Taille de colonne : " + colonnes.get(i).getLengthString());
	        
		}
		
	}

}
