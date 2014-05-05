package org.openlegislature.io;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import org.openlegislature.util.Helpers;

/**
 *
 * @author jnphilipp
 * @version 0.0.2
 */
public class FileWriter {
	/**
	 * Writes the given content to the given file.
	 * @param file path to file
	 * @param append if <code>true</code> content will be added if the file exists
	 * @param content content
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 */
	public static void write(String file, boolean append, String content) throws FileNotFoundException, IOException {
		Writer writer = null;

		try {
			writer = new BufferedWriter(new java.io.FileWriter(file));
			if ( append )
				writer.append(content);
			else
				writer.write(content);
		}
		finally {
			if ( writer != null )
				writer.close();
		}
	}

	/**
	 * Writes the given content as csv.
	 * @param file path to file
	 * @param content content
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 */
	public static void writeCSV(String file, Collection<String[]> content) throws FileNotFoundException, IOException {
		FileWriter.writeCSV(file, content, ";");
	}

	/**
	 * Writes the given content as csv.
	 * @param file path to file
	 * @param content content
	 * @param cement cement
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 */
	public static void writeCSV(String file, Collection<String[]> content, String cement) throws FileNotFoundException, IOException {
		String c = "";
		for ( String[] line : content )
			c += System.lineSeparator() + Helpers.join(line, cement);
		FileWriter.write(file, false, c);
	}
}