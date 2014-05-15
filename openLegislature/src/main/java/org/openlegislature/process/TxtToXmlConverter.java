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

	private String speakerAtt(String spline){
		String name="";
		String party="";
		String publicoffice="";
		spline=spline.replaceAll(" \\.", "");
		spline=spline.replaceAll("   *", " ");
		spline=spline.replaceAll(" [0-9][0-9][0-9][0-9]* ?[A-ZÖÄÜ] ?", "");
		spline=spline.replaceAll("\\'", "");
		if(spline.matches(".*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\).*")){
			if(spline.matches(".*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\),.*")){
				publicoffice=spline.substring(spline.lastIndexOf(",")+1);
				spline=spline.substring(0, spline.lastIndexOf(","));
			}
			name=spline.substring(0, spline.indexOf("("));
			if(spline.indexOf("(")==spline.lastIndexOf("(")){
				party=spline.substring(spline.indexOf("(")+1, spline.indexOf(")"));
				if(publicoffice.length()<2){
					spline="<speaker><name>"+name+"</name><party>"+party+"</party></speaker>\n";
				}else{
					spline="<speaker><name>"+name+"</name><party>"+party+"</party><public_office>"+publicoffice+"</public_office></speaker>\n";
				}
			}
			else{
				spline=spline.replace(spline.substring(0, spline.indexOf("(")+1), "");
				String[] part = spline.split("\\)");
				for(int i=0;i<part.length;i++){
					if(part[i].matches(".*[A-ZÖÄÜ][^a-zäöü][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*.*")){
						party=part[i].substring(part[i].indexOf("(")+1);
					}else{
						if(part[i].length()>2)
							if(part[i].contains("(")){name+=part[i]+")";}else{name+="("+part[i]+")";}
						}
				}
			}
			if(publicoffice.length()<2){
				spline="<speaker><name>"+name+"</name><party>"+party+"</party></speaker>\n";
			}else{
				spline="<speaker><name>"+name+"</name><party>"+party+"</party><public_office>"+publicoffice+"</public_office></speaker>\n";
			}
		}else{
			name=spline.split(",")[0];
			publicoffice=spline.split(",")[1];
			String[] part = publicoffice.split(" ");
			publicoffice="";
			for(int i =0; i<part.length;i++){
				if(part[i].length()>1&&part[i].matches("[0-9][0-9][0-9] ?[A-ZÄÖÜ]")==false){publicoffice+=part[i]+" ";}
			}
			if(name.length()>2)spline="<speaker><name>"+name+"</name><public_office>"+publicoffice+"</public_office></speaker>\n";
		}
		return spline;
	}
	
	
	
	private String speaker(String spline){
		String party="";
		String name="";
		String ort="";
		Boolean frau=false;
		Boolean von=false;
		String[] partbefore=spline.split(" ");
		if(spline.matches(".*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\).*")){
			for(int i=0;i<partbefore.length;i++ ){
				if(partbefore[i].matches(".*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\).*")){
					party=partbefore[i].substring(partbefore[i].indexOf("(")+1, partbefore[i].indexOf(")"));
					spline=spline.replaceAll("\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\)", "");
				}
			}
			if(spline.contains("(")&&spline.contains(")")){
				ort=spline.substring(spline.indexOf("("), spline.indexOf(")")+1);
				//System.out.println(spline);
				spline=spline.replaceAll(ort, "");
				spline=spline.replaceAll("\\(", "");
				spline=spline.replaceAll("\\)", "");

			}	
		}
		String[] part=spline.split(" ");

		String publicoffice="";
		for(int i=0; i<part.length;i++){if(part[i].matches(" ?von ?")){von=true;}}
		for(int i=0; i<part.length;i++){
			if(part.length==1){
				name=part[0];
				break;
			}
			
			if(part[i].matches("Frau")){frau=true; continue;}
			
			if(i==0 && part[i].matches("Dr\\.")){
				if(part[i].matches(".*,.*")){part[i]=part[i].replace(",","");}
				name="";publicoffice="";
				if(part.length>2){
					for(int j=0; j<part.length;j++){
						if(part[j].matches(".*,.*")){part[j]=part[j].replace(",","");}
						if(part.length>3||von==false){
							if(j<part.length-1){name+=part[j]+" ";}
							else{if(part[j].length()>2)publicoffice=part[j];}}
						else{
							if(part[i+1].matches(".*,.*")){part[i+1]=part[i+1].replace(",","");}
							name=part[i]+" "+part[i+1]+" "+part[i+2];
							}
					}
				}else{
					if(part[i+1].matches(".*,.*")){part[i+1]=part[i+1].replace(",","");}
					name=part[i]+" "+part[i+1];}
				break;
			}
			if(i!=0 && part[i].matches("Dr\\.")){
				name="";publicoffice="";
				for(int j=i;j<part.length;j++){name+=part[j]+" ";}
				for(int j=0;j<i;j++){if(part[j].matches("Frau")==false){
					if((part[j].length()>2 && von==false)
						||part[j].length()>3){
						publicoffice+=part[j]+" ";}}}
				break;
			}
			if(part[i].matches(".*,.*")){
				name="";publicoffice="";
				part[i]=part[i].replace(",","");
				for(int j=0; j<=i;j++){name+=part[j]+" ";}
				for(int j=i+1; j<part.length;j++){if(part[j].length()>2)publicoffice+=part[j]+" ";}
				//name.replace(",","");
				//publicoffice.replace(",","");
				break;
			}
			if(i==0){
				if(part[i].length()>2)publicoffice=part[i];}
			else{name+=part[i]+" ";}
		}
		if(name.endsWith(" ")){name=name.substring(0, name.length()-1);}
		if(frau){name="Frau "+name;}
		if(ort.length()>1){name+=" "+ort;}
		spline="<speech>\n<speaker>\n<name>"+name+"</name>";
		if(party.length()>0){
			if(party.endsWith(" ")){party=party.substring(0, party.length()-1);}
			spline+="<party>"+party+"</party>";}
		if(publicoffice.length()>0){
			if(publicoffice.endsWith(" ")){publicoffice=publicoffice.substring(0, publicoffice.length()-1);}
			spline+="<public_office>"+publicoffice+"</public_office>";}
		spline+="\n</speaker>\n";
		
		return spline;
	}
	
	
	
	
	File convertToXml(File zuparsen, File outputFile) throws IOException {
		boolean openBrace = false;
		String memory = "";
		boolean speech = false;
		int count = 0;
		BufferedReader in = null;
		Writer writer = null;
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
				
				if(count==2
						&& (zeile.matches("[^\\(\\*\\)a-z]*[A-ZÖÄÜ][a-zöäü]*[,][ ]?[A-ZÄÖÜ][^,]*")
								||zeile.matches("[^\\(\\*\\)a-z]*[A-ZÖÄÜ][a-zöäü.]* [A-ZÄÖÜ][a-zäöü]*[,][ ]?[A-ZÄÖÜ].*")
								|| zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\).*"))
						&& zeile.matches("[A-ZÄÖÜ][A-ZÄÖÜ].*")==false){
					writer.write(speakerAtt(zeile));
					continue;
				}
				
				if (count == 4
						&& (zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zöäü]*[,.]? [A-ZÄÖÜ][a-zäöü]*:.*")
								||zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zöäü]*[,.]? [A-ZÄÖÜ][a-zäöü]*[,.]? [A-ZÄÖÜ][a-zäöü]*.*:.*")
								||zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\).*:.*"))
						&& zeile.substring(0, zeile.indexOf(":")).split(" ").length<5
						&& openBrace==false) {
					speech = true;
					writer.write(speaker(zeile.substring(0, zeile.indexOf(":"))));
					if (zeile.endsWith(":") || zeile.endsWith(": ")) {}
					else {writer.write(zeile.substring(zeile.indexOf(":") + 1) + "\n");}
					count++;
					continue;
				}
				
				if (count == 5
						&& (zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zöäü]*[,.]? [A-ZÄÖÜ][a-zäöü]*:.*")
								||zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zöäü]*[,.]? [A-ZÄÖÜ][a-zäöü]*[,.]? [A-ZÄÖÜ][a-zäöü]*.*:.*")
								||zeile.matches("[^\\(\\*\\)a-z]*[A-ZÄÖÜ][a-zäöü]*.*\\([A-ZÖÄÜ][A-ZÖÄÜ]*.?[A-ZÄÖÜ]*\\).*:.*")) 
						&& speech
						&& openBrace==false
						&& zeile.substring(0, zeile.indexOf(":")).split(" ").length<5) {
					writer.write("</speech>\n"+speaker(zeile.substring(0, zeile.indexOf(":"))));
					if (zeile.endsWith(":") || zeile.endsWith(": ")) {}
					else {writer.write(zeile.substring(zeile.indexOf(":") + 1) + "\n");}
					continue;
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
		return outputFile;

	}

	public File convertToXml(File zuparsen) throws IOException {
		String neu = zuparsen.getPath();
		neu = neu.replace(".txt", ".xml");
		return convertToXml(zuparsen, new File(neu));
	}

}
