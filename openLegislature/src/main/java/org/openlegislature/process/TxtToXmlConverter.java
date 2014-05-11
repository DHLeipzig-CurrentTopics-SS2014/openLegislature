package org.openlegislature.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;


public class TxtToXmlConverter {

	public File convertToXml(File zuparsen) throws IOException {
		boolean openBrace = false;
		String memory = "";
		boolean speech = false;
		int count = 0;
		String neu = zuparsen.getPath();
		neu = neu.replace(".txt", ".xml");
		BufferedReader in = null;
		Writer writer = null;
		try {
			in = new BufferedReader(new FileReader(zuparsen));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(neu), "utf-8"));
			String zeile = null;
			while ((zeile = in.readLine()) != null) {
				if (count == 0 && zeile.matches(".*[0-9]\\. Sitzung.*")) {
					writer.write("<protocol session_number=\"" + zeile.substring(0, zeile.indexOf("Sitzung") + 7) + "\" >\n<header>\n");
					count++;
					continue;
				}
				if (count == 0) {
					continue;
				}

				if (count == 1 && zeile.matches(".*[0-9]\\..* [A-Z][a-z]*.* [12][0-9][0-9][0-9].*")) {
					writer.write(zeile + "\n</header>\n<agenda>\n");
					count++;
					continue;
				}
				if (count == 3 && zeile.matches(".*Sitzung.*")) {
					count++;
					writer.write("</agenda>\n<session>\n" + zeile + "\n");
					continue;
				}
				if (count == 2 && zeile.matches(".*Nächste Sitzung.*")) {
					count++;
				}

				if (count == 4
						&& (zeile.matches("[^\\(a-z]*[A-ZÄÖÜ][a-zöäü]* [A-ZÄÖÜ][a-zäöü]*.*:.*") || zeile
								.matches("[^\\(a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ][A-ZÄÖÜ][A-ZÄÖÜ]*\\).*:.*"))) {
					speech = true;
					writer.write("<speech>\n<speaker>\n" + zeile.substring(0, zeile.indexOf(":") + 1) + "\n</speaker>\n");
					if (zeile.endsWith(":") || zeile.endsWith(": ")) {
					} else {
						writer.write(zeile.substring(zeile.indexOf(":") + 1) + "\n");
					}
					count++;
					continue;
				}
				if (count == 5
						&& (zeile.matches("[^\\(a-z]*[A-ZÄÖÜ][a-zäöü]* [A-ZÄÖÜ][a-zäöü]*.*:.*") || zeile
								.matches("[^\\(a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ][A-ZÄÖÜ][A-ZÄÖÜ]*\\).*:.*")) && speech) {
					writer.write("</speech>\n<speech>\n<speaker>\n" + zeile.substring(0, zeile.indexOf(":") + 1) + "\n</speaker>\n");
					if (zeile.endsWith(":") || zeile.endsWith(": ")) {
					} else {
						writer.write(zeile.substring(zeile.indexOf(":") + 1) + "\n");
					}
					continue;
				}

				if (zeile.matches(".*Schlu(ß|(ss)) .* Sitzung .*") && count == 5) {
					speech = false;
					count++;
					writer.write(zeile + "</speech>\n<attachement>\n");

					continue;
				}

				if (count == 5 && zeile.startsWith("(")) {
					if (zeile.endsWith(")") || zeile.endsWith(") ")) {
						writer.write("<interjection>" + zeile + "</interjection>\n");
						continue;
					} else {
						openBrace = true;
						memory = "<interjection>\n" + zeile + "\n";
						continue;
					}
				}
				if (openBrace) {
					if (zeile.matches(".*\\).*")) {
						writer.write(memory + zeile + "\n</interjection>\n");
						memory = "";
						openBrace = false;
						continue;
					} else {
						memory = memory + zeile;
						continue;
					}
				}

				writer.write(zeile + "\n");
			}
			if (speech) {
				writer.write("</speech>\n<attachement>\n");
			}

			writer.write("</attachement>\n</session>\n</protocol>\n");
			in.close();

		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				
			}
		}
		return new File(neu);
	}

}
