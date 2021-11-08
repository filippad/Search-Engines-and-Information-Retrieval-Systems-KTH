import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import java.util.List;
import java.io.File;
import java.awt.Desktop;


public class testGUI extends JFrame {


    ElasticsearchClient client = new ElasticsearchClient();
    List<PostingEntry> results;
     /*
     *   Common GUI resources
     */
    public JCheckBox[] box = null;
    public JPanel resultWindow = new JPanel();
    private JScrollPane resultPane = new JScrollPane( resultWindow );
    public JTextField searchQueryWindow = new JTextField( "", 28 );
    public JTextArea docTextView = new JTextArea( "", 15, 28 );
    private JScrollPane docViewPane = new JScrollPane( docTextView );
    private Font queryFont = new Font( "Arial", Font.BOLD, 24 );
    private Font resultFont = new Font( "Arial", Font.BOLD, 16 );
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu( "File" );
    JMenu optionsMenu = new JMenu( "Search options" );
    JMenuItem quitItem = new JMenuItem( "Quit" );
    JRadioButtonMenuItem fileNameItem = new JRadioButtonMenuItem( "Sök på filnamn" );
    JRadioButtonMenuItem reportNameItem = new JRadioButtonMenuItem( "Sök på rapport" );
    JRadioButtonMenuItem yearItem = new JRadioButtonMenuItem( "Sök på år" );
    JRadioButtonMenuItem authorityItem = new JRadioButtonMenuItem( "Sök på myndighet" );
    JRadioButtonMenuItem taskItem = new JRadioButtonMenuItem( "Sök på uppgift" );
    JRadioButtonMenuItem goalItem = new JRadioButtonMenuItem( "Sök efter mål" );
    ButtonGroup queries = new ButtonGroup();



    void init() {
        // Create the GUI
        setSize( 1200, 650 );
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        JPanel p = new JPanel(new BorderLayout());
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        resultWindow.setLayout(new BoxLayout(resultWindow, BoxLayout.Y_AXIS));
        resultPane.setLayout(new ScrollPaneLayout());
        resultPane.setBorder( new EmptyBorder(10,10,10,0) );
        resultPane.setPreferredSize( new Dimension(400, 450 ));
        getContentPane().add(p, BorderLayout.CENTER);
        // Top menus
        menuBar.add( fileMenu );
        menuBar.add( optionsMenu );
        fileMenu.add( quitItem );
        optionsMenu.add( fileNameItem );
        optionsMenu.add( reportNameItem );
        optionsMenu.add( yearItem );
        optionsMenu.add( authorityItem );
        optionsMenu.add( taskItem );
        optionsMenu.add( goalItem );
        fileNameItem.setSelected( true );
        fileNameItem.setActionCommand("fileName");
        reportNameItem.setActionCommand("report");
        yearItem.setActionCommand("year");
        authorityItem.setActionCommand("authority");
        taskItem.setActionCommand("tasks");
        goalItem.setActionCommand("goals");
        queries.add(fileNameItem);
        queries.add(reportNameItem);
        queries.add(yearItem);
        queries.add(authorityItem);
        queries.add(taskItem);
        queries.add(goalItem);
        

        p.add( menuBar );
        // Logo
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add( new JLabel( new ImageIcon( "mtumme.png" )));
        p.add( p1 );
        //rubrik
        p.add(Box.createRigidArea(new Dimension(0,10)));
        JLabel label2 = new JLabel("Sök genom att fylla i text i rutan nedan. Specifiera din sökning i menyn ovan");
        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(label2, BorderLayout.NORTH);
        p2.add( searchQueryWindow );
        searchQueryWindow.setFont( queryFont );
        p.add( p2 );

        p.add(Box.createRigidArea(new Dimension(0,10)));
        JPanel p3 = new JPanel(new BorderLayout());
        JLabel label3 = new JLabel("Klicka på filnamnet för att öppna PDF");
        p3.add(label3, BorderLayout.NORTH);
        p.add( p3 );

        p.add( resultPane );

        docTextView.setFont(resultFont);
        docTextView.setText("\n  The contents of the document will appear here.");
        docTextView.setLineWrap(true);
        docTextView.setWrapStyleWord(true);
        p.add(docViewPane);
        setVisible( true );

        Action search = new AbstractAction(){
            public void actionPerformed(ActionEvent e){
                displayInfoText( " " );
                
                String queryString = searchQueryWindow.getText().toLowerCase().trim();
                String selection = queries.getSelection().getActionCommand();
                try {
                    results = client.search(selection, queryString);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                displayResult();
            }
        };
       
        searchQueryWindow.registerKeyboardAction( search,
                            "", 
                            KeyStroke.getKeyStroke( "ENTER" ),
                            JComponent.WHEN_FOCUSED );

        Action quit = new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                System.exit( 0 );
            }
            };
        quitItem.addActionListener( quit );  

    }

    void displayInfoText( String info ) {
        resultWindow.removeAll();
        JLabel label = new JLabel( info );
        label.setFont( resultFont );
        resultWindow.add( label );
        revalidate();
        repaint();
    }

   void displayResult() {
       String contents = "";
       System.out.println("Found : " + results.size() + " entries.");
       JPanel result = new JPanel();
       result.setAlignmentX(Component.LEFT_ALIGNMENT);
       result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
       for (PostingEntry entry : results) {
                String reportName = entry.getReport();

           contents += "File name: " + entry.getFilename() + "\n";
           for(Goal g : entry.goals) {
                contents += g.getTitle() + ": " + g.getContent() + "\n";
           }
           for (Task t : entry.tasks) {
               contents += t.getTitle() +": " + t.getContent() + "\n";
           }
           contents += "----------------------------" + "\n";
           docTextView.setText(contents);
           JLabel label = new JLabel(reportName);
           label.setLayout(new BoxLayout(label, BoxLayout.Y_AXIS));
           label.setFont(resultFont);

           MouseAdapter showPDF = (new MouseAdapter() {
               public void mousePressed(MouseEvent e){
                   String fileName = ((JLabel)e.getSource()).getText();
                   fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                   fileName += ".pdf";
                   System.out.println(fileName);

                   File pdfFile = new File("././ESV-data/"+fileName);
                   try{
                       if(Desktop.isDesktopSupported()){
                           try{
                               Desktop.getDesktop().open(pdfFile);
                           } catch (IOException ioException){
                               ioException.printStackTrace();
                           }
                       } else {
                           System.out.println("AWT not supported");
                       }
                   } catch (Exception ex){
                       ex.printStackTrace();
                   }
               }
           });
           label.addMouseListener(showPDF);
           result.add(label);
           resultWindow.add(result);
           revalidate();
           repaint();
       }



        /*
       for (PostingEntry entry : results) {
           System.out.println("File name: " + entry.getFilename());
           for (Goal g : entry.goals) {
               System.out.println(g.getTitle() + ": " + g.getContent());
           }
           for (Task t : entry.tasks) {
               System.out.println(t.getTitle() + ": " + t.getContent());
           }
           System.out.println("--------------------------");
       }
        */
   }


    public static void main( String[] args ) {
        testGUI e = new testGUI();
        e.init();
    }

}
