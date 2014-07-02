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
public class PartyTest {
	@Test
	public void testParty() {
		Party party = new Party("SPD");

		EntityManager em = HibernateUtils.getEntityManager();
		em.getTransaction().begin();
		em.persist(party);
		em.getTransaction().commit();

		assertTrue(party.getId() != 0);
	}
}