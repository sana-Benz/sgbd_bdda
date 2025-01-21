package Relation;

public class ColInfo {
	private String ColName;
	private ColmType typeCol;
	private int lengthChar;
	
	public ColInfo(String ColName, ColmType typeCol, int lengthChar) {
        this.ColName = ColName;
        this.typeCol = typeCol;
        this.lengthChar = lengthChar;
        
    }


    public String getNameCol() {
        return ColName;
    }

    
    public ColmType getTypeCol() {
        return typeCol;
    }

    public int getLengthChar() {
        return lengthChar;
    }

    
    public String toString(){
        return "name : "+ColName+" type : "+typeCol+ " lengthString : "+lengthChar+" ";
    }
}
