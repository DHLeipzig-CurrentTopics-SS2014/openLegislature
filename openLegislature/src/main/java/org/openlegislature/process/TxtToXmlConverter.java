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
	String testoff="";
	String testname="";
	String testpart="";
	String [] publicoffices={"kanzler","Präsident","Schriftführer","minister","Staatssekretär","Berichterstatter","präsident"};
	String [] anhaenge={"Frau", "Herr", "Fräulein", "herr", "Dr.","Dr ", "von", "Abgeordnet"};
	Boolean change=false;
	private String speakerAtt(String spline){
		return speakerAtt(spline, "");
	}
	
	private String speakerAtt(String spline, String publicoffice){
		String vorname="";
		String line=spline;
		for(int i=0;i<anhaenge.length;i++){
			if(line.contains(anhaenge[i])){
				String [] part=line.split(" ");
				line=line.replaceAll("[ ,\\.']?[a-zäöüA-ZÄÖÜ ,\\.]*"+anhaenge[i]+"[a-zäöü\\. ]*", "");
				for (int j=0;j<part.length;j++){
					if(part[j].contains(anhaenge[i])){
						if(anhaenge[i].matches(".*[fFH].*")){
							vorname=part[j]+" "+vorname;
						}else{
							vorname+=" "+part[j];
						}
					}
				}
			}
		}
		String name="";
		String party="";
		line=line.replaceAll(" \\.", "");
		line=line.replaceAll("   *", " ");
		line=line.replaceAll("[ ,]?[0-9][0-9][0-9][0-9]* ?[A-ZÖÄÜ]? ?", "");
		line=line.replaceAll("\\'", "");
		line=line.replaceAll("\\s?-\\s?", "-");
		if(line.matches(".*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\).*")){
			if(line.matches(".*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\),.*")){
				line=line.substring(0, line.lastIndexOf(","));
			}
			if(line.substring(0, line.indexOf("(")).split(" ").length<=2){
				name=line.substring(0, line.indexOf("("));
				if(line.indexOf("(")==line.lastIndexOf("(")){
					if(line.indexOf(")")-line.indexOf("(")>2){
						party=line.substring(line.indexOf("(")+1, line.indexOf(")"));
					}else{party=line.charAt(line.indexOf("(")+1)+"";}
				}
				else{
					line=line.replace(line.substring(0, line.indexOf("(")+1), "");
					String[] part = line.split("\\)");
					for(int i=0;i<part.length;i++){
						if(part[i].matches(".*[A-ZÖÄÜ][^a-zäöü][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*.*")){
							party=part[i].substring(part[i].indexOf("(")+1);
							break;
						}else{
							if(part[i].length()>2)
								if(part[i].contains("(")){name+=part[i]+")";}else{name+="("+part[i]+")";}
						}
					}
				}
			}
		}else{
			if(line.split(",")[0].split(" ").length<=2)name=line.split(",")[0];
		}
		if((name.length()>2&&party.length()>0)||(name.length()>2&&publicoffice.length()>0)){
			change=true;
			name=name.replaceAll("[ ]?[\\.,\\?!;:_—][ ]?", "");
			name=vorname+" "+name;
			name=name.replaceAll("  ", " ");
			if(name.startsWith(" ")){name=name.substring(1);}
			if(name.endsWith(" ")||name.endsWith("-")){name=name.substring(0, name.length()-1);}
			if(testname.contains(name)==false){testname+=name+"\n";}
			line="<speaker><name>"+name+"</name>";
			if(party.length()>0){line+="<party>"+party+"</party>";
				if(testpart.contains(party)==false){testpart+=party+"\n";}}
			if(publicoffice.length()>2){
				publicoffice=publicoffice.replaceAll("\\p{Punct}", "");
				line+="<public_office>"+publicoffice+"</public_office>";
				if(testoff.contains(publicoffice)==false){testoff+=publicoffice+"\n";}}
			line+="</speaker>\n";
			spline=line;
		}else{change=false;spline+=" "+publicoffice+" \n";}
		return spline;
	}
	
	private String speaker(String spline){
		return speaker(spline, "");
	}
	
	private String speaker(String spline, String publicoffice){
		spline=spline.replaceAll("[ ]?-[ ]?", "-");
		spline=spline.replaceAll("'", "");
		String line=spline;
		String vorname="";
		for(int i=0;i<anhaenge.length;i++){
			if(spline.contains(anhaenge[i])){
				String [] part=line.split(" ");
				line=line.replaceAll("[ ,\\.']?[a-zäöüA-ZÄÖÜ ,\\.]*"+anhaenge[i]+"[a-zäöü\\. ]*", "");
				for (int j=0;j<part.length;j++){
					if(part[j].contains(anhaenge[i])){
						if(anhaenge[i].matches(".*[fFH].*")){
							vorname=part[j]+" "+vorname;
						}else{
							vorname+=" "+part[j];
						}
					}
				}
			}
		}		
		String party="";
		String name="";
		String ort="";
		if(line.matches(".*\\(CDU CSU\\).*")){line=line.replaceAll("\\(CDU CSU\\)", "\\(CDU/CSU\\)");}
		String[] partbefore=line.split(" ");
		if(line.matches(".*\\([A-ZÖÄÜ]+.?[A-ZÄÖÜ]*\\).*")){
			for(int i=0;i<partbefore.length;i++ ){
				if(partbefore[i].matches(".*\\([A-ZÖÄÜ]+.?[A-ZÄÖÜ]*\\).*")){
					if(partbefore[i].indexOf(")")-partbefore[i].indexOf("(")>2){
						party=partbefore[i].substring(partbefore[i].indexOf("(")+1, partbefore[i].indexOf(")"));
					}else{party=partbefore[i].charAt(partbefore[i].indexOf("(")+1)+"";}
					line=line.replaceAll("\\([A-ZÖÄÜ]+.?[A-ZÄÖÜ]*\\)", "");
				}
			}
		}
		name=line;

			name=name.replaceAll("[ ]?[\\.,\\?!;:_—][ ]?", "");
			if((name.length()>2&&party.length()>0)||(name.length()>2&&publicoffice.length()>0)){
				change=true;
				if(name.endsWith(" ")){name=name.substring(0, name.length()-1);}
				if(ort.length()>1){name+=" "+ort;}
				if(testname.contains(name)==false){testname+=name+"\n";}
				name=vorname+" "+name;
				name=name.replaceAll("  ", " ");
				if(name.startsWith(" ")){name=name.substring(1);}
				if(name.endsWith(" ")||name.endsWith("-")){name=name.substring(0, name.length()-1);}
				line="<speech>\n<speaker>\n<name>"+name+"</name>";
				if(party.length()>0){
					if(party.endsWith(" ")){party=party.substring(0, party.length()-1);}
					if(testpart.contains(party)==false){testpart+=party+"\n";}
					line+="<party>"+party+"</party>";}
				if(publicoffice.length()>0){
					if(publicoffice.endsWith(" ")){publicoffice=publicoffice.substring(0, publicoffice.length()-1);}
					if(testoff.contains(publicoffice)==false){testoff+=publicoffice+"\n";}
					publicoffice=publicoffice.replaceAll("\\p{Punct}", "");
					line+="<public_office>"+publicoffice+"</public_office>";}
				line+="\n</speaker>\n";
				spline=line;
		}else{spline+=" "+publicoffice+" \n";change=false;}
		return spline;
	}

	File convertToXml(File zuparsen, File outputFile) throws IOException {
		boolean openBrace = false;
		String memory = "";
		boolean speech = false;
		int count = 0;
		BufferedReader in = null;
		Writer writer = null;
		//System.out.println(zuparsen);
		try {
			in = new BufferedReader(new FileReader(zuparsen));
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile), "utf-8"));
			String zeile = null;
			while ((zeile = in.readLine()) != null) {
				zeile=zeile.replaceAll("   *", " ");
				if (count == 0 && zeile.matches(".*[0-9]\\. Sitzung.*")) {
					writer.write("<protocol session_number=\""
							+ zeile.substring(0, zeile.indexOf("Sitzung") + 7)
							+ "\" >\n<header>\n");
					count++;
					continue;
				}
				if (count == 0) {
					continue;
				}

				if (count == 1
						&& (zeile.matches(".*[0-9]\\..* [A-Z][a-z]*.* [12][0-9][0-9][0-9].*"))) {
					writer.write(zeile + "\n</header>\n<agenda>\n");
					count++;
					continue;
				}
				if (count == 1
						&& (zeile.matches(".*[Ii]nhalt.*"))) {
					writer.write("</header>\n<agenda>\n"+zeile);
					count++;
					continue;
				}
				
				if(count==2 && zeile.matches(".*Nächste Sitzung.*Sitzung.*")){
					String part=zeile.substring(0, zeile.lastIndexOf("Sitzung"));
					String part2=zeile.substring(zeile.lastIndexOf("Sitzung"), zeile.length());
					writer.write(part+"\n"+"</agenda>\n<session>\n" + part2 + "\n");
					count+=2;
					continue;
				}
				if (count == 2 && zeile.matches(".*Nächste Sitzung.*")) {count++;writer.write(zeile+"\n");continue;}
				if (count == 3 && zeile.matches(".*Sitzung.*")) {
					count++;
					writer.write("</agenda>\n<session>\n" + zeile + "\n");
					continue;
				}
				
				if(count==2 && (zeile.matches("[^\\(\\*\\)a-z]*[A-ZÖÄÜ][a-zöäü]*[,][ ]?[A-ZÄÖÜ][^,]*")
						||zeile.matches("[^\\(\\*\\)a-z]*[A-ZÖÄÜ][a-zöäü.]* [A-ZÄÖÜ][a-zäöü]*[,][ ]?[A-ZÄÖÜ].*"))
						&& zeile.matches("[A-ZÄÖÜ][A-ZÄÖÜ].*")==false){
					boolean found=false;
					for(int i=0;i<publicoffices.length;i++){
						if(zeile.contains(publicoffices[i])){
							String teil=zeile.replaceAll("[ ,\\.']?[a-zäöüA-ZÄÖÜ]*"+publicoffices[i]+"[a-zäöü]*", "");
							String [] part=zeile.split(" ");
							for(int j=0;j<part.length;j++){
								if(part[j].contains(publicoffices[i])){
									writer.write(speakerAtt(teil,part[j]));
									found=true;
									break;
								}
							}
						break;
						}
					}
					if(found)continue;
				}
				
				if(count==2
						&& zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\).*")
						&& zeile.matches("[A-ZÄÖÜ][A-ZÄÖÜ].*")==false){
					boolean found=false;
					for(int i=0;i<publicoffices.length;i++){
						if(zeile.contains(publicoffices[i])){
							String teil=zeile.replaceAll("[ ,\\.']?[a-zäöüA-ZÄÖÜ]*"+publicoffices[i]+"[a-zäöü]*", "");
							String [] part=zeile.split(" ");
							for(int j=0;j<part.length;j++){
								if(part[j].contains(publicoffices[i])){
									writer.write(speakerAtt(teil,part[j]));
									found=true;
									break;
								}
							}
							break;
						}
					}
					if(found){}else{writer.write(speakerAtt(zeile));}
					continue;
				}
				
				if (count == 4
						&& (zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zöäü]*[,.]? [A-ZÄÖÜ][a-zäöü]*:.*")
								||zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zöäü]*[,.]? [A-ZÄÖÜ][a-zäöü]*[,.]? [A-ZÄÖÜ][a-zäöü]*.*:.*"))
						&& zeile.substring(0, zeile.indexOf(":")).split(" ").length<5
						&& openBrace==false) {
					String zeilevor=zeile.substring(0,zeile.indexOf(":"));
					speech = true;
					boolean found=false;
					for(int i=0;i<publicoffices.length;i++){
						if(zeilevor.contains(publicoffices[i])){
							String teil=zeilevor.replaceAll("[ ,\\.\\']?[a-zäöüA-ZÄÖÜ]*"+publicoffices[i]+"[a-zäöü]*", "");
							String [] part=zeilevor.split(" ");
							for(int j=0;j<part.length;j++){
								if(part[j].contains(publicoffices[i])){
									writer.write(speaker(teil,part[j]));
									found=true;
									break;
								}
							}
							break;
						}
					}
					if(found){
						if (zeile.endsWith(":") || zeile.endsWith(": ")) {}
						else {writer.write(zeile.substring(zeile.indexOf(":") + 1) + "\n");}
						count++;
						continue;
					}
				}
				
				if (count == 4
						&& zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\).*:.*")
						&& zeile.substring(0, zeile.indexOf(":")).split(" ").length<5
						&& openBrace==false) {
					speech = true;
					boolean puboffice=false;
					String zeilevor=zeile.substring(0,zeile.indexOf(":"));
					for(int i=0;i<publicoffices.length;i++){
						if(zeilevor.contains(publicoffices[i])){
							String teil=zeilevor.replaceAll("[ ,\\.\\']?[a-zäöüA-ZÄÖÜ]*"+publicoffices[i]+"[a-zäöü]*", "");
							String [] part=zeilevor.split(" ");
							for(int j=0;j<part.length;j++){
								if(part[j].contains(publicoffices[i])){
									writer.write(speaker(teil,part[j]));
									puboffice=true;
									break;
								}
							}
							break;
						}
					}
					if(puboffice){}else{writer.write(speaker(zeile.substring(0, zeile.indexOf(":"))));}
					if (zeile.endsWith(":") || zeile.endsWith(": ")) {}
					else {writer.write(zeile.substring(zeile.indexOf(":") + 1) + "\n");}
					count++;
					continue;
				}
				
				if (count == 5
						&&zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\).*:.*")
						&& speech
						&& openBrace==false
						&& zeile.substring(0, zeile.indexOf(":")).split(" ").length<5) {
					boolean puboffice=false;
					String zeilevor=zeile.substring(0,zeile.indexOf(":"));

					for(int i=0;i<publicoffices.length;i++){
						if(zeilevor.contains(publicoffices[i])){
							String teil=zeilevor.replaceAll("[ ,\\.\\']?[a-zäöüA-ZÄÖÜ]*"+publicoffices[i]+"[a-zäöü]*", "");
							String [] part=zeilevor.split(" ");
							for(int j=0;j<part.length;j++){
								if(part[j].contains(publicoffices[i])){
									String scribe=speaker(teil,part[j]);
									if(change){
										writer.write("</speech>\n"+scribe);
										puboffice=true;
									}else{writer.write(zeile);}
									break;
								}
							}
							break;
						}
					}
					if(puboffice){}
					else{
						String scribe=speaker(zeile.substring(0, zeile.indexOf(":")));
						if(change){
							writer.write("</speech>\n"+scribe);
						}
						//else{writer.write(scribe);}
					}
					if (zeile.endsWith(":") || zeile.endsWith(": ")) {}
					else {
						if(change){
							writer.write(zeile.substring(zeile.indexOf(":") + 1) + "\n");
							}
						}
					if(change){continue;}

				}
		
				if (count == 5
						&& (zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zöäü]*[,.]? [A-ZÄÖÜ][a-zäöü]*:.*")
								||zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zöäü]*[,.]? [A-ZÄÖÜ][a-zäöü]*[,.]? [A-ZÄÖÜ][a-zäöü]*.*:.*")) 
						&& speech
						&& openBrace==false
						&& zeile.substring(0, zeile.indexOf(":")).split(" ").length<5) {
					String zeilevor=zeile.substring(0,zeile.indexOf(":"));
					boolean found=false;
					for(int i=0;i<publicoffices.length;i++){
						if(zeilevor.contains(publicoffices[i])){
							String teil=zeilevor.replaceAll("[ ,\\.\\']?[a-zäöüA-ZÄÖÜ]*"+publicoffices[i]+"[a-zäöü]*", "");
							String [] part=zeilevor.split(" ");
							for(int j=0;j<part.length;j++){
								if(part[j].contains(publicoffices[i])){
									String scribe=speaker(teil,part[j]);
									if(change){
										writer.write("</speech>\n"+scribe);
									//}
									//else{
									//	writer.write(scribe);
									}
									found=true;
									break;
								}
							}
						break;
						}
					}
					if(found){
						if (zeile.endsWith(":") || zeile.endsWith(": ")) {}
						else {
							if(change){writer.write(zeile.substring(zeile.indexOf(":") + 1) + "\n");}
						}
						if(change){continue;}
					}
				}
				
				if ((zeile.matches(".*Schlu[ß(ss)] .* Sitzung.*")
						|| zeile.matches(".*Sitzung.*geschlossen.*"))
						&& count == 5) {
					speech = false;
					count++;
					writer.write(zeile + "\n</speech>\n<attachement>\n");
					continue;
				}
				
				if (count == 5 && zeile.startsWith("(")) {
					if (zeile.endsWith(")") || zeile.endsWith(") ")||zeile.endsWith(")'")||zeile.endsWith(")' ")) {
						writer.write("<interjection>" + zeile
								+ "</interjection>\n");
						continue;
					} else {
						if(zeile.contains(")")){}else{
							openBrace = true;
							memory = "<interjection>\n" + zeile + "\n";
							continue;
						}
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
		//System.out.println("File: "+zuparsen+"\n name: "+testname+"\n office: "+ testoff+"\n Party: "+testpart+"\n");
		return outputFile;
	}

	public File convertToXml(File zuparsen) throws IOException {
		String neu = zuparsen.getPath();
		neu = neu.replace(".txt", ".xml");
		return convertToXml(zuparsen, new File(neu));
	}

}
