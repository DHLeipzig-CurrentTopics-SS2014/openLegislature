package org.openlegislature.nlp.cooccurrence;

import java.util.LinkedList;
import java.util.List;
import javax.persistence.EntityManager;
import org.hibernate.criterion.Restrictions;
import org.openlegislature.db.models.Speech;
import org.openlegislature.db.models.SpeechCooccurrence;
import org.openlegislature.db.models.Token;
import org.openlegislature.nlp.tokenization.Tokenizer;
import org.openlegislature.util.HibernateUtils;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class CalculateCooccurrence {
	public static void calculateSpeechCooccurrences(Speech speech) {
		String[] ts = Tokenizer.tokenize(speech.getSpeech());
		List<Token> tokens = new LinkedList<>();

		for ( String t : ts ) {
			t = t.trim();
			if ( t.isEmpty() )
				continue;

			Token token = (Token)HibernateUtils.getSession().createCriteria(Token.class).add(Restrictions.eq("token", t)).uniqueResult();

			if ( token == null ) {
				token = new Token(t);

				EntityManager em = HibernateUtils.getEntityManager();
				em.getTransaction().begin();
				em.persist(token);
				em.getTransaction().commit();
			}

			tokens.add(token);
		}

		ts = null;

		for ( Token token : tokens ) {
			for ( Token cooccurrence : tokens ) {
				if ( token.equals(cooccurrence) )
					continue;

				SpeechCooccurrence cooccur = (SpeechCooccurrence)HibernateUtils.getSession().createCriteria(SpeechCooccurrence.class).add(Restrictions.and(Restrictions.eq("token", token), Restrictions.eq("cooccurrence", cooccurrence), Restrictions.eq("speech", speech))).uniqueResult();

				if ( cooccur == null )
					cooccur = new SpeechCooccurrence(token, cooccurrence, speech);
				else
					cooccur.setCount(cooccur.getCount() + 1);

				EntityManager em = HibernateUtils.getEntityManager();
				em.getTransaction().begin();
				em.persist(cooccur);
				em.getTransaction().commit();
			}
		}
	}
}