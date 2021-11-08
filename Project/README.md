# Fyndighet - A Swedish democracy search tool
Every year the Swedish government issues instructions("regleringsbrev") to its agencies
and specifies goals and tasks that the agencies should reach during the year. When the year ends, each agency will send
annual report (“årsredovisning”) to the government to detail what has been achieved. This repository is an 
application to search for goals and tasks that the government has given to specific agencies 
as well as to find the reports that are linked to those tasks and goals. 

This is a project in the course DD2476 Search Engines and Information Retrieval Systems at KTH  Royal Institute of Technology.

## Data and data indexing
The project is hosted by server on an Amazon cloud web service until 18 May 2020 where the data has already
been processed and indexed. If you wish to run the application after the date mentioned above, please contact us
by sending email to one of the addresses: isander@kth.se, hultber@kth.se, fmodigh@kth.se, krehn@kth.se.

In order to display pdf-files, a directory of all pdf-files is required. It has to be located 
in the project directory and must furthermore be named **ESV-data**.

## Project setup using Homebrew and Intellij
1. If you have not already installed Maven, run the following commands in Terminal
    ```console
    $ brew update
    $ brew install maven
    ```
2. Make sure that you have cloned the project from github and correctly specified 
project configurations in Intellij as followed: 
    * Select **File|Open** from the main menu, choose the directory that you have just cloned. 
    * In **File|Project structure**, select **Project** in the left panel:
        * Check so that Java 1.8 is chosen in both **Project structures** and **Project language level**.
        Select **Apply**
        * In the left panel, select **Modules**, choose **Sources** and select `src` to specify `src` as 
        source folder.
        * Finally, select **Apply** and **OK**.
        
3. If Intellij does not automatically recognise the project as a Maven project, add Maven support:
    * In the Project tool window on the left panel, right-click and select 
    **Add Framework Support**.
    * In the pop-up window, select Maven and click **OK**.
     
    More information can be found on [Intellij support website](https://www.jetbrains.com/help/idea/2020.1/convert-a-regular-project-into-a-maven-project.html?utm_campaign=IU&utm_content=2020.1&utm_medium=link&utm_source=product#add_maven_support).
     
4. Now you have all set up, run `main()` in `testGUI.java` to start the application.
