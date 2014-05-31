package org.openlegislature.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openlegislature.util.Logger;
import org.openlegislature.util.OpenLegislatureConstants;

import com.google.inject.Inject;

public class TxtToXmlConverter {
	private static final String[] publicoffices = { "sekretär","kanzler", "Präsident", "Schriftführer", "minister", "Staatssekretär", "Berichterstatter",
			"berichterstatter", "präsident" };
	private static final String[] anhaenge = { "Frau", "Herr", "Fräulein", "herr", "Dr.", "Dr ", "von", "Abgeordnet" };
	// private static final String[] partys = { "Parteilos", "CDU", "Z", "NR",
	// "FDP", "SPD", "PDS", "WAV", "BP", "NR", "KPD" };

    private final String PROTOCOL = "protocol";
    private final String AGENDA = "agenda";
    private final String PARTY = "party";
    private final String SPEAKER = "speaker";
    private final String SPEECH = "speech";
    private final String NAME = "name";
    private final String HEADER = "header";
    private final String SESSION = "session";
    private final String PUBLIC_OFFICE = "public_office";
    private final String INTERJECTION = "interjection";
    private final String ITEM ="item";

	private String testpart = "";
	private String testoff = "";
	private String testname = "";
	Boolean change = false;
	Boolean inPub=false;
    private OpenLegislatureConstants constants;

    @Inject
    public TxtToXmlConverter(OpenLegislatureConstants constants) {
        this.constants = constants;
    }

    private String speakerAtt(String spline) {
		return speakerAtt(spline, "");
	}

	private String speakerAtt(String spline, String publicoffice) {
		publicoffice = publicoffice.replaceAll("[—,\\.!\\?\\-;_]", "");
		publicoffice = publicoffice.replaceAll("\\*\\)", "");

		String vorname = " ";
		String line = spline;
		for (int i = 0; i < anhaenge.length; i++) {
			if (line.contains(anhaenge[i])) {
				String[] part = line.split(" ");
				line = line.replaceAll("[ ,\\.']?[a-zäöüA-ZÄÖÜ ,\\.]*" + anhaenge[i] + "[a-zäöü\\. ]*", "");
				for (int j = 0; j < part.length; j++) {
					if (part[j].contains(anhaenge[i])) {
						if (anhaenge[i].matches(".*[fFH].*")) {
							vorname = part[j] + " " + vorname;
						} else {
							vorname += " " + part[j];
						}
					}
				}
			}
		}
		String name = "";
		String party = "";
		line = line.replaceAll(" \\.", "");
		line = line.replaceAll("   *", " ");
		line = line.replaceAll("[ ,]?[0-9][0-9][0-9][0-9]* ?[A-ZÖÄÜ]? ?", "");
		line = line.replaceAll("\\'", "");
		line = line.replaceAll("\\s?-\\s?", "-");
		line = line.replaceAll("Antrag[a-zäöü]*", "");

		if (line.matches(".*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\).*")) {
			if (line.matches(".*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\),.*")) {
				line = line.substring(0, line.lastIndexOf(","));
			}
			if (line.substring(0, line.indexOf("(")).split(" ").length <= 2) {
				name = line.substring(0, line.indexOf("("));
				if (line.indexOf("(") == line.lastIndexOf("(")) {
					if (line.indexOf(")") - line.indexOf("(") > 2) {
						party = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
					} else {
						party = line.charAt(line.indexOf("(") + 1) + "";
					}
				} else {
					line = line.replace(line.substring(0, line.indexOf("(") + 1), "");
					String[] part = line.split("\\)");
					for (int i = 0; i < part.length; i++) {
						if (part[i].matches(".*[A-ZÖÄÜ][^a-zäöü][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*.*")) {
							party = part[i].substring(part[i].indexOf("(") + 1);
							break;
						} else {
							if (part[i].length() > 2)
								if (part[i].contains("(")) {
									name += part[i] + ")";
								} else {
									name += "(" + part[i] + ")";
								}
						}
					}
				}
			}
		} else {
			if (line.split(",")[0].split(" ").length <= 2)
				name = line.split(",")[0];
		}
		if ((name.length() > 2 && party.length() > 0) || (name.length() > 2 && publicoffice.length() > 0)) {
			change = true;
			name = name.replaceAll("[ ]*[\\.]", "");
			name = vorname + " " + name;
			name = name.replaceAll("[ ]*[,\\?!;:_—]", "");
			name = name.replaceAll("\\-", " ");
			name = name.replaceAll("  *", " ");
			name = name.replaceAll("\\*\\)", "");

			if (name.startsWith(" ")) {
				name = name.substring(1);
			}
			if (name.endsWith(" ") || name.endsWith("-")) {
				name = name.substring(0, name.length() - 1);
			}
			if (name.endsWith(" ") || name.endsWith("-")) {
				name = name.substring(0, name.length() - 1);
			}
			if (testname.contains(name) == false) {
				testname += name + "; ";
			}
			line = "<speaker><name>" + escapeString(name) + "</name>";
			if (party.length() > 0) {
				line += "<party>" + escapeString(party) + "</party>";
				if (testpart.contains(party) == false) {
					testpart += party + "; ";
				}
			}
			if (publicoffice.length() > 2) {
				publicoffice = publicoffice.replaceAll("\\p{Punct}", "");
				line += "<public_office>" + escapeString(publicoffice) + "</public_office>";
				if (testoff.contains(publicoffice) == false) {
					testoff += publicoffice + "; ";
				}
			}
			line += "</speaker>\n";
			spline = line;
			spline = line.replaceAll("  *", " ");

		} else {
			change = false;
			spline += " " + publicoffice + " \n";
		}
		return spline;
	}

