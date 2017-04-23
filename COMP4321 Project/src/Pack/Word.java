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

  public Word(String text, String[] position){
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
  public void setDocIDAndPosition(String[] docIDAndPosition){
    this.docIDAndPosition = new Vector<String>();

    for (int i=0;i<docIDAndPosition.length;i++){
      String[] splitDocIDAndPosition = docIDAndPosition[i].split(":");
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
    for (String IDWordPair:word2DocIDandPosition){
      if (word1DocIDandPosition.contains(IDWordPair)){

      }
    }
  }

  public String checkTheyAreStickTogether(Vector<Word> allWord){

  }
}
