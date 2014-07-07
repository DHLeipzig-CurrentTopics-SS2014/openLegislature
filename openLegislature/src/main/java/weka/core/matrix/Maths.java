package weka.core.matrix;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class Maths {
	/**
	 * absolute norm
	 * @param m m
	 * @return absolute norm
	 */
	public static double absNorm(double[] m) {
		double n = 0.0;
		for ( int i = 0; i < m.length; i++ )
			n += abs(m[i]);

		if ( n > 0.0 )
			for ( int i = 0; i < m.length; i++ )
				m[i] /= n;
		return n;
	}

	/**
	 * one norm
	 * @param m m
	 * @return one norm
	 */
	public static double oneNorm(double[] m) {
		double n = 0.0;
		for ( int i = 0; i < m.length; i++ )
			n += m[i];
		return n;
	}

	/**
	 * two norm
	 * @param m m
	 * @return two norm
	 */
	public static double twoNorm(double[] m) {
		double n = 0.0;
		for ( int i = 0; i < m.length; i++ )
			n += m[i] * m[i];
		return sqrt(n);
	}

	/**
	 * infinity norm
	 * @param m m
	 * @return infinity norm
	 */
	public static double infNorm(double[] m) {
		double n = Double.NEGATIVE_INFINITY;
		for ( int i = 0; i < m.length; i++ )
			if ( abs(m[i]) > n )
				n = abs(m[i]);
		return n;
	}

	/**
	 * dot
	 * @param m1 m1
	 * @param m2 m2
	 * @return dot product
	 */
	public static double dotProduct(double[] m1, double[] m2) {
		double dot = 0.0;
		for ( int i = 0; i < m1.length; i++ )
			dot += m1[i] * m2[i];
		return dot;
	}

	/**
	 * sum scalar
	 * @param m1 m1
	 * @param m2 m2
	 * @param factor scalar
	 */
	public static void plusEquals(double[] m1, double[] m2, double factor) {
		for ( int i = 0; i < m1.length; i++ )
			m1[i] = Double.isInfinite(m1[i]) && Double.isInfinite(m2[i]) && (m1[i] * m2[i] < 0) ? 0.0 : m1[i] + m2[i] * factor;
			/*if ( Double.isInfinite(m1[i]) && Double.isInfinite(m2[i]) && (m1[i] * m2[i] < 0) )
				m1[i] = 0.0;
			else
				m1[i] += m2[i] * factor;*/
	}

	/**
	 * scalar product
	 * @param m m
	 * @param factor scalar
	 */
	public static void timesEquals(double[] m, double factor) {
		for ( int i = 0; i < m.length; i++ )
			m[i] *= factor;
	}

	/**
	 * small abs diff
	 * @param x x
	 * @param oldx old x
	 * @param absTolx abs tol x
	 * @return returns true if we've converged based on absolute x difference
	 */
	public static boolean smallAbsDiff(double[] x, double[] oldx, double absTolx) {
		for ( int i = 0; i < x.length; i++ )
			if ( abs(x[i] - oldx[i]) > absTolx )
				return false;
		return true;
	}
}