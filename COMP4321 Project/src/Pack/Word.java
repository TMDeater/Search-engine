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

  public Vector<String> checkTwoWordStickTogether(Word word1,Word word2){
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

  public Vector<String> checkTheyAreStickTogether(Vector<Word> allWord){
    Vector<String> result = new Vector<String>();
    return result;
  }

  public static void main(String[] args){
    String testword1 ="-12:1 3 8-13:2 4";
    String testword2 ="-12:2";
    String testword3 ="-11:10";

    Word A = new Word("aaa", testword1);
    Word B = new Word("bbb", testword2);
    Word C = new Word("ccc", testword3);

    Vector<String> result = A.checkTwoWordStickTogether(A, B);
    for (String word: result){
      System.out.println(word);
      System.out.println("\n");
    }
  }
}

