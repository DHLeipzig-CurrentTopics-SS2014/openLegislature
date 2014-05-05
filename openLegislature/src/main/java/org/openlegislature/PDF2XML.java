package org.openlegislature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 *
 * @author riddlore
 * @version 0.0.1
 */
public class PDF2XML {
	private String destFolder;

	/**
	 * @return the destFolder
	 */
	public String getDestFolder() {
		return destFolder;
	}

	/**
	 * @param destFolder the destFolder to set
	 */
	public void setDestFolder(String destFolder) {
		this.destFolder = destFolder;
	}

	public void processPdf(File f, boolean regex) throws IOException {
		PDDocument pddDocument = PDDocument.load(f);
		String fname = f.getName();
		PDFTextStripper textStripper = new PDFTextStripper();
		String[]  t = textStripper.getText(pddDocument).split("\n");	

		List<String> al = new ArrayList<>();
		for ( int i = 0; i < t.length; i++ )
			al.add(t[i]);
		pddDocument.close();

		if ( regex ) {
			String doc = this.clean(al);
			//new writeAListToFile(alregex, "finished/"+fname.substring(0, fname.length()-4)+"-regex.txt", false);
		}

		//new writeAListToFile(al, "finished/"+fname.substring(0, fname.length()-4)+".txt", false);
	}

	private String clean(List<String> al) {
		String doc = "";

		for ( String l : al )
			doc += l;

		doc = doc.replaceAll("-[\r\n|\n]", "");
		doc = doc.replaceAll("[\r\n|\n]", "");

		return doc;
	}
}