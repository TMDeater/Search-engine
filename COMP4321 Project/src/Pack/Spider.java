package Pack;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class Spider {
	private static final int MAX = 30;
	private static int numOfPage = 0;
	private static StemStop stopStem = new StemStop("COMP4321 Project/src/Pack/stopwords.txt");
	private static Vector<String> TodoList = new Vector<String>();
	private static Vector<String> DoneList = new Vector<String>();
	private static IndexTool PageIndexer;
	private static IndexTool WordIndexer;
	private static IndexTool FullWordIndexer;
	private static IndexTool TitleIndexer;
	private static InvertedIndex titleInverted;
	private static InvertedIndex wordInverted;
	private static InvertedIndex wordForward;
	private static InvertedIndex fullWordInverted;
	private static InvertedIndex fullWordForward;
	private static InvertedIndex ChildParent;
	private static RecordManager recman;
	private static InvertedIndex ParentChild;
	private static PageInfm PageProperty;
	private static IndexTool maxTermFreq;
	private static InvertedIndex termW;

	public static void main(String[] args) throws IOException, ParseException{
		
		try
		{
			recman = RecordManagerFactory.createRecordManager("COMP4321 Project/public_html/database");
			PageIndexer = new IndexTool(recman, "page");
			WordIndexer = new IndexTool(recman, "word");
			FullWordIndexer = new IndexTool(recman, "fullWord");
			TitleIndexer = new IndexTool(recman, "title");
			titleInverted = new InvertedIndex(recman, "titleInvertedIndex");
			wordInverted = new InvertedIndex(recman, "invertedIndex");
			wordForward = new InvertedIndex(recman, "ForwardIndex");
			fullWordInverted = new InvertedIndex(recman, "fullInvertedIndex");
			fullWordForward = new InvertedIndex(recman, "fullForwardIndex");
			ChildParent = new InvertedIndex(recman, "ParentChild");
			ParentChild = new InvertedIndex(recman, "PC");
			PageProperty = new PageInfm(recman, "PPT");
			maxTermFreq = new IndexTool(recman, "maxTermFreq");
			termW = new InvertedIndex(recman, "termW");

            //wordInverted.printAll();

			System.out.println("load in webpage...");
			fetchPages("https://course.cse.ust.hk/comp4321/labs/TestPages/testpage.htm");
			while(!TodoList.isEmpty() && numOfPage < MAX){
				if(DoneList.contains(TodoList.firstElement())){
					TodoList.removeElementAt(0);
					continue;
				}
//				else if (TodoList.firstElement().contains("http://www.cse.ust.hk/ug/hkust_only") ){
//					TodoList.removeElementAt(0);
//					int pageIndex = PageIndexer.getIdxNumber("http://www.cse.ust.hk/ug/hkust_only");
//					wordForward.delEntry(Integer.toString(pageIndex));
//					PageProperty.delEntry(Integer.toString(pageIndex));
//					//numOfPage--;
//					continue;
//				}
//				else if (TodoList.firstElement().contains("http://www.cse.ust.hk/pg/hkust_only") ){
//					TodoList.removeElementAt(0);
//					int pageIndex = PageIndexer.getIdxNumber("http://www.cse.ust.hk/pg/hkust_only");
//					wordForward.delEntry(Integer.toString(pageIndex));
//					PageProperty.delEntry(Integer.toString(pageIndex));
//					//numOfPage--;
//					continue;
//				}
				else if(fetchable(TodoList.firstElement())==false){
					TodoList.removeElementAt(0);
					int pageIndex = PageIndexer.getIdxNumber(TodoList.firstElement());
					wordForward.delEntry(Integer.toString(pageIndex));
					PageProperty.delEntry(Integer.toString(pageIndex));
					System.out.println("Exception encountered or HTTP response other than 200 for: "+TodoList.firstElement());
					//numOfPage--;
					continue;
				}
				else{
					fetchPages(TodoList.firstElement());
					TodoList.removeElementAt(0);
				}
			}

			//calculate termWeight
			calculateAllTermWeight();
			
			recman.commit();
			//PageProperty.printAll();

			recman.close();
			System.out.println("\nFinished");
		}
		catch (ParserException e)
		{
			e.printStackTrace ();
		}
	}

	private static void calculateAllTermWeight() throws IOException {
		FastIterator iterator =  wordInverted.AllKey();
		String invertedIndexKey;
		while ((invertedIndexKey = (String) iterator.next()) != null) {
            //documentFrequency is the number of document contain the word_t
			int documentFrequency = wordInverted.numOfElement(invertedIndexKey);
            for(int i = 0; i < documentFrequency ; i++){
                //temp[0] is the word
                //temp[1] is the termfrequency
                //get the max term frequency from the getIndexNumber(word)
                String[] temp = wordInverted.getElement(invertedIndexKey, i).split(":");
                int maxTermFrequency = maxTermFreq.getIdxNumber(temp[0]);
                //As the termfrequency get from the database is string, so we need to change the string to interger
                int termFrequency = Integer.parseInt(temp[1]);
                double weight = calculateTermWeight(termFrequency, maxTermFrequency, documentFrequency, MAX);
                termW.addEntry2(invertedIndexKey, temp[0]+":"+weight);
            }
        }
	}

	public static boolean fetchable(String link) throws IOException {
		URL url = new URL(link);
		try{
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			int code = connection.getResponseCode();
			if (code==200) return true;
			else return false;
		}catch(UnknownHostException ex){
			System.out.println("UnknownHostException encountered.");
			return false;
		}

	}
	
	public static void fetchPages(String url) throws ParserException, IOException, ParseException{
		System.out.println(url);
		DoneList.add(url);
		numOfPage++;
		
		//crawler
		Crawl crawler = new Crawl(url);

        //extract last update
        String date = crawler.lastUpdate();

        //extract pagesize
        int pageSize = crawler.pageSize();

		//link
		Vector<String> links = crawler.extractLinks();
		if (!links.isEmpty()){
            int j=0;
            do{
                //checkAndAddList(links, j);
				if(!DoneList.contains(links.elementAt(j))){
					TodoList.add(links.elementAt(j));
				}else{
					links.removeElementAt(j);
					j--;
				}
                j++;
            }while(j<links.size());
        }



		int pgidx;
		
		//check contain or not
		if(PageIndexer.isContain(url) && PageProperty.isContain(PageIndexer.getIdx(url))){
			pgidx = PageIndexer.getIdxNumber(url);
			String temp_date = PageProperty.getLastDate(Integer.toString(pgidx));
			if(date.compareTo(temp_date)==0){
				System.out.println("Same as data stored...");
				return;
			}else{
				System.out.println("update information...");
				//update if last modification date are not same
				String text = wordForward.getValue(Integer.toString(pgidx));
				String fulltext = fullWordForward.getValue(Integer.toString(pgidx));
				String[] temp = text.split(" ");
				for(int i = 0; i < temp.length; i++){
					System.out.println(temp[i]);
				}
				wordForward.delEntry(Integer.toString(pgidx));
				fullWordForward.delEntry(Integer.toString(pgidx));
				PageProperty.delEntry(Integer.toString(pgidx));
			}
		}else{
			System.out.println("NewPage...");
			pgidx = PageIndexer.addEntry(url, Integer.toString(PageIndexer.getLastIdx()));
		}
		
		//extract word
		Vector<String> words = crawler.extractWords();
		Hashtable<Integer, Integer> map = new Hashtable<Integer,Integer>(); 
		for(int i = 0; i < words.size(); i++){
			if (!stopStem.isStopWord(words.get(i))){
				String temp = stopStem.stem(words.get(i));
				int index = WordIndexer.addEntry(temp, Integer.toString(WordIndexer.getLastIdx()));
				//Inverted-file
				addFreqOrNew(map, index);
				//forward
				wordForward.addEntry2(pgidx+"", temp);
			}
		}

		//extract all words
		Vector<String> allWords = crawler.extractWords();
		Hashtable<Integer, String> allWordMap = new Hashtable<Integer,String>();
		for (int i=0;i<allWords.size();i++){
			String word = allWords.elementAt(i);
			if (word.length()>2){
				if (!((word.charAt(0) >= 'a' && word.charAt(0) <= 'z')||(word.charAt(0) >= 'A' && word.charAt(0) <= 'Z'))){
					System.out.println("cut first char "+word);
					word = word.substring(1);
					allWords.set(i,word);
				}
				if (!((word.charAt(word.length()-1) >= 'a' && word.charAt(word.length()-1) <= 'z')||
						(word.charAt(word.length()-1) >= 'A' && word.charAt(word.length()-1) <= 'Z'))){
					System.out.println("cut last char "+word);
					word = word.substring(0,word.length()-1);
					allWords.set(i,word);
				}
			}
		}
		for (int i=0; i<allWords.size(); i++){
			int index = FullWordIndexer.addEntry(allWords.get(i), Integer.toString(FullWordIndexer.getLastIdx()));
			fullAddFreqOrNew(allWordMap, index, i);
			fullWordForward.addEntry2(pgidx+"", allWords.get(i));
		}

		//find max freq in a doc
		Set<Integer> set = map.keySet();
	    Iterator<Integer> itr = set.iterator();
		int max = findMaxFreq(pgidx, map, itr);
	    maxTermFreq.addEntry(Integer.toString(pgidx), Integer.toString(max));

	    //set inverted index for full word
		Set<Integer> fullWordSet =allWordMap.keySet();
		Iterator<Integer> fullitr = fullWordSet.iterator();
		while (fullitr.hasNext()) {
			int idx = fullitr.next();
			String position = allWordMap.get(idx);
			fullWordInverted.fullWordAddEntry(idx+"", pgidx, position);
		}
	    
	    
		//title
		StemStop stopStem = new StemStop("COMP4321 Project/src/Pack/stopwords.txt");
		String title = "";
		try{
			Vector<String> titleWords = crawler.getTitle();

			if (titleWords.firstElement()!=""){
				for(int i = 0; i < titleWords.size(); i++){
					title += titleWords.elementAt(i);
                    stopStemCheckAndPutInTitleIndex(stopStem, titleWords, i, pgidx);
                }
			}
		}catch(ParserException ex){
			title = " ";
		}



		PageProperty.addEntry(Integer.toString(pgidx), title, url, date, pageSize);

        UpdateChildParentRelationship(links, pgidx);

		TodoList.addAll(links);
	}

    public static void UpdateChildParentRelationship(Vector<String> links, int pgidx) throws IOException {
        int k=0;
        while (k < links.size()){
            int pageId = PageIndexer.addEntry(links.elementAt(k),Integer.toString(PageIndexer.getLastIdx()) );
            ChildParent.addEntry2(Integer.toString(pageId),Integer.toString(pgidx));
            ParentChild.addEntry2(Integer.toString(pgidx), Integer.toString(pageId));
            k++;
        }
    }

    public static void stopStemCheckAndPutInTitleIndex(StemStop stopStem, Vector<String> titleWords, int i, int pgidx) throws IOException {
        if (!stopStem.isStopWord(titleWords.get(i))){
            int iwordIndex=TitleIndexer.addEntry(stopStem.stem(titleWords.get(i)), Integer.toString(TitleIndexer.getLastIdx()));
        	String wordIndex = Integer.toString(iwordIndex);
            titleInverted.addEntry2(wordIndex, Integer.toString(pgidx));
            //titleInverted.printAll();
            System.out.println("add "+titleWords.get(i)+" for "+pgidx);
        }
    }

    public static int findMaxFreq(int pgidx, Hashtable<Integer, Integer> map, Iterator<Integer> itr) throws IOException {
		int max = 0;
		while (itr.hasNext()) {
          int idx = itr.next();
          int num = map.get(idx);
          wordInverted.addEntry(idx+"", pgidx, num);
          if (num>max)	max=num;
          else 			; //do nothing
        }
		return max;
	}

	public static void addFreqOrNew(Hashtable<Integer, Integer> map, int index) {
		if(map.containsKey(index)){
            map.put(index, map.get(index) + 1);
        }else{
            map.put(index, 1);
        }
	}

	public static void fullAddFreqOrNew(Hashtable<Integer, String> map, int index, int position){
		if(map.containsKey(index)){
			map.put(index, new String(map.get(index) + " " + Integer.toString(position)));
		}else{
			map.put(index, Integer.toString(position));
		}
	}

	public static void checkAndAddList(Vector<String> links, int i) {
		if(!DoneList.contains(links.elementAt(i))){
            TodoList.add(links.elementAt(i));
        }else{
            links.removeElementAt(i);
        }
	}

	public static double calculateTermWeight(double tf, double maxTf, double numOfDoc, double maxOfDoc){
		double idf = Math.log(maxOfDoc/numOfDoc)/Math.log(2);
		return (tf*idf)/maxTf;
	}
}
