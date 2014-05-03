
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;




public class PDF2Text {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.out.println("+-------------------------");
		System.out.println("| scanning for files  ");
		ArrayList<File> files = getFiles();
		System.out.println("+-------------------------");
		
		
		for(File f:files)
		{
			String[] temp = f.getAbsolutePath().split("\\.");
			String ext = temp[temp.length-1];
			if(ext.equalsIgnoreCase("pdf"))
			{
				System.out.println("| process:"+f.getName());
				new File(temp[0]);
				processPdf(f,temp[0]+"/",true);
			}
			
		}
		System.out.println("+-------------------------");
		System.out.println("+ finished all");
		System.out.println("+-------------------------");
	}
	
	
	private static ArrayList<String> regexAL(ArrayList<String> al ){
		
		String doc ="";
		
		for(String l : al){
			doc+=l;
		}
		doc=doc.replaceAll("-[\r\n|\n]", "");
		doc=doc.replaceAll("[\r\n|\n]", "");
		
		ArrayList<String> alr = new ArrayList<String>();
		alr.add(doc);
		return alr;
	}
	
	
	
	private static void processPdf(File f,String destDir, boolean regex)
	{
		
		try {
			PDDocument pddDocument = PDDocument.load(f);
			String fname = f.getName();
			PDFTextStripper textStripper=new PDFTextStripper();	
			String[]  t = textStripper.getText(pddDocument).split("\n");	
			
			ArrayList<String> al = new ArrayList<>();
			for(int i=0;i<t.length;i++){
				al.add(t[i]);
			}
			pddDocument.close();
			
			if(regex) {
				ArrayList<String> alregex = new ArrayList<String>();
				alregex.addAll( regexAL(al));
				new writeAListToFile(alregex, "finished/"+fname.substring(0, fname.length()-4)+"-regex.txt", false);
			}
			
			new writeAListToFile(al, "finished/"+fname.substring(0, fname.length()-4)+".txt", false);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	private static ArrayList<File> getFiles()
	{
		DirScanner ds = new DirScanner();
		ds.scan("todo/");
		
		return ds.getFileList();
	}
}
