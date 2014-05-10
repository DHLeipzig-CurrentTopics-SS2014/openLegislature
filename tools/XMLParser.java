import java.io.*;
import java.util.*;
public class Parser {
	 private static Set<File> alleFiles = new TreeSet<File>();
	 
	 /*
	  * In dieser Klasse den Pfad anpassen wenn es verwendet werden soll. Dann schreibt parsen() in den selben Pfad die xml.
	  */
	 public Set<File> cleanedFiles() throws FileNotFoundException
	 {
		String dateiName=".*cleaned.txt";
		Parser test = new Parser();
		File datei=test.convertStringtoFile("/home/peggy/Copy/dh/data/bundestag/");
		Set<File> dateien=test.listDir(datei, dateiName);		 
		 return dateien;
	 }
	 
	 
	 public void parsen(File zuparsen)
	 {
		 boolean openBrace=false;
		 String memory="";
		 boolean speech=false;
		 int count=0;
		 String neu= zuparsen.getPath();
		 neu=neu.replace(".txt", ".xml");
		 Writer writer = null;
		 try {
			 BufferedReader in = new BufferedReader(new FileReader(zuparsen));
			 String zeile = null;
		     writer = new BufferedWriter(new OutputStreamWriter(
		           new FileOutputStream(neu), "utf-8"));
		     while ((zeile = in.readLine()) != null) {
		    	 if(count==0 && zeile.matches(".*[0-9]\\. Sitzung.*")){
		    		 writer.write("<protocol session_number=\""+zeile.substring(0, zeile.indexOf("Sitzung")+7)+"\" >\n<header>\n");
		    		 count++;
		    		 continue;
		    	 }
		    	 if(count==0){
		    		 continue;
		    	 }
		    	 
		    	 if(count==1 && zeile.matches(".*[0-9]\\..* [A-Z][a-z]*.* [12][0-9][0-9][0-9].*")){
		    		 writer.write(zeile+"\n</header>\n<agenda>\n");
		    		 count++;
		    		 continue;
		    	 }
		    	 if(count==3 && zeile.matches(".*Sitzung.*")){
		    		 count++;
		    		 writer.write("</agenda>\n<session>\n"+zeile+"\n");
		    		 continue;
		    	 }
		    	 if(count==2 && zeile.matches(".*Nächste Sitzung.*")){count++;}
		    	 
		    	 if(count==4 && (zeile.matches("[^\\(a-z]*[A-ZÄÖÜ][a-zöäü]* [A-ZÄÖÜ][a-zäöü]*.*:.*")||zeile.matches("[^\\(a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ][A-ZÄÖÜ][A-ZÄÖÜ]*\\).*:.*"))){
		    		 speech=true;
		    		 writer.write("<speech>\n<speaker>\n"+zeile.substring(0, zeile.indexOf(":")+1)+"\n</speaker>\n");
		    		 if(zeile.endsWith(":")||zeile.endsWith(": ")){}else{
		    			 writer.write(zeile.substring(zeile.indexOf(":")+1)+"\n");}
		    		 count++;
		    		 continue;
		    	 }
		    	 if(count==5 && (zeile.matches("[^\\(a-z]*[A-ZÄÖÜ][a-zäöü]* [A-ZÄÖÜ][a-zäöü]*.*:.*")||zeile.matches("[^\\(a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ][A-ZÄÖÜ][A-ZÄÖÜ]*\\).*:.*"))&& speech){
		    		 writer.write("</speech>\n<speech>\n<speaker>\n"+zeile.substring(0, zeile.indexOf(":")+1)+"\n</speaker>\n");
		    		 if(zeile.endsWith(":")||zeile.endsWith(": ")){}else{
		    			 writer.write(zeile.substring(zeile.indexOf(":")+1)+"\n");}
		    		 continue;
		    	 }
		    	 
		    	 if(zeile.matches(".*Schlu(ß|(ss)) .* Sitzung .*") && count==5){
		    		 speech=false;
		    		 count++;
		    		 writer.write(zeile+"</speech>\n<attachement>\n");
		    		 
		    		 continue;
		    	 }
		    	 
		    	 if(count==5 && zeile.startsWith("(")){
		    		 if(zeile.endsWith(")")||zeile.endsWith(") ")){
		    			 writer.write("<interjection>"+zeile+"</interjection>\n");
		    			 continue;
		    		 }
		    		 else{openBrace=true;memory="<interjection>\n"+zeile+"\n";continue;}
		    	 }
		    	 if(openBrace){
		    		 if(zeile.matches(".*\\).*")){
		    			 writer.write(memory+zeile+"\n</interjection>\n");
		    			 memory="";
		    			 openBrace=false;
		    			 continue;
		    		 }
		    		 else{memory=memory+zeile;continue;}
		    	 }
		    	 		    	 
					writer.write(zeile+"\n");
			}
		    if(speech){writer.write("</speech>\n<attachement>\n");}
		  
		    writer.write("</attachement>\n</session>\n</protocol>\n");
			in.close();
		    
		     
		 } catch (IOException ex) {
		   // report
		 } finally {
		    try {writer.close();} catch (Exception ex) {}
		 }
	 }
	
	public static void main(String[] argv) throws FileNotFoundException
	{

		Parser test = new Parser();
		Set<File> ausgabe = test.cleanedFiles();
		Iterator it = ausgabe.iterator();
				while(it.hasNext())
				{
					File eintraege=(File) it.next();
					test.parsen(eintraege);
				}
					
	}
	
	private File[] convertSetToFile(Set<File> eingabe)
	{
		File[] ende= new File[eingabe.size()];
		Iterator it = eingabe.iterator();
		for(int i=0; i<ende.length;i++)
		 {
		  ende[i]=(File) it.next();
		 }	
		return ende;
	}
	
	private Set<File> listDir(File dir, String dateiName) {
		  File[] files = dir.listFiles();
		  if (files != null) {
		    for (int i = 0; i < files.length; i++) {
		      if (files[i].isDirectory()) {
		        listDir(files[i], dateiName); // ruft sich selbst auf
		      }
		      else {
		    	  String pruef = files[i].getName();
		    	  if(pruef.matches(dateiName) && alleFiles.contains(files[i])==false)
		    	  {
		    	  alleFiles.add(files[i]);
		    	  }
		      }
		    }
		  }		  
		  return alleFiles;
	}
	
	
	private File convertStringtoFile(String pfad) throws FileNotFoundException
	{
		File verzeichnis;
		verzeichnis=new File(pfad);
		return verzeichnis;
	}
	
}
