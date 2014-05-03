

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class writeAListToFile {

	public writeAListToFile(ArrayList<String> content,String filename,boolean writeinoldfile){
		 
		
		 try {
			 FileWriter writer = new FileWriter(filename ,writeinoldfile);
			 for (int i=0;i<content.size();i++){
				 writer.write(content.get(i)+"\n");
				 
			 }
			 writer.flush(); //writer leeren
		     writer.close();
			 
		 }catch (IOException e) {
		      e.printStackTrace();
		   
		    }
		
		  }
	
}
