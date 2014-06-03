package org.openlegislature.mongo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
 
public class XmlToJsonParser {
 

	public XmlToJsonParser() {}

	public JSONObject parseXml(File xmlFile) throws JSONException, IOException{
		return XML.toJSONObject(IOUtils.toString(new FileInputStream(xmlFile)));
	}
	
	public JSONObject parseXml(String filepath){
		String content = readFile(filepath);
		return XML.toJSONObject(content);
	}
	
	public void writeToMongo(String collection, JSONObject json){
		new MongoCon().insert(collection, json.toString());
	}
	
	public void process(String colName, File xmlFile) throws JSONException, IOException {
		writeToMongo(colName, parseXml(xmlFile));
	}
	
	private String readFile(String filepath){
		
		BufferedReader br;
		String ret="";
		try {
			br = new BufferedReader(new FileReader(filepath));
			
			 StringBuilder sb = new StringBuilder();
		        String line = br.readLine();

		        while (line != null) {
		            sb.append(line);
		            sb.append(System.lineSeparator());
		            line = br.readLine();
		        }
		        ret = sb.toString();
		        br.close();
		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    return ret;
	}
 
}