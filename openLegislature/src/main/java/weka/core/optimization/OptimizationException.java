package weka.core.optimization;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class OptimizationException extends RuntimeException {
	public OptimizationException() {
		super();
	}

	public OptimizationException(String message) {
		super(message);
	}

	public OptimizationException (String message, Throwable cause) {
		super(message, cause);
	}

	public OptimizationException (Throwable cause) {
		super(cause);
	}
}