package org.openlegislature.process;

import java.io.File;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.stumbleupon.async.Callback;

public class TxtToXmlConverterCallback implements Callback<File, File> {

    private Provider<TxtToXmlConverter> converterProvider;

    @Inject
	public TxtToXmlConverterCallback(Provider<TxtToXmlConverter> converterProvider) {
        this.converterProvider = converterProvider;
    }
	
	@Override
	public File call(File arg) throws Exception {
		return converterProvider.get().convertToXml(arg);
	}

}
