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
public class PublicOfficeTest {
	@Test
	public void testPublicOffice() {
		PublicOffice publicOffice = new PublicOffice("Bundeskanzler");

		EntityManager em = HibernateUtils.getEntityManager();
		em.getTransaction().begin();
		em.persist(publicOffice);
		em.getTransaction().commit();

		assertTrue(publicOffice.getId() != 0);
	}
}