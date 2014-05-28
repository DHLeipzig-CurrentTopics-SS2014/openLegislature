package org.openlegislature.process;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openlegislature.util.Logger;

import com.stumbleupon.async.Callback;

public class ThrowableCallback implements Callback<Void, Throwable> {

	private String errorMessage;

	public ThrowableCallback(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public Void call(Throwable arg) throws Exception {
		Logger logger = Logger.getInstance();
		synchronized (logger) {
			logger.error(errorMessage);
			logger.error(ExceptionUtils.getStackTrace(arg));
		}
		return null;
	}
}