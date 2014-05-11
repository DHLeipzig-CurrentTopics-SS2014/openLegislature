package org.openlegislature.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.openlegislature.App;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jnphilipp
 * @version 0.0.2
 */
public class Logger {
	/**
	 * instance
	 */
	private static Logger instance;
	/**
	 * logger
	 */
	private static org.slf4j.Logger logger;

	private Logger() {
		logger = LoggerFactory.getLogger(App.class);
	}

	private Logger(String log4jFile) throws IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream(log4jFile));
		PropertyConfigurator.configure(prop);
		logger = LoggerFactory.getLogger(App.class);
	}

	/**
	 * Returns an instance of this class.
	 * @return instance
	 */
	public static synchronized Logger getInstance() {
		if ( instance == null )
			instance = new Logger();

		return instance;
	}

	/**
	 * Returns an instance of this class.
	 * @param log4jFile path to log4j property file
	 * @return instance
	 * @throws java.io.IOException
	 */
	public static synchronized Logger getInstance(String log4jFile) throws IOException {
		if ( instance == null )
			instance = new Logger(log4jFile);

		return instance;
	}

	/**
	 * Logs debug message.
	 * @param msg message
	 */
	public void debug(final String msg) {
		if ( logger.isDebugEnabled() )
			logger.debug(msg);
	}

	/**
	 * Logs debug message.
	 * @param msg messages
	 */
	public void debug(final String... msg) {
		if ( logger.isDebugEnabled() )
			logger.debug(Helpers.join(msg, "\n\t- "));
	}

	/**
	 * Logs debug message,
	 * @param clazz Class
	 * @param msg message
	 */
	public void debug(final Class<?> clazz, final String msg) {
		if ( LoggerFactory.getLogger(clazz).isDebugEnabled() )
			LoggerFactory.getLogger(clazz).debug(msg);
	}

	/**
	 * Logs debug message.
	 * @param clazz Class
	 * @param msg messages
	 */
	public void debug(final Class<?> clazz, final String... msg) {
		if ( LoggerFactory.getLogger(clazz).isDebugEnabled() )
			LoggerFactory.getLogger(clazz).debug(Helpers.join(msg, "\n\t- "));
	}

	/**
	 * Logs error message.
	 * @param msg message
	 */
	public void error(final String msg) {
		if ( logger.isErrorEnabled() )
			logger.error(msg);
	}

	/**
	 * Logs error message.
	 * @param msg messages
	 */
	public void error(final String... msg) {
		if ( logger.isErrorEnabled() )
			logger.error(Helpers.join(msg, "\n\t- "));
	}

	/**
	 * Logs error message.
	 * @param clazz Class
	 * @param msg message
	 */
	public void error(final Class<?> clazz, final String msg) {
		if ( LoggerFactory.getLogger(clazz).isErrorEnabled() )
			LoggerFactory.getLogger(clazz).error(msg);
	}

	/**
	 * Logs error message.
	 * @param clazz Class
	 * @param msg messages
	 */
	public void error(final Class<?> clazz, final String... msg) {
		if ( LoggerFactory.getLogger(clazz).isErrorEnabled() )
			LoggerFactory.getLogger(clazz).error(Helpers.join(msg, "\n\t- "));
	}

	/**
	 * Logs info message.
	 * @param msg message
	 */
	public void info(final String msg) {
		if ( logger.isInfoEnabled() )
			logger.info(msg);
	}

	/**
	 * Logs info message.
	 * @param msg messages
	 */
	public void info(final String... msg) {
		if ( logger.isInfoEnabled() )
			logger.info(Helpers.join(msg, "\n\t- "));
	}

	/**
	 * Logs info message.
	 * @param clazz Class
	 * @param msg message
	 */
	public void info(final Class<?> clazz, final String msg) {
		if ( LoggerFactory.getLogger(clazz).isInfoEnabled() )
			LoggerFactory.getLogger(clazz).info(msg);
	}

	/**
	 * Logs info message.
	 * @param clazz Class
	 * @param msg messages
	 */
	public void info(final Class<?> clazz, final String... msg) {
		if ( LoggerFactory.getLogger(clazz).isInfoEnabled() )
			LoggerFactory.getLogger(clazz).info(Helpers.join(msg, "\n\t- "));
	}

	/**
	 * Logs warn message.
	 * @param msg message
	 */
	public void warn(final String msg) {
		if ( logger.isWarnEnabled() )
			logger.warn(msg);
	}

	/**
	 * Logs warn message.
	 * @param msg messages
	 */
	public void warn(final String... msg) {
		if ( logger.isWarnEnabled() )
			logger.warn(Helpers.join(msg, "\n\t- "));
	}

	/**
	 * Logs warn message.
	 * @param clazz Class
	 * @param msg message
	 */
	public void warn(final Class<?> clazz, final String msg) {
		if ( LoggerFactory.getLogger(clazz).isWarnEnabled() )
			LoggerFactory.getLogger(clazz).warn(msg);
	}

	public void warn(final Class<?> clazz, final String... msg) {
		if ( LoggerFactory.getLogger(clazz).isWarnEnabled() )
			LoggerFactory.getLogger(clazz).warn(Helpers.join(msg, "\n\t- "));
	}
}