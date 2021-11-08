package HTMLParser;
/**
A class for parsing HTML files
**/
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.nio.charset.*;
import java.util.List;
import java.util.ArrayList;


public class HTMLParser{

  public HTMLParser(){
  }


  public Regleringsbrev parseFile(File file){

    String path = file.getPath();
    //System.out.println(path);

    Document doc;
    try {
      doc = Jsoup.parse(file, "UTF-8");
    }
    catch(IOException ioException){
      throw new RuntimeException("IO Exception reading html file",ioException);
    }

    //Remove path specifics to find filename
    String fileName = path.replaceAll("../regleringsbrev/Html/", "");

    //Set main fields of this regleringsbrev
    Regleringsbrev regbrev = new Regleringsbrev(fileName);
    int year = getYear(fileName);
    regbrev.setYear(year);
    regbrev.setReport(getReportName(fileName, year));
    regbrev.setAuthority(getAuthorityName(fileName));

    Element regleringsbrevBody = doc.body().getElementById("RegleringsbrevBody");
    if(regleringsbrevBody == null){
        System.out.println("No regleringsbrevbody found for file: " + fileName);
    }

    //Start looping from first child in regleringsbrevBody
    Element element = regleringsbrevBody.child(0);
    Regleringsbrev newObjectives = parseChildren(element);
    regbrev = concatObjectives(regbrev, newObjectives);

    //Help print for debugging
    //printRegBrev(regbrev);

    return regbrev;

  }

  public Regleringsbrev concatObjectives(Regleringsbrev original, Regleringsbrev toAdd){
    //Add new goals and tasks found
    ArrayList<Objective> newGoals = original.getGoals();
    newGoals.addAll(toAdd.getGoals());
    original.setGoals(newGoals);
    ArrayList<Objective> newTasks = original.getTasks();
    newTasks.addAll(toAdd.getTasks());
    original.setTasks(newTasks);

    return original;
  }

  //Uses recursion to call parseSiblings repeated times on different depths
  public Regleringsbrev parseChildren(Element element){
    Regleringsbrev regbrev = parseSiblings(element);
    while(element != null){
      if(element.childrenSize() > 0){
        Regleringsbrev newObjectives = parseChildren(element.child(0));
        regbrev = concatObjectives(regbrev, newObjectives);
      }
      //Go to next element
      element = element.nextElementSibling();
    }

    return regbrev;
  }

