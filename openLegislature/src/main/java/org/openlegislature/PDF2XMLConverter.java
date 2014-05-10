package org.openlegislature;

import com.google.inject.Inject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openlegislature.io.FileWriter;

/**
 * Performs the convertion from pdf to txt. 
 * Will be also used to convert the txt to xml.
 *
 * @author riddlore, dhaeb
 * @version 0.0.2
 */
public class PDF2XMLConverter {
	private static final Pattern INDENTION2_PATTERN = Pattern.compile("(\r\n|\n)-(\r\n|\n)([a-zäöüß])");
	private static final Pattern INDENTION_PATTERN = Pattern.compile("-(\r\n|\n)([a-zäöüß])");
	private OpenLegislatureConstants constants;

	@Inject
	public PDF2XMLConverter(OpenLegislatureConstants constants) {
		this.constants = constants;
	}
	
	/**
	 * Process a pdf file if it is not already converted to txt. 
	 * 
	 * @param file	The file to be processed
	 * @throws IOException When an error (also with parsing) occurs
	 */
	public void processPdfWhenNotAlreadyDone(File file) throws IOException {
		this.processPdfWhenNotAlreadyDone(file, constants.isClean());
	}

	public void processPdfWhenNotAlreadyDone(File f, boolean regex) throws IOException {
		String fname = f.getName().substring(0, f.getName().lastIndexOf("."));
		String filepathToParsedFile = createTargetFilepath(f, fname, ".parsed.txt");
		String filepathToCleanedFile = createTargetFilepath(f, fname, ".cleaned.txt");
		String doc = "";
		doc = createTxtRepresentation(f, fname, filepathToParsedFile, doc);
		cleanTxtFile(regex, fname, filepathToCleanedFile, doc);
	}

	private String createTargetFilepath(File f, String fname, String appendix) {
		StringBuilder targetFilePathBuilder = new StringBuilder();
		targetFilePathBuilder.append(f.getParentFile().getAbsolutePath());
		targetFilePathBuilder.append("/");
		targetFilePathBuilder.append(fname);
		targetFilePathBuilder.append(appendix);
		return targetFilePathBuilder.toString();
	}
	
	private String createTxtRepresentation(File f, String fname, String filepathToParsedFile, String doc) throws IOException {
		boolean convertedTxtAlreadyExists = new File(filepathToParsedFile).exists();
		if (convertedTxtAlreadyExists) {
			Logger.getInstance().debug(String.format("%s is already pdf processed", fname));
		} else {

			PdfReader reader = new PdfReader(f.getAbsolutePath());
			PdfReaderContentParser parser = new PdfReaderContentParser(reader);
			TextExtractionStrategy strategy;

			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
				doc += strategy.getResultantText() + "\n\n";
			}

			FileWriter.write(filepathToParsedFile, doc);
		}
		return doc;
	}

	private void cleanTxtFile(boolean regex, String fname, String filepathToCleanedFile, String doc) throws IOException {
		boolean cleanedTxtALreadyExists = new File(filepathToCleanedFile).exists();
		if(cleanedTxtALreadyExists ){
			Logger.getInstance().debug(String.format("%s is already txt processed", fname));
		} else {
			if (regex) {
				doc = this.clean(doc);
				FileWriter.write(filepathToCleanedFile, doc);
				Logger.getInstance().debug(String.format("Cleaned %s", fname));
			}
		}
	}

	private String clean(String doc) {
		Matcher m = INDENTION2_PATTERN.matcher(doc);
		while (m.find()) {
			doc = doc.replaceAll(m.group(), m.group(3));
		}

		m = INDENTION_PATTERN.matcher(doc);
		while (m.find()) {
			doc = doc.replaceAll(m.group(), m.group(2));
		}
		doc = doc.replaceAll("kk", "ck");
		doc = doc.replaceAll("\\([ABCD]\\)", "");
		doc = doc.replaceAll(
						"(\\d+)?(\\s+)?Deutscher\\sBundestag.+\\d+\\.\\sSitzung.+\\d+\\s+",
						"\n");

		return doc;
	}
}