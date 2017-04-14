package Pack;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by MSI on 2017/4/14.
 */
public class SearchTool {
    private  StemStop stopStem;
    private Vector<String> TaskList;
    private  Vector<String> DoneList;
    private static IndexTool PageIdxr;
    private static  IndexTool WordIdxr;
    private static IndexTool TitleIdxr;
    private static  InvertedIndex invertedIdx;
    private static  InvertedIndex ForwardIdx;
    private static InvertedIndex pagePro;
    private static InvertedIndex ChildPar;
    private static RecordManager recManager;
    private static InvertedIndex ParChild;
    private static  PageInfm Pageinfm;
    private static IndexTool maxTermFreq;
    private static InvertedIndex termWeight;
    private static InvertedIndex titleForwardIndex;
    private static InvertedIndex titleInverted;
    private static IndexTool titleMaxTermFreq;

    public SearchTool() throws IOException {
        stopStem = new StemStop("/COMP4321 Project/src/stopwords.txt");
        TaskList = new Vector<String>();
        DoneList = new Vector<String>();
        recManager = RecordManagerFactory.createRecordManager("/COMP4321 Project/public_html/database");

        loadFromDatabase();
    }

    private void loadFromDatabase() throws IOException {
        PageIdxr = new IndexTool(recManager, "page");
        WordIdxr = new IndexTool(recManager, "word");
        TitleIdxr = new IndexTool(recManager, "title");

        //index for word
        //load from the database
        invertedIdx = new InvertedIndex(recManager, "invertedIndex");
        ForwardIdx = new InvertedIndex(recManager, "ForwardIdx");
        ChildPar = new InvertedIndex(recManager, "ParChild");
        ParChild = new InvertedIndex(recManager, "PC");
        Pageinfm = new PageInfm(recManager, "PPT");
        maxTermFreq = new IndexTool(recManager, "maxTermFreq");
        termWeight = new InvertedIndex(recManager, "termWth");
    }

    public Vector<Webpage> search(Vector<String> keywords) throws IOException{
        //check if the keyword is stopword and add into keyword value
        Vector<String> keywordValue = new Vector<String>();
        int keywordNumber=0;
        while (keywordNumber< keywords.size()){
            String word = keywords.elementAt(keywordNumber);
            if (!stopStem.isStopWord(word)){
                keywordValue.add(WordIdxr.getIdx(stopStem.stem(word)));
            }
            keywordNumber++;
        }

        //loop weight
        Hashtable<String, Double> map = new Hashtable<String,Double>();
        Hashtable<String, Double> map2 = new Hashtable<String,Double>();
        for(int i = 0; i< keywordValue.size(); i++){
            String[] temp = termWeight.getValue(keywordValue.elementAt(i)).split(" ");
            for(int j = 0; j < temp.length; j++){
                String[] temp2 = temp[j].split(":");
                if(!map.containsKey(temp2[0])){
                    double v2 = Double.parseDouble(temp2[1])*Double.parseDouble(temp2[1]);
                    double v1 = Double.parseDouble(temp2[1]);
                    map.put(temp2[0], v1);
                    map2.put(temp2[0], v2);
                }else{
                    double v2 = map2.get(temp2[0]) + Double.parseDouble(temp2[1])*Double.parseDouble(temp2[1]);
                    double v1 = map2.get(temp2[0]) + Double.parseDouble(temp2[1]);
                    map.put(temp2[0], v1);
                    map2.put(temp2[0], v2);
                    //System.out.println(v1 + " "+v2);
                }
            }
        }
        Set<String> set = map2.keySet();
        Iterator<String> itr = set.iterator();
        while (itr.hasNext()) {
            String index = itr.next();
            map2.put(index, Math.sqrt(map2.get(index)));
        }
        set = map2.keySet();
        itr = set.iterator();
        Vector<Webpage> result = new Vector<Webpage>();
        while (itr.hasNext()) {
            String index = itr.next();
            double totalScore = map.get(index)/(map2.get(index) * Math.sqrt(keywordValue.size()));
            //System.out.println(map.get(index)+", "+map2.get(index)+", "+map3.get(index)+"\n"+totalScore);
            result.add(toWebpage(index,totalScore));
        }
        Collections.sort(result);
        return result;
    }

    public static Webpage toWebpage(String index, Double score) throws IOException{
        Webpage result = new Webpage();
        result.setScore(score);
        result.setTitle(Pageinfm.getTitle(index));
        result.setURL(Pageinfm.getUrl(index));
        result.setLastUpdate(Pageinfm.getLastDate(index));
        result.setPageSize(Pageinfm.getPageSize(index));

        //keywords
        String WordList = ForwardIdx.getValue(index);
        String[] temp = WordList.split(" ");
        for(int i = 0; i < temp.length;i++){
            Word a = new Word();
            a.setText(temp[i]);
            String str = invertedIdx.getValue(WordIdxr.getIdx(temp[i]));
            String[] temp2 = str.split(" ");
            for(int j = 0 ; j < temp2.length;j++){
                String[] temp3 = temp2[j].split(":");
                if(index.compareTo(temp3[0])==0){
                    a.setFreq(Integer.parseInt(temp3[1]));
                    result.addKeyword(a);
                    break;
                }
            }
        }
        result.sortKeyword();

        //child Links
        String child = ParChild.getValue(index);
        temp = child.split(" ");
        for(int i = 0; i < temp.length;i++){
            result.addChildLk(PageIdxr.getValue(temp[i]));
        }

        //parent Links
        String par = ChildPar.getValue(index);
        temp = par.split(" ");
        for(int i = 0; i < temp.length;i++){
            result.addParentLk(PageIdxr.getValue(temp[i]));
        }


        return result;
    }
}
