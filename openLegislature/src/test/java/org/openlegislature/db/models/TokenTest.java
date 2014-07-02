package org.openlegislature.db.models;

import static org.junit.Assert.assertNotEquals;

import javax.persistence.EntityManager;
import org.junit.Test;
import org.openlegislature.util.HibernateUtils;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class TokenTest {
	@Test
	public void testToken() {
		Token t = new Token("ipsum");

		EntityManager em = HibernateUtils.getEntityManager();
		em.getTransaction().begin();
		em.persist(t);
		em.getTransaction().commit();

		assertNotEquals(0, t.getId());
	}
}