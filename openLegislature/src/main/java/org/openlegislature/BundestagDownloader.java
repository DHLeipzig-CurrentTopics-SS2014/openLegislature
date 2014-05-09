package org.openlegislature;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.validator.routines.UrlValidator;
import org.openlegislature.util.Helpers;

public class BundestagDownloader {

	private static final String BUNSTAG_PROTOKOLL_URL = "http://dip21.bundestag.de/dip21/btp/{period}/{period}{session}.pdf";
	
	private int maxPeriod;
	private int maxSession;

	private boolean sessionsExeeded; 
	
	public BundestagDownloader() {
		maxPeriod = initparamInt(18, "openlegislature.maxPeriod");
		maxSession = initparamInt(500, "openlegislature.maxPeriod");
	}

	private int initparamInt(int defaultValue, String propertyName) {
		String intValue = System.getProperty(propertyName);
		if(intValue == null){
			return defaultValue;
		} else {
			return Integer.parseInt(intValue);
		}
	}
	
	/**
	 * Returns a list of filenames which have been downloaded. 
	 * 
	 * @return
	 */
	public List<String> downloadAllBundestagIfNotAlreadyDownloaded() {
		List<String> protocols = new ArrayList<>();
		for (int period = 1; period <= maxPeriod; period++){
			String p = createPeriodReplaceable(period);
			String path = Helpers.getUserDir() + App.BUNDESTAG_DEFAULT_DIR + "/" + p;
			createPathIfNeeded(path);
			sessionsExeeded = false;
			for(int session = 1; session <= maxSession && !sessionsExeeded; session++){
				String s = createSessionReplaceable(session);
				String file = path + "/" + p + s +".pdf";
				String url = createCrawlableUrl(p, s);
				if ( new UrlValidator().isValid(url) ) {
					boolean isFileAlreadyExisting = new File(file).exists(); // and therefore already downloaded
					if(!isFileAlreadyExisting){
						downloadProtocolAndAddToResultlist(protocols, file, url);
					}
				}
			}
		}
		return protocols;
	}

	private String createPeriodReplaceable(int period) {
		return period < 10 ? "0" + period : "" + period;
	}
	
	private void createPathIfNeeded(String path) {
		if ( !new File(path).exists() )
			new File(path).mkdirs();
	}
	
	private String createSessionReplaceable(int session) {
		return session < 10 ? "00" + session : "" + (session < 100 ? "0" + session : "" + session);
	}
	
	private String createCrawlableUrl(String p, String s) {
		return BUNSTAG_PROTOKOLL_URL.replaceAll("\\{period\\}", p).replaceAll("\\{session\\}", s);
	}
	
	private void downloadProtocolAndAddToResultlist(List<String> protocols, String file, String url) {
		try {
			Helpers.saveURLToFile(url, file);
			protocols.add(file);
			Logger.debug("Downloaded protocol: " + url + " to file: " + file);
		} catch(FileNotFoundException e){
			sessionsExeeded = true;
			Logger.debug("There was no protocol for URL " + url);
		} catch ( IOException e ) {
			Logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
}
