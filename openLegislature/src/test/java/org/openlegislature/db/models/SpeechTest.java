package org.openlegislature.db.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import org.junit.Test;
import org.openlegislature.util.HibernateUtils;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class SpeechTest {
	@Test
	public void testSpeaker() {
		Speaker speaker = new Speaker("Frank-Walter Steinmeier");
		Speech speech = new Speech("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", speaker);

		EntityManager em = HibernateUtils.getEntityManager();
		em.getTransaction().begin();
		em.persist(speech);
		em.getTransaction().commit();

		assertTrue(speech.getId() != 0);
		assertTrue(speaker.getId() != 0);

		em.getTransaction().begin();
		em.refresh(speaker);
		em.getTransaction().commit();

		assertEquals(1, speaker.getSpeeches().size());

		Speech speech2 = new Speech("Sed ut perspiciatis, unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam eaque ipsa, quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt, explicabo. Nemo enim ipsam voluptatem, quia voluptas sit, aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit amet consectetur adipisci[ng] velit, sed quia non numquam [do] eius modi tempora inci[di]dunt, ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit, qui in ea voluptate velit esse, quam nihil molestiae consequatur, vel illum, qui dolorem eum fugiat, quo voluptas nulla pariatur?", speaker);

		em.getTransaction().begin();
		em.persist(speech2);
		em.getTransaction().commit();

		assertTrue(speech2.getId() != 0);

		em.getTransaction().begin();
		em.refresh(speaker);
		em.getTransaction().commit();

		assertEquals(2, speaker.getSpeeches().size());
	}
}