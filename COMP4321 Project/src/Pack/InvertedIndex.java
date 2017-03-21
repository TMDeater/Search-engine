//finalize() -> finish()
package Pack;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.util.Vector;
import java.io.IOException;
import java.io.Serializable;

public class InvertedIndex
{
    private RecordManager recman;
    private HTree hashtable;

    InvertedIndex(String recordmanager, String objectname) throws IOException
    {
        recman = RecordManagerFactory.createRecordManager(recordmanager);
        long recid = recman.getNamedObject(objectname);

        if (recid != 0)
            hashtable = HTree.load(recman, recid);
        else
        {
            hashtable = HTree.createInstance(recman);
            recman.setNamedObject( objectname, hashtable.getRecid() );
        }
    }

    public void finish() throws IOException
    {
        recman.commit();
        recman.close();
    }

    public void addEntry(String word, int x, int y) throws IOException {
      // Add a "docX Y" entry for the key "word" into hashtable
  		if (hashtable.get(word)!=null && ((String) hashtable.get(word)).contains( "doc" + x+ " " + y))
  		{
  			return;
  		}
  		String new_entry = x + ":" + y + " ";
  		//因為"put"會cover previous insertion,
  		// 要extract existed data;
  		String existed_entry = "";
  		if (hashtable.get(word) != null) {
  			existed_entry = (String) hashtable.get(word);
  		}
  		hashtable.put(word, existed_entry + new_entry);
      //而家就好似append入尾咁
  	}

    public void addEntry2(String word, String value) throws IOException {
      // Add a "value" entry for the key "word" into hashtable

      if (hashtable.get(word)!=null && ((String) hashtable.get(word)).contains(value))
      {
        return;
      }
      String new_entry = value + " ";
      String existed_entry = "";
      if (hashtable.get(word) != null) {
        existed_entry = (String) hashtable.get(word);
      }
      hashtable.put(word, existed_entry + new_entry);
    }

    public void delEntry(String word) throws IOException {
  		// Delete the word and its list from the hashtable
  		// ADD YOUR CODES HERE
  		hashtable.remove(word);
  	}

    public int numOfElement(String word) throws IOException{
  		if (hashtable.get(word) != null) {
  			String wordEntry = (String) hashtable.get(word);
        //get "word" list and count
  			String[] temp = wordEntry.split(" ");
  			return temp.length;
  		}
  		else  return 0;
  	}

  	public String getElement(String word, int index) throws IOException{
  		if (hashtable.get(word) != null) {
  			String wordEntry = (String) hashtable.get(word);
  			String[] temp = wordEntry.split(" ");
  			return temp[index];
  		}
  		else  return null;
  	}

  	public void delElement(String word, String new_element) throws IOException{
  		String wordEntry = "";
  		if (hashtable.get(word) != null) {
  			wordEntry = (String) hashtable.get(word);
  		}
  		String newstr = wordEntry.replaceAll("\\s["+new_element+"]*\\s", " ");
  		hashtable.put(word, newstr);
  	}

  	public FastIterator AllKey() throws IOException{
  		return hashtable.keys();
  	}

  	public void printAll() throws IOException {
  		// Print all the data in the hashtable
  		FastIterator i = hashtable.keys();
  		String key;
  		int num = 0;
  		while ((key = (String) i.next()) != null) {
  			System.out.println(key + ": " + hashtable.get(key));
  			num++;
  		}
  	}

}
class PostingElement implements Serializable {
	public String document;
	public int frequency;

	PostingElement(String doc, int freq) {
		this.document = doc;
		this.frequency = freq;
	}
}
