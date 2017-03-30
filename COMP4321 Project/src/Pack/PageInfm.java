package Pack;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.util.Vector;
import java.io.IOException;
import java.io.Serializable;
import javax.swing.text.html.parser.Parser;

class Infm implements Serializable {
	public String title;
	public String url;
	public String lastModifyDate;
	public int size;

	Infm(String title, String url, String lastModifyDate, int size){
		this.title = title;
		this.url = url;
		this.lastModifyDate = lastModifyDate;
		this.size = size;
	}
}

public class PageInfm {
	private RecordManager recman;
	private HTree hashtable;

	public PageInfm(RecordManager recman, String objectname) throws IOException {
		this.recman = recman;
		long recid = recman.getNamedObject(objectname);

		checkOrCreateHashtable(recman, objectname, recid);
	}

	private void checkOrCreateHashtable(RecordManager recman, String objectname, long recid) throws IOException {
		if (recid != 0) {
			hashtable = HTree.load(recman, recid);
		}
		else // If not, create a new hashtable;
		{
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject(objectname, hashtable.getRecid());
		}
	}

	public boolean isContain(String word) throws IOException{
		return hashtable.get(word)!=null;
	}
	
	public void finish() throws IOException {
		recman.commit();
		recman.close();
	}
	
	public void addEntry(String key, String title, String url, String lastModifyDate, int size) throws IOException {
		if (hashtable.get(key) != null){
			return;
		}
		hashtable.put(key, new Infm(title, url, lastModifyDate, size));
	}

	public void delEntry(String key) throws IOException{

			hashtable.remove(key);
	}
	
	public String getTitle(String key) throws IOException{
		Infm temp = (Infm)hashtable.get(key);
		if(temp==null) return "null";
		else return temp.title;
	}
	
	public String getUrl(String key) throws IOException{
		Infm temp = (Infm)hashtable.get(key);
		return temp.url;
	}
	
	public String getLastDate(String key) throws IOException{
		Infm temp = (Infm)hashtable.get(key);
		return temp.lastModifyDate;
	}
	
	public int getPageSize(String key) throws IOException{
		Infm temp = (Infm)hashtable.get(key);
		return temp.size;
	}
	
	public void update(String key, String title, String url, String lastModifyDate, int size) throws IOException{
		if (hashtable.get(key) == null){
			return;
		}
		Infm temp = (Infm)hashtable.get(key);
		temp.title = title;
		temp.url = url;
		temp.lastModifyDate = lastModifyDate;
		temp.size = size;
	}
	
//	public void delEntry(String word) throws IOException {
//		hashtable.remove(word);
//	}
	
	public void printAll() throws IOException {
		FastIterator iter = hashtable.keys();
		String key;
		while ((key = (String) iter.next()) != null) {
			Infm temp = (Infm)hashtable.get(key);
			System.out.println(key + ": " + temp.title+","+temp.url+", last modified: "+temp.lastModifyDate+", size: "+temp.size);
		}
	}
}
