package org.openlegislature;

import java.io.File;
import java.io.IOException;
import org.openlegislature.util.Helpers;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class App {
	private static final String BUNSTAG_PROTOKOLL_URL = "http://dip21.bundestag.de/dip21/btp/{period}/{period}{session}.pdf";

	public static void main(String[] args) {
		Logger.getInstance();

		if ( !new File(Helpers.getUserDir() + "/data/bundestag").exists() ) {
			new File(Helpers.getUserDir() + "/data/bundestag").mkdirs();
			App.downloadAllBundestag();
		}
		else {
			App.checkBundestagRSS();
		}

		if ( !new File(Helpers.getUserDir() + "/data/bundesrat").exists() ) {
			new File(Helpers.getUserDir() + "/data/bundesrat").mkdirs();
			App.downloadAllBundesrat();
		}
		else {
			App.checkBundesratRSS();
		}
	}

	private static void downloadAllBundestag() {
		int period = 1, session = 1;
		while ( true ) {
			String p = (period < 10 ? "0" + period : "" + period);
			String s = (session < 10 ? "00" + session : "" + (session < 100 ? "0" + session : "" + session));
			String path = Helpers.getUserDir() + "/data/bundestag/" + p;

			if ( !new File(path).exists() )
				new File(path).mkdirs();

			String file = path + "/" + p + s +".pdf";
			String url = App.BUNSTAG_PROTOKOLL_URL.replaceAll("\\{period\\}", p).replaceAll("\\{session\\}", s);

			try {
				Logger.debug("Downloding protokol: " + p + s);
				Helpers.saveURLToFile(url, file);
				session++;
			}
			catch ( IOException e ) {
				period++;
				session = 1;
			}
		}
	}

	private static void checkBundestagRSS() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	private static void downloadAllBundesrat() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	private static void checkBundesratRSS() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}