package Pack;

public class Word implements Comparable<Word>{
  private String text;
  private int frequency;

  public Word(){}

  public Word(String text, int freq){
    this.setText(text);
    this.setFreq(freq);
  }
  public int compareTo(Word word){
    return word.getFreq - this.getFreq;
  }
  public void setText(String txt){ this.text = txt;}
  public String getText(){  return text;}
  public void setFreq(int f){  this.frequency = f;}
  public int getFreq(){ return frequency;}
}
