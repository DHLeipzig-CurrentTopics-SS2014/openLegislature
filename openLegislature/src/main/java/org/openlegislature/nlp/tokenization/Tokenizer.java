package org.openlegislature.nlp.tokenization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class Tokenizer {
	public static String[] tokenize(String text) {
		text = replace("[!\"§$%&/\\(\\)=?\\\\\\}\\]\\[\\{+*#';,.:_<>|–“”„]", " ", text);
		text = replace("\\s*\n\\s*", " ", text);
		text = replace("\\s(\\d+|-)\\s", " ", text);
		text = replace("\\s\\s+", " ", text);

		return text.trim().split("\\s+");
	}

	private static String replace(String pattern, String replacement, String text) {
		Matcher m = Pattern.compile(pattern).matcher(text);
		while ( m.find() ) {
			text = m.replaceAll(replacement);
			m = Pattern.compile(pattern).matcher(text);
		}

		return text;
	}
}