  //Searches for goals and targets among html siblings to element
  public Regleringsbrev parseSiblings(Element element){
    //Variables keeping state while parsing
    Regleringsbrev regbrev = new Regleringsbrev("");
    String lastHeader = "";
    ArrayList<Objective> goals = new ArrayList<Objective>();
    ArrayList<Objective> tasks = new ArrayList<Objective>();
    Boolean isGoal = false;
    Boolean isTask = false;
    Boolean subHeadersAllowed = false;
    int lastHeaderSize = 5;

    //Loop through file
    while(element != null){
      //System.out.println("New element ---------------------");
      //System.out.println(element.text());

      //is this the special case matching <em>Mål<em>?
      if(element.is("div:has(em:matchesOwn(^M.l$))")){
        //System.out.println("em found");
        isGoal = true;
        isTask = false;
        subHeadersAllowed = false;
      }
      //is this the special case with nestled p tags?
      // if(element.select("div:has(p>p)") != null){
      //   //System.out.println("p contains p");
      //   if(element.childrenSize() > 0 && (isGoal || isTask)){
      //     Regleringsbrev newObjectives = parseSiblings(element.child(0), isGoal, isTask, subHeadersAllowed, lastHeaderSize);
      //     regbrev = concatObjectives(regbrev, newObjectives);
      //   }
      // }

      //Is this a header?
      if(element.is("div:has(h3)") || element.is("div:has(h2)") || element.is("h3")){

        //System.out.println("header found");
        Elements headers = element.select("h3, h2");

        //Check if this matches <h3>Mål</h3> or <h3> Mål 1</h3>
        if(headers.is("h3:matchesOwn(M.l(\\s\\d)?)")){
        //System.out.println("Found <h3>Mål</h3>");
          isGoal = true;
          isTask = false;
          lastHeaderSize = 3;
          subHeadersAllowed = false;
        }
        //Check if this matches <h2>Övriga mål och återrapporteringskrav</h2>
        else if(headers.is("h2:matchesOwn(.vriga m.l och .terrapporteringskrav)")){
          //System.out.println("Found <h2>Övriga mål och återrapporteringskrav</h2>");
          isGoal = true;
          isTask = false;
          lastHeaderSize = 2;
          subHeadersAllowed = true;
          lastHeader = "Övriga mål och återrapporteringskrav";
        }
        //Check if this matches <h2>Mål och återrapporteringskrav</h2>
        else if(headers.is("h2:matchesOwn(M.l och .terrapporteringskrav)")){
          //System.out.println("Found <h2>Mål och återrapporteringskrav</h2>");
          isGoal = true;
          isTask = false;
          lastHeaderSize = 2;
          subHeadersAllowed = true;
          lastHeader = "Mål och återrapporteringskrav";
        }
         //Check if this matches <h2>Uppdrag</h2>
        else if(headers.is("h2:matchesOwn(^Uppdrag$)")){
          //System.out.println("Found <h2>Uppdrag</h2>");
          isGoal = false;
          isTask = true;
          lastHeaderSize = 2;
          subHeadersAllowed = true;
          lastHeader = "Uppdrag";

        }
        //This is the case for random headers which content we might want to save.
        else{
          //Could this be a sub-header or should we switch context?
          if(headers.last().is("h3")){
            //System.out.println("Found sub-header");
            if(3 < lastHeaderSize){
              //System.out.println("less than");
              //System.out.println(headers.last().text());
              isGoal = false;
              isTask = false;
              subHeadersAllowed = false;
            }
            if((3 == lastHeaderSize) && (!subHeadersAllowed)){
              //System.out.println("equals");
              //System.out.println(headers.last().text());
              isGoal = false;
              isTask = false;
              subHeadersAllowed = false;
            }
            lastHeaderSize = 3;

          } else if(headers.last().is("h2")){
            isGoal = false;
            isTask = false;
            subHeadersAllowed = false;
            lastHeaderSize = 2;
          }
          else{
            lastHeaderSize = 5;
          }
          if(!headers.last().ownText().equals("Återrapportering")){
            lastHeader = headers.last().ownText();
          }
        }
      }

      //Is this text that should be saved?
      else if(isGoal || isTask){
        Elements pcontent = element.children().select("p");
        if(pcontent != null){
          List<String> pStrings = pcontent.eachText();
          //loop through the text boxes
          for(int i = 0; i < pStrings.size(); i++){
            String text = pStrings.get(i);
            if(text.equals("")){
              continue;
            }
            else if(text.matches("M.l")){
              isGoal = true;
              continue;
            }
            else if(text.matches(".terrapporteringskrav")){
              isGoal = false;
              continue;
            }
            if(isGoal || isTask){
              Objective obj = new Objective(lastHeader, text);
              if(isGoal){
                // System.out.println("Goal found");
                // System.out.println("Header: " + lastHeader);
                // System.out.println(text);
                // System.out.println("");
                goals.add(obj);
              }
              else if(isTask){
                // System.out.println("Task found");
                // System.out.println("Header: " + lastHeader);
                // System.out.println(text);
                // System.out.println("");
                tasks.add(obj);
              }
            }
          }
        }
      }

      //Go to next element
      element = element.nextElementSibling();
      //System.out.println(element.text());
      //System.out.println("-------------");

    }
    regbrev.setGoals(goals);
    regbrev.setTasks(tasks);

    return regbrev;
  }


  //Gets the year from filename.
  public int getYear(String fileName){
    String[] splitUrl = fileName.split("_", 2);
    int year = Integer.parseInt(splitUrl[0]);
    return year;
  }

  //Gets the filename of corresponding report given filename of relgeringsbrev and year
  public String getReportName(String fileName, int year){
    fileName = fileName.split("_", 2)[1];
    int nextYear = year + 1;
    String report = String.valueOf(nextYear) + "_" + fileName;
    return report;
  }

  //Get the name of the authority given fileName
  public String getAuthorityName(String fileName){
    String authority = fileName.split("_")[1];
    return authority;
  }

  //Help print for debugging
  public void printRegBrev(Regleringsbrev regbrev){
    System.out.println(regbrev.getFileName());
    System.out.println(regbrev.getReport());
    System.out.println(regbrev.getYear());
    System.out.println(regbrev.getAuthority());

    ArrayList<Objective> goals = regbrev.getGoals();
    System.out.println("------------------GOALS--------------------");
    for(int i = 0; i < goals.size(); i++){
      System.out.println(goals.get(i).getHeader());
      System.out.println(goals.get(i).getContent());
      System.out.println("");
    }
    System.out.println("------------------TASKS--------------------");
    ArrayList<Objective> tasks = regbrev.getTasks();
    for(int i = 0; i < tasks.size(); i++){
      System.out.println(tasks.get(i).getHeader());
      System.out.println(tasks.get(i).getContent());
      System.out.println("");
    }

  }

}
