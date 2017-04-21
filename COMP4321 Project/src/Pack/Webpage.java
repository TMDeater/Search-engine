package Pack;

import java.util.Collections;
import java.util.Vector;

public class Webpage implements Comparable<Webpage>{
	private String url;
	private String title;
	private double score;
	private int pageSize;
	private String lastUpdate;
	private Vector<String> ParentLk;
	private Vector<String> ChildLk;
	private Vector<Word> keyword;

	public Webpage(){
		ParentLk = new Vector<String>();
		ChildLk = new Vector<String>();
		keyword = new Vector<Word>();
	}

	public void copy(Webpage web){
		setURL(new String(web.getURL()));
		setTitle(new String(web.getTitle()));
		setLastUpdate(new String(web.getLastUpdate()));
		setPageSize(web.getPageSize());
		setScore(web.getScore());

	}

	public String getURL(){	return url;}
	public void setURL(String url){	this.url=url;}
	public String getTitle(){return title;}
	public void setTitle(String title){ this.title=title;}
	public double getScore(){	return score;}
	public void setScore(double score){	this.score=score;}
	public int getPageSize(){	return pageSize;}
	public void setPageSize(int size){	this.pageSize=size;}
	public String getLastUpdate(){	return lastUpdate;}
	public void setLastUpdate(String date){	this.lastUpdate=date;}
	public void addParentLk(String link){	ParentLk.add(link);}
	public Vector<String> getParentLk(){	return ParentLk;}
	public void addChildLk(String link){	ChildLk.add(link);}
	public Vector<String> getChildLk(){	return ChildLk;}
	public void addKeyword(Word word){	keyword.add(word);}
	public Vector<Word> getKeyword(){	return keyword;}
	public void sortKeyword(){	Collections.sort(keyword);}

	public int compareTo(Webpage webpage){
		double difference = this.score - webpage.getScore();
		if (difference>0.0)	{return -1;}
		if (difference<0.0)	{return 1;}
		else				{return 0;}
		//1:less score ; 0:equal score ; -1:higher score
	}
//this is a function reserve for future if need
//	public String showInfm(){
//		String infm = score+"\t";infm += title+"\n";
//		infm += "\t"+url+"\n";
//		infm += "\t"+lastUpdate+","+pageSize+"\n";
//		infm += "\t";
//		for(int i = 0; i < keyword.size(); i++){
//			Word word = keyword.elementAt(i);
//			infm += word.getText()+" "+word.getFreq()+"; ";
//		}
//		infm+="\n";
//		infm+="\t";
//		for(int i = 0; i < ParentLk.size(); i++){
//			infm += ParentLk.elementAt(i)+"\n";
//		}
//		infm+="\t";
//		for(int i = 0; i < ChildLk.size(); i++){
//			infm += ChildLk.elementAt(i)+"\n";
//		}
//		return infm+"\n";
//	}

//	@Override
//	public int compareTo(Webpage o) {
//		// TODO Auto-generated method stub
//		return new Double(score).compareTo( o.score);
//
//	}
}
