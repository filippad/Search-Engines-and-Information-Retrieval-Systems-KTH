package HTMLParser; /**
This is a class for holding all of the information from a regleringsbrev that should be indexed
*/

import java.util.ArrayList;

public class Regleringsbrev{

  String fileName;
  String authority;
  int year;
  String report;
  ArrayList<Objective> goals = new ArrayList<Objective>();
  ArrayList<Objective> tasks = new ArrayList<Objective>();


  public Regleringsbrev(String fileName){
    this.fileName = fileName;
  }

  //Set-methods below

  public void setAuthority(String authority){
    this.authority = authority;
  }

  public void setYear(int year){
    this.year = year;
  }

  public void setReport(String report){
    this.report = report;
  }

  public void setGoals(ArrayList<Objective> goals){
    this.goals = goals;
  }

  public void setTasks(ArrayList<Objective> tasks){
    this.tasks = tasks;
  }


  //Get-methods below

  public String getFileName(){
    return fileName;
  }

  public String getAuthority(){
    return authority;
  }

  public int getYear(){
    return year;
  }

  public String getReport(){
    return report;
  }

  public ArrayList<Objective> getGoals(){
    return goals;
  }

  public ArrayList<Objective> getTasks(){
    return tasks;
  }

}
