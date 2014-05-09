package org.openlegislature;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.validator.routines.UrlValidator;
import org.openlegislature.util.Helpers;

/**
 *
 * @author jnphilipp, dhaeb
 * @version 0.0.2
 */
public class App {
	public static final String BUNDESTAG_DEFAULT_DIR = "/data/bundestag";
	private static final String BUNSTAG_PROTOKOLL_RSS = "http://www.bundestag.de/rss_feeds/plenarprotokolle.rss";

	public static void main(String[] args) {
		Logger.getInstance();
		downloadProtocolsIfNeeded();
		updateProtocols();

		/*if ( !new File(Helpers.getUserDir() + "/data/bundesrat").exists() ) {
			new File(Helpers.getUserDir() + "/data/bundesrat").mkdirs();
			App.downloadAllBundesrat();
		}
		else {
			App.checkBundesratRSS();
		}*/
	}

	private static void downloadProtocolsIfNeeded() {
		PDF2XMLConverter converter = new PDF2XMLConverter();
		File bundestagDir = bundestagDir();
		Logger.info("Downloading all Bundestag protocols.");
		bundestagDir.mkdirs();
		List<String> newProtocls = new BundestagDownloader().downloadAllBundestagIfNotAlreadyDownloaded();
		for (String p : newProtocls) {
			try {
				Logger.debug("Converting protocol: " + p);
				convertPdfProtocolToTxt(converter, p);
			} catch (IOException e) {
				Logger.error("Error while converting pdf to xml.", p, e.toString());
				e.printStackTrace();
			}
		}
	}

	private static void convertPdfProtocolToTxt(PDF2XMLConverter converter, String p) throws IOException {
		File f = new File(p);
		converter.setDestFolder(f.getParent());
		converter.processPdf(f);
	}

	private static void updateProtocols() {
		PDF2XMLConverter converter = new PDF2XMLConverter();
		Logger.info("Updating bundestag protocols.");
		List<String> newProtocls = App.checkBundestagRSS();
		for ( String p : newProtocls ) {
			try {
				Logger.debug("Converting protocol: " + p);
				convertPdfProtocolToTxt(converter, p);
			}
			catch ( IOException e ) {
				Logger.error("Error while converting pdf to xml.", p, e.toString());
			}
		}
	}

	private static File bundestagDir() {
		return new File(Helpers.getUserDir() + BUNDESTAG_DEFAULT_DIR);
	}

	private static List<String> checkBundestagRSS() {
		List<String> protocols = new ArrayList<>();

		try {
			XmlReader xmlReader = new XmlReader(new URL(App.BUNSTAG_PROTOKOLL_RSS));
			SyndFeed syndFeed = new SyndFeedInput().build(xmlReader);

			Collection<?> syndEntries = syndFeed.getEntries();
			for ( Object entry : syndEntries ) {
				if ( !new UrlValidator().isValid(((SyndEntry)entry).getLink()) )
					continue;

				String name = new File(((SyndEntry)entry).getLink()).getName();

				Logger.debug("Protocol: " + name);

				String period = name.substring(0, 2);
				String file = Helpers.getUserDir() + "/data/bundestag/" + period + "/" + name;
				if ( !new File(file).exists() ) {
					new File(file).getParentFile().mkdirs();
					Helpers.saveURLToFile(((SyndEntry)entry).getLink(), file);
					protocols.add(file);
				}
			}
		}
		catch ( IllegalArgumentException | IOException | FeedException e ) {
			Logger.error(e.toString());
		}

		return protocols;
	}

	private static void downloadAllBundesrat() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	private static void checkBundesratRSS() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}