	private String speaker(String spline) {
		return speaker(spline, "");
	}

	private String speaker(String spline, String publicoffice) {
		publicoffice = publicoffice.replaceAll("\\*\\)", "");
		publicoffice = publicoffice.replaceAll("—", "");
		spline = spline.replaceAll("[ ]?-[ ]?", "-");
		spline = spline.replaceAll("'", "");
		String line = spline;
		String vorname = "";
		String[] part = line.split(" ");
		for (int j = 0; j < part.length; j++) {
		for (int i = 0; i < anhaenge.length; i++) {
			if (part[j].contains(anhaenge[i])) {
				//part = line.split(" ");
				if(anhaenge[i].matches("von")){
					line = line.replaceAll("[ ,\\.']?[a-zäöüA-ZÄÖÜ]*" + anhaenge[i] + "[a-zäöü\\. ]*", " ");
				}else{
					line = line.replaceAll("[ ,\\.']?[a-zäöüA-ZÄÖÜ \\.]*" + anhaenge[i] + "[a-zäöü\\. ]*", " ");
				}
						if (anhaenge[i].matches(".*[fFH].*")) {
							vorname = part[j] + " " + vorname;
						} else {
							vorname += " " + part[j];
						}
					
				
			}
		}
		}
		String party = "";
		String name = "";
		String ort = "";
		if (line.matches(".*\\(CDU CSU\\).*")) {
			line = line.replaceAll("\\(CDU CSU\\)", "\\(CDU/CSU\\)");
		}
		if (line.matches(".*\\(DIE LINKE\\).*")) {
			line = line.replaceAll("\\(DIE LINKE\\)", "\\(DIE_LINKE\\)");
		}
		line = line.replaceAll("Antrag[a-zäöü]*", "");

		String[] partbefore = line.split(" ");
		if (line.matches(".*\\([A-ZÖÄÜ]+.?[A-ZÄÖÜ]*\\).*")) {
			for (int i = 0; i < partbefore.length; i++) {
				if (partbefore[i].matches(".*\\([A-ZÖÄÜ]+.?[A-ZÄÖÜ]*\\).*")) {
					if (partbefore[i].indexOf(")") - partbefore[i].indexOf("(") > 2) {
						party = partbefore[i].substring(partbefore[i].indexOf("(") + 1, partbefore[i].indexOf(")"));
					} else {
						party = partbefore[i].charAt(partbefore[i].indexOf("(") + 1) + "";
					}
					if (party.contains("DIE_LINKE")) {
						party = "DIE LINKE";
					}
					line = line.replaceAll("\\([A-ZÖÄÜ]+.?[A-ZÄÖÜ]*\\)", "");
				}
			}
		}
		name = line;
		// System.out.print(line+"; ");
		name = name.replaceAll("[ ]*[\\.,\\?!;:_—]", "");
		if ((name.length() > 2 && party.length() > 0) || (name.length() > 2 && publicoffice.length() > 0)) {
			change = true;
			if (name.endsWith(" ")) {
				name = name.substring(0, name.length() - 1);
			}
			if (ort.length() > 1) {
				name += " " + ort;
			}
			name = name.replaceAll("[ ]*[\\.]", "");
			name = vorname + " " + name;
			name = name.replaceAll("[ ]*[,\\?!;:_—]", "");
			name = name.replaceAll("  *", " ");
			name = name.replaceAll("\\*\\)", "");
			if (name.startsWith(" ")) {
				name = name.substring(1);
			}
			if (name.endsWith(" ") || name.endsWith("-")) {
				name = name.substring(0, name.length() - 1);
			}
			if (name.endsWith(" ") || name.endsWith("-")) {
				name = name.substring(0, name.length() - 1);
			}
			line = String.format("%s\n%s\n%s%s%s", createTagFrom(SPEECH),
                                                   createTagFrom(SPEAKER),
                                                   createTagFrom(NAME),
                                                   escapeString(name),
                                                   createClosingTagFrom(NAME));
			if (testname.contains(name) == false) {
				testname += name + "; ";
			}
			if (party.length() > 0) {
				if (party.endsWith(" ")) {
					party = party.substring(0, party.length() - 1);
				}
				if (testpart.contains(party) == false) {
					testpart += party + "; ";
				}
				line += createTagFrom(PARTY) + escapeString(party) + createClosingTagFrom(PARTY);
			}
			if (publicoffice.length() > 0) {
				if (publicoffice.endsWith(" ")) {
					publicoffice = publicoffice.substring(0, publicoffice.length() - 1);
				}
				if (testoff.contains(publicoffice) == false) {
					testoff += publicoffice + "; ";
				}
				publicoffice = publicoffice.replaceAll("\\p{Punct}", "");
				line += createTagFrom(PUBLIC_OFFICE) + escapeString(publicoffice) + createClosingTagFrom(PUBLIC_OFFICE);
			}
			line += String.format("\n%s\n", createClosingTagFrom(SPEAKER));

			spline = line.replaceAll("  *", " ");
		} else {
			spline += " " + publicoffice + " \n";
			change = false;
		}
		return spline;
	}

	public File convertToXml(File zuparsen) throws IOException {
		String neu = zuparsen.getPath();
		neu = neu.replace(".txt", ".xml");
		File outputFile = new File(neu);
        if(constants.isProcessXml()){
            outputFile = convertToXml(zuparsen, outputFile);
        } else if(!outputFile.exists()) {
			outputFile = convertToXml(zuparsen, outputFile);
		} 
        Logger.getInstance().info(String.format("Finished %s successfully", outputFile.getName()));
        return outputFile;
	}
	
	File convertToXml(File zuparsen, File outputFile) throws IOException {
		BufferedReader in = null;
		Writer writer = null;
		// System.out.println(zuparsen);
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(zuparsen), constants.getEncoding()));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), constants.getEncoding()));
			parseSpeachAndWriteBack(in, writer);
		} finally {
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(in);
		}
		Logger.getInstance().debug("File: " + zuparsen + "\n name: " + testname + "\n office: " + testoff + "\n Party: " + testpart + "\n");
		return outputFile;
	}
	
	private String publicoff(String office, boolean attach){
		office=office.replaceAll("[\\. ]*[0-9][0-9][0-9]*.?[A-ZÄÖÜ][ \\.]*", "");
		boolean found=false;
		boolean fur=false;
		boolean und=false;
		String teil=office;
		String pub="";
		int index=0;
		int zeiger=1024;
		int beginAnhang=0;
		boolean test=false;
		String[] part = office.split(" ");

		for(int i=0;i<anhaenge.length;i++){
			if(part[0].matches(anhaenge[i])){
				beginAnhang=2;
			}
			if(part[0].matches("Der")){
				if(part.length == 2 && part[1].matches(anhaenge[i])){
					beginAnhang=3;
					break;
				}
			}
		}
		
		for (int i = 0; i < publicoffices.length; i++) {
			if (office.contains(publicoffices[i])) {
				test=true;
				if(zeiger>office.indexOf(publicoffices[i]))
					zeiger=office.indexOf(publicoffices[i]);
			}
		}
		
		for (int i = 0; i < publicoffices.length; i++) {
			
			if (office.contains(publicoffices[i])&&office.indexOf(publicoffices[i])==zeiger) {
				for (int j = beginAnhang; j < part.length; j++) {
					if (part[j].contains(publicoffices[i])&&found==false) {
						teil = teil.replaceFirst(Pattern.quote(part[j]), "");
						found = true;
						pub=part[j];
						index=j;
						continue;
					}
					if(found&&j==index+1&&(part[j].matches("für")||part[j].matches("im")||part[j].matches("des")||part[j].matches("der")||part[j].matches("beim"))){
						teil = teil.replaceFirst(Pattern.quote(part[j]), "");
						pub+=" "+part[j];
						fur=true;
						continue;
					}
					if(fur&&j>=index+2){
						teil = teil.replaceFirst(Pattern.quote(part[j]), "");
						pub+=" "+part[j];
						continue;
					}
				}
				break;
			}
		}
		String output="";
		if(pub.length()>0){
			if(attach){output=speakerAtt(teil, pub);}
			else{output=speaker(teil,pub);
			}
			output=output.replaceAll(" \\s+", " ");
			output=output.replaceAll("> +",">");
			output=output.replaceAll(" +<","<");
			if(output.endsWith(" ")){output=output.substring(0,output.length()-1);}
			if(output.startsWith(" ")){output=output.substring(1,output.length());}
		}
		return output;
	}

	private void parseSpeachAndWriteBack(BufferedReader in, Writer writer) throws IOException {
		int count = 0;
		int id=1;
		String zeile = null;
		boolean openBrace = false;
		String memory = "";
		boolean speech = false;
		boolean sitzung = false;
		boolean berichtigung=false;
		boolean anlage = false;
		while ((zeile = in.readLine()) != null) {
			zeile = zeile.replaceAll("\\s+", " ");
			zeile = zeile.replaceAll("Parteilos", "PARTEILOS");
			if (count == 0 && zeile.matches(".*[0-9]\\. Sitzung.*")) {
				writeXml(writer, String.format("<%s session_number=\"", PROTOCOL)
                                + escapeString(zeile.substring(0, zeile.indexOf("Sitzung") + 7))
                                + String.format("\" >\n%s\n", createTagFrom(HEADER)));
				count++;
				continue;
			}
			if (count == 0) {
				continue;
			}

			if (count == 1 && (zeile.matches(".*[0-9]\\..* [A-Z][a-z]*.* [12][0-9][0-9][0-9].*"))) {
				writeXml(writer, escapeString(zeile)
								+ String.format("\n%s\n%s\n", createClosingTagFrom(HEADER), createTagFrom(AGENDA))
								+ String.format("<%s id=\""+id+"\" >\n", ITEM));
				id++;
				count++;
				continue;
			}
			if (count == 1 && (zeile.matches(".*[Ii]nhalt.*"))) {
				writeXml(writer, String.format("%s\n%s\n%s", createClosingTagFrom(HEADER), createTagFrom(AGENDA), escapeString(zeile))
								+ String.format("<%s id=\""+id+"\" >\n", ITEM));
				id++;
				count++;
				continue;
			}

			if (count == 2 && zeile.matches(".*N.chste Sitzung.*Sitzung.*")) {
				String part = zeile.substring(0, zeile.lastIndexOf("Sitzung"));
				String part2 = zeile.substring(zeile.lastIndexOf("Sitzung"), zeile.length());
				writeXml(writer, String.format("%s\n%s\n%s\n%s\n%s\n", escapeString(part),
																createClosingTagFrom(ITEM),
                                                                 createClosingTagFrom(AGENDA),
                                                                 createTagFrom(SESSION),
                                                                escapeString(part2)));
				count += 2;
				id=1;
				continue;
			}
			if (count == 2 && zeile.matches(".*N.chste Sitzung.*")) {
				count++;
				writeXml(writer, escapeString(zeile + "\n"));
				continue;
			}

			if (count == 2 && zeile.matches(".*Sitzung.*er.ffnet.*")) {
				count += 2;
				id=1;
				writeXml(writer, "</item>\n</agenda>\n<session>\n" + escapeString(zeile) + "\n");
				continue;
			}
			if (count == 3 && zeile.matches(".*Sitzung.*")) {
				count++;
				id=1;
				writeXml(writer, "</item>\n</agenda>\n<session>\n" + escapeString(zeile) + "\n");
				continue;
			}
			if (count == 2 && zeile.matches(".*Sitzung.*")) {
				sitzung = true;
			}
			if (count == 2 && sitzung == true && zeile.matches(".*eröffne.*")) {
				count += 2;
				id=1;
				writeXml(writer, "</item>\n</agenda>\n<session>\n" + escapeString(zeile) + "\n");
				continue;
			}
			if (count == 2
					&& (zeile.matches("[^\\(\\*\\)a-z]*[A-ZÖÄÜ][a-zöäü]*[,][ ]?[A-ZÄÖÜ][^,]*") || zeile
							.matches("[^\\(\\*\\)a-z]*[A-ZÖÄÜ][a-zöäü\\.]* [A-ZÄÖÜ][a-zäöü]*[,][ ]?[A-ZÄÖÜ].*"))
					&& zeile.matches("[A-ZÄÖÜ][A-ZÄÖÜ].*") == false) {
				String pub=publicoff(zeile, true);
				if(pub.length()>0){
				writeXml(writer, pub);
				continue;
				}
			}

			if (count == 2 && zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\).*")
					&& zeile.matches("[A-ZÄÖÜ][A-ZÄÖÜ].*") == false) {
				String pub=publicoff(zeile, true);
				if(pub.length()>0){
				writeXml(writer, pub);
				}else {
					writeXml(writer, speakerAtt(zeile));
				}
				continue;
			}

			if (count == 4
					&& (zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zöäü]*[,\\.]? [A-ZÄÖÜ][a-zäöü]*:.*") || zeile
							.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zöäü]*[,\\.]? [A-ZÄÖÜ][a-zäöü]*[,\\.]? [A-ZÄÖÜ][a-zäöü]*.*:.*"))
					&& zeile.substring(0, zeile.indexOf(":")).split(" ").length < 12 && openBrace == false) {
				String zeilevor = zeile.substring(0, zeile.indexOf(":"));
				if(zeilevor.matches(".*[a-zäöüA-ZÄÖÜ][a-zäöü][a-zäöü][a-zäöü]\\..*")&&zeilevor.indexOf("Dr.")>zeilevor.indexOf(".")){
					writeXml(writer,escapeString(zeilevor.substring(0, zeilevor.indexOf(".")+1)+"\n"));
					zeilevor=zeilevor.substring(zeilevor.indexOf(".")+1);
					zeile=zeile.substring(zeile.indexOf(".")+1);
				}
				
				
				speech = true;
				String pub=publicoff(zeilevor, false);
				if(pub.length()>0){
					writeXml(writer, pub);
					if (zeile.endsWith(":") || zeile.endsWith(": ")) {} 
					else {
						writeXml(writer, escapeString(zeile.substring(zeile.indexOf(":") + 1) + "\n"));
					}
					count++;
					continue;
				}
			}

			if (count == 4 && (zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\)[ ]*:.*")
					|| zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\),.*:.*"))
					&& zeile.substring(0, zeile.indexOf(":")).split(" ").length < 12 && openBrace == false) {
				speech = true;
				String zeilevor = zeile.substring(0, zeile.indexOf(":"));
				if(zeilevor.matches(".*[a-zäöüA-ZÄÖÜ][a-zäöü][a-zäöü][a-zäöü]\\..*")&&zeilevor.indexOf("Dr.")>zeilevor.indexOf(".")){
					writeXml(writer,escapeString(zeilevor.substring(0, zeilevor.indexOf(".")+1)+"\n"));
					zeilevor=zeilevor.substring(zeilevor.indexOf(".")+1);
					zeile=zeile.substring(zeile.indexOf(".")+1);

				}
				String pub=publicoff(zeilevor, false);
				if(pub.length()>0){
					writeXml(writer, pub);
				} else {
					writeXml(writer, speaker(zeile.substring(0, zeile.indexOf(":"))));
				}
				if (zeile.endsWith(":") || zeile.endsWith(": ")) {
				} else {
					writeXml(writer, escapeString(zeile.substring(zeile.indexOf(":") + 1) + "\n"));
				}
				count++;
				continue;
			}

			if (count == 5 && (zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\)[ ]*:.*") 
					|| zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\),.*:.*"))
					&& speech
					&& openBrace == false && zeile.substring(0, zeile.indexOf(":")).split(" ").length < 12) {
				String zeilevor = zeile.substring(0, zeile.indexOf(":"));
				if(zeilevor.matches(".*[a-zäöüA-ZÄÖÜ][a-zäöü][a-zäöü][a-zäöü]\\..*")&&zeilevor.indexOf("Dr.")>zeilevor.indexOf(".")){
					writeXml(writer,escapeString(zeilevor.substring(0, zeilevor.indexOf(".")+1)+"\n"));
					zeilevor=zeilevor.substring(zeilevor.indexOf(".")+1);
					zeile=zeile.substring(zeile.indexOf(".")+1);

				}
				String pub=publicoff(zeilevor, false);
				if(pub.length()>0){
					writeXml(writer, "</speech>\n"+pub);
				} else {
					String scribe = speaker(zeile.substring(0, zeile.indexOf(":")));
					if (change) {writeXml(writer, "</speech>\n" + scribe);}
				}
				if (zeile.endsWith(":") || zeile.endsWith(": ")) {
				} else {
					if (change) {
						writeXml(writer, escapeString(zeile.substring(zeile.indexOf(":") + 1) + "\n"));
					}
				}
				if (change) {continue;}
			}

			if (count == 5
					&& (zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zöäü]*[,\\.]? [A-ZÄÖÜ][a-zäöü]*:.*") || zeile
							.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zöäü]*[,\\.]? [A-ZÄÖÜ][a-zäöü]*[,\\.]? [A-ZÄÖÜ][a-zäöü]*.*:.*")) && speech
					&& openBrace == false && zeile.substring(0, zeile.indexOf(":")).split(" ").length < 12) {
				
				String zeilevor = zeile.substring(0, zeile.indexOf(":"));
				if(zeilevor.matches(".*[a-zäöüA-ZÄÖÜ][a-zäöü][a-zäöü][a-zäöü]\\..*")&&zeilevor.indexOf("Dr.")>zeilevor.indexOf(".")){
					writeXml(writer,escapeString(zeilevor.substring(0, zeilevor.indexOf(".")+1)+"\n"));
					zeilevor=zeilevor.substring(zeilevor.indexOf(".")+1);
					zeile=zeile.substring(zeile.indexOf(".")+1);

				}
				String pub=publicoff(zeilevor, false);
				if(pub.length()>0&&change){
					writeXml(writer, "</speech>\n"+pub);
					if (zeile.endsWith(":") || zeile.endsWith(": ")) {
					} else {
						if (change) {
							writeXml(writer, escapeString(zeile.substring(zeile.indexOf(":") + 1) + "\n"));
						}
					}
					if (change) {
						continue;
					}
				}
			}

			if ((zeile.matches(".*Schlu[ß(ss)] .* Sitzung.*") || zeile.matches(".*Sitzung.*geschlossen.*")) && count == 5) {
				speech = false;
				count++;
				writeXml(writer, escapeString(zeile) + "\n</speech>\n");
				continue;
			}

			if (count == 5 && zeile.startsWith("(")) {
				if (zeile.endsWith(")") || zeile.endsWith(") ") || zeile.endsWith(")'") || zeile.endsWith(")' ")) {
					writeXml(writer, "<interjection>" + escapeString(zeile) + "</interjection>\n");
					continue;
				} else {
					if (zeile.contains(")")) {
					} else {
						openBrace = true;
						memory = "<interjection>\n" + escapeString(zeile) + "\n";
						continue;
					}
				}
			}
			
			if(count==6 && zeile.matches(".*[Aa]nlage.*")){
				anlage=true;
				writeXml(writer, "<attachment>"
						+String.format("\n<%s id=\""+id+"\" >\n", ITEM)
						+escapeString(zeile)+"\n");
				id++;
				count++;
				continue;
			}
			if(count==6){continue;}
			if(count==6 && zeile.matches(".*Berichtigung.*")){
				berichtigung=true;
				writeXml(writer, "<adjustment>\n"+escapeString(zeile)+"\n");
				count++;
				continue;
			}
			
			if(anlage && zeile.matches(" *[Aa]nlage ?[1-9][0-9]?.*")){
				writeXml(writer, String.format("%s\n",createClosingTagFrom(ITEM))
						+String.format("\n<%s id=\""+id+"\" >\n", ITEM));
				id++;
			}
			if(anlage && zeile.matches(".*\\.[Aa]nlage ?[1-9][0-9]?.*")){
				String zeilevor=zeile.substring(0, zeile.lastIndexOf(".")+1);
				writeXml(writer, escapeString(zeilevor+"\n"));
				zeile=zeile.substring(zeile.lastIndexOf(".")+1);
				writeXml(writer, String.format("%s\n",createClosingTagFrom(ITEM))
						+String.format("\n<%s id=\""+id+"\" >\n", ITEM));
				id++;
			}
			
			if(count==7&&anlage&&zeile.matches(".*Berichtigung.*")){
				anlage=false;
				berichtigung=true;
				writeXml(writer, "</item>\n</attachment>\n<adjustment>\n"+escapeString(zeile)+"\n");
				count++;
				continue;
			}
			if(count==7&&berichtigung&&zeile.matches(".*[Aa]nlage.*")){
				berichtigung=false;
				anlage=true;
				writeXml(writer, "</adjustment>\n<attachment>"
						+String.format("\n<%s id=\""+id+"\" >\n", ITEM)
						+escapeString(zeile)+"\n");
				count++;
				continue;
			}
						
			if (openBrace) {
				if (zeile.matches(".*\\).*")) {
					writeXml(writer, memory + escapeString(zeile) + "\n</interjection>\n");
					memory = "";
					openBrace = false;
					continue;
				} else {
					memory += escapeString(zeile);
					continue;
				}
			}
			writeXml(writer, escapeString(zeile) + "\n");
		}
		if (speech) {
			writeXml(writer, "</speech>\n");
		}

		if(anlage){
			writeXml(writer, "</item>\n</attachment>\n");

		}
		if(berichtigung){
			writeXml(writer, "</adjustment>\n");
		}
		writeXml(writer, "</session>\n</protocol>\n");
	}

    private void writeXml(Writer w, String writable) throws IOException {
        w.write(writable);
    }

    private String escapeString(String escapable){
        return StringEscapeUtils.escapeXml11(escapable);
    }

    String createTagFrom(String tagName){
        return String.format("<%s>", tagName);
    }

    String createClosingTagFrom(String tagName){
        return String.format("</%s>", tagName);
    }
}
