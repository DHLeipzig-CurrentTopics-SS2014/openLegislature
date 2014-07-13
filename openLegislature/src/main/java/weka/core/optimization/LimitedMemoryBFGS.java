package weka.core.optimization;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import static weka.core.matrix.Maths.absNorm;
import static weka.core.matrix.Maths.dotProduct;
import static weka.core.matrix.Maths.infNorm;
import static weka.core.matrix.Maths.oneNorm;
import static weka.core.matrix.Maths.plusEquals;
import static weka.core.matrix.Maths.smallAbsDiff;
import static weka.core.matrix.Maths.timesEquals;
import static weka.core.matrix.Maths.twoNorm;

import java.util.LinkedList;
import org.apache.log4j.Logger;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class LimitedMemoryBFGS {
	/**
	 * maximum iterations
	 */
	private final int MAX_ITER = 1000;
	private final int MAX_LINE_ITERATIONS = 200;
	/**
	 * logger
	 */
	private final Logger logger = Logger.getLogger(LimitedMemoryBFGS.class);

	//options
	/**
	 * The number of corrections used in BFGS update ideally 3 <= m <= 7. Larger m means more cpu time, memory.
	 */
	private final int M = 5;
	/**
	 * converged
	 */
	private boolean converged = false;
	/**
	 * tolerance
	 */
	private double tolerance = 0.0001;
	/**
	 * gradient tolerance
	 */
	private double gradientTolerance = 0.001;
	/**
	 * eps
	 */
	private double eps = 1.0e-5;

	//attributes
	/**
	 * optimizable function
	 */
	private Optimizable.ByGradientValue optimizable;
	/**
	 * iterations
	 */
	private int iterations;

	/**
	 * Creates a new limited memory BFGS.
	 * @param function optimizable function
	 */
	public LimitedMemoryBFGS(Optimizable.ByGradientValue function) {
		this.optimizable = function;
	}

	/**
	 * @return <code>true</code> if converged
	 */
	public boolean isConverged () {
		return this.converged;
	}

	public int getIteration () {
		return this.iterations;
	}

	/**
	 * @return the optimizable
	 */
	public Optimizable getOptimizable() {
		return this.optimizable;
	}

	/**
	 * @param tolerance tolerance to set
	 */
	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	/**
	 * Runs the optimization.
	 * @return <code>true</code> if converged
	 */
	public boolean optimize() {
		return this.optimize(this.MAX_ITER);
	}

	/**
	 * Runs the optimization.
	 * @param numIterations number of iterations.
	 * @return <code>true</code> if converged
	 */
	public boolean optimize(int numIterations) {
		double initialValue = this.optimizable.getValue(), step = 1.0;
		double[] alpha, g, oldg, direction, p, oldp;
		LinkedList s = new LinkedList();
		LinkedList y = new LinkedList();
		LinkedList rho = new LinkedList();

		this.logger.debug("Entering L-BFGS.optimize(). Initial Value=" + initialValue );
		this.logger.debug("First time through L-BFGS");

		alpha = new double[this.M];
		p = new double[this.optimizable.size()];
		oldp = new double[this.optimizable.size()];
		g = new double[this.optimizable.size()];
		oldg = new double[this.optimizable.size()];
		direction = new double[this.optimizable.size()];

		this.optimizable.getX(p);
		System.arraycopy(p, 0, oldp, 0, p.length);

		this.optimizable.getValueGradient(g);
		System.arraycopy(g, 0, oldg, 0, g.length);
		System.arraycopy(g, 0, direction, 0, g.length);

		if ( absNorm(direction) == 0 ) {
			this.logger.warn("L-BFGS initial gradient is zero; saying converged");
			this.converged = true;
			return true;
		}

		if ( Logger.getRootLogger().isDebugEnabled() )
			this.logger.debug("direction.2norm: " + twoNorm(direction));

		timesEquals(direction, 1.0 / twoNorm(direction));

		if ( Logger.getRootLogger().isDebugEnabled() )
			this.logger.debug("before initial jump:" +
							"\ndirection.2norm: " + twoNorm(direction) +
							"\ngradient.2norm: " + twoNorm(g) +
							"\nparameters.2norm: " + twoNorm(p));

		//make initial jump
		step = this.optimizeLine(direction, step);
		if ( step == 0.0 ) {//could not step in this direction.
			//give up and say converged.
			step = 1.0;
			throw new OptimizationException("Line search could not step in the current direction. (This is not necessarily cause for alarm. Sometimes this happens close to the maximum, where the function may be very flat.)");
		}

		this.optimizable.getX(p);
		this.optimizable.getValueGradient(g);
		if ( Logger.getRootLogger().isDebugEnabled() )
			this.logger.debug("after initial jump:" +
							"\ndirection.2norm: " + twoNorm(direction) +
							"\ngradient.2norm: " + twoNorm(g));

		for ( int iterationCount = 0; iterationCount < numIterations; iterationCount++ ) {
			double value = this.optimizable.getValue();

			if ( Logger.getRootLogger().isDebugEnabled() )
				this.logger.debug("L-BFGS iteration=" + iterationCount +
								", value=" + value + " g.twoNorm: " + twoNorm(g) +
								" oldg.twoNorm: " + twoNorm(oldg));

			//get difference between previous 2 gradients and parameters
			double sy = 0.0, yy = 0.0;
			for ( int i = 0; i < oldp.length; i++ ) {
				oldp[i] = Double.isInfinite(p[i]) && Double.isInfinite(oldp[i]) && (p[i] * oldp[i] > 0) ? 0.0 : p[i] - oldp[i];
				oldg[i] = Double.isInfinite(g[i]) && Double.isInfinite(oldg[i]) && (g[i] * oldg[i] > 0) ? 0.0 : g[i] - oldg[i];

				sy += oldp[i] * oldg[i];//si * yi
				yy += oldg[i] * oldg[i];
				direction[i] = g[i];
			}

			if ( sy > 0 )
				throw new OptimizationException("sy = " + sy + " > 0");

			double gamma = sy / yy;//scaling factor
			if ( gamma > 0 )
				throw new OptimizationException("gamma = " + gamma + " > 0");

			this.push(rho, 1.0 / sy);
			this.push(s, oldp);
			this.push(y, oldg);

			//calculate new direction
			for ( int i = s.size() - 1; i >= 0; i-- ) {
				alpha[i] = ((Double)rho.get(i)) * dotProduct((double[])s.get(i), direction);
				plusEquals(direction, (double[])y.get(i), -1.0 * alpha[i]);
			}
			timesEquals(direction, gamma);
			for ( int i = 0; i < y.size(); i++ ) {
				double beta = (((Double)rho.get(i))) * dotProduct((double[])y.get(i), direction);
				plusEquals(direction, (double[])s.get(i), alpha[i] - beta);
			}

			for ( int i = 0; i < oldg.length; i++ ) {
				oldp[i] = p[i];
				oldg[i] = g[i];
				direction[i] *= -1.0;
			}

			if ( Logger.getRootLogger().isDebugEnabled() )
				this.logger.debug("before linesearch: direction.gradient.dotprod: " + dotProduct(direction, g) +
								"\ndirection.2norm: " +	twoNorm(direction) +
								"\nparameters.2norm: " + twoNorm(p));

			step = this.optimizeLine(direction, step);
			if ( step == 0.0 ) {//could not step in this direction.
				step = 1.0;
				throw new OptimizationException("Line search could not step in the current direction. (This is not necessarily cause for alarm. Sometimes this happens close to the maximum, where the function may be very flat.)");
			}

			this.optimizable.getX(p);
			this.optimizable.getValueGradient(g);

			if ( Logger.getRootLogger().isDebugEnabled() )
				this.logger.debug("after linesearch: direction.2norm: " + twoNorm(direction));

			//Test for terminations
			double newValue = this.optimizable.getValue();
			if ( 2.0 * abs(newValue - value) <= this.tolerance * (abs(newValue) + abs(value) + this.eps) ) {
				this.logger.debug("Exiting L-BFGS on termination #1:\nvalue difference below tolerance (oldValue: " + value + " newValue: " + newValue);
				this.converged = true;
				return true;
			}

			double gg = twoNorm(g);
			if ( gg < this.gradientTolerance ) {
				this.logger.debug("Exiting L-BFGS on termination #2:\ngradient=" + gg + " < " + this.gradientTolerance);
				this.converged = true;
				return true;
			}

			if ( gg == 0.0 ) {
				this.logger.debug("Exiting L-BFGS on termination #3:\ngradient==0.0");
				this.converged = true;
				return true;
			}

			this.logger.debug("Gradient = " + gg);
			this.iterations++;
			if ( this.iterations > numIterations ) {
				this.logger.error("Too many iterations in L-BFGS.java. Continuing with current parameters.");
				this.converged = true;
				return true;
			}
		}

		return false;
	}

	/**
	 * Line search and backtracking.
	 * @param line line
	 * @param initialStep initial step
	 * @return 0.0 if could not step in direction
	 */
	public double optimizeLine(double[] line, double initialStep) {
		int iteration;
		double slope, disc, f, fold, f2;
		double stpmax = 100;
		double relTolx = 1e-7;
		double absTolx = 1e-4;//tolerance on absolute value difference
		double ALF = 1e-4;
		double[] g, x, oldx;

		g = new double[this.optimizable.size()];//gradient
		x = new double[this.optimizable.size()];//parameters
		oldx = new double[this.optimizable.size()];

		this.optimizable.getX(x);
		System.arraycopy(x, 0, oldx, 0, x.length);

		this.optimizable.getValueGradient(g);
		f2 = fold = this.optimizable.getValue();

		this.logger.debug("Entering backtrack.");
		if ( Logger.getRootLogger().isDebugEnabled() )
			this.logger.debug("Entering backtrack line search, value=" + fold +
							"\ndirection.oneNorm: " + oneNorm(line) +
							"\ndirection.infNorm: " + infNorm(line));

		double sum = twoNorm(line);
		if ( sum > stpmax ) {
			this.logger.warn("Attempted step too big. scaling: sum=" + sum + ", stpmax=" + stpmax);
			timesEquals(line, stpmax / sum);
		}

		slope = dotProduct(g, line);
		this.logger.debug("slope=" + slope);

		if ( slope < 0 )
			throw new OptimizationException("Slope = " + slope + " is negative");
		if ( slope == 0 )
			throw new OptimizationException("Slope = " + slope + " is zero");

		//find maximum lambda
		//converge when (delta x) / x < REL_TOLX for all coordinates.
		//the largest step size that triggers this threshold is
		//precomputed and saved in alamin
		double test = 0.0;
		for ( int i = 0; i < oldx.length; i++ ) {
			double tmp = abs(line[i]) / max(abs(oldx[i]), 1.0);
			if ( tmp > test )
				test = tmp;
		}

		double alamin = relTolx / test;
		double alam = 1.0, alam2 = 0.0, oldAlam = 0.0, tmplam = 0.0;
		for ( iteration = 0; iteration < this.MAX_LINE_ITERATIONS; iteration++ ) {//look for step size in direction given by "line"
			//x = oldParameters + alam * line
			//initially, alam = 1.0, i.e. take full Newton step

			this.logger.debug("BackTrack loop iteration " + iteration + "\nalam=" + alam + ". oldAlam=" + oldAlam);
			if ( Logger.getRootLogger().isDebugEnabled() )
				this.logger.debug("before step, x.1norm: " + oneNorm(x) + 	"\nalam: " + alam + ", oldAlam: " + oldAlam);

			plusEquals(x, line, alam - oldAlam);//step
			if ( Logger.getRootLogger().isDebugEnabled() )
				this.logger.debug("after step, x.1norm: " + oneNorm(x));

			//check for convergence
			//convergence on delta x
			if ( (alam < alamin) || smallAbsDiff(oldx, x, absTolx) ) {
				this.optimizable.setX(oldx);
				f = this.optimizable.getValue();
				this.logger.debug("Exiting backtrack: Jump too small (alamin=" + alamin + ").\nExiting and using xold. Value=" + f);
				return 0.0;
			}

			this.optimizable.setX(x);
			oldAlam = alam;
			f = this.optimizable.getValue();
			this.logger.debug("value=" + f);

			//sufficient function increase (Wolf condition)
			if ( f >= (fold + ALF * alam * slope) ) {
				this.logger.debug("Exiting backtrack: value=" + f);

				if ( f < fold )
					throw new IllegalStateException("Function did not increase: f = " + f + " < " + fold + " = fold");
				return alam;
			}
			else if ( Double.isInfinite(f) || Double.isInfinite(f2) ) {
				this.logger.debug("Value is infinite after jump " + oldAlam + ". f=" + f + ", f2=" + f2 + ". Scaling back step size...");
				tmplam = 0.2 * alam;
				if ( alam < alamin ) {//convergence on delta x
					this.optimizable.setX(oldx);
					f = this.optimizable.getValue();
					this.logger.debug("Exiting backtrack: Jump too small. Exiting and using xold. Value=" + f);
					return 0.0;
				}
			}
			else {//backtrack
				if ( alam == 1.0 )//first time through
					tmplam = -slope / (2.0 * (f - fold - slope));
				else {
					double rhs1 = f - fold - alam * slope;
					double rhs2 = f2 - fold - alam2 * slope;

					double a = (rhs1 / (alam * alam) - rhs2 / (alam2 * alam2)) / (alam - alam2);
					double b = (-alam2 * rhs1 / (alam * alam) + alam * rhs2 / (alam2 * alam2)) / (alam - alam2);

					if ( a == 0.0 )
						tmplam = -slope / (2.0 * b);
					else {
						disc = b * b - 3.0 * a * slope;
						if ( disc < 0.0 )
							tmplam = 0.5 * alam;
						else if ( b <= 0.0 )
							tmplam = (-b + sqrt(disc)) / (3.0 * a);
						else
							tmplam = -slope / (b + sqrt(disc));
					}

					if ( tmplam > 0.5 * alam )
						tmplam = 0.5 * alam;//lambda <= 0.5 lambda_1
				}
			}

			alam2 = alam;
			f2 = f;

			this.logger.debug("tmplam:" + tmplam);
			alam = max(tmplam, 0.1 * alam);//lambda >= 0.1 * lambda_1
		}

		if ( iteration >= this.MAX_LINE_ITERATIONS )
			throw new IllegalStateException("Too many iterations.");

		return 0.0;
	}

	/**
	 * Pushes a new object onto the queue l
	 * @param l linked list queue of Matrix obj's
	 * @param toadd matrix to push onto queue
	 */
	private void push(LinkedList l, double[] toadd) {
		if( l.size() == this.M ) {
			//remove oldest matrix and add newset to end of list.
			//to make this more efficient, actually overwrite#
			//memory of oldest matrix

			//this overwrites the oldest matrix
			double[] last = (double[])l.get(0);
			System.arraycopy(toadd, 0, last, 0, toadd.length);
			Object ptr = last;

			//this readjusts the pointers in the list
			for ( int i = 0; i < l.size() - 1; i++ )
				l.set(i, (double[])l.get(i + 1));
			l.set(this.M - 1, ptr);
		}
		else {
			double[] newArray = new double[toadd.length];
			System.arraycopy(toadd, 0, newArray, 0, toadd.length);
			l.addLast(newArray);
		}
	}

	/**
	 * Pushes a new object onto the queue l
	 * @param l linked list queue of Double obj's
	 * @param toAdd double value to push onto queue
	 */
	private void push(LinkedList l, double toAdd) {
		if ( l.size() == this.M ) {//pop old double and add new
			l.removeFirst();
			l.addLast(toAdd);
		}
		else
			l.addLast(toAdd);
	}
}