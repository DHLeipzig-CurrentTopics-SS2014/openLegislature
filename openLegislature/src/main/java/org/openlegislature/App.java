package org.openlegislature;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.validator.routines.UrlValidator;
import org.openlegislature.process.*;
import org.openlegislature.util.GuiceInjectorRetriever;
import org.openlegislature.util.Helpers;
import org.openlegislature.util.Logger;
import org.openlegislature.util.OpenLegislatureConstants;

import com.google.inject.Injector;
import com.stumbleupon.async.Deferred;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 *
 * @author jnphilipp, dhaeb
 * @version 0.0.2
 */
public class App {
	public static void main(String[] args) throws InterruptedException, IOException {
		downloadProtocolsIfNeeded();
		updateProtocols();
	}

	private static void downloadProtocolsIfNeeded() {
		File bundestagDir = bundestagDir();
		bundestagDir.mkdirs();
		Logger.getInstance().info("Downloading all Bundestag protocols.");
		Injector injector = GuiceInjectorRetriever.getInjector();
		BundestagDownloader downloader = injector.getInstance(BundestagDownloader.class);
		OpenLegislatureConstants constants = injector.getInstance(OpenLegislatureConstants.class);

        for(int period = 1; period <= constants.getMaxPeriod(); period++ ){
            for(int session = 1; session <= constants.getSessionMap().get(period); session++){
                downloadAndConvertIfNeeded(downloader, period, session);
            }
        }

        startCliXPathQueryInterface(injector);
    }

    private static void downloadAndConvertIfNeeded(BundestagDownloader downloader, int period, int session) {
		Deferred<File> futureFile = downloader.downloadProtocolAsynchronously(period, session);
		Injector injector = GuiceInjectorRetriever.getInjector();
		PdfToTxtConverterCallback pdf2txtConverter = injector.getInstance(PdfToTxtConverterCallback.class);
		TxtToXmlConverterCallback txt2xmlConverter = injector.getInstance(TxtToXmlConverterCallback.class);
        XPathQueryEngineCallback xmlQueryEngine = injector.getInstance(XPathQueryEngineCallback.class);
        futureFile.addCallbacks(pdf2txtConverter, new ThrowableCallback("An error occured in the pdf to txt converting stage:"))
		          .addCallbacks(txt2xmlConverter, new ThrowableCallback("An error occured in the txt to xml converting stage:"))
                  .addCallbacks(xmlQueryEngine, new ThrowableCallback("An error occured in the xml query stage"));
    }

    private static void startCliXPathQueryInterface(Injector injector) {
        XPathQueryEngine queryEngine = injector.getInstance(XPathQueryEngine.class);
        Scanner inscanner = new Scanner(System.in);
        System.out.println("XPathEval");
        String currentQuery = null;
        do {
            System.out.print("> ");
            currentQuery = inscanner.nextLine().trim();
            try {
                queryEngine.query(currentQuery);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (!currentQuery.equals("quit"));
        inscanner.close();
    }

	private static void updateProtocols() {
		Pdf2TxtConverter converter = GuiceInjectorRetriever.getInjector().getInstance(Pdf2TxtConverter.class);
		Logger.getInstance().info("Updating bundestag protocols.");
		List<String> newProtocls = App.checkBundestagRSS();
		for ( String p : newProtocls ) {
			try {
				Logger.getInstance().debug("Converting protocol: " + p);
				convertPdfProtocolToTxt(converter, p);
			}
			catch ( IOException e ) {
				Logger.getInstance().error("Error while converting pdf to xml.", p, e.toString());
			}
		}
	}
	
	private static void convertPdfProtocolToTxt(Pdf2TxtConverter converter, String p) throws IOException {
		File f = new File(p);
		converter.processPdfWhenNotAlreadyDone(f);
	}

	private static File bundestagDir() {
		return new File(Helpers.getUserDir() + OpenLegislatureConstants.BUNDESTAG_DEFAULT_DIR);
	}

	private static List<String> checkBundestagRSS() {
		List<String> protocols = new ArrayList<>();

		try {
			XmlReader xmlReader = new XmlReader(new URL(OpenLegislatureConstants.BUNSTAG_PROTOKOLL_RSS));
			SyndFeed syndFeed = new SyndFeedInput().build(xmlReader);

			Collection<?> syndEntries = syndFeed.getEntries();
			for ( Object entry : syndEntries ) {
				if ( !new UrlValidator().isValid(((SyndEntry)entry).getLink()) )
					continue;

				String name = new File(((SyndEntry)entry).getLink()).getName();

				Logger.getInstance().debug("Protocol: " + name);

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
			Logger.getInstance().error(e.toString());
		}

		return protocols;
	}
	
}