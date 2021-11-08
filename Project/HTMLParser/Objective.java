package HTMLParser;

/**
This is a class for the information of a goal or task.
*/

public class Objective{

  String header;
  String content;


  public Objective(String header, String content){
    this.header = header;
    this.content = content;
  }

  public String getHeader(){
    return header;
  }

  public String getContent(){
    return content;
  }

}
