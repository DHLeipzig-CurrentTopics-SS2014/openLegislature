package org.openlegislature.process;

import java.io.File;

import org.openlegislature.mongo.XmlToJsonParser;
import org.openlegislature.util.OpenLegislatureConstants;

import com.google.inject.Inject;
import com.stumbleupon.async.Callback;

public class XmlToMongoCallback implements Callback<File, File> {

	private OpenLegislatureConstants constants;
	private XmlToJsonParser xmlToJsonParser;

	@Inject
	public XmlToMongoCallback(OpenLegislatureConstants constants) {
		this.constants = constants;
		xmlToJsonParser = new XmlToJsonParser();
	}
	
	@Override
	public File call(File xmlFile) throws Exception {
		xmlToJsonParser.process(constants.getCollectionName(), xmlFile);
		return xmlFile;
	}

}
