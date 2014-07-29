package org.openlegislature.parser.html;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openlegislature.parser.Parser;

/**
 *
 * @author jnphilipp
 * @version 1.7.6
 */
public class HTMLParser implements Parser {
	public static final String DEFAULT_USER_AGENT = "Mozilla/5.0";
	public static final int DEFAULT_TIMEOUT = 10000;
	/**
	 * response code
	 */
	protected int responseCode = 0;
	/**
	 * Code of the web page.
	 */
	protected String code = "";
	/**
	 * User-Agent which is used when connecting to the page.
	 */
	protected String userAgent = "";
	/**
	 * content type
	 */
	protected String contentType = "";
	/**
	 * timeout
	 */
	protected int timeout;

	/**
	 * Default constructor.
	 */
	public HTMLParser() {
		this.setDefaultUserAgent();
		this.setDefaultTimeout();
	}

	/**
	 * Creates a new HTMLParser and fetches the given site.
	 * @param url URL which will be fetched
	 * @throws java.lang.Exception
	 */
	public HTMLParser(String url) throws Exception {
		this.setDefaultUserAgent();
		this.setDefaultTimeout();
		this.fetch(url);
	}

	/**
	 * Creates a new HTMLParser and fetches the given site.
	 * @param url URL which will be fetched
	 * @param decodeHTML if <code>true</code> HTML encoded characters will be decoded.
	 * @throws java.lang.Exception
	 */
	public HTMLParser(String url, boolean decodeHTML) throws Exception {
		this.setDefaultUserAgent();
		this.setDefaultTimeout();
		this.fetch(url, decodeHTML);
	}

	/**
	 * Sets the User-Agent to the given agent.
	 * @param agent New User-Agent to use.
	 */
	public void setUserAgent(String agent) {
		this.userAgent = agent;
	}

	/**
	 * Sets the User-Agent to the default User-Agent.
	 */
	public void setDefaultUserAgent() {
		this.userAgent = HTMLParser.DEFAULT_USER_AGENT;
	}

	/**
	 * Sets the User-Agent to the given agent.
	 * @param agent New User-Agent to use.
	 */
	public void setTimeout(String agent) {
		this.userAgent = agent;
	}

	/**
	 * Sets the timeout to the default timeout.
	 */
	public void setDefaultTimeout() {
		this.timeout = HTMLParser.DEFAULT_TIMEOUT;
	}

	/**
	 * Returns the web pages code.
	 * @return code
	 */
	public String getCode() {
		return this.code;
	}

	/**
	 * Returns the content type.
	 * @return content type
	 */
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public int getResponseCode() {
		return this.responseCode;
	}

	@Override
	public boolean isResponseCodeOK() {
		return this.responseCode == HttpURLConnection.HTTP_OK;
	}

	/**
	 * Builds a connection to the given URL and retrieves it. If a user-agent is given it will be used.
	 * @param url URL which will be fetched
	 * @throws Exception 
	 */
	@Override
	public void fetch(String url) throws Exception {
		this.fetch(url, false, Proxy.NO_PROXY);
	}

	/**
	 * Builds a connection to the given URL and retrieves it. If a user-agent is given it will be used.
	 * @param url URL which will be fetched
	 * @param decodeHTML if <code>true</code> the HTML special will be decoded
	 * @throws Exception
	 */
	public void fetch(String url, boolean decodeHTML) throws Exception {
		this.fetch(url, decodeHTML, Proxy.NO_PROXY);
	}

	/**
	 * Builds a connection to the given URL using the given proxy and retrieves it. If a user-agent is given it will be used.
	 * @param url URL which will be fetched
	 * @param proxy proxy
	 * @throws Exception 
	 */
	@Override
	public void fetch(String url, Proxy proxy) throws Exception {
		this.fetch(url, false, proxy);
	}

