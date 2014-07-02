package org.openlegislature;

import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.openlegislature.util.FlywayIntegrator;
import org.openlegislature.util.Logger;

/**
 *
 * @author jnphilipp
 * @version 0.0.6
 */
@RunWith(Suite.class)
@SuiteClasses({
	org.openlegislature.db.models.PartyTest.class,
	org.openlegislature.db.models.PublicOfficeTest.class,
	org.openlegislature.db.models.SpeakerTest.class,
	org.openlegislature.db.models.SpeechTest.class,
	org.openlegislature.db.models.TokenTest.class
})
public class OpenLegislatureTestSuite {
	@BeforeClass
	public static void setUp() {
	}

	@AfterClass
	public static void tearDown() throws SQLException {
		Logger.getInstance().info(OpenLegislatureTestSuite.class, "Tearing down flyway.");
		FlywayIntegrator.getFlyway().clean();
	}
}