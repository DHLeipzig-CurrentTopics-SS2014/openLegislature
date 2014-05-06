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
 * @author jnphilipp
 * @version 0.0.1
 */
public class App {
	private static final String BUNSTAG_PROTOKOLL_URL = "http://dip21.bundestag.de/dip21/btp/{period}/{period}{session}.pdf";
	private static final String BUNSTAG_PROTOKOLL_RSS = "http://www.bundestag.de/rss_feeds/plenarprotokolle.rss";

	public static void main(String[] args) {
		Logger.getInstance();

		PDF2XMLConverter converter = new PDF2XMLConverter();
		if ( !new File(Helpers.getUserDir() + "/data/bundestag").exists() ) {
			Logger.info("Downloading all Bundestag protocols.");
			new File(Helpers.getUserDir() + "/data/bundestag").mkdirs();
			List<String> newProtocls = App.downloadAllBundestag();

			for ( String p : newProtocls ) {
				try {
					Logger.debug("Converting protocol: " + p);
					File f = new File(p);
					converter.setDestFolder(f.getParent());
					converter.processPdf(f);
				}
				catch ( IOException e ) {
					Logger.error("Error while converting pdf to xml.", p, e.toString());
				}
			}
		}
		else {
			Logger.info("Updating bundestag protocols.");
			List<String> newProtocls = App.checkBundestagRSS();
			for ( String p : newProtocls ) {
				try {
					Logger.debug("Converting protocol: " + p);
					File f = new File(p);
					converter.setDestFolder(f.getParent());
					converter.processPdf(f);
				}
				catch ( IOException e ) {
					Logger.error("Error while converting pdf to xml.", p, e.toString());
				}
			}
		}

		/*if ( !new File(Helpers.getUserDir() + "/data/bundesrat").exists() ) {
			new File(Helpers.getUserDir() + "/data/bundesrat").mkdirs();
			App.downloadAllBundesrat();
		}
		else {
			App.checkBundesratRSS();
		}*/
	}

	private static List<String> downloadAllBundestag() {
		List<String> protocols = new ArrayList<>();

		int period = 1, session = 1;
		while ( true ) {
			String p = (period < 10 ? "0" + period : "" + period);
			String s = (session < 10 ? "00" + session : "" + (session < 100 ? "0" + session : "" + session));
			String path = Helpers.getUserDir() + "/data/bundestag/" + p;

			if ( !new File(path).exists() )
				new File(path).mkdirs();

			String file = path + "/" + p + s +".pdf";
			String url = App.BUNSTAG_PROTOKOLL_URL.replaceAll("\\{period\\}", p).replaceAll("\\{session\\}", s);

			if ( !new UrlValidator().isValid(url) )
				session++;
			else {
				try {
					Logger.debug("Downloding protocol: " + p + s);
					Helpers.saveURLToFile(url, file);
					protocols.add(file);
					session++;
				}
				catch ( IOException e ) {
					if ( session == 1 )
						break;

					period++;
					session = 1;
				}
			}
		}

		return protocols;
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