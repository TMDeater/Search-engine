package Pack;
import org.htmlparser.util.ParserException;

import Pack.IndexTool;
import Pack.InvertedIndex;
import Pack.PageInfm;
import Pack.StemStop;
import Pack.*;
import java.io.*;
import java.util.Vector;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;

public class Extract_db {
	private static final int MAX = 30;
	private static int numOfPage = 0;
	private static StemStop stopStem = new StemStop("D:/Search-engine/COMP4321 Project/src/Pack/stopwords.txt");
	private static Vector<String> TaskList = new Vector<String>();
	private static Vector<String> DoneList = new Vector<String>();
	private static IndexTool PageIndexer;
	private static IndexTool WordIndexer;
	private static IndexTool TitleIndexer;
	private static InvertedIndex inverted;
	private static InvertedIndex ForwardIndex;
	private static InvertedIndex pagePro;
	private static InvertedIndex ChildParent;
	private static FileWriter fstream;
	private static BufferedWriter out;
	private static RecordManager recman;
	private static InvertedIndex ParentChild;
	private static PageInfm Pageppt;
	private static IndexTool maxTermFreq;
	private static InvertedIndex termWth;
	private static InvertedIndex titleForwardIndex;
	private static InvertedIndex titleInverted;
	private static IndexTool titleMaxTermFreq;
	
	
	public static void main(String[] args) throws IOException, ParserException {
		recman = RecordManagerFactory.createRecordManager("D:/Search-engine/COMP4321 Project/public_html/database");
		//recman = RecordManagerFactory.createRecordManager("/Users/JasonPoon/Documents/workspaceForJ2EE/final/database");
		
		PageIndexer = new IndexTool(recman, "page");
		WordIndexer = new IndexTool(recman, "word");
		TitleIndexer = new IndexTool(recman, "title");
		
		//word index
		inverted = new InvertedIndex(recman, "invertedIndex");
		ForwardIndex = new InvertedIndex(recman, "ForwardIndex");
		ChildParent = new InvertedIndex(recman, "ParentChild");
		ParentChild = new InvertedIndex(recman, "PC");
		Pageppt  = new PageInfm(recman, "PPT");
		fstream = new FileWriter("spider_result.txt");
		out = new BufferedWriter(fstream);
		maxTermFreq = new IndexTool(recman, "maxTermFreq");
		termWth = new InvertedIndex(recman, "termWth");
		titleForwardIndex = new InvertedIndex(recman, "titleFI");
		titleInverted = new InvertedIndex(recman, "titleI");
		titleMaxTermFreq = new IndexTool(recman, "titleMaxTermFreq");
		
		
		int printedpage=0;
		int i=0;
		while( printedpage < 30){		
			String iString = String.valueOf(i);
			String pageidxval=PageIndexer.getValue(iString);
			int idx = PageIndexer.getIdxNumber(pageidxval);
			if (Pageppt.getTitle(Integer.toString(idx)).equals("null")){
				i++;
				continue;
			}
			else{
				fetch(pageidxval);
				printedpage++;
				i++;
			}
		}

			
		
		//Close the output stream
		out.close();
		recman.close();
	}
	
	public static void fetch(String url) throws IOException{
		int index = PageIndexer.getIdxNumber(url);
		
		System.out.println(Pageppt.getTitle(Integer.toString(index)));
		out.append(Pageppt.getTitle(Integer.toString(index))+"\n");
		System.out.println(Pageppt.getUrl(Integer.toString(index)));
		out.append(Pageppt.getUrl(Integer.toString(index))+"\n");
		System.out.println(Pageppt.getLastDate(Integer.toString(index))+","+Pageppt.getPageSize(Integer.toString(index)));
		out.append(Pageppt.getLastDate(Integer.toString(index))+","+Pageppt.getPageSize(Integer.toString(index))+"\n");

		
		String WordList = ForwardIndex.getValue(String.valueOf(index));
		String[] temp = WordList.split(" ");
		for(int i = 0; i < temp.length;i++){
			System.out.print(temp[i]+" ");
			out.append(temp[i]+" ");
			String tempID = WordIndexer.getIdx(temp[i]);
			String str = inverted.getValue(tempID);
			String[] temp2 = str.split(" ");
			for(int j = 0 ; j < temp2.length;j++){
				String[] temp3 = temp2[j].split(":");
				int num = Integer.parseInt(temp3[0]);
				if(index == num){
					System.out.print(" "+temp3[1]+"; ");
					out.append(" "+temp3[1]+"; ");
					break;
				}
			}
		}
		System.out.println();
		out.append("\n");
		
		String child = ParentChild.getValue(String.valueOf(index));
		temp = child.split(" ");
		for(int i = 0; i < temp.length;i++){
			System.out.println(PageIndexer.getValue(temp[i]));
			out.append(PageIndexer.getValue(temp[i])+"\n");
		}
		
		System.out.println("-------------------------------------------------------------------------------------------");
		out.append("-------------------------------------------------------------------------------------------\n");
	}
}
