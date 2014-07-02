package org.openlegislature.util;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class FlywayIntegrator implements Integrator {
	public static final Logger logger = Logger.getInstance();
	private static Flyway flyway;

	public static Flyway getFlyway() {
		return flyway;
	}

	@Override
	public void integrate(final Configuration configuration, final SessionFactoryImplementor sessionFactoryImplementor, final SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
		logger.info("Starting Flyway Migration");

		String driver = sessionFactoryImplementor.getProperties().getProperty("hibernate.connection.driver_class");
		String url = sessionFactoryImplementor.getProperties().getProperty("hibernate.connection.url");
		String user = sessionFactoryImplementor.getProperties().getProperty("hibernate.connection.username");
		String password = sessionFactoryImplementor.getProperties().getProperty("hibernate.connection.password");

		flyway = new Flyway();
		try {
			flyway.setDataSource(url, user, password);
			flyway.migrate();
		}
		catch ( FlywayException e ) {
			logger.error("Error while migrating:", e.toString());
		}

		logger.info("Finished Flyway Migration");
	}

	@Override
	public void integrate(final MetadataImplementor metadataImplementor, final SessionFactoryImplementor sessionFactoryImplementor, final SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {}

	@Override
	public void disintegrate(final SessionFactoryImplementor sessionFactoryImplementor, final SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {}
}