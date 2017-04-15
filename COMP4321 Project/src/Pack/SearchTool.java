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
        //get the term weight first into wordAndWeight
        //Then split the ":" into word and weight
        //map for normal weight
        //mapForCalSquare for the square of the weight
        Hashtable<String, Double> map = new Hashtable<String,Double>();
        Hashtable<String, Double> mapForCalSquare = new Hashtable<String,Double>();
        SumOfWeightForEachDoc(keywordValue, map, mapForCalSquare);
        
        Set<String> set = mapForCalSquare.keySet();
        Iterator<String> iterator = set.iterator();
        //calculate the square root of the weight of different doc
        while (iterator.hasNext()) {
            String index = iterator.next();
            mapForCalSquare.put(index, Math.sqrt(mapForCalSquare.get(index)));
        }
        //***************this may can be removed
        set = mapForCalSquare.keySet();
        iterator = set.iterator();
        //***********************
        Vector<Webpage> result = new Vector<Webpage>();
        while (iterator.hasNext()) {
            String index = iterator.next();
            double totalScore = map.get(index)/(mapForCalSquare.get(index) * Math.sqrt(keywordValue.size()));
            result.add(toWebpage(index,totalScore));
        }
        //final calculation
        //score = sum(weight) / ( sqrt(sum(weight^2)) * sqrt(queryLength^2) )
        //                                                 ^note that the query weight is set to be 1 so no need to calculate the square
        //sortting the result in decending order so it can be view and get the best page
        Collections.sort(result);
        return result;
    }

    private void SumOfWeightForEachDoc(Vector<String> keywordValue, Hashtable<String, Double> map, Hashtable<String, Double> mapForCalSquare) throws IOException {
        for(int i = 0; i< keywordValue.size(); i++){
            String[] docIDAndWeight = termWeight.getValue(keywordValue.elementAt(i)).split(" ");
            for(int j = 0; j < docIDAndWeight.length; j++){
                String[] splittedIDAndWeight = docIDAndWeight[j].split(":");
                //splittedIDAndWeight[0] is the docID,
                //splittedIDAndWeight[1] is the weight
                double weightVal = Double.parseDouble(splittedIDAndWeight[1]);
                String docIDString = splittedIDAndWeight[0];
                if(!map.containsKey(docIDString)){
                    double weightSquare = weightVal * weightVal;
                    double weight = weightVal;
                    map.put(docIDString, weight);
                    mapForCalSquare.put(docIDString, weightSquare);
                }else{
                    //the word already exist in the map so add the weight to the map's value
                    double weightSquare = mapForCalSquare.get(docIDString) + weightVal * weightVal;
                    double weight = mapForCalSquare.get(docIDString) + weightVal;
                    map.put(docIDString, weight);
                    mapForCalSquare.put(docIDString, weightSquare);
                }
            }
        }
    }

    public static Webpage toWebpage(String index, Double score) throws IOException{
        Webpage pageResult = new Webpage();
        pageResult.setScore(score);
        pageResult.setTitle(Pageinfm.getTitle(index));
        pageResult.setURL(Pageinfm.getUrl(index));
        pageResult.setLastUpdate(Pageinfm.getLastDate(index));
        pageResult.setPageSize(Pageinfm.getPageSize(index));

        //keywords
        WordsAndWordFreqForPage(index, pageResult);

        //child Links
        String child = ParChild.getValue(index);
        String[] childLinkArray = child.split(" ");
        for(int i = 0; i < childLinkArray.length;i++){
            pageResult.addChildLk(PageIdxr.getValue(childLinkArray[i]));
        }

        //parent Links
        String par = ChildPar.getValue(index);
        String[] parentLinkArray = par.split(" ");
        for(int i = 0; i < parentLinkArray.length;i++){
            pageResult.addParentLk(PageIdxr.getValue(parentLinkArray[i]));
        }


        return pageResult;
    }

    private static void WordsAndWordFreqForPage(String index, Webpage pageResult) throws IOException {
        String WordList = ForwardIdx.getValue(index);
        //temp is docID to keywords
        String[] arrayOfKeywords = WordList.split(" ");
        for(int i = 0; i < arrayOfKeywords.length;i++){
            Word a = new Word();
            a.setText(arrayOfKeywords[i]);
            String docIDAndFreqArray = invertedIdx.getValue(WordIdxr.getIdx(arrayOfKeywords[i]));
            String[] docIDAndFreqPair = docIDAndFreqArray.split(" ");
            for(int j = 0 ; j < docIDAndFreqPair.length;j++){
                String[] docIDAndFreq = docIDAndFreqPair[j].split(":");
                if(index.compareTo(docIDAndFreq[0])==0){
                    a.setFreq(Integer.parseInt(docIDAndFreq[1]));
                    pageResult.addKeyword(a);
                    break;
                }
            }
        }
        pageResult.sortKeyword();
    }
}
