package Pack;

import java.util.HashSet;
import java.io.*;

public class StemStop{
  private HashSet stopwd;
  private Porter porter;

  public boolean isStopWord(String string){
    return stopwd.contains(string);
  }

//constructor
  public StemStop(String string){
    super();
    try{
      porter = new Porter();
      stopwd = new HashSet();
//readd stopword.txt file and put in HashSet
      FileInputStream filestream = new FileInputStream(string);
      DataInputStream input = new DataInputStream(filestream);
      BufferReader buffer = new BufferReader(new InputStreamReader(input));
      String strLine;
      while ((strLine = buffer.readLine()) != null){
        stopwd.add(strLine);
      }
      input.close();
    }
    catch(Exception exception){
      System.err.println("Error Found: "+exception.getMessage());
    }
  }

  public String stem(String string){
    return porter.stripAffixes(string);
  }

//run in main
  public static void main(String[] arg){
    StemStop stopStem = new StemStop("stopwords.txt");
		String input="";
		try{
			do
			{
				System.out.print("Please enter a single English word: ");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				input = in.readLine();
				if(input.length()>0)
				{
					if (stopStem.isStopWord(input))
						System.out.println("It should be stopped");
					else
			   			System.out.println("The stem of it is \"" + stopStem.stem(input)+"\"");
				}
			}
			while(input.length()>0);
		}
		catch(IOException ioe)
		{
			System.err.println(ioe.toString());
    }
  }
}
