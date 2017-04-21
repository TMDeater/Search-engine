package Pack;

import org.apache.commons.codec.binary.Base64;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import javax.swing.text.Document;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

public class Crawl{

    private String url;
    private String username;
    private String password;

  public Crawl(String url){
    this.url = url;
    username = new String("wwngaa");
    password = new String("gdtgxjh00");
  }

    public String getUrl() {
        return url;
    }

  public int pageSize() throws IOException{
    URL website = new URL(url);
    HttpURLConnection webconnect = (HttpURLConnection) website.openConnection();
    handleUSTLogin(webconnect);
    Reader reader=new InputStreamReader(webconnect.getInputStream(), "utf-8");
    BufferedReader buffer = new BufferedReader(reader);
    String readLine;
    String now = "";
    while ((readLine = buffer.readLine()) != null){
      now = now + readLine;
    }
    buffer.close();
    //close before return
    return now.length();
  }

  public String lastUpdate() throws IOException, ParseException{
//    String[] now = url.split ("://");
//    //get the connection to url
//    URL u = new URL("http", now[1], 80 , "/");
      try {
          URL urlTypeUrl = new URL(url);
          HttpURLConnection urlConnect = (HttpURLConnection) urlTypeUrl.openConnection();
          handleUSTLogin(urlConnect);
          DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
          java.util.Date lastdate = new java.util.Date(urlConnect.getLastModified());
          if (urlConnect.getLastModified() == 0) {

              Reader reader = new InputStreamReader(urlConnect.getInputStream(), "utf-8");
              BufferedReader buffer = new BufferedReader(reader);
              String inLine;
              String now = "";
              while ((inLine = buffer.readLine()) != null) {
                  if (inLine.contains("Last updated on")) {
                      String[] parts = inLine.split(" ");
                      String date = parts[3];
                      SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd");
                      SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");
                      String reformatStr = myFormat.format(fromUser.parse(date));
                      return reformatStr;
                  } else {
                      //String slastdate = new String("21-04-2017");
                      String timeStamp = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());
                      return timeStamp;
                  }
              }
              lastdate = new Date(System.currentTimeMillis());
          }
          return dateFormat.format(lastdate);
      } catch (Exception e) {
          //String slastdate = new String("21-04-2017");
          String timeStamp = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());
          return timeStamp;
      }
  }

  public Vector<String> extractWords() throws ParserException, IOException

  {
  		// extract words in url and return them
  		// use StringTokenizer to tokenize the result from StringBean
  		// ADD YOUR CODES HERE
        org.jsoup.nodes.Document words= Jsoup.parse(new URL(url).openStream(), "utf-8", url);
        String text = words.body().text();
        //text=text.replaceAll("[.,?@»'|()]", "");
      Vector<String> result = splitAndPutInVector(text);
//          Vector<String> result = new Vector<String>();
//  		StringBean bean = new StringBean();
//  		bean.setURL(url);
//  		bean.setLinks(false);
//  		String contents = bean.getStrings();
//  		StringTokenizer st = new StringTokenizer(contents);
//  		while (st.hasMoreTokens()) {
//  		    result.add(st.nextToken());
//  		}
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


    //filter the titletag
    //check the nodelist into an array
    //check the singleNode is titletag and put it into variable called line
    //split the title by " " and return vector
  public Vector<String> getTitle() throws ParserException{
    Parser pars = new Parser(url);
    pars.setEncoding("UTF-8");
    NodeFilter filt = new NodeClassFilter(TitleTag.class);
    NodeList nodeLst = pars.parse(filt);
    Node[] node = nodeLst.toNodeArray();
    String line ="";
    for (int i=0;i<node.length; i++){
      Node sgNode = node[i];
        line = findTheTitleTagInNode(line, sgNode);
    }
      Vector<String> vector = splitAndPutInVector(line);
    return vector;
  }

    private String findTheTitleTagInNode(String line, Node sgNode) {
        if (sgNode instanceof TitleTag){
          TitleTag title = (TitleTag) sgNode;
          line = title.getTitle();
        }
        return line;
    }

    @NotNull
    private Vector<String> splitAndPutInVector(String line) {
        String[] string = line.split(" ");
        Vector<String> vector = new Vector<String>();
        for(int k=0; k< string.length;k++){
          vector.add(string[k]);
        }
        return vector;
    }

    public void handleUSTLogin(HttpURLConnection connect) throws MalformedURLException {
        URL url = new URL(this.url);
        try{
            connect = (HttpURLConnection)url.openConnection();
            connect.setRequestMethod("GET");
            connect.connect();
            int code = connect.getResponseCode();
            if (code==401 && this.url.contains("ust")) {
                System.out.println("401/n");
                connect = (HttpURLConnection) url.openConnection();
                String namePD = new String(username+":"+password);
                String encoding = new String(Base64.encodeBase64(namePD.getBytes()));
                connect.setRequestProperty( "Authorization","Basic "+encoding);
                connect.connect();
                Authenticator.setDefault (new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication ("wwngaa", "gdtgxjh00".toCharArray());
                    }
                });
            }
        }catch(Exception ex){
            System.out.println("UnknownHostException encountered.");
        }
    }

    public static void main (String[] args)
  {
    try
    {
      Crawl crawler = new Crawl("http://www.cse.ust.hk?lang=hk");


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
              } catch (IOException e) {
        e.printStackTrace();
    }

  }

}
