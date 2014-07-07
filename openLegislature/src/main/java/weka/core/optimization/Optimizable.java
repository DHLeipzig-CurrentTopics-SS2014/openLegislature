package weka.core.optimization;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public interface Optimizable {
	/**
	 * Number of parameters.
	 * @return number of parameters
	 */
	public int size();

	/**
	 * Returns the parameters.
	 * @param buffer parameters
	 */
	public void getX(double[] buffer);

	/**
	 * Returns a specific parameter.
	 * @param index index
	 * @return parameter
	 */
	public double getX(int index);

	/**
	 * Sets the parameters.
	 * @param params parameters to set
	 */
	public void setX(double[] params);

	/**
	 * Set a specific parameter.
	 * @param index index
	 * @param value value
	 */
	public void setX(int index, double value);

	/**
	 * Optimization by value.
	 */
	public interface ByValue extends Optimizable {
		/**
		 * Calculates the optimization value.
		 * @return optimization value
		 */
		public double getValue();
	}

	/**
	 * Optimization by gradient.
	 */
	public interface ByGradient extends Optimizable {
		/**
		 * Calculates the gradient.
		 * @param buffer gradient
		 */
		public void getValueGradient(double[] buffer);
	}

	/**
	 * Optimization by value and gradient.
	 */
	public interface ByGradientValue extends Optimizable {
		/**
		 * Calculates the optimization value.
		 * @return optimization value
		 */
		public double getValue();

		/**
		 * Calculates the gradient.
		 * @param buffer gradient
		 */
		public void getValueGradient(double[] buffer);
	}
}