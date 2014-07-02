package org.openlegislature.util;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionImpl;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class HibernateUtils {
	static {
		getEntityManager();
	}

	public static EntityManager getEntityManager() {
		if ( HibernateUtils.em == null )
			HibernateUtils.em = Persistence.createEntityManagerFactory("openlegislature").createEntityManager();
		return HibernateUtils.em;
	}

	static transient EntityManager em;

	public static Session getSession() {
		Object o = HibernateUtils.getEntityManager().getDelegate();
		Session s = null;
		if ( o instanceof SessionImpl )
			s = HibernateUtils.getEntityManager().unwrap(Session.class);

		if ( o instanceof SessionFactory ) {
			s = ((SessionFactory)o).getCurrentSession();
		}

		return s;
	}
}