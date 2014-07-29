package org.openlegislature.process;

import java.io.File;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openlegislature.io.FileWriter;
import org.openlegislature.parser.html.HTMLParser;
import org.openlegislature.util.Helpers;
import org.openlegislature.util.OpenLegislatureConstants;

/**
 * Parses the members from Wikipedia and saves them as XML.
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class MembersXML {
	private OpenLegislatureConstants constants;

	/*@Inject
	public MembersXML(OpenLegislatureConstants constants) {
		this.constants = constants;
	}*/

	public File createMembersXMLWhenNotAlreadyDone() {
		File f = new File(Helpers.getUserDir() + "/data/members.xml");

		

		return f;
	}

	private String[] getSessions() throws Exception {
		HTMLParser parser = new HTMLParser();
		parser.fetch("https://de.wikipedia.org/wiki/Liste_der_Listen_der_Mitglieder_des_Deutschen_Bundestages");

		String ul = parser.getFirstTag("ul");
		List<String> links = HTMLParser.getLinkURLs("https://de.wikipedia.org", parser.replaceTagContent("ul", "", ul.substring(4, ul.length() - 5)));

		return links.toArray(new String[links.size()]);
	}

	private void getMembers(String link, Map<String, Map<String, String>> sessions, Map<String, Map<String, Object>> members) throws Exception {
		HTMLParser parser = new HTMLParser();
		parser.fetch(link, true);

		String session = "";
		String begin = "";
		String end = "";
		Matcher matcher = Pattern.compile("(\\d+)\\.\\sWahlperiode.+?(\\d{4})[–-](\\d{4}).", Pattern.DOTALL).matcher(parser.getCode());
		if ( matcher.find() ) {
			session = matcher.group(1);
			begin = matcher.group(2);
			end = matcher.group(3);
			Map<String, String> s = new LinkedHashMap<>();
			s.put("begin", begin);
			s.put("end", end);

			sessions.put(session, s);
		}

		String table = "";
		matcher = Pattern.compile("id=\"Abgeordnete\".*?<table.*?>.*?<th>Mitglied des Bundestages</th>.+?</table>", Pattern.DOTALL).matcher(parser.getCode());
		if ( matcher.find() )
			table = matcher.group();

		matcher = Pattern.compile("<tr>.*?<td>(.*?)</td>.*?<td>(.*?)</td>.*?<td>(.*?)</td>.*?<td>(.*?)</td>.*?<td>(.*?)</td>.*?<td>(.*?)</td>.*?<td>(.*?)</td>.*?</tr>", Pattern.DOTALL).matcher(table);
		int i = 1;
		while ( matcher.find() ) {
			String wiki = this.getLink("https://de.wikipedia.org", matcher.group(1));
			String birth = this.clean(matcher.group(2));
			String party = this.clean(matcher.group(3));
			String land = this.clean(matcher.group(4));
			String constituency = this.clean(matcher.group(5));
			String wikiConstituency = this.getLink("https://de.wikipedia.org", matcher.group(5));

			String name = URLDecoder.decode(wiki.substring(wiki.lastIndexOf("/") + 1), "UTF-8").replaceAll("_", " ").replaceAll("\\s+\\([^\\)]+\\)", "");

			Map<String, String> s = new LinkedHashMap<>();
			s.put("party", party);
			s.put("land", land);
			if ( !constituency.isEmpty() ) {
				s.put("constituency", constituency);
				s.put("wiki_constituency", wikiConstituency);
			}

			if ( members.containsKey(wiki) ) {
				Map<String, Object> member = members.get(wiki);
				member.put(session, s);
				members.put(wiki, member);
			}
			else {
				Map<String, Object> member = new LinkedHashMap<>();
				member.put("id", (session.length() == 1 ? "0" : "") + session + (i < 10 ? "00" : (i < 100 ? "0" : "")) + (i++));
				member.put("name", name);
				member.put("birth", birth);
				member.put(session, s);
				members.put(wiki, member);
			}
		}
	}

	private String toSessionsXML(Map<String, Map<String, String>> sessions) {
		String xml = "";

		/*
			<session id="" begin="" end=""/>
		*/

		for ( Entry<String, Map<String, String>> session : sessions.entrySet() ) {
			xml += String.format("\n\t<session id=\"%s\" begin=\"%s\" end=\"%s\"></session>", session.getKey(), session.getValue().get("begin"), session.getValue().get("end"));
		}

		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + Helpers.tagContent("sessions", xml + "\n");
	}

	private String toMembersXML(Map<String, Map<String, Object>> members) {
		String membersXML = "";

		/*
		<member id="">
			<name wikipedia=""></name>
			<birth></birth>
			<session id="">
				<party></part>
				<land></land>
				<constituency wikipedia=""></constituency>
		</session>
		</member>
		*/

		for ( Entry<String, Map<String, Object>> member : members.entrySet() ) {
			Map<String, Object> values = member.getValue();

			String sessions = "";
			for ( Entry<String, Object> value : values.entrySet() ) {
				if ( value.getKey().equals("id") || value.getKey().equals("name") || value.getKey().equals("birth") )
					continue;

				Map<String, String> sessionInfo = (Map<String, String>)value.getValue();
				sessions += String.format("\n\t\t\t<session id=\"%s\">\n\t\t\t\t<party>%s</party>\n\t\t\t\t<land>%s</land>%s\n\t\t\t</session>", value.getKey(), sessionInfo.get("party"), sessionInfo.get("land"), sessionInfo.containsKey("constituency") ? String.format("\n\t\t\t\t<constituency wikipedia=\"%s\">%s</constituency>", sessionInfo.get("wiki_constituency"), sessionInfo.get("constituency")) : "");
			}

			String id = values.get("id").toString();
			String name = values.get("name").toString();
			String birth = values.get("birth").toString();
			membersXML += String.format("\t<member id=\"%s\">\n\t\t<name wikipedia=\"%s\">%s</name>\n\t\t<birth>%s</birth>\n\t\t<sessions>%s\n\t\t</sessions>\n\t</member>\n", id, member.getKey(), name, birth, sessions);
		}

		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + Helpers.tagContent("members", "\n" + membersXML);
	}

	private String clean(String toClean) {
		return toClean.replaceAll("(?s)\\s+\n\\s+", " ").replaceAll("<[^>]+>", " ").replaceAll("\\s\\s+", " ").trim();
	}

	private String getLink(String base, String html) {
		Matcher matcher = Pattern.compile("href=\"([^\"]+)\"").matcher(html);
		if ( matcher.find() )
			return base + (base.endsWith("/") && matcher.group(1).startsWith("/") ? matcher.group(1).substring(1) : matcher.group(1));

		return "";
	}

	public static void main(String[] args) throws Exception {
		MembersXML xml = new MembersXML();
		String[] sessionLinks = xml.getSessions();
		Map<String, Map<String, Object>> members = new LinkedHashMap<>();
		Map<String, Map<String, String>> sessions = new LinkedHashMap<>();

		for ( String session : sessionLinks )
			xml.getMembers(session, sessions, members);

		String sessionsXML = xml.toSessionsXML(sessions);
		FileWriter.write(Helpers.getUserDir() + "/data/sessions.xml", sessionsXML, "UTF-8");

		String membersXML = xml.toMembersXML(members);
		FileWriter.write(Helpers.getUserDir() + "/data/members.xml", membersXML, "UTF-8");
	}
}