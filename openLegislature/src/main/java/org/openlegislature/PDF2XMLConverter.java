package org.openlegislature;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 *
 * @author riddlore
 * @version 0.0.1
 */
public class PDF2XMLConverter {
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

	public void processPdf(File file) throws IOException {
		this.processPdf(file, false);
	}

	public void processPdf(File f, boolean regex) throws IOException {
		PDDocument pddDocument = PDDocument.load(f);
		String fname = f.getName();
		PDFTextStripper textStripper = new PDFTextStripper();
		String[] lines = textStripper.getText(pddDocument).split("\n");
		pddDocument.close();

		String doc = "";
		for ( String line : lines )
			doc += line;

		if ( regex ) {
			doc = this.clean(doc);
			//new writeAListToFile(alregex, "finished/"+fname.substring(0, fname.length()-4)+"-regex.txt", false);
		}

		Logger.info(PDF2XMLConverter.class, doc);
		//new writeAListToFile(al, "finished/"+fname.substring(0, fname.length()-4)+".txt", false);
	}

	private String clean(String doc) {
		Matcher m = Pattern.compile("-(\r\n|\n)([a-zäöüß])").matcher(doc);

		while ( m.find() ) {
			doc = doc.replaceAll(m.group(), m.group(2));
		}
		doc = doc.replaceAll("kk", "ck");

		return doc;
	}
}