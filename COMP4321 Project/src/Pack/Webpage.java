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
	private Vector<Vocab> keyword;

	public Webpage(){
		ParentLk = new Vector<String>();
		ChildLk = new Vector<String>();
		keyword = new Vector<Vocab>();
	}

	public String getURL(){	return url;}
	public void setURL(String url){	this.url=url;}
	public String getTitle(){return title;}
	public void setTitle(String title){ this.title=title}
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
	public void addKeyword(Vocab word){	keyword.add(word);}
	public Vector<Vocab> getKeyword(){	return keyword;}
	public void sortKeyword(){	Collections.sort(keyword);}

	public int compareWith(Webpage webpage){
		double difference = this.score - webpage.getScore();
		if (difference>0.0)	{return -1;}
		if (difference<0.0)	{return 1;}
		else				{return 0;}
		//1:less score ; 0:equal score ; -1:higher score
	}

	public String showInfm(){
		String infm = score+"\t";result += title+"\n";
		result += "\t"+url+"\n";
		result += "\t"+lastUpdate+","+pageSize+"\n";
		result += "\t";
		for(int i = 0; i < keyword.size(); i++){
			Vocab word = keyword.elementAt(i);
			result += word.getText()+" "+word.getFreq()+"; ";
		}
		result+="\n";
		result+="\t";
		for(int i = 0; i < PLink.size(); i++){
			result += PLink.elementAt(i)+"\n";
		}
		result+="\t";
		for(int i = 0; i < CLink.size(); i++){
			result += CLink.elementAt(i)+"\n";
		}
		return result+"\n";
	}
