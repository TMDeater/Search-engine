//it is the Indexer
//finalize() is changed to finish()
//getLastIndex -> getLastIdx
//getIndex -> getIdx

package Pack;

import jdbm.RecordManager;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;
import java.io.IOException;

public class IndexTool{
  private RecordManager recman;
  private HTree hashtable1;
  private HTree hashtable2;
  private int lastIdx;

  public IndexTool(RecordManager recordingManger, String objName) throws IOException{
    recman = recordingManger;

    long recid1 = recman.getNamedObject(objName+"1");
    long recid2 = recman.getNamedObject(objName+"2");
    long recid3 = recman.getNamedObject(objName+"Size");

//憒�ecord���店
    if (recid1 != 0){
        loadHashtableAndSetLastIndex(recid1, recid2, recid3);
    }else{
        createTwoHashtable(objName);
        long recid4 = recman.insert(new Integer(0));
        recman.setNamedObject(objName+"Size",recid4);
    }
  }

    private void createTwoHashtable(String objName) throws IOException {
        hashtable1 = HTree.createInstance(recman);
        recman.setNamedObject(objName+"1", hashtable1.getRecid());
        hashtable2 = HTree.createInstance(recman);
        recman.setNamedObject(objName+"2", hashtable2.getRecid());
    }

    private void loadHashtableAndSetLastIndex(long recid1, long recid2, long recid3) throws IOException {
        hashtable1 = HTree.load(recman, recid1);
        hashtable2 = HTree.load(recman, recid2);
        lastIdx = (Integer)recman.fetch(recid3);
    }

    public int getLastIdx(){  return lastIdx; }

  public void finish() throws IOException{
    recman.commit();
    recman.close();
  }

//for checking the hash table have the word or not
  public boolean isContain(String wd) throws IOException{
		return hashtable1.get(wd)!=null;
	}

  public int addEntry(String word, String word2) throws IOException {
		// Add a "docX Y" entry for the key "word" into hashtable
		// ADD YOUR CODES HERE
		// Test "word" in index file.true stop insertion and return directly.
		if (hashtable1.get(word)!=null)
		{
			return getIdxNumber(word);
		}
    // false, insert to the inverted file;
	  putInForwardAndBackwardHashtable(word, word2);
	  lastIdx++;
	  return  Integer.parseInt(word2);
	}

	private void putInForwardAndBackwardHashtable(String word, String word2) throws IOException {
		hashtable1.put(word, word2);
		hashtable2.put(word2, word);
	}

	public void delEntry(String word) throws IOException {
		// Delete the word and its list from the hashtable
		// ADD YOUR CODES HERE
		if (hashtable1.get(word)!=null)lastIdx--;{
			hashtable2.remove(hashtable1.get(word));
			hashtable1.remove(word);

		}

	}

	public String getIdx(String word) throws IOException{
		if(hashtable1.get(word) == null){
			return "-1";
		}
		return String.valueOf(hashtable1.get(word));
	}

	public int getIdxNumber(String word) throws IOException{
		if(hashtable1.get(word) == null){
			return -1;
		}
		return new Integer((String)hashtable1.get(word));
	}

	public String getValue(String index) throws IOException{
		if(hashtable2.get(index) == null){
			return "-1";
		}
		return String.valueOf(hashtable2.get(index));
	}

	public int getValueNumber(String index) throws IOException{
		if(hashtable2.get(index) == null){
			return -1;
		}
		return new Integer((String)hashtable2.get(index));
	}
	
	public FastIterator Allkeys() throws IOException{
		return hashtable1.keys();
	}

	public String findTitleWordID(String titleword) throws IOException {
		//printAll();
		if (titleword=="") {return "-1";}
    	FastIterator iterator = this.Allkeys();
		String invertedIndexKey;
		String result = "";
		while ((invertedIndexKey = (String) iterator.next()) != null){
			if (invertedIndexKey.contains(titleword)){
				String docID = getIdx(invertedIndexKey);
				if (result.equals("")){
					result = docID;
				}else {
					result = result + " " + docID;
				}
			}
		}
		return result;
	}

  	public void printAll() throws IOException {
  		// Print all the data in the hashtable
  		// ADD YOUR CODES HERE
  		FastIterator i = hashtable1.keys();
  		String key;
  		while ((key = (String) i.next()) != null) {
  			System.out.println(key + ": " + hashtable1.get(key));
  		}

  	}
}
