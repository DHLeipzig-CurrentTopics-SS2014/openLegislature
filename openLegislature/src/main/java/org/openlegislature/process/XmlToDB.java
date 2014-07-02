package org.openlegislature.process;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import org.hibernate.criterion.Restrictions;
import org.openlegislature.db.models.Party;
import org.openlegislature.db.models.PublicOffice;
import org.openlegislature.db.models.Speaker;
import org.openlegislature.db.models.Speech;
import org.openlegislature.io.FileReader;
import org.openlegislature.nlp.cooccurrence.CalculateCooccurrence;
import org.openlegislature.util.HibernateUtils;
import org.openlegislature.util.Logger;

public class XmlToDB {
	private String protocol;
	private String text;

	public void process(File xmlFile) throws IOException {
		if ( xmlFile != null ) {
			this.protocol = xmlFile.getName();
			this.readFile(xmlFile);
			this.createModels();
		}
	}

	private void readFile(File file) throws IOException {
		this.text = FileReader.read(file);
	}

	private void createModels() {
		Matcher m = Pattern.compile("<speech>.+?</speech>", Pattern.DOTALL | Pattern.MULTILINE).matcher(this.text);

		int i = 1;
		while ( m.find() ) {
			Matcher m2 = Pattern.compile("<speaker>(.+?)</speaker>", Pattern.DOTALL | Pattern.MULTILINE).matcher(m.group());
			Speaker speaker = (m2.find() ? this.getSpeaker(m2.group(1)) : null);

			m2 = Pattern.compile("<party>(.+?)</party>", Pattern.DOTALL | Pattern.MULTILINE).matcher(m.group());
			Party party = (m2.find() ? this.getParty(m2.group(1)) : null);
			speaker.getParties().add(party);

			m2 = Pattern.compile("<public_office>(.+?)</public_office>", Pattern.DOTALL | Pattern.MULTILINE).matcher(m.group());
			PublicOffice publicOffice = (m2.find() ? this.getPublicOffice(m2.group(1)) : null);
			speaker.getPublicOffices().add(publicOffice);

			String s = m.group().replaceAll("<[^>]+?>", " ").replaceAll("\\s+\n\\s+", " ").replaceAll("\\s\\s+", " ");
			Speech speech = new Speech(s, speaker);

			EntityManager em = HibernateUtils.getEntityManager();
			em.getTransaction().begin();
			em.persist(speech);
			em.getTransaction().commit();

			this.calculateCooccurrences(speech);

			Logger.getInstance().info(XmlToDB.class, "speech: " + i + " (" + this.protocol + ")");
			i++;
		}
	}

	private Speaker getSpeaker(String name) {
		Speaker speaker = (Speaker)HibernateUtils.getSession().createCriteria(Speaker.class).add(Restrictions.eq("name", name)).uniqueResult();

		if ( speaker == null )
			speaker = new Speaker(name);

		return speaker;
	}

	private Party getParty(String name) {
		Party party = (Party)HibernateUtils.getSession().createCriteria(Party.class).add(Restrictions.eq("name", name)).uniqueResult();

		if ( party == null )
			party = new Party(name);

		return party;
	}

	private PublicOffice getPublicOffice(String name) {
		PublicOffice publicOffice = (PublicOffice)HibernateUtils.getSession().createCriteria(PublicOffice.class).add(Restrictions.eq("name", name)).uniqueResult();

		if ( publicOffice == null )
			publicOffice = new PublicOffice(name);

		return publicOffice;
	}

	private void calculateCooccurrences(Speech speech) {
		CalculateCooccurrence.calculateSpeechCooccurrences(speech);
	}
}