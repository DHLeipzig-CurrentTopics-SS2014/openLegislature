package org.openlegislature.parser;

import java.net.Proxy;

/**
 *
 * @author jnphilipp
 * @version 0.0.2
 */
public interface Parser {
	/**
	 * Returns the response code of the request.
	 * @return response code
	 */
	public abstract int getResponseCode();

	/**
	 * @return <code>true</code> if response code is 200
	 */
	public abstract boolean isResponseCodeOK();

	/**
	 * Builds a connection to the given URL and retrieves it. If a user-agent is given it will be used.
	 * @param url URL which will be fetched
	 * @throws Exception 
	 */
	public abstract void fetch(String url) throws Exception;

	/**
	 * Builds a connection to the given URL using the given proxy and retrieves it. If a user-agent is given it will be used.
	 * @param url URL which will be fetched
	 * @param proxy proxy
	 * @throws Exception 
	 */
	public abstract void fetch(String url, Proxy proxy) throws Exception;
}