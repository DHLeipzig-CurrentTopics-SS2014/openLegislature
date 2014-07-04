package org.openlegislature.io;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jnphilipp, dhaeb
 * @version 0.0.4
 */
public class FileWriter {
	/**
	 * Writes the given content to the given file.
	 *
	 * @param file path to file
	 * @param content content
	 * @param encoding
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 */
	public static void write(String file, String content, String encoding) throws FileNotFoundException, IOException {
		FileWriter.write(file, false, content, encoding);
	}

	/**
	 * Writes the given content to the given file.
	 *
	 * @param file path to file
	 * @param append if <code>true</code> content will be added if the file exists
	 * @param content content
	 * @param encoding the character encoding
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 */
	public static void write(String file, boolean append, String content, String encoding) throws FileNotFoundException, IOException {
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), encoding));
			writer.write(content);
		}
		finally {
			IOUtils.closeQuietly(writer);
		}
	}

	/**
	 * Writes a CSV file with the given content and cement.
	 * @param file path to file
	 * @param content content as matrix
	 * @param cement cement to use as separator between cells
	 * @param encoding the character encoding
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws UnsupportedEncodingException 
	 */
	public static void writeCSV(String file, String[][] content, String cement, String encoding) throws FileNotFoundException, IOException, UnsupportedEncodingException {
		FileWriter.writeCSV(file, false, content, cement, encoding);
	}

	/**
	 * Writes a CSV file with the given content and cement.
	 * @param file path to file
	 * @param append if <code>true</code> content will be added if the file exists
	 * @param content content as matrix
	 * @param cement cement to use as separator between cells
	 * @param encoding the character encoding
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws UnsupportedEncodingException 
	 */
	public static void writeCSV(String file, boolean append, String[][] content, String cement, String encoding) throws FileNotFoundException, IOException, UnsupportedEncodingException {
		Writer writer = null;

		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), encoding));
			for ( String[] line : content ) {
				for (int j = 0; j < line.length; j++) {
					writer.write(line[j] + (j != line.length - 1 ? cement : ""));
				}
				writer.write(System.lineSeparator());
			}
		}
		finally {
			IOUtils.closeQuietly(writer);
		}
	}
}