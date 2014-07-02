package org.openlegislature.process;

import com.google.inject.Inject;
import com.stumbleupon.async.Callback;
import java.io.File;
import org.openlegislature.util.OpenLegislatureConstants;

public class XmlToDBCallback implements Callback<File, File> {
	private OpenLegislatureConstants constants;
	private XmlToDB xmlToDB;

	@Inject
	public XmlToDBCallback(OpenLegislatureConstants constants) {
		this.constants = constants;
		xmlToDB = new XmlToDB();
	}
	
	@Override
	public File call(File xmlFile) throws Exception {
		xmlToDB.process(xmlFile);
		return xmlFile;
	}
}