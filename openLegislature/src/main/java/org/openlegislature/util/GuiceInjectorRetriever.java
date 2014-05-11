package org.openlegislature.util;

import java.util.concurrent.ExecutorService;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Singleton wrapper which holds a reference to the Guice {@link Injector}.
 * Used to create classes which should be managed by guice for terms of simplicity.
 * 
 * @author dhaeb
 *
 */
public class GuiceInjectorRetriever {

	private static Injector injector;
	
	public static synchronized Injector getInjector(){
		if(injector == null){
			injector = initInjector();
		} 
		return injector;
	}
	
	private static Injector initInjector() {
		return Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(ExecutorService.class).to(InjectableFixedExecutorService.class);
			}
		});
	}

	private GuiceInjectorRetriever() {}
}
