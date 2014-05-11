package org.openlegislature.process;

import org.openlegislature.util.Logger;

import com.stumbleupon.async.Callback;

public class ErrorCallback implements Callback<Void, Exception> {
	
	private String errorMessage;

	public ErrorCallback(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	@Override
	public Void call(Exception arg) throws Exception {
		Logger.getInstance().error(errorMessage);
		Logger.getInstance().error(arg.toString());
		return null;
	}
}