package Pack;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

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
    private static IndexTool WordIdxr;
    private static IndexTool FullWordIdxr;
    private static IndexTool TitleIdxr;
    private static InvertedIndex invertedIdx;
    private static InvertedIndex ForwardIdx;
    private static InvertedIndex fullWordInverted;
    private static InvertedIndex fullWordForward;
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
        stopStem = new StemStop("COMP4321 Project/src/Pack/stopwords.txt");
        TaskList = new Vector<String>();
        DoneList = new Vector<String>();
        recManager = RecordManagerFactory.createRecordManager("COMP4321 Project/public_html/database");

        loadFromDatabase();
    }

    private void loadFromDatabase() throws IOException {
        PageIdxr = new IndexTool(recManager, "page");
        WordIdxr = new IndexTool(recManager, "word");
        FullWordIdxr = new IndexTool(recManager, "fullWord");
        TitleIdxr = new IndexTool(recManager, "title");

        //index for word
        //load from the database
        titleInverted = new InvertedIndex(recManager, "titleInvertedIndex");
        invertedIdx = new InvertedIndex(recManager, "invertedIndex");
        ForwardIdx = new InvertedIndex(recManager, "ForwardIdx");
        fullWordInverted = new InvertedIndex(recManager, "fullInvertedIndex");
        fullWordForward = new InvertedIndex(recManager, "fullForwardIndex");
        ChildPar = new InvertedIndex(recManager, "ParChild");
        ParChild = new InvertedIndex(recManager, "PC");
        Pageinfm = new PageInfm(recManager, "PPT");
        maxTermFreq = new IndexTool(recManager, "maxTermFreq");
        termWeight = new InvertedIndex(recManager, "termW");
    }

    public Vector<Webpage> search(Vector<String> keywords) throws IOException{
        //check if the keyword is stopword and add into keyword value
        Vector<String> keywordValue = new Vector<String>();
        Vector<String> quoteKeywordValue = new Vector<String>();

        //pharse search=====================================
        String initialString = "";
        //group up the query to be : abc "ahg" ddfh
        //one space is at the beginning
        for (String keyword: keywords){
            initialString=initialString+" "+keyword;
        }
        //check contain two "
        int count = StringUtils.countMatches(initialString,"\"");
        if ((count%2)==0 && count!=0){
            Hashtable<String, Double> map = new Hashtable<String,Double>();
            Hashtable<String, Double> mapForCalSquare = new Hashtable<String,Double>();

            //handle have quote text
            String[] stringSplitByQuote = initialString.split("\"");
            Vector<String> quotedString = new Vector<String>();
            Vector<String> nonQuotedString = new Vector<String>();
            for (int i=0;i<stringSplitByQuote.length;i++){
                if ((i%2)==1) {
                    quotedString.add(stringSplitByQuote[i]);
                } else {
                    nonQuotedString.add(stringSplitByQuote[i]);
                }
            }
            //ideal quotedString[0]: "bbb ccc hhh"
            for (int i = 0; i<quotedString.size();i++) {
                String[] splitQuoteString = quotedString.get(i).split(" ");

                for (int j = 0; j < splitQuoteString.length; j++) {
                    String wordInQuotedString = new String(splitQuoteString[j]);
                    //remove non char at front and end of the string
                    if (wordInQuotedString.length() > 1) {
                        if (!((wordInQuotedString.charAt(0) >= 'a' && wordInQuotedString.charAt(0) <= 'z') || (wordInQuotedString.charAt(0) >= 'A' && wordInQuotedString.charAt(0) <= 'Z'))) {
                            splitQuoteString[j] = wordInQuotedString.substring(1);
                        }
                        if (!((wordInQuotedString.charAt(wordInQuotedString.length() - 1) >= 'a' && wordInQuotedString.charAt(wordInQuotedString.length() - 1) <= 'z') ||
                                (wordInQuotedString.charAt(wordInQuotedString.length() - 1) >= 'A' && wordInQuotedString.charAt(wordInQuotedString.length() - 1) <= 'Z'))) {
                            splitQuoteString[j] = wordInQuotedString.substring(0, wordInQuotedString.length() - 1);
                        }
                    }
                    //load the word index first
                    if (!(FullWordIdxr.getIdx(wordInQuotedString).equals("-1"))) {
                        quoteKeywordValue.add(FullWordIdxr.getIdx(wordInQuotedString));
                    } else {
                        //database do not have the query word so the database must not have result
                        return new Vector<Webpage>();
                    }
                }

                Vector<Word> wordVector = new Vector<>();
                for (String quoteWordIndex : quoteKeywordValue) {
                    String docIDandPositionCombine = fullWordInverted.getFullWordDocIDandPosition(quoteWordIndex);
                    wordVector.add(new Word(WordIdxr.getValue(quoteWordIndex), docIDandPositionCombine));
                }
                Vector<String> docContainingWordVector = Word.checkTheyAreStickTogether(wordVector);

//                for (String docPair : docContainingWordVector) {
//                    System.out.println(docPair);
//                }

                //count the occur phrase in each document
                Hashtable<String, Double> quotedStringWeightMap = new Hashtable<>();
                for (String docIDPositionPair:docContainingWordVector){
                    String[] splitPair = docIDPositionPair.split(":");
                    if (quotedStringWeightMap.containsKey(splitPair[0])){
                        Double DocCount = quotedStringWeightMap.get(splitPair[0]);
                        quotedStringWeightMap.replace(splitPair[0],DocCount+1);
                    } else {
                        quotedStringWeightMap.put(splitPair[0], 1.0);
                    }
                }
                Set<String> set = quotedStringWeightMap.keySet();
                Iterator<String> iterator = set.iterator();
                //normalize the quoted string weight
                while (iterator.hasNext()) {
                    String index = iterator.next();
                    double normalisedWeight = quotedStringWeightMap.get(index)/docContainingWordVector.size();
                    if (map.containsKey(index)){
                        map.put(index, map.get(index)+normalisedWeight);
                        mapForCalSquare.put(index, mapForCalSquare.get(index)+(normalisedWeight*normalisedWeight));
                    } else {
                        map.put(index, normalisedWeight+1);
                        mapForCalSquare.put(index, normalisedWeight*normalisedWeight+1);
                    }
                }
            }
            Set<String> quoteSet = mapForCalSquare.keySet();
            Iterator<String> quoteIterator = quoteSet.iterator();
            //calculate the square root of the weight of different doc
            while (quoteIterator.hasNext()) {
                String index = quoteIterator.next();
                mapForCalSquare.put(index, Math.sqrt(mapForCalSquare.get(index)));
            }
            //==================================finish find phrase,rest is simple search
            //String a = nonQuotedString.get(0);

            if (nonQuotedString.size()==1 && nonQuotedString.get(0).equals(" ")) {
                Vector<Webpage> result = finalCalculateFormula(keywordValue.size()+quotedString.size(), map, mapForCalSquare);
                Collections.sort(result);
                return result;
            }

            Vector<String> splitNonQuotedString = new Vector<>();
            for (int l=0;l<nonQuotedString.size();l++){
                String[] split = nonQuotedString.elementAt(l).split(" ");
                for (int p=0;p<split.length;p++){
                    if (split[p].equals("")||split[p].equals(" ")) {    continue;}
                    splitNonQuotedString.add(split[p]);
                }
            }
            nonQuotedString=splitNonQuotedString;
            for (int p=0;p<nonQuotedString.size();p++){
                String word = nonQuotedString.elementAt(p);
                if (word.equals("")||word.equals(" ")){ continue;}
                if(!stopStem.isStopWord(word)){
                    String a = stopStem.stem(word);
                    keywordValue.add(WordIdxr.getIdx(stopStem.stem(word)));
                }
            }
            SumOfWeightForEachDoc(keywordValue, map, mapForCalSquare);
            FindTitle(nonQuotedString, map, mapForCalSquare);

            Set<String> set = mapForCalSquare.keySet();
            Iterator<String> iterator = set.iterator();
            //calculate the square root of the weight of different doc
            while (iterator.hasNext()) {
                String index = iterator.next();
                mapForCalSquare.put(index, Math.sqrt(mapForCalSquare.get(index)));
            }
            Vector<Webpage> result = finalCalculateFormula(keywordValue.size()+quotedString.size(), map, mapForCalSquare);
            Collections.sort(result);
            return result;

        }


        int keywordNumber=0;
        while (keywordNumber< keywords.size()){
            String word = keywords.elementAt(keywordNumber);
            if (!stopStem.isStopWord(word)){
                if (!(WordIdxr.getIdx(stopStem.stem(word)).equals("-1"))){
                    keywordValue.add(WordIdxr.getIdx(stopStem.stem(word)));
                }
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
        FindTitle(keywords, map, mapForCalSquare);

        Set<String> set = mapForCalSquare.keySet();
        Iterator<String> iterator = set.iterator();
        //calculate the square root of the weight of different doc
        while (iterator.hasNext()) {
            String index = iterator.next();
            mapForCalSquare.put(index, Math.sqrt(mapForCalSquare.get(index)));
        }
        //***************this may can be removed
        Vector<Webpage> result = finalCalculateFormula(keywordValue.size(), map, mapForCalSquare);
        //final calculation
        //score = sum(weight) / ( sqrt(sum(weight^2)) * sqrt(queryLength^2) )
        //                                                 ^note that the query weight is set to be 1 so no need to calculate the square
        //sortting the result in decending order so it can be view and get the best page

        Collections.sort(result);
        return result;
    }

    @NotNull
    private Vector<Webpage> finalCalculateFormula(Integer keywordValueSize, Hashtable<String, Double> map, Hashtable<String, Double> mapForCalSquare) throws IOException {
        Set<String> set;
        Iterator<String> iterator;
        set = mapForCalSquare.keySet();
        iterator = set.iterator();
        //***********************
        Vector<Webpage> result = new Vector<Webpage>();
        while (iterator.hasNext()) {
            String index = iterator.next();
            double sum_d = map.get(index);
            double sumsq_d = mapForCalSquare.get(index);
            double sqrt_q = Math.sqrt(keywordValueSize);
            double totalScore = map.get(index)/(mapForCalSquare.get(index) * Math.sqrt(keywordValueSize));
            result.add(toWebpage(index,totalScore));
        }
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
                    double weight = map.get(docIDString) + weightVal;
                    map.put(docIDString, weight);
                    mapForCalSquare.put(docIDString, weightSquare);
                }
            }
        }
    }
    private void FindTitle(Vector<String> keyword, java.util.Hashtable<String, Double> map, java.util.Hashtable<String, Double> mapForCalSquare) throws IOException {
        for (int i = 0; i< keyword.size(); i++){
            System.out.println(stopStem.stem(keyword.get(i)));
            String titleWordID = TitleIdxr.findTitleWordID(stopStem.stem(keyword.get(i)));
            String[] docIDString = titleInverted.getDocIDForTitle(titleWordID).split(" ");
            if (docIDString[0].equals("-1")) {
                continue;
            }
            for (int j =0; j<docIDString.length; j++){
                if(!map.containsKey(docIDString[j])){
                    double weight=1.0;
                    map.put (docIDString[j], weight);
                    mapForCalSquare.put(docIDString[j], weight);
                }else{
                    //title word alredy exist in the map
                    double weight = map.get(docIDString[j]) + 1;
                    double weightSquare = mapForCalSquare.get(docIDString[j]) + 1;
                    map.put(docIDString[j], weight);
                    mapForCalSquare.put(docIDString[j], weightSquare);
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
