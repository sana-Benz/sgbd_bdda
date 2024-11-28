import java.util.ArrayList;

import java.io.Serializable;

public class Relation implements Serializable {
	private static final long serialVersionUID = 1L; 
	private String nomRelation;
	private int nbCol;
	private ArrayList<ColInfo> tableCols;
	private PageId headerPageId;
	private DiskManager disk;
	
	public Relation(String nomRelation, int nbCol, ArrayList<ColInfo> tableCols, PageId headerPageId,
			DiskManager disk) {
		this.nomRelation = nomRelation;
		this.nbCol = nbCol;
		this.tableCols = tableCols;
		this.headerPageId = headerPageId;
		this.disk = disk;
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
	
	public PageId getHeaderPageId() {
		return headerPageId;
	}

	public DiskManager getDisk() {
		return disk;
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
	
	public ArrayList<Record> GetAllRecords(){
		ArrayList<Record> recordes = new ArrayList<>();
		ArrayList<PageId> pages = getDataPages();
		for(int i=0; pages.size(); i++) {
			ArrayList<Record> recordsInPage = getRecordsInDataPage(pages.get(i));
		}
		for( Record record : recordsInPage) {
			recordes.add(record);
		}
		return recordes;
	}

}
