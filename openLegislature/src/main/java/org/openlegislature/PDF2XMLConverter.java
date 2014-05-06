package org.openlegislature;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openlegislature.io.FileWriter;
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
		this.processPdf(file, true);
	}

	public void processPdf(File f, boolean regex) throws IOException {
		String fname = f.getName().substring(0, f.getName().lastIndexOf("."));

		PdfReader reader = new PdfReader(f.getAbsolutePath());
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);
		TextExtractionStrategy strategy;

		String doc = "";
		for ( int i = 1; i <= reader.getNumberOfPages(); i++ ) {
			strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
			doc += strategy.getResultantText() + "\n\n";
		}

		FileWriter.write(f.getParentFile().getAbsolutePath() + "/" + fname + ".parsed.txt", doc);
		if ( regex ) {
			doc = this.clean(doc);
			FileWriter.write(f.getParentFile().getAbsolutePath() + "/" + fname + ".cleaned.txt", doc);
		}
	}

	private String clean(String doc) {
		Matcher m = Pattern.compile("(\r\n|\n)-(\r\n|\n)([a-zäöüß])").matcher(doc);
		while ( m.find() ) {
			doc = doc.replaceAll(m.group(), m.group(3));
		}

		m = Pattern.compile("-(\r\n|\n)([a-zäöüß])").matcher(doc);
		while ( m.find() ) {
			doc = doc.replaceAll(m.group(), m.group(2));
		}
		doc = doc.replaceAll("kk", "ck");
		doc = doc.replaceAll("\\([ABCD]\\)", "");
		doc = doc.replaceAll("(\\d+)?(\\s+)?Deutscher\\sBundestag.+\\d+\\.\\sSitzung.+\\d+\\s+", "");

		return doc;
	}
}