package HTMLParser;

import java.util.ArrayList;
import java.io.*;


public class Main{

  public static void main(String[] args) {

    ArrayList<Regleringsbrev> regbrev = new ArrayList<Regleringsbrev>();
    HTMLParser parser = new HTMLParser();
    File dir = new File("../regleringsbrev/Html");
    if(dir.canRead() && dir.isDirectory()){
      File[] files = dir.listFiles();
      //Call parser for all files
      for(int i = 0; i < files.length; i++ ){
        regbrev.add(parser.parseFile(files[i]));
      }
    }
    else{
      System.out.println("something wrong with path to files");
    }
    // System.out.println("Number of files found: ");
    // System.out.println(regbrev.size());
    // System.out.println("");
    //
    // System.out.println("Files with no goals or tasks: ");
    //
    // int count = 0;
    // for(int i = 0; i < regbrev.size(); i++){
    //   Regleringsbrev brev = regbrev.get(i);
    //   if((brev.getGoals().size() == 0) && (brev.getTasks().size() == 0 )){
    //     System.out.println(brev.getFileName());
    //     count++;
    //   }
    // }
    // System.out.println("Number of files missing goals and tasks");
    // System.out.println(count);


    // HTMLParser parser = new HTMLParser();
    // File file = new File("../regleringsbrev/Html/2006_Affärsverket svenska kraftnät_2021004284.html");
    // parser.parseFile(file);
  }
}
