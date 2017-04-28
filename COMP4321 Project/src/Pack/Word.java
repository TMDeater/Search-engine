package Pack;

import java.util.Vector;

public class Word implements Comparable<Word>{
  private String text;
  private int frequency;
  private Vector<String> docIDAndPosition;

  public Word(){}

  public Word(String text, int freq){
    this.setText(text);
    this.setFreq(freq);
  }

  public Word(String text, String position){
    this.setText(text);
    this.setDocIDAndPosition(position);
  }

  public int compareTo(Word word){
    return word.getFreq() - this.getFreq();
  }
  public void setText(String txt){ this.text = txt;}
  public String getText(){  return text;}
  public void setFreq(int f){  this.frequency = f;}
  public int getFreq(){ return frequency;}

  public void setDocIDAndPosition(String docIDAndPosition){
    this.docIDAndPosition = new Vector<String>();

    String[] splitEachDoc = docIDAndPosition.split("-");
    for (int i=1;i<splitEachDoc.length;i++){
      String[] splitDocIDAndPosition = splitEachDoc[i].split(":");
      String[] position = splitDocIDAndPosition[1].split(" ");
      String docID = splitDocIDAndPosition[0];
      for (int j=0;j<position.length;j++){
        this.docIDAndPosition.add(new String(docID+":"+position[j]));
      }
    }
  }
  public Vector<String> getDocIDAndPosition(){
    return this.docIDAndPosition;
  }

  public static Vector<String> checkTwoWordStickTogether(Word word1,Word word2){
    Vector<String> word1DocIDandPosition = word1.getDocIDAndPosition();
    Vector<String> word2DocIDandPosition = word2.getDocIDAndPosition();
    Vector<String> result = new Vector<>();
    for (String IDWordPair:word1DocIDandPosition){
      String[] splitIDWordPair = IDWordPair.split(":");
      Integer position =Integer.valueOf(splitIDWordPair[1]);
      String nextPositionWord = new String(splitIDWordPair[0]+":"+String.valueOf(position+1));
      if (word2DocIDandPosition.contains(nextPositionWord)){
        result.add(IDWordPair);
      }
    }
    return result;
  }

  public static Vector<String> checkTheyAreStickTogether(Vector<Word> allWord){
    Vector<String> result = new Vector<String>();
    if (allWord.size()==0){ return result;}
    if (allWord.size()==1){ return allWord.get(0).getDocIDAndPosition();}
    else if (allWord.size()==2) { return checkTwoWordStickTogether(allWord.get(0), allWord.get(1));}
    else{
      result = checkTwoWordStickTogether(allWord.get(0), allWord.get(1));
      for (int i=1;i<allWord.size()-1;i++){
        Vector<String> comparedResult = new Vector<>();
        Vector<String> twoWordResult;
        twoWordResult = checkTwoWordStickTogether(allWord.get(i), allWord.get(i+1));
        for (String singleTwoWordResult:twoWordResult){
          //change the singleTwoWordResult to "-i" in position to compare later
          String[] splitIDPosition = singleTwoWordResult.split(":");
          String positionMinusI = String.valueOf( Integer.valueOf(splitIDPosition[1])-i );
          String positionMinusIForCompare = new String(splitIDPosition[0]+":"+positionMinusI);

          if (result.contains(positionMinusIForCompare)){
            //check if the next word pair connected in each document from "result"
            //if contain means the next word pair is still connected and add to compared result
            comparedResult.add(positionMinusIForCompare);
          }
        }
        if (comparedResult.isEmpty()){ return comparedResult;} //empty means not stick together
        else{ result = comparedResult;}
      }
      return result;
    }
  }

  public static void main(String[] args){
    String testword1 ="-12:1 3 8-13:2 4";
    String testword2 ="-12:2-13:5";
    String testword3 ="-11:10-12:3-13:6";

    Word A = new Word("aaa", testword1);
    Word B = new Word("bbb", testword2);
    Word C = new Word("ccc", testword3);

    Vector<Word> wordVector= new Vector<>();
    wordVector.add(A);
    wordVector.add(B);
    wordVector.add(C);

    Vector<String> result = A.checkTheyAreStickTogether(wordVector);

    for (String word: result){
      System.out.println(word);
      System.out.println("\n");
    }

    String test = "iii \"abcde\" abc";
    String[] stringSplitByQuote = test.split("\"");
    Vector<String> quotedString = new Vector<String>();
    Vector<String> nonQuotedString = new Vector<String>();
    for (int i=0;i<stringSplitByQuote.length;i++){
      if ((i%2)==1) {
        quotedString.add(stringSplitByQuote[i]);
      } else {
        nonQuotedString.add(stringSplitByQuote[i]);
      }
    }
    for (String word: quotedString){
      System.out.println(word);
      System.out.println("\n");
    }
    for (String word: nonQuotedString){
      System.out.println(word);
      System.out.println("\n");
    }
  }
}

