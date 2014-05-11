package org.openlegislature.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * An {@link ExecutorService} which uses a thread pool of fixed size.
 * It is also injectable by guice.
 * Basically this class just delegates all call to an private instance of {@link ExecutorService}.
 * The default max used threads are 4. 
 * 
 * @author dhaeb
 *
 */
@Singleton
public class InjectableFixedExecutorService implements ExecutorService {
	
	private ExecutorService e;

	@Inject
	public InjectableFixedExecutorService(OpenLegislatureConstants constants) {
		int maxThreads = constants.getMaxThreads();
		ThreadPoolExecutor delegateExecutor = new ThreadPoolExecutor(maxThreads, maxThreads,
                constants.getThreadIdleTime(), TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
		delegateExecutor.allowCoreThreadTimeOut(true);
		e = delegateExecutor;
	}
	
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return e.awaitTermination(timeout, unit);
	}

	public void execute(Runnable arg0) {
		e.execute(arg0);
	}

	public <T> List<Future<T>> invokeAll(
			Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return e.invokeAll(tasks, timeout, unit);
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		return e.invokeAll(tasks);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return e.invokeAny(tasks, timeout, unit);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		return e.invokeAny(tasks);
	}

	public boolean isShutdown() {
		return e.isShutdown();
	}

	public boolean isTerminated() {
		return e.isTerminated();
	}

	public void shutdown() {
		e.shutdown();
	}

	public List<Runnable> shutdownNow() {
		return e.shutdownNow();
	}

	public <T> Future<T> submit(Callable<T> task) {
		return e.submit(task);
	}

	public <T> Future<T> submit(Runnable task, T result) {
		return e.submit(task, result);
	}

	public Future<?> submit(Runnable task) {
		return e.submit(task);
	}
	
}
