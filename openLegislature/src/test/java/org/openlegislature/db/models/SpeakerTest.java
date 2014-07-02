package org.openlegislature.db.models;

import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import org.junit.Test;
import org.openlegislature.util.HibernateUtils;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class SpeakerTest {
	@Test
	public void testSpeaker() {
		Party party = new Party("CDU");
		PublicOffice publicOffice = new PublicOffice("Bundeskanzlerin");
		Speaker speaker = new Speaker("Angela Merkel");
		speaker.getParties().add(party);
		speaker.getPublicOffices().add(publicOffice);

		EntityManager em = HibernateUtils.getEntityManager();
		em.getTransaction().begin();
		em.persist(speaker);
		em.getTransaction().commit();

		assertTrue(speaker.getId() != 0);
		assertTrue(party.getId() != 0);
		assertTrue(publicOffice.getId() != 0);
	}
}