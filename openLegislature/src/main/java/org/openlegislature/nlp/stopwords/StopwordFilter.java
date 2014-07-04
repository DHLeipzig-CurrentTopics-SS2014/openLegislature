package org.openlegislature.nlp.stopwords;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.openlegislature.io.FileReader;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class StopwordFilter {
	private Set<String> stopwords;

	public StopwordFilter() {}

	public StopwordFilter(String stopwordListFile) throws IOException {
		this.stopwords = new LinkedHashSet<>(Arrays.asList(FileReader.readLines(stopwordListFile)));
	}

	public void loadStopwordList(String file) throws IOException {
		this.stopwords = new LinkedHashSet<>(Arrays.asList(FileReader.readLines(file)));
	}

	public String[] filter(String[] tokens) {
		List<String> filteredTokens = new LinkedList<>();
		for ( String token : tokens )
			if ( !this.stopwords.contains(token) )
				filteredTokens.add(token);

		return filteredTokens.toArray(new String[filteredTokens.size()]);
	}
}