
public class TestColInfo {
	public static void main(String[] args) {
		
        ColInfo colInfo1 = new ColInfo("Colonne1", ColmType.INT, 0);
        System.out.println("Nome de colonne : " + colInfo1.getNameCol());
        System.out.println("Type de colonne : " + colInfo1.getTypeCol());
        System.out.println("Taille de colonne : " + colInfo1.getLengthChar());
        
        ColInfo colInfo2 = new ColInfo("Colonne2", ColmType.FLOAT, 0);
        System.out.println("Nome de colonne : " + colInfo2.getNameCol());
        System.out.println("Type de colonne : " + colInfo2.getTypeCol());
        System.out.println("Taille de colonne : " + colInfo2.getLengthChar());
        
        ColInfo colInfo3 = new ColInfo("Colonne3", ColmType.CHAR, 15);
        System.out.println("Nome de colonne : " + colInfo3.getNameCol());
        System.out.println("Type de colonne : " + colInfo3.getTypeCol());
        System.out.println("Taille de colonne : " + colInfo3.getLengthChar());
        
        ColInfo colInfo4 = new ColInfo("Colonne4", ColmType.VARCHAR, 25);
        System.out.println("Nome de colonne : " + colInfo4.getNameCol());
        System.out.println("Type de colonne : " + colInfo4.getTypeCol());
        System.out.println("Taille de colonne : " + colInfo4.getLengthChar());
        
        
	}

}
