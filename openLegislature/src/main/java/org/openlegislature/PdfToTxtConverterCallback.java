package org.openlegislature;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.google.inject.Inject;
import com.stumbleupon.async.Callback;

/**
 * Class to use the PDF2XMLConverter and chain it to an instance of deferred. 
 * The call method starts is triggered by the deferred if the resulting file was downloaded succesfully. 
 * Afterwards the file is processed asynchronously. 
 * In the moment, the process contains convert the PDF to txt and clean it using a simple regex.
 * For details, {@link PDF2XMLConverter} 
 * @author dhaeb
 *
 */
public class PdfToTxtConverterCallback implements Callback<File, File> {

	private ExecutorService e;

	@Inject
	public PdfToTxtConverterCallback(ExecutorService e) {
		this.e = e;
	}
	
	@Override
	public File call(final File arg) throws Exception {
		e.submit(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName(String.format("Converter Thread for file %s", arg.getName()));
				PDF2XMLConverter converter = GuiceInjectorRetriever.getInjector().getInstance(PDF2XMLConverter.class);
				try {
					converter.processPdfWhenNotAlreadyDone(arg);
					Logger.getInstance().info("Finished file: " + arg);
				} catch (IOException e) {
					Logger.getInstance().error("Error while converting pdf to xml." + arg, e.toString());
				}
			}
		});
		return arg;
	}
	
}