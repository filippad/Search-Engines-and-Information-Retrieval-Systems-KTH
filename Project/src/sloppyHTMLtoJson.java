import java.nio.charset.*;
import java.io.*;

public class sloppyHTMLtoJson{

    public void readAndWrite(File readfile){
        try{
            //File readfile=new File("../TestHTML/"+fn);  
            String fn = readfile.getPath();
            String cleanfn = fn.replaceAll(".html", "");
            cleanfn = cleanfn.replaceAll("/TestHTML/", "/TestJson/");
            File writefile = new File(cleanfn + ".json");
            InputStreamReader reader = new InputStreamReader(
                new FileInputStream(readfile), "ISO-8859-15");//behöver kanske checka att alla filerna är i IS0-8859-15?
            BufferedReader bufReader = new BufferedReader(reader);
            OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(writefile),"UTF-8"); //json vill iaf ha utf-8
            BufferedWriter bufWriter = new BufferedWriter(writer);
            cleanfn = cleanfn.replaceAll("/home/karla/Search_Engines_DD2476/Project/TestJson/", "");
            bufWriter.write("{ \"title\":\""+cleanfn+"\", \"content\": \"");
            while(bufReader.readLine() != null){
                String line = bufReader.readLine();
                String cleanline = line.replaceAll("[^A-Z|a-z|å|Å|ä|Ä|ö|Ö|0-9| |<|>|/|.|=|;|#|\"]", "");
                cleanline = cleanline.replaceAll("\"", "\\\\\"");// vill kanske bara ta bort citattecknen? " ->\"
                bufWriter.write(cleanline+"\n");
            }
            bufWriter.write("\"}");
            reader.close();  
            bufReader.close();
            bufWriter.close();
        }  
        catch(IOException e){  
            e.printStackTrace();  
            }

    }

    public void processFiles( File f, sloppyHTMLtoJson conv ) {
        // do not try to index fs that cannot be read
            if ( f.canRead() ) {
                System.out.println("Processing files");
            
                if ( f.isDirectory() ) {
                    String[] fs = f.list();
                    // an IO error could occur
                    if ( fs != null ) {
                        for ( int i=0; i<fs.length; i++ ) {
                            processFiles( new File( f, fs[i] ), conv);
                        }
                    }
                }
                else{
                    conv.readAndWrite(f);  
                }
            }
            
        }

public static void main(String[] args) {
    sloppyHTMLtoJson conv = new sloppyHTMLtoJson(); 
    File dir = new File("/home/karla/Search_Engines_DD2476/Project/TestHTML");//yeah det här borde inte behöva vara absolute path  
    conv.processFiles(dir, conv);    
}

}
