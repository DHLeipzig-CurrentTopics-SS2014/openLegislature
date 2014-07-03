package org.openlegislature.nlp.tokenization;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class Tokenizer {
	public static String[] tokenize(String text) {
		String s = text.replaceAll("[!\"§$%&/\\(\\)=?\\\\\\}\\]\\[\\{+*#';,.:_<>|–]", " ");
		s = s.replaceAll("\n", " ").replaceAll("\\s\\s+", " ");
		return s.split("\\s+");
	}
}