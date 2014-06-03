package org.openlegislature.process;

import com.google.inject.Inject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
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
	 * @param pdfFile
	 *            The file to be processed
	 * @throws IOException
	 *             When an error (also with parsing) occurs
	 */
	public File processPdfWhenNotAlreadyDone(File pdfFile) throws IOException {
		return this.processPdfWhenNotAlreadyDone(pdfFile, constants.isClean());
	}

	public File processPdfWhenNotAlreadyDone(File pdfFile, boolean isCleaning) throws IOException {
		String fname = pdfFile.getName().substring(0, pdfFile.getName().lastIndexOf("."));
		String filepathToParsedFile = createTargetFilepath(pdfFile, fname, ".parsed.txt");
		String filepathToCleanedFile = createTargetFilepath(pdfFile, fname, ".cleaned.txt");
		String doc = createTxtRepresentation(pdfFile, fname, filepathToParsedFile);
		File cleanTxtFile = cleanTxtFile(isCleaning, fname, filepathToCleanedFile, doc);
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

	private String createTxtRepresentation(File pdfFile, String fname, String filepathToParsedFile) throws IOException {
        String doc = "";
        File cleandedFile = new File(filepathToParsedFile);
        boolean convertedTxtAlreadyExists = cleandedFile.exists();
		if (convertedTxtAlreadyExists) {
			Logger.getInstance().debug(String.format("%s is already pdf processed", fname));
            doc = IOUtils.toString(new BufferedReader(new InputStreamReader(new FileInputStream(cleandedFile), constants.getEncoding())));
		} else {

			PdfReader reader = new PdfReader(pdfFile.getAbsolutePath());
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

	private void cleanDoc(String fname, String filepathToCleanedFile, String doc) throws IOException {
		FileWriter.write(filepathToCleanedFile, this.clean(doc), constants.getEncoding());
		Logger.getInstance().debug(String.format("Cleaned %s", fname));
	}

	private String clean(String doc) {
		Matcher m = INDENTION_PATTERN.matcher(doc);
		while (m.find()) {
			doc = doc.replaceAll(m.group(), m.group(3));
		}
		doc = doc.replaceAll("kk", "ck");
		doc = doc.replaceAll("\\([ABCD]\\)", "");
		doc = doc.replaceAll("(\\d+)?(\\s+)?Deutscher\\sBundestag.+\\d+\\.\\sSitzung.+\\d+\\s+", "\n");

		return doc;
	}
}
