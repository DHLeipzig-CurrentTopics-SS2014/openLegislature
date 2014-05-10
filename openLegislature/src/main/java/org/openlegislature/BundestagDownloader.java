package org.openlegislature;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;

import org.apache.commons.validator.routines.UrlValidator;
import org.openlegislature.util.Helpers;

import com.google.inject.Inject;
import com.stumbleupon.async.Deferred;

/**
 * Downloader for specific protocols of the German Bundestag. 
 * Downloads asynchronously. 
 * It is threadsafe. 
 * 
 * @author dhaeb
 *
 */
public class BundestagDownloader {

	private static final String BUNSTAG_PROTOKOLL_URL = "http://dip21.bundestag.de/dip21/btp/{period}/{period}{session}.pdf";
	
	@Inject
	private ExecutorService e;

	@Inject
	public BundestagDownloader() {}
	
	/**
	 * Method to download a specific protocol in asynchronous fashion.  
	 * 
	 * @param period The period for which a protocol should be downloaded
	 * @param session The session identifying the protocol
	 * @return A instance of type {@link Deferred} which triggers the appended callbacks automatically when the downloaded file is available
	 */
	public Deferred<File> downloadProtocolAsynchronously(final int period, final int session){
		final Deferred<File> d = new Deferred<File>();
		e.submit(new Runnable(){
			@Override
			public void run() {
				Thread.currentThread().setName(String.format("DownloaderThread for period %d and session %d", period, session));
				try {
					d.callback(downloadProtocols(period, session));
					Logger.getInstance().debug("Downloaded successfully finished");
				} catch (IOException e) {
					d.callback(e);
					Logger.getInstance().error(String.format("Download failed for period %d and session %d", period, session));
					Logger.getInstance().error(e.getMessage());
				}
			}});
		return d;
	}
	
	private File downloadProtocols(int period, int session) throws IOException {
		String p = createPeriodReplaceable(period);
		String s = createSessionReplaceable(session);
		String path = Helpers.getUserDir() + App.BUNDESTAG_DEFAULT_DIR + "/" + p;
		createPathIfNeeded(path);
		String filePath = createPdfFilePath(p, s, path);
		File file = new File(filePath);
		String url = createCrawlableUrl(p, s);
		if(new UrlValidator().isValid(url)){
			if(!file.exists()) {
				Helpers.saveURLToFile(url, filePath);
			}
			return file;
		} else {
			throw new MalformedURLException("The url was not valid");
		}
	}
	
	private String createPeriodReplaceable(int period) {
		return period < 10 ? "0" + period : "" + period;
	}
	
	private void createPathIfNeeded(String path) {
		if ( !new File(path).exists() )
			new File(path).mkdirs();
	}
	
	private String createPdfFilePath(String p, String s, String path) {
		StringBuilder pathToTargetFile = new StringBuilder();
		pathToTargetFile.append(path);
		pathToTargetFile.append("/");
		pathToTargetFile.append(p);
		pathToTargetFile.append(s);
		pathToTargetFile.append(".pdf");
		String filePath = pathToTargetFile.toString();
		return filePath;
	}
	
	private String createSessionReplaceable(int session) {
		return session < 10 ? "00" + session : "" + (session < 100 ? "0" + session : "" + session);
	}
	
	private String createCrawlableUrl(String p, String s) {
		return BUNSTAG_PROTOKOLL_URL.replaceAll("\\{period\\}", p).replaceAll("\\{session\\}", s);
	}
}
