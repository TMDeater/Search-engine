package Pack;
import java.io.IOException;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;

import org.htmlparser.util.ParserException;

public class Spider {
	private static final int MAX = 300;
	private static int numOfPage = 0;
	private static StemStop stopStem = new StemStop("D:/Search-engine/COMP4321 Project/src/Pack/stopwords.txt");
	private static Vector<String> TodoList = new Vector<String>();
	private static Vector<String> DoneList = new Vector<String>();
	private static IndexTool PageIndexer;
	private static IndexTool WordIndexer;
	private static IndexTool TitleIndexer;
	private static InvertedIndex wordInverted;
	private static InvertedIndex wordForward;
	private static InvertedIndex ChildParent;
	private static RecordManager recman;
	private static InvertedIndex ParentChild;
	private static PageInfm PageProperty;
	private static IndexTool maxTermFreq;

	public static void main(String[] args) throws IOException, ParseException{
		
		try
		{
			//recman = RecordManagerFactory.createRecordManager("/comp4321/khpoon/public_html/database");
			recman = RecordManagerFactory.createRecordManager("D:/Search-engine/COMP4321 Project/public_html/database");
			PageIndexer = new IndexTool(recman, "page");
			WordIndexer = new IndexTool(recman, "word");
			TitleIndexer = new IndexTool(recman, "title");
			wordInverted = new InvertedIndex(recman, "invertedIndex");
			wordForward = new InvertedIndex(recman, "ForwardIndex");
			ChildParent = new InvertedIndex(recman, "ParentChild");
			ParentChild = new InvertedIndex(recman, "PC");
			PageProperty = new PageInfm(recman, "PPT");
			maxTermFreq = new IndexTool(recman, "maxTermFreq");

			System.out.println("load in webpage...");
			fetchPages("http://www.cse.ust.hk");
			while(!TodoList.isEmpty() && numOfPage < MAX){
				if(DoneList.contains(TodoList.firstElement())){
					TodoList.removeElementAt(0);
					continue;
				}
				else if (TodoList.firstElement().contains("http://www.cse.ust.hk/ug/hkust_only") ){
					TodoList.removeElementAt(0);
					int pageIndex = PageIndexer.getIdxNumber("http://www.cse.ust.hk/ug/hkust_only");
					wordForward.delEntry(Integer.toString(pageIndex));
					PageProperty.delEntry(Integer.toString(pageIndex));
					//numOfPage--;
					continue;
				}
				else if (TodoList.firstElement().contains("http://www.cse.ust.hk/pg/hkust_only") ){
					TodoList.removeElementAt(0);
					int pageIndex = PageIndexer.getIdxNumber("http://www.cse.ust.hk/pg/hkust_only");
					wordForward.delEntry(Integer.toString(pageIndex));
					PageProperty.delEntry(Integer.toString(pageIndex));
					//numOfPage--;
					continue;
				}
				else{
					fetchPages(TodoList.firstElement());
					TodoList.removeElementAt(0);
				}
				
			}
			
			recman.commit();
			PageProperty.printAll();
			recman.close();
			System.out.println("\nFinished");
		}
		catch (ParserException e)
		{
			e.printStackTrace ();
		}
	}
	
	public static double termWeight(double tf, double maxTf, double numOfDoc, double maxOfDoc){
		double idf = Math.log(maxOfDoc/numOfDoc)/Math.log(2);
		return (tf*idf)/maxTf;
	}
	
	public static void fetchPages(String url) throws ParserException, IOException, ParseException{
		System.out.println(url);
		DoneList.add(url);
		numOfPage++;
		
		//crawler
		Crawl crawler = new Crawl(url);
		Vector<String> links = crawler.extractLinks();
		for(int i = 0; i < links.size(); i++){
			if(!DoneList.contains(links.elementAt(i))){
				TodoList.add(links.elementAt(i));
			}else{
				links.removeElementAt(i);
			}
		}
		int pageIndex;
		
		//check contain or not
		if(PageIndexer.isContain(url) && PageProperty.isContain(PageIndexer.getIdx(url))){
			pageIndex = PageIndexer.getIdxNumber(url);
			String date = crawler.lastUpdate();
			String date2 = PageProperty.getLastDate(Integer.toString(pageIndex));
			if(date.compareTo(date2)==0){
				System.out.println("Same as data stored...");
				return;
			}else{
				System.out.println("update information...");
				//update if last modification date are not same
				String text = wordForward.getValue(Integer.toString(pageIndex));
				String[] temp = text.split(" ");
				for(int i = 0; i < temp.length; i++){
					System.out.println(temp[i]);
				}
				wordForward.delEntry(Integer.toString(pageIndex));
				PageProperty.delEntry(Integer.toString(pageIndex));
			}
		}else{
			System.out.println("NewPage...");
			pageIndex = PageIndexer.addEntry(url, Integer.toString(PageIndexer.getLastIdx()));
		}
		
		//extract word
		Vector<String> words = crawler.extractWords();
		Hashtable<Integer, Integer> map = new Hashtable<Integer,Integer>(); 
		for(int i = 0; i < words.size(); i++){
			if (!stopStem.isStopWord(words.get(i))){
				String temp = stopStem.stem(words.get(i));
				int index = WordIndexer.addEntry(temp, Integer.toString(WordIndexer.getLastIdx()));
				//Inverted-file index
				if(!map.containsKey(index)){
					map.put(index, 1);
				}else{
					
					map.put(index, map.get(index) + 1);
				}
				//forward index
				wordForward.addEntry2(pageIndex+"", temp);
			}
		}
		Set<Integer> set = map.keySet();
	    Iterator<Integer> itr = set.iterator();
	    int max = 0;
	    while (itr.hasNext()) {
	      int index = itr.next();
	      int num = map.get(index);
	      wordInverted.addEntry(index+"", pageIndex, num);
	      max = num > max ? num : max;
	    }
	    maxTermFreq.addEntry(Integer.toString(pageIndex), Integer.toString(max));
	    
	    
		//extract title
		StemStop stopStem = new StemStop("D:/Search-engine/COMP4321 Project/src/Pack/stopwords.txt");
		Vector<String> titleWords = crawler.getTitle();
		String title = "";
		if (titleWords.firstElement()!=""){
			for(int i = 0; i < titleWords.size(); i++){
				title += titleWords.elementAt(i);
				if (!stopStem.isStopWord(titleWords.get(i))){
					int index = TitleIndexer.addEntry(stopStem.stem(titleWords.get(i)), Integer.toString(TitleIndexer.getLastIdx()));
				}
			}
		}
		
		//extract last update
		String date = crawler.lastUpdate();
		
		//extract pagesize
		int pageSize = crawler.pageSize();
		PageProperty.addEntry(Integer.toString(pageIndex), title, url, date, pageSize);
		for(int i = 0; i < links.size(); i++){
			int pageId = PageIndexer.addEntry(links.elementAt(i),Integer.toString(PageIndexer.getLastIdx()) );
			ChildParent.addEntry2(Integer.toString(pageId),Integer.toString(pageIndex));
			ParentChild.addEntry2(Integer.toString(pageIndex), Integer.toString(pageId));
		}
		TodoList.addAll(links);
	}
}
