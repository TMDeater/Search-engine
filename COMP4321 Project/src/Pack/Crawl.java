package Pack;

import java.text.*;
import java.util.*;

import org.htmlparser.beans.StringBean;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.*;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.beans.LinkBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.htmlparser.tags.*;

public class Crawl{
  private String url;
  public Crawl(String url){
    this.url = url;
  }

  public int pageSize() throws IOException{
    URL website = new URL(url);
    URLConnection webconnect = website.openConnection();
    BufferReader buffer = new BufferReader(
                          new InputStreamReader(
                          webconnect.getInputStream()
                          )
                          );
    String inLine;
    String now = "";
    while (inLine = buffer.readLine()) != null){
      now = now + inLine;
    }
    buffer.close();
    //close before return
    return now.length();
  }

  public String lastUpdate() throws IOException{
    String[] now = url.split ("://");
    //get the connection to url
    URL u = new URL("http", now[1], 80 , "/");
    URLConnection urlConnect = u.openConnection();
    DataFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    Date lastdate = new Date(urlConnect.getLastModified());
    return dateFormat.format(lastdate);
  }

  public Vector<String> extractWords() throws ParserException

  	{
  		// extract words in url and return them
  		// use StringTokenizer to tokenize the result from StringBean
  		// ADD YOUR CODES HERE
  		Vector<String> result = new Vector<String>();
  		StringBean bean = new StringBean();
  		bean.setURL(url);
  		bean.setLinks(false);
  		String contents = bean.getStrings();
  		StringTokenize st = new StringTokenizer(contents);
  		while (st.hasMoreTokens()) {
  		    result.add(st.nextToken());
  		}
  		return result;
  	}
	public Vector<String> extractLinks() throws ParserException

	{
		// extract links in url and return them
		// ADD YOUR CODES HERE
		Vector<String> result = new Vector<String>();
		LinkBean bean = new LinkBean();
		bean.setURL(url);
		URL[] urls = bean.getLinks();
		for (URL s : urls) {
		    result.add(s.toString());
		}
		return result;
	}


  public Vector<String> getTitle() throws ParserException{
    Parser pars = new Parser(url);
    NodeFilter filt = new NodeClassFilter(TitleTag.class);
    NodeList nodeLst = pars.parse(filt);
    Node[] node = nodeLst.toNodeArray();
    String line ="";
    for (int i=0;i<node.length; i++){
      Node sgNode = node[i];
      if (sgNode instanceof TitleTag){
        TitleTag title = (TitleTag) node;
        line = title.getTitle();
      }
    }
    String[] string = line.split(" ");
    Vector<String> vector = new Vector<String>();
    for(int k=0; k< string.length;k++){
      vector.add(string[k]);
    }
    return vec;
  }

  public static void main (String[] args)
  {
    try
    {
      Crawl crawler = new Crawl("http://www.cs.ust.hk/~dlee/4321/");


      Vector<String> words = crawler.extractWords();

      System.out.println("Words in "+crawler.url+":");
      for(int i = 0; i < words.size(); i++)
        System.out.print(words.get(i)+" ");
      System.out.println("\n\n");



      Vector<String> links = crawler.extractLinks();
      System.out.println("Links in "+crawler.url+":");
      for(int i = 0; i < links.size(); i++)
        System.out.println(links.get(i));
      System.out.println("");

    }
    catch (ParserException e)
              {
                  e.printStackTrace ();
              }

  }

}