	/**
	 * Builds a connection to the given URL and retrieves it. If a user-agent is given it will be used.
	 * @param url URL which will be fetched
	 * @param decodeHTML if <code>true</code> the HTML special will be decoded
	 * @param proxy proxy
	 * @throws Exception 
	 */
	public void fetch(String url, boolean decodeHTML, Proxy proxy) throws Exception {
		if ( proxy == null )
			proxy = Proxy.NO_PROXY;

		URL u = new URL(url);
		HttpURLConnection con = (HttpURLConnection)u.openConnection(proxy);
		con.setConnectTimeout(this.timeout);
		con.setReadTimeout(this.timeout);

		if ( !this.userAgent.equals("") )
			con.setRequestProperty("User-Agent", this.userAgent);

		con.setRequestProperty("Accept-Charset", "UTF-8");
		con.connect();
		String header = con.getHeaderField("Content-Type");
		String charset = "utf-8";

		if ( header != null ) {
			if ( header.contains("ISO-8859-15") )
  			charset = "ISO-8859-15";
			else if ( header.contains("ISO-8859-1") )
				charset = "ISO-8859-1";
		}

		this.contentType = header;
		this.responseCode = con.getResponseCode();

		if ( this.responseCode == HttpURLConnection.HTTP_OK ) {
			InputStreamReader in = new InputStreamReader(con.getInputStream(), charset);
			BufferedReader buff = new BufferedReader(in);

			String line;
			StringBuilder text = new StringBuilder();

			while ( (line = buff.readLine()) != null ) {
				text.append(line);
				text.append("\n");
			}

			buff.close();
			in.close();

			this.code = text.toString().replace("\0", " ").replace("\u2028", "\n").replace(String.valueOf((char)160), " ");
		}
		con.disconnect();

		if ( decodeHTML )
			this.code = this.decode();
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @return list of tags
	 */
	public List<String> getTags(String tag) {
		return getTags(tag, "", false, this.code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @return first occurrence for given tag
	 */
	public String getFirstTag(String tag) {
		List<String> tags = getTags(tag, "", false, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @return list of tags
	 */
	public List<String> getTags(String tag, boolean clean) {
		return getTags(tag, "", clean, this.code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @return first occurrence for given tag
	 */
	public String getFirstTag(String tag, boolean clean) {
		List<String> tags = getTags(tag, "", clean, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @return list of tags
	 */
	public List<String> getTags(String tag, String param) {
		return getTags(tag, param, false, this.code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @return first occurrence for given tag
	 */
	public String getFirstTag(String tag, String param) {
		List<String> tags = getTags(tag, param, false, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @return list of tags
	 */
	public List<String> getTags(String tag, String param, boolean clean) {
		return getTags(tag, param, clean, this.code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @return first occurrence for given tag
	 */
	public String getFirstTag(String tag, String param, boolean clean) {
		List<String> tags = getTags(tag, param, clean, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param code The HTML-code which will be searched.
	 * @return list of tags
	 */
	public static List<String> getTags(String tag, String param, String code) {
		return getTags(tag, param, false, code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param code The HTML-code which will be searched.
	 * @return first occurrence for given tag
	 */
	public static String getFirstTag(String tag, String param, String code) {
		List<String> tags = getTags(tag, param, false, code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param code The HTML-code which will be searched.
	 * @return list of tags
	 */
	public static List<String> getTags(String tag, boolean clean, String code) {
		return getTags(tag, "", clean, code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param codes The HTML-codes which will be searched.
	 * @return list of tags
	 */
	public static List<String> getTags(String tag, boolean clean, Collection<String> codes) {
		List<String> tags = new ArrayList<>();
		for ( String code : codes )
			tags.addAll(getTags(tag, "", clean, code));

		return tags;
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param code The HTML-code which will be searched.
	 * @return first occurrence for given tag
	 */
	public static String getFirstTag(String tag, boolean clean, String code) {
		List<String> tags = getTags(tag, "", clean, code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param code The HTML-code which will be searched.
	 * @return list of tags
	 */
	public static List<String> getTags(String tag, String param, boolean clean, String code) {
		ArrayList<String> l = new ArrayList<>();

		int i = -1;

		while ( (i = code.indexOf("<" + tag, i + 1)) != -1 ) {
			if ( !param.equals("") ) {
				if ( !code.substring(i, code.indexOf(">", i)).contains(param) ) {
					i = code.indexOf(">", i);
					continue;
				}
			}

			int j = code.indexOf("</" + tag, i);
			String s = code.substring(i, code.indexOf(">", j) + 1);

			int k = tag.length();

			while ( (k = s.indexOf("<" + tag, k)) != -1 ) {
				k = s.indexOf(">", k);
				j = code.indexOf("</" + tag, j + tag.length());

				if ( j < 0 )
					break;

				s = code.substring(i, code.indexOf(">", j) + 1);
			}

			if ( j < 0 ) {
				i += tag.length() + param.length();
				continue;
			}

			if ( clean )
				s = s.replaceAll("<.*?>", "");

			if ( !s.equals("") )
				l.add(s);

			i = j;
		}

		return l;
	}

	/**
	 * 
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns.
	 * @param tag tag
	 * @param param A parameter the tags must contain.
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param code The HTML-code which will be searched.
	 * @return first occurrence for given tag
	 */
	public static String getFirstTag(String tag, String param, boolean clean, String code) {
		List<String> tags = getTags(tag, param, clean, code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @return list of tags
	 */
	public List<String> getOnlyTags(String tag) {
		return getOnlyTags(tag, false, this.code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @return first occurrence for given tag
	 */
	public String getFirstOnlyTag(String tag) {
		List<String> tags = getOnlyTags(tag, false, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @return list of tags
	 */
	public List<String> getOnlyTags(String tag, boolean clean) {
		return getOnlyTags(tag, clean, this.code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @return first occurrence for given tag
	 */
	public String getFirstOnlyTag(String tag, boolean clean) {
		List<String> tags = getOnlyTags(tag, clean, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @param code The HTML-code which will be searched.
	 * @return list of tags
	 */
	public static List<String> getOnlyTags(String tag, String code) {
		return getOnlyTags(tag, false, code);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @param code The HTML-code which will be searched.
	 * @return first occurrence for given tag
	 */
	public String getFirstOnlyTag(String tag, String code) {
		List<String> tags = getOnlyTags(tag, false, code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param code The HTML-code which will be searched.
	 * @return list of tags
	 */
	public static List<String> getOnlyTags(String tag, boolean clean, String code) {
		ArrayList<String> l = new ArrayList<>();

		int i = -1;

		while ( (i = code.indexOf("<" + tag + ">", i + 1)) != -1 ) {
			int j = code.indexOf("</" + tag + ">", i);
			String s = code.substring(i, code.indexOf(">", j) + 1);

			int k = tag.length();

			while ( (k = s.indexOf("<" + tag + ">", k)) != -1 ) {
				k = s.indexOf(">", k);
				j = code.indexOf("</" + tag + ">", j + tag.length());

				if ( j < 0 )
					break;

				s = code.substring(i, code.indexOf(">", j) + 1);
			}

			if ( clean )
				s = s.replaceAll("<.*?>", "");

			if ( !s.equals("") )
				l.add(s);

			i = j;
		}

		return l;
	}

	/**
	 * Returns a list of the entire code beginning with the given tag and ending with the corresponding closing tag. By nested tags the most outer tag with all containing code will be returns. Makes sure that the HTML-tags only contains the given value.
	 * @param tag HTML-tag like b, i, div
	 * @param clean If <code>true</code> all HTML-code will be cleaned.
	 * @param code The HTML-code which will be searched.
	 * @return first occurrence for given tag
	 */
	public String getFirstOnlyTag(String tag, boolean clean, String code) {
		List<String> tags = getOnlyTags(tag, clean, code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @return list of tag occurrences
	 */
	public List<String> getTagsWithoutEnd(String tag) {
		return getTagsWithoutEnd(tag, "", "", this.code);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @return first occurrences of given tag
	 */
	public String getFirstTagWithoutEnd(String tag) {
		List<String> tags = getTagsWithoutEnd(tag, "", "", this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @return list of tag occurrences
	 */
	public List<String> getTagsWithoutEnd(String tag, String param) {
		return getTagsWithoutEnd(tag, param, "", this.code);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @return first occurrences of given tag
	 */
	public String getFirstTagWithoutEnd(String tag, String param) {
		List<String> tags = getTagsWithoutEnd(tag, param, "", this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param returnContent regular expression for the content that should be returned
	 * @return list of tag occurrences
	 */
	public List<String> getTagsWithoutEnd(String tag, String param, String returnContent) {
		return getTagsWithoutEnd(tag, param, returnContent, this.code);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param returnContent regular expression for the content that should be returned
	 * @return first occurrences of given tag
	 */
	public String getFirstTagWithoutEnd(String tag, String param, String returnContent) {
		List<String> tags = getTagsWithoutEnd(tag, param, returnContent, this.code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param returnContent regular expression for the content that should be returned
	 * @param code The HTML-code which will be searched.
	 * @return list of tag occurrences
	 */
	public static List<String> getTagsWithoutEnd(String tag, String param, String returnContent, String code) {
		List<String> l = new ArrayList<>();

		Pattern p = Pattern.compile("<" + Pattern.quote(tag) + "[^>]*?" + Pattern.quote(param) + "[^>]*?>");
		Matcher m = p.matcher(code);
		while ( m.find() ) {
			if ( returnContent.isEmpty() )
				l.add(m.group());
			else {
				p = Pattern.compile(returnContent);
				Matcher mrc = p.matcher(m.group());
				while ( mrc.find() )
					l.add(mrc.group(mrc.groupCount()));
			}
		}

		return l;
	}

	/**
	 * Returns a list of the entire code of the given tag.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param returnContent regular expression for the content that should be returned
	 * @param code The HTML-code which will be searched.
	 * @return first occurrences of given tag
	 */
	public static String getFirstTagWithoutEnd(String tag, String param, String returnContent, String code) {
		List<String> tags = getTagsWithoutEnd(tag, param, returnContent, code);

		if ( tags.isEmpty() )
			return "";

		return tags.get(0);
	}

	/**
	 * Removes all tags from the parsed code.
	 * @return clean code
	 */
	public String removeAllTags() {
		return removeAllTags(this.code);
	}

	/**
	 * Removes all tags from the given code.
	 * @param code code
	 * @return clean code
	 */
	public static String removeAllTags(String code) {
		return code.replaceAll("<[^>]*>", "");
	}

	/**
	 * Remove all occurrences of the given tag in the code.
	 * @param tag HTML-tag like b, i, div
	 * @return cleaned code
	 */
	public String replaceTagContent(String tag) {
		return this.replaceTagContent(tag, "", this.code);
	}

	/**
	 * Remove all occurrences of the given tag in the code.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @return cleaned code
	 */
	public String replaceTagContent(String tag, String param) {
		return this.replaceTagContent(tag, param, this.code);
	}

	/**
	 * Remove all occurrences of the given tag in the code.
	 * @param tag HTML-tag like b, i, div
	 * @param param A parameter the tags must contain.
	 * @param code The HTML-code which will be searched.
	 * @return cleaned code
	 */
	public String replaceTagContent(String tag, String param, String code) {
		String returnCode = code;
		List<String> tags = getTags(tag, param, code);
		for ( String r : tags )
			returnCode = returnCode.replaceAll(Pattern.quote(r), "");

		return returnCode;
	}

	/**
	 * Returns all links this site contains.
	 * @return list of all links
	 */
	public List<String> getLinkURLs() {
		return getLinkURLs("", this.code);
	}

	/**
	 * Returns the first link this site contains.
	 * @return first link
	 */
	public String getFirstLinkURL() {
		List<String> links = getLinkURLs("", this.code);

		if ( links.isEmpty() )
			return "";

		return links.get(0);
	}

	/**
	 * Returns all links this site contains.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @return list of all links
	 */
	public List<String> getLinkURLs(String base) {
		return getLinkURLs(base, this.code);
	}

	/**
	 * Returns first link this site contains.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @return first link
	 */
	public String getFirstLinkURL(String base) {
		List<String> links = getLinkURLs(base, this.code);

		if ( links.isEmpty() )
			return "";

		return links.get(0);
	}

	/**
	 * Returns all links in the given code.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @param code the HTML-code which will be searched
	 * @return list of all links
	 */
	public static List<String> getLinkURLs(String base, String code) {
		List<String> a = new ArrayList<>();

		Pattern p = Pattern.compile("href=\"?.[^\"\\s>]*(.*?)\"?");
		Matcher m = p.matcher(code);

		while ( m.find() ) {
			//a.add(this.code.substring(m.start() + 6, m.end()-1).startsWith("?") || this.code.substring(m.start() + 6, m.end()-1).startsWith("/") ? base + this.code.substring(m.start() + 6, m.end()-1) : this.code.substring(m.start() + 6, m.end()-1));
			String s = code.substring(m.start() + (m.group().startsWith("href=\"") ? 6 : 5), m.end() - (m.group().endsWith("\"") ? 1 : 0));
			if ( s.contains("javascript") || s.equals("#") )
				continue;
			else if ( s.startsWith("?") || s.startsWith("/") )
				s = base + s.replaceAll(" ", "");

			a.add(s);
		}

		return a;
	}

	/**
	 * Returns first link in the given code.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @param code the HTML-code which will be searched
	 * @return first link
	 */
	public static String getFirstLinkURL(String base, String code) {
		List<String> links = getLinkURLs(base, code);

		if ( links.isEmpty() )
			return "";

		return links.get(0);
	}

	/**
	 * Returns all links in the given code.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @param codes multiple HTML-codes which will be searched
	 * @return list of all links
	 */
	public static Collection<String> getLinkURLs(String base, List<String> codes) {
		Collection<String> links = new ArrayList<>();
		for ( String code : codes )
			links.addAll(getLinkURLs(base, code));

		return links;
	}

	/**
	 * Returns all links and link texts in the given code.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @param code the HTML-code which will be searched
	 * @return list of all links and link texts
	 */
	public static Collection<String[]> getLinkURLsAndTexts(String base, String code) {
		Collection<String[]> links = new ArrayList<>();

		Matcher matcher = Pattern.compile("<a[^>]*href=\"([^\"]+)\"[^>]*>(.+)</a>").matcher(code);

		while ( matcher.find() ) {
			String[] s = {matcher.group(1), matcher.group(2)};

			if ( s[0].contains("javascript") || s[0].equals("#") )
				continue;
			else if ( s[0].startsWith("?") || s[0].startsWith("/") )
				s[0] = base + s[0].replaceAll(" ", "");

			links.add(s);
		}

		return links;
	}

	/**
	 * Returns all links and link texts in the given code.
	 * @param base add these URL to the found one, if it starts either with / or ?
	 * @param codes multiple HTML-codes which will be searched
	 * @return list of all links and link texts
	 */
	public static Collection<String[]> getLinkURLsAndTexts(String base, Collection<String> codes) {
		Collection<String[]> links = new ArrayList<>();

		for ( String code : codes )
			links.addAll(getLinkURLsAndTexts(base, code));

		return links;
	}

	/**
	 * Returns the base URL of the given URL.
	 * @param url URL
	 * @return base URL or an empty string
	 */
	public static String getBaseURL(String url) {
		Pattern p = Pattern.compile("(^https?://(www\\.)?[^/]+\\.[^/\\.]+)/?.*$");
		Matcher m = p.matcher(url);
		if ( m.find() )
			return m.group(1);
		else
			return "";
	}

	/**
	 * Check if the given URL is a base URL.
	 * @param url URL
	 * @return <code>true</code> if given URL is a base URL
	 */
	public static boolean isBaseURL(String url) {
		return Pattern.compile("^https?://(www\\.)?[^/]+\\.[^/\\.]+/?$").matcher(url).find();
	}

	/**
	 * Replaces all HTML special characters in the page code with the original character.
	 * @return replace String
	 */
	public String decode() {
		return decode(this.code);
	}

	/**
	 * Replaces all HTML special characters in the given String with the original character.
	 * @param toDecode List of String with HTML characters to replace
	 * @return replace List of String
	 */
	public static List<String> decode(List<String> toDecode) {
		List<String> decoded = new ArrayList<>();

		for ( String s : toDecode )
			decoded.add(decode(s));

		return decoded;
	}

	/**
	 * Replaces all HTML special characters in the given String with the original character.
	 * @param toDecode String with HTML characters to replace
	 * @return replace String
	 */
	public static String decode(String toDecode) {
		return StringEscapeUtils.unescapeHtml4(toDecode);
	}

	/**
	 * Replaces all HTML special characters in the given String with the original character and cleans the string.
	 * @param toClean String to clean and decode
	 * @return clean string
	 */
	public static String cleanAndDecode(String toClean) {
		return StringEscapeUtils.unescapeHtml4(toClean).replaceAll("<[^>]+>", "").replaceAll("\\s*\\n\\s*", " ").replace("\\s\\s+", " ").trim();
	}
}