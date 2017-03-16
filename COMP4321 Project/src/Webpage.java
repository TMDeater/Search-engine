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

	public int compareWith(Webpage webpage){
		double difference = this.score - webpage.getScore();
		if (difference>0.0)	{return -1;}
		if (difference<0.0)	{return 1;}
		else				{return 0;}
		//1:less score ; 0:equal score ; -1:higher score
	}
