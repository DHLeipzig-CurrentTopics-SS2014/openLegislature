package org.openlegislature.process;

import java.io.File;

import com.google.inject.Inject;
import com.stumbleupon.async.Callback;

public class TxtToXmlConverterCallback implements Callback<File, File> {

	@Inject
	public TxtToXmlConverterCallback() {}
	
	@Override
	public File call(File arg) throws Exception {
		return new TxtToXmlConverter().convertToXml(arg);
	}

}
