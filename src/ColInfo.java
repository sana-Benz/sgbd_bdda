

public class ColInfo {
	private String ColName;
	private ColmType typeCol;
	private int lengthString;
	
	public ColInfo(String ColName, ColmType typeCol, int lengthString) {
        this.ColName = ColName;
        this.typeCol = typeCol;
        this.lengthString = lengthString;
        
    }


    public String getNameCol() {
        return ColName;
    }

    
    public ColmType getTypeCol() {
        return typeCol;
    }

    public int getLengthString() {
        return lengthString;
    }

    
    public String toString(){
        return "name : "+ColName+" type : "+typeCol+ " lengthString : "+lengthString+" ";
    }
}
