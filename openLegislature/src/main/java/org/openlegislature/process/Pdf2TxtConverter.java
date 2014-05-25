package org.openlegislature.process;

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
import org.openlegislature.util.Logger;
import org.openlegislature.util.OpenLegislatureConstants;

/**
 * Performs the convertion from pdf to txt.
 * Always performs this when {@link OpenLegislatureConstants.VM_PARAM_CLEAN}.
 *
 * @author riddlore, dhaeb, jnphilipp
 * @version 0.0.3
 */
public class Pdf2TxtConverter {
	private static final Pattern INDENTION_PATTERN = Pattern.compile("(\r\n|\n)?-(\r\n|\n)([a-zäöüß])");
	private OpenLegislatureConstants constants;

	@Inject
	public Pdf2TxtConverter(OpenLegislatureConstants constants) {
		this.constants = constants;
	}

	/**
	 * Process a pdf file if it is not already converted to txt.
	 * 
	 * @param file
	 *            The file to be processed
	 * @throws IOException
	 *             When an error (also with parsing) occurs
	 */
	public File processPdfWhenNotAlreadyDone(File file) throws IOException {
		return this.processPdfWhenNotAlreadyDone(file, constants.isClean());
	}

	public File processPdfWhenNotAlreadyDone(File f, boolean regex) throws IOException {
		String fname = f.getName().substring(0, f.getName().lastIndexOf("."));
		String filepathToParsedFile = createTargetFilepath(f, fname, ".parsed.txt");
		String filepathToCleanedFile = createTargetFilepath(f, fname, ".cleaned.txt");
		String doc = "";
		doc = createTxtRepresentation(f, fname, filepathToParsedFile, doc);
		File cleanTxtFile = cleanTxtFile(regex, fname, filepathToCleanedFile, doc);
		Logger.getInstance().debug(String.format("%s is processed and cleaned when needed", fname));
		return cleanTxtFile;
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

			FileWriter.write(filepathToParsedFile, doc, constants.getEncoding());
			Logger.getInstance().debug(String.format("%s is now pdf processed", fname));
		}
		return doc;
	}

	private File cleanTxtFile(boolean regex, String fname, String filepathToCleanedFile, String doc) throws IOException {
		if (regex) {
			cleanDoc(fname, filepathToCleanedFile, doc);
		} else {
			boolean cleanedTxtALreadyExists = new File(filepathToCleanedFile).exists();
			if (cleanedTxtALreadyExists) {
				Logger.getInstance().debug(String.format("%s is already txt processed", fname));
			} else {
				cleanDoc(fname, filepathToCleanedFile, doc);
			}
		}
		return new File(filepathToCleanedFile);
	}

	private void cleanDoc(String fname, String filepathToCleanedFile, String doc) throws FileNotFoundException, IOException {
		doc = this.clean(doc);
		FileWriter.write(filepathToCleanedFile, doc, constants.getEncoding());
		Logger.getInstance().debug(String.format("Cleaned %s", fname));
	}

	private String clean(String doc) {
		Matcher m = INDENTION_PATTERN.matcher(doc);
		while (m.find()) {
			doc = doc.replaceAll(m.group(), m.group(2));
		}
		doc = doc.replaceAll("kk", "ck");
		doc = doc.replaceAll("\\([ABCD]\\)", "");
		doc = doc.replaceAll("(\\d+)?(\\s+)?Deutscher\\sBundestag.+\\d+\\.\\sSitzung.+\\d+\\s+", "\n");

		return doc;
	}
}