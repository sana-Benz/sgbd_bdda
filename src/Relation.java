import java.util.ArrayList;

import java.io.Serializable;

public class Relation implements Serializable {
	private static final long serialVersionUID = 1L; 
	private String nomRelation;
	private int nbCol;
	private ArrayList<ColInfo> tableCols;
	
	PageId headerPageId;
	DiskManager disk;
	
	public Relation(String nomRelation, int nbCol, ArrayList<ColInfo> tableCols) {
		this.nomRelation = nomRelation;
		this.nbCol = nbCol;
		this.tableCols = tableCols;
		
	}

	public String getNomRelation() {
		return nomRelation;
	}

	public int getNbCol() {
		
		return nbCol;
	}

	public ArrayList<ColInfo> getTableCols() {
		return tableCols;
	}

	public ArrayList<Record> getAllRecords (){

        ArrayList <Record> listRecords=new ArrayList<>();  //list of record
        ArrayList<PageId> Pages=getDataPages();  //list of pages inside a relation
        
        for(int i=0;i<listPages.size();i++){
            ArrayList<Record> listRecords=getRecordsInDataPage(Pages.get(i)); //get the record of a page
            
           
            for(Record record:listRecords){
                listRecords.add(record);
            }
        }
        return listRecords;
    }
	@Override
	public String toString() {
		String TableInfo = "--" + nomRelation + "--";
		for (ColInfo col : tableCols) {
			TableInfo += col.toString();
		}
		TableInfo += "-- nombre de colonnes : " + nbCol + "|";
		return TableInfo;
	}

	public int getColIndex(String colName) {
		for (int i = 0; i < tableCols.size(); i++) {
			if (tableCols.get(i).getNameCol().equals(colName)) {
				return i;
			}
		}
		return -1;
	}

}
