package weka.classifiers.functions;

import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.floor;
import static java.lang.Math.log;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static weka.core.Math.digamma;
import static weka.core.Math.logGamma;
import static weka.core.Math.logSum;
import static weka.core.matrix.Maths.absNorm;
import static weka.core.matrix.Maths.dotProduct;
import static weka.core.matrix.Maths.infNorm;
import static weka.core.matrix.Maths.oneNorm;
import static weka.core.matrix.Maths.plusEquals;
import static weka.core.matrix.Maths.smallAbsDiff;
import static weka.core.matrix.Maths.timesEquals;
import static weka.core.matrix.Maths.twoNorm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.optimization.Optimizable;
import weka.core.optimization.OptimizationException;

/**
 * Supervised Latent Dirichlet Allocation
 * 
 * Valid options are:
 * 
 * <pre>-A &lt;double&gt;
 * Dirichlet prior</pre>
 * 
 * <pre>-T &lt;num&gt;
 * Number of topics</pre>
 * 
 * <pre>-R
 * Random start method</pre>
 * 
 * <pre>-S
 * Seeded start method</pre>
 * 
 * <pre>-N &lt;num&gt;
 * Number of iterations for corpus initialize sufficient statistic.</pre>
 * 
 * <pre>-L &lt;num&gt;
 * Maximum initial iterrations for LDA.</pre>
 * 
 * <pre>-mstep-iter &lt;num&gt;
 * Maximum iterration for m-step</pre>
 * 
 * <pre>-var-converged &lt;double&gt;
 * var convergence</pre>
 * 
 * <pre>-var-iter &lt;num&gt;
 * var maxmium iteration</pre>
 * 
 * <pre>-em-converged &lt;double&gt;
 * em convergence</pre>
 * 
 * <pre>-em-iter &lt;num&gt;
 * em maxmium iteration</pre>
 * 
 * <pre>-P &lt;double&gt;
 * penalty</pre>
 * 
 * @author jnphilipp
 * @version 1.0.0
 */
public class SLDA extends Classifier implements OptionHandler, TechnicalInformationHandler {
	/**
	 * random start method
	 */
	private static final String RANDOM_START_METHOD = "random";
	/**
	 * seeded start method
	 */
	private static final String SEEDED_START_METHOD = "seeded";
	/**
	 * logger
	 */
	private static final Logger logger = Logger.getLogger(SLDA.class);

	// options
	/**
	 * Dirichlet prior
	 */
	private double alpha = 1.0;
	/**
	 * number of topics
	 */
	private int numTopics = 20;
	/**
	 * start method
	 */
	private String startMethod = SLDA.RANDOM_START_METHOD;
	/**
	 * maximum iterations for m-step
	 */
	private int mstepMaxIter = 50;
	/**
	 * number of iterations for corpus initialize sufficient statistic
	 */
	private int numInit = 50;
	/**
	 * maximum initial iterations for LDA
	 */
	private int ldaInitMax = 0;
	/**
	 * var convergence
	 */
	private double varConverged = 0.001;
	/**
	 * var maximum iterations
	 */
	private int varMaxIter = 20;
	/**
	 * em convergence
	 */
	private double emConverged = 0.0001;
	/**
	 * em maximum iterations
	 */
	private int emMaxIter = 50;
	/**
	 * penalty
	 */
	private double penalty = 0.01;

	// properties
	/**
	 * size vocabulary
	 */
	private int sizeVocab;
	/**
	 * number of classes
	 */
	private int numClasses;
	/**
	 * log probability of words
	 */
	private double[][] logProbW;
	/**
	 * eta
	 */
	private double[][] eta;
	/**
	 * class attribute
	 */
	private Attribute classAttribute;
	/**
	 * attributes
	 */
	private Attribute[] attributes;

	public SLDA () {
		this.logProbW = new double[0][0];
		this.eta = new double[0][0];
	}

	/**
   * Returns an instance of a TechnicalInformation object, containing 
   * detailed information about the technical background of this class,
   * e.g., paper reference or book this class is based on.
   * 
   * @return the technical information about this class
   */
	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.TITLE, "Supervised Topic Models");
		result.setValue(Field.AUTHOR, "David M. Blei");
		result.setValue(Field.AUTHOR, "Jon McAuliffe");
		result.setValue(Field.YEAR, "2010");
		result.setValue(Field.MONTH, "March");
		result.setValue(Field.URL, "http://www.cs.princeton.edu/~blei/papers/BleiMcAuliffe2007.pdf");

		TechnicalInformation aditional = new TechnicalInformation(Type.ARTICLE);
		aditional.setValue(Field.TITLE, "Simultaneous Image Classification and Annotation");
		aditional.setValue(Field.AUTHOR, "Chong Wang");
		aditional.setValue(Field.AUTHOR, "David M. Blei");
		aditional.setValue(Field.AUTHOR, "Li Fei-Fei");
		aditional.setValue(Field.YEAR, "2009");
		aditional.setValue(Field.MONTH, "September");
		aditional.setValue(Field.URL, "http://cs.stanford.edu/groups/vision/documents/WangBleiFei-Fei_CVPR2009.pdf");
		result.add(aditional);

		TechnicalInformation aditional2 = new TechnicalInformation(Type.MISC);
		aditional2.setValue(Field.URL, "https://github.com/danstowell/slda");
		result.add(aditional2);

		return result;
	}

	/**
	 * Returns an enumeration describing the available options.
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration listOptions() {
		List<Option> options = new ArrayList<>();
		options.add(new Option("Direchlet prior", "A", 1, "-A"));
		options.add(new Option("Number of topics", "T", 1, "-T"));
		options.add(new Option("Random start method", "R", 0, "-R"));
		options.add(new Option("Seeded start method", "S", 0, "-S"));
		options.add(new Option("Number of iterations for corpus initialize sufficient statistic.", "N", 1, "-N"));
		options.add(new Option("Maximum initial iterations for LDA.", "L", 1, "-L"));
		options.add(new Option("Maximum iterration for m-step", "mstep-iter", 1, "-mstep-iter"));
		options.add(new Option("var convergence", "var-converged", 1, "-var-converged"));
		options.add(new Option("var maxmium iteration", "var-iter", 1, "-var-iter"));
		options.add(new Option("em convergence", "em-converged", 1, "-em-converged"));
		options.add(new Option("em maximum iteration", "em-iter", 1, "-em-iter"));
		options.add(new Option("Penalty", "P", 1, "-P"));

		return Collections.enumeration(options);
	}

	/**
	 * Parses a given list of options. Valid options are:
	 * 
	 * <pre>-A &lt;double&gt;
	 * Dirichlet prior</pre>
	 * 
	 * <pre>-T &lt;num&gt;
	 * Number of topics</pre>
	 * 
	 * <pre>-R
	 * Random start method</pre>
	 * 
	 * <pre>-S
	 * Seeded start method</pre>
	 * 
	 * <pre>-N &lt;num&gt;
	 * Number of iterations for corpus initialize sufficient statistic.</pre>
	 * 
	 * <pre>-L &lt;num&gt;
	 * Maximum initial iterrations for LDA.</pre>
	 * 
	 * <pre>-mstep-iter &lt;num&gt;
	 * Maximum iterration for m-step</pre>
	 * 
	 * <pre>-var-converged &lt;double&gt;
	 * var convergence</pre>
	 * 
	 * <pre>-var-iter &lt;num&gt;
	 * var maxmium iteration</pre>
	 * 
	 * <pre>-em-converged &lt;double&gt;
	 * em convergence</pre>
	 * 
	 * <pre>-em-iter &lt;num&gt;
	 * em maxmium iteration</pre>
	 * 
	 * <pre>-P &lt;double&gt;
	 * penalty</pre>
	 * 
	 * @param options
	 * @throws Exception 
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		String a = Utils.getOption("A", options);
		if ( !a.isEmpty() )
			this.alpha = Double.parseDouble(a);

		String t = Utils.getOption("T", options);
		if ( !t.isEmpty() )
			this.numTopics = Integer.parseInt(t);

		boolean r = Utils.getFlag('R', options);
		boolean s = Utils.getFlag('S', options);
		if ( r && !s )
			this.startMethod = SLDA.RANDOM_START_METHOD;
		else if ( s && !r )
			this.startMethod = SLDA.SEEDED_START_METHOD;
		else
			this.startMethod = SLDA.RANDOM_START_METHOD;

		String n = Utils.getOption("N", options);
		if ( !n.isEmpty() )
			this.numInit = Integer.parseInt(n);

		String l = Utils.getOption("L", options);
		if ( !l.isEmpty() )
			this.ldaInitMax = Integer.parseInt(l);

		String mstepIter = Utils.getOption("mstep-iter", options);
		if ( !mstepIter.isEmpty() )
			this.mstepMaxIter = Integer.parseInt(mstepIter);

		String varConverged = Utils.getOption("var-converged", options);
		if ( !varConverged.isEmpty() )
			this.varConverged = Double.parseDouble(varConverged);

		String varIter = Utils.getOption("var-iter", options);
		if ( !varIter.isEmpty() )
			this.varMaxIter = Integer.parseInt(varIter);

		String emConverged = Utils.getOption("em-converged", options);
		if ( !emConverged.isEmpty() )
			this.emConverged = Double.parseDouble(emConverged);

		String emIter = Utils.getOption("em-iter", options);
		if ( !emIter.isEmpty() )
			this.emMaxIter = Integer.parseInt(emIter);

		String p = Utils.getOption("P", options);
		if ( !p.isEmpty() )
			this.penalty = Double.parseDouble(p);

		Utils.checkForRemainingOptions(options);
	}

	/**
	 * Gets the current settings of the classifier.
	 * @return an array of strings suitable for passing to setOptions
	 */
	@Override
	public String[] getOptions() {
		List<String> options = new ArrayList<>();
		options.add("-A");
		options.add(Double.toString(this.alpha));
		options.add("-T");
		options.add(Double.toString(this.numTopics));

		if ( this.startMethod.equals(SLDA.RANDOM_START_METHOD) )
			options.add("-R");
		else if ( this.startMethod.equals(SLDA.SEEDED_START_METHOD) )
			options.add("-S");

		options.add("-N");
		options.add(Integer.toString(this.numInit));
		options.add("-L");
		options.add(Integer.toString(this.ldaInitMax));
		options.add("-mstep-iter");
		options.add(Integer.toString(this.mstepMaxIter));
		options.add("-var-converged");
		options.add(Double.toString(this.varConverged));
		options.add("-var-iter");
		options.add(Integer.toString(this.varMaxIter));
		options.add("-em-converged");
		options.add(Double.toString(this.emConverged));
		options.add("-em-iter");
		options.add(Integer.toString(this.emMaxIter));
		options.add("-P");
		options.add(Double.toString(this.penalty));

		return options.toArray(new String[options.size()]);
	}

	/**
	 * Returns default capabilities of the classifier.
	 * @return the capabilities of this classifier
	 */
	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();

		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);

		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.NUMERIC_CLASS);
		result.disable(Capability.MISSING_CLASS_VALUES);
		result.disable(Capability.NO_CLASS);
		result.setMinimumNumberInstances(1);

		return result;
	}

	/**
	 * Generates the classifier. 
	 * @param instances set of instances serving as training data
	 * @throws Exception if the classifier has not been generated successfully
	 */
	@Override
	public void buildClassifier(Instances instances) throws Exception {
		this.getCapabilities().testWithFail(instances);

		instances = new Instances(instances);
		instances.deleteWithMissingClass();

		int num = instances.numInstances();
		for ( int n = instances.numInstances() - 1; n >= 0; n-- ) {
			Enumeration attrs = instances.enumerateAttributes();
			boolean empty = true;
			while ( attrs.hasMoreElements() ) {
				if ( instances.instance(n).value((Attribute)attrs.nextElement()) > 0 ) {
					empty = false;
					break;
				}
			}

			if ( empty )
				instances.delete(n);
		}

		if ( instances.numInstances() < num )
			logger.warn("Deleted " + (num - instances.numInstances()) + " instances with only zero counts.");

		this.sizeVocab = instances.numAttributes() - 1;
		this.numClasses = instances.numClasses();

		this.logProbW = new double[this.numTopics][this.sizeVocab];
		this.eta = new double[this.numClasses][this.numTopics];

		this.classAttribute = instances.classAttribute();

		int n = 0;
		this.attributes = new Attribute[this.sizeVocab];
		Enumeration attrs = instances.enumerateAttributes();
		while ( attrs.hasMoreElements() ) {
			this.attributes[n] = (Attribute)attrs.nextElement();
			n++;
		}

		n = 0;
		double[] instanceTotal = new double[instances.numInstances()];
		Enumeration ins = instances.enumerateInstances();
		while ( ins.hasMoreElements() ) {
			Instance instance = (Instance)ins.nextElement();

			for ( Attribute attribute : this.attributes )
				instanceTotal[n] += instance.value(attribute);

			n++;
		}

		double[][] varGamma = new double[instances.numInstances()][this.numTopics];
		double[][] phi = new double[this.sizeVocab][this.numTopics];
		double oldLikelihood = 0, converged = 1, likelihood = 0;
		SuffStats ss = new SuffStats(instances.numInstances());

		switch ( this.startMethod ) {
			case "seeded":
				corpusInitializeSS(ss, instances, instanceTotal);
				mle(ss, 0);
				break;
			case "random":
				randomInitializeSS(ss, instances, instanceTotal);
				mle(ss, 0);
				break;
			default:
		}

		int etaUpdate = 0, i = 1;
		while ( ((converged < 0) || (converged > this.emConverged) || (i <= this.ldaInitMax + 2)) && (i <= this.emMaxIter) ) {
			logger.info("*****  em iteration " + i + "  *****");
			likelihood = 0;
			ss = zeroInitializeSuffStats(ss);

			if ( i > this.ldaInitMax )
				etaUpdate = 1;

			//e-step
			logger.info("*****  e-step  *****");
			for ( int d = 0; d < instances.numInstances(); d++ )
				likelihood += this.docEStep(instances.instance(d), instanceTotal[d], varGamma[d], phi, ss, etaUpdate);

			logger.debug("likelihood: " + likelihood);

			// m-step
			logger.info("*****  m-step  *****");
			mle(ss, etaUpdate);

			// check for convergence
			converged = abs((oldLikelihood - likelihood) / (oldLikelihood));
			oldLikelihood = likelihood;
			i++;
		}

		for ( int d = 0; d < instances.numInstances(); d++ )//final inference
			likelihood += sldaInference(instances.instance(d), instanceTotal[d], varGamma[d], phi);
	}

	/**
	 * Classifies an instance.
	 * @param instance the instance to classify
	 * @return the classification for the instance
	 * @throws Exception if instance can't be classified successfully 
	 */
	@Override
	public double classifyInstance(Instance instance) throws Exception {
		double[] distribution = this.distributionForInstance(instance);
		double max = 0.0;
		int label = -1;

		for ( int i = 0; i < distribution.length; i++ ) {
			if ( distribution[i] > max ) {
				max = distribution[i];
				label = i;
			}
		}

		return label;
	}

	/**
	 * Calculates the class membership probabilities for the given test instance. 
	 * @param instance the instance to be classified
	 * @return predicted class probability distribution
	 * @throws Exception if there is a problem generating the prediction
	 */
	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		double total = 0.0;
		for ( int n = 0; n < this.sizeVocab; n++ )
			total += instance.value(this.attributes[n]);

		double[] varGamma = new double[this.numTopics];
		double[][] phi = new double[this.sizeVocab][this.numTopics];
		double[] phiM = new double[this.numTopics];
		double likelihood = ldaInference(instance, total, varGamma, phi);

		for ( int n = 0; n < this.sizeVocab; n++ )
			for ( int k = 0; k < this.numTopics; k++ )
				phiM[k] += instance.value(this.attributes[n]) * phi[n][k];


		for ( int k = 0; k < this.numTopics; k++ )
			phiM[k] /= total;

		//do classification
		double[] distribution = new double[this.numClasses];
		for ( int i = 0; i < this.numClasses; i++ ) {
			double score = 0.0;

			for ( int k = 0; k < this.numTopics; k++ )
				score += this.eta[i][k] * phiM[k];

			distribution[i] = score / this.numClasses;
		}

		return distribution;
	}

	/**
	 * Returns the top words for each class. First word is the class name.
	 * @return 
	 */
	public String[][] getTopWords() {
		double[][] wordScores = new double[this.numClasses][this.sizeVocab];
		for ( int i = 0; i < this.numClasses; i++ )
			for ( int w = 0; w < this.sizeVocab; w++ )
				for ( int k = 0; k < this.numTopics; k++ )
					wordScores[i][w] += this.eta[i][k] * this.logProbW[k][w];

		String[][] topWords = new String[this.numClasses][this.numTopics + 1];
		for ( int i = 0; i < this.numClasses; i++ ) {
			int[] indices = new int[this.numTopics];
			double[] maxs = new double[this.numTopics];

			Arrays.fill(indices, -1);
			Arrays.fill(maxs, -Double.MAX_VALUE);

			for ( int w = 0; w < this.sizeVocab; w++ ) {
				for ( int k = 0; k < this.numTopics; k++ ) {
					if ( maxs[k] < wordScores[i][w] ) {
						for ( int j = this.numTopics - 1; j > k; j-- ) {
							maxs[j] = maxs[j - 1];
							indices[j] = indices[j - 1];
						}

						maxs[k] = wordScores[i][w];
						indices[k] = w;
						break;
					}
				}
			}

			topWords[i][0] = this.classAttribute.value(i);
			for ( int k = 1; k < this.numTopics + 1; k++ )
				if ( indices[k - 1] != -1 )
					topWords[i][k] = this.attributes[indices[k - 1]].name();
		}

		return topWords;
	}

	/**
	 * Random initializes the given sufficient statistic.
	 * @param ss sufficient statistic
	 * @param instances instances
	 * @param instanceTotal total value per instance
	 */
	private void randomInitializeSS(SuffStats ss, Instances instances, double[] instanceTotal) {
		Random r = new Random(System.currentTimeMillis());
		for ( int k = 0; k < this.numTopics; k++ ) {
			for ( int w = 0; w < this.sizeVocab; w++ ) {
				ss.wordSS[k][w] = 1.0 / this.sizeVocab + 0.1 * r.nextDouble();
				ss.wordTotalSS[k] += ss.wordSS[k][w];
			}
		}

		for ( int d = 0; d < ss.numDocs; d++ ) {
			Instance instance = instances.instance(d);
			ss.labels[d] = (int)instance.classValue();
			ss.totLabels[(int)instance.classValue()]++;

			double total = 0.0;
			for ( int k = 0; k < this.numTopics; k++ ) {
				ss.zBarM[d][k] = r.nextDouble();
				total += ss.zBarM[d][k];
			}

			for ( int k = 0; k < this.numTopics; k++ )
				ss.zBarM[d][k] /= total;

			for ( int k = 0; k < this.numTopics; k++ ) {
				for ( int j = k; j < this.numTopics; j++ ) {
					int idx = this.mapIdx(k, j, this.numTopics);

					if ( j == k )
						ss.zBarVar[d][idx] = ss.zBarM[d][k] / instanceTotal[d];
					else
						ss.zBarVar[d][idx] = 0.0;

					ss.zBarVar[d][idx] -= ss.zBarM[d][k] * ss.zBarM[d][j] / instanceTotal[d];
				}
			}
		}
	}

	/**
	 * Corpus initializes the given sufficient statistic.
	 * @param ss sufficient statistic
	 * @param instances instances
	 * @param instance_total total value per instance
	 */
	private void corpusInitializeSS(SuffStats ss, Instances instances, double[] instance_total) {
		Random r = new Random(System.currentTimeMillis());
		for ( int k = 0; k < this.numTopics; k++ ) {
			for ( int i = 0; i < this.numInit; i++ ) {
				int d = (int)floor(r.nextDouble() * ss.numDocs);
				Instance instance = instances.instance(d);

				for ( int n = 0; n < this.sizeVocab; n++ )
					ss.wordSS[k][n] += instance.value(this.attributes[n]);
			}

			for ( int w = 0; w < this.sizeVocab; w++ ) {
				ss.wordSS[k][w] = 2 * ss.wordSS[k][w] + 5 + r.nextDouble();
				ss.wordTotalSS[k] = ss.wordTotalSS[k] + ss.wordSS[k][w];
			}
		}

		for ( int d = 0; d < ss.numDocs; d++ ) {
			Instance instance = instances.instance(d);
			ss.labels[d] = (int)instance.classValue();
			ss.totLabels[(int)instance.classValue()]++;

			double total = 0.0;
			for ( int k = 0; k < this.numTopics; k++ ) {
				ss.zBarM[d][k] = r.nextDouble();
				total += ss.zBarM[d][k];
			}

			for ( int k = 0; k < this.numTopics; k++ )
				ss.zBarM[d][k] /= total;

			for ( int k = 0; k < this.numTopics; k++ ) {
				for ( int j = k; j < this.numTopics; j++ ) {
					int idx = this.mapIdx(k, j, this.numTopics);

					if ( j == k )
						ss.zBarVar[d][idx] = ss.zBarM[d][k] / instance_total[d];
					else
						ss.zBarVar[d][idx] = 0.0;

					ss.zBarVar[d][idx] -= ss.zBarM[d][k] * ss.zBarM[d][j] / instance_total[d];
				}
			}
		}
	}

	/**
	 * Optimizes eta through LM-BFGS.
	 * @param ss sufficient statistic
	 * @param etaUpdate if eta update is zero this will run without LM-BFGS.
	 */
	private void mle(SuffStats ss, int etaUpdate) throws IllegalArgumentException {
    for ( int k = 0; k < this.numTopics; k++ )
			for ( int w = 0; w < this.sizeVocab; w++ )
				if ( ss.wordSS[k][w] > 0 )
					this.logProbW[k][w] = log(ss.wordSS[k][w]) - log(ss.wordTotalSS[k]);
				else
					this.logProbW[k][w] = -100.0;

		if ( etaUpdate == 0 )
			return;

		double[] x = new double[this.numClasses * this.numTopics];
		for ( int l = 0; l < this.numClasses; l++ )
			System.arraycopy(this.eta[l], 0, x, l * this.numTopics, this.numTopics);

		//LimitedMemoryBFGS bfgs = new LimitedMemoryBFGS(ss, x);
		OptimizeEta optimizable = new OptimizeEta(ss, x);
		weka.core.optimization.LimitedMemoryBFGS bfgs = new weka.core.optimization.LimitedMemoryBFGS(optimizable);
		bfgs.setTolerance(1e-4);
		try {
			bfgs.optimize(this.mstepMaxIter);
		}
		catch ( OptimizationException e ) {
			logger.warn(e);
			if ( Logger.getRootLogger().isDebugEnabled() )
				logger.trace(e);
		}

		bfgs.getOptimizable().getX(x);
		for ( int l = 0; l < this.numClasses; l++ )
			for ( int k = 0; k < this.numTopics; k++ )
				this.eta[l][k] = x[l * this.numTopics + k];
				//this.eta[l][k] = bfgs.getOptimizedParameter(l * this.numTopics + k);
	}

	/**
	 * E-Step per document.
	 * @param instance instance
	 * @param instanceTotal total value
	 * @param gamma gamma
	 * @param phi phi
	 * @param ss sufficient statistic
	 * @param etaUpdate eta update
	 * @return likelihood
	 */
	private double docEStep(Instance instance, double instanceTotal, double[] gamma, double[][] phi, SuffStats ss, int etaUpdate) {
		double likelihood;
		if ( etaUpdate == 1 )
			likelihood = sldaInference(instance, instanceTotal, gamma, phi);
		else
			likelihood = ldaInference(instance, instanceTotal, gamma, phi);

		//update sufficient statistics
		for ( int n = 0; n < this.sizeVocab; n++ ) {
			if ( instance.value(this.attributes[n]) == 0.0 )
				continue;

			for ( int k = 0; k < this.numTopics; k++ ) {
				ss.wordSS[k][n] += instance.value(this.attributes[n]) * phi[n][k];
				ss.wordTotalSS[k] += instance.value(this.attributes[n]) * phi[n][k];

				//statistics for each document of the supervised part
				ss.zBarM[ss.numDocs][k] += instance.value(this.attributes[n]) * phi[n][k];//mean
				for ( int i = k; i < this.numTopics; i++ ) {//variance
					int idx = this.mapIdx(k, i, this.numTopics);
					if ( i == k )
						ss.zBarVar[ss.numDocs][idx] += instance.value(this.attributes[n]) * instance.value(this.attributes[n]) * phi[n][k];

					ss.zBarVar[ss.numDocs][idx] -= instance.value(this.attributes[n]) * instance.value(this.attributes[n]) * phi[n][k] * phi[n][i];
				}
			}
		}

		for ( int k = 0; k < this.numTopics; k++ )
			ss.zBarM[ss.numDocs][k] /= instanceTotal;

		for ( int i = 0; i < (this.numTopics * (this.numTopics + 1) / 2); i++ )
			ss.zBarVar[ss.numDocs][i] /= (instanceTotal * instanceTotal);

		ss.numDocs = ss.numDocs + 1;//because we need it for store statistics for each docs
		return likelihood;
	}

	/**
	 * LDA inference.
	 * @param instance instance
	 * @param instanceTotal total value
	 * @param varGamma var gamma
	 * @param phi phi
	 * @return likelihood
	 */
	private double ldaInference(Instance instance, double instanceTotal, double[] varGamma, double[][] phi) {
		double converged = 1, likelihood = 0, oldLikelihood = 0;
		double[] oldphi = new double[this.numTopics];
		double[] digammaGam = new double[this.numTopics];

		// compute posterior dirichlet
		for ( int k = 0; k < this.numTopics; k++ ) {
			varGamma[k] = this.alpha + (instanceTotal / ((double)this.numTopics));
			digammaGam[k] = digamma(varGamma[k]);

			for ( int n = 0; n < this.sizeVocab; n++ )
				phi[n][k] = 1.0 / (double)this.numTopics;
		}

		int varIter = 0;
		while ( converged > this.varConverged && (varIter < this.varMaxIter || this.varMaxIter == -1) ) {
			varIter++;

			for ( int n = 0; n < this.sizeVocab; n++ ) {
				double phisum = 0;
				for ( int k = 0; k < this.numTopics; k++) {
					oldphi[k] = phi[n][k];
					phi[n][k] = digammaGam[k] + this.logProbW[k][n];

					phisum = ( k > 0 ? logSum(phisum, phi[n][k]) : phi[n][k]);//note, phi is in log space
				}

				for ( int k = 0; k < this.numTopics; k++ ) {
					phi[n][k] = exp(phi[n][k] - phisum);
					varGamma[k] += instance.value(this.attributes[n]) * (phi[n][k] - oldphi[k]);
					digammaGam[k] = digamma(varGamma[k]);
				}
			}

			likelihood = ldaComputeLikelihood(instance, phi, varGamma);
			logger.debug("lda inference likelihood: " + likelihood);
			converged = (oldLikelihood - likelihood) / oldLikelihood;
			oldLikelihood = likelihood;
		}

		return likelihood;
	}

	/**
	 * SLDA inference.
	 * @param instance instance
	 * @param instanceTotal total value
	 * @param varGamma var gamma
	 * @param phi phi
	 * @return likelihood
	 */
	private double sldaInference(Instance instance, double instanceTotal, double[] varGamma, double[][] phi) {
		final int FP_MAX_ITER = 10;
		double converged = 1, likelihood = 0, oldLikelihood = 0;
		double[] oldphi = new double[this.numTopics];
		double[] digammaGam = new double[this.numTopics];
		double[] sfAux = new double[this.numClasses];

		//compute posterior dirichlet
		for ( int k = 0; k < this.numTopics; k++ ) {
			varGamma[k] = this.alpha + (instanceTotal / ((double)this.numTopics));
			digammaGam[k] = digamma(varGamma[k]);

			for ( int n = 0; n < this.sizeVocab; n++ )
				phi[n][k] = 1.0 / (double)this.numTopics;
		}

		for ( int l = 0; l < this.numClasses; l++ ) {
			sfAux[l] = 1.0;//the quantity for equation 6 of each class

			for ( int n = 0; n < this.sizeVocab; n++ ) {
				if ( instance.value(this.attributes[n]) == 0.0 )
					continue;
	
				double t = 0.0;
				for ( int k = 0; k < this.numTopics; k++ )
						t += phi[n][k] * exp(this.eta[l][k] * instance.value(this.attributes[n]) / instanceTotal);

				sfAux[l] *= t;
			}
		}

		int varIter = 0;
		while ( (converged > this.varConverged) && ((varIter < this.varMaxIter) || (this.varMaxIter == -1)) ) {
			varIter++;

			for ( int n = 0; n < this.sizeVocab; n++ ) {//compute sf_params
				if ( instance.value(this.attributes[n]) == 0.0 )
					continue;

				double[] sfParams = new double[this.numTopics];
				for ( int l = 0; l < this.numClasses; l++ ) {
					double t = 0.0;

					for ( int k = 0; k < this.numTopics; k++ )
						t += phi[n][k] * exp(eta[l][k] * instance.value(this.attributes[n]) / instanceTotal);

					sfAux[l] /= t;//take out word n

					for ( int k = 0; k < this.numTopics; k++ )//h in the paper
						sfParams[k] += sfAux[l] * exp(this.eta[l][k] * instance.value(this.attributes[n]) / instanceTotal);
				}

				System.arraycopy(phi[n], 0, oldphi, 0, this.numTopics);
				for ( int fp_iter = 0; fp_iter < FP_MAX_ITER; fp_iter++ ) {//fixed point update
					double sf_val = 1.0;//the base class, in log space

					for ( int k = 0; k < this.numTopics; k++ )
						sf_val += sfParams[k] * phi[n][k];

					double phisum = 0;
					for ( int k = 0; k < this.numTopics; k++ ) {
						phi[n][k] = digammaGam[k] + this.logProbW[k][n];

						//added softmax parts
							phi[n][k] += this.eta[(int)instance.classValue()][k] / instanceTotal;
						phi[n][k] -= sfParams[k] / (sf_val * instance.value(this.attributes[n]));
						phisum = (k > 0 ? logSum(phisum, phi[n][k]) : phi[n][k]);//note, phi is in log space
					}

					for ( int k = 0; k < this.numTopics; k++ )
						phi[n][k] = exp(phi[n][k] - phisum);//normalize
				}

				//back to sf_aux value
				for ( int l = 0; l < this.numClasses; l++ ) {
					double t = 0.0;

					for ( int k = 0; k < this.numTopics; k++ )
						t += phi[n][k] * exp(this.eta[l][k] * instance.value(this.attributes[n]) / instanceTotal);

					sfAux[l] *= t;
				}

				for ( int k = 0; k < this.numTopics; k++ ) {
					varGamma[k] += instance.value(this.attributes[n]) * (phi[n][k] - oldphi[k]);
					digammaGam[k] = digamma(varGamma[k]);
				}
			}

			likelihood = sldaComputeLikelihood(instance, instanceTotal, phi, varGamma);
			logger.debug("slda inference likelihood: " + likelihood);
			if ( Double.isNaN(likelihood) ) {
				System.out.println(instance);
				System.exit(1);
			}
			converged = abs((oldLikelihood - likelihood) / oldLikelihood);
			oldLikelihood = likelihood;
		}

		return likelihood;
	}

	/**
	 * Compute LDA likelihood.
	 * @param instance instance
	 * @param phi phi
	 * @param varGamma var gamma
	 * @return likelihood
	 */
	private double ldaComputeLikelihood(Instance instance, double[][] phi, double[] varGamma) {
		double[] dig = new double[this.numTopics];
		double varGammaSum = 0, alphaSum = this.numTopics * this.alpha;

		for ( int k = 0; k < this.numTopics; k++ ) {
			dig[k] = digamma(varGamma[k]);
			varGammaSum += varGamma[k];
		}

		double digsum = digamma(varGammaSum);
		double likelihood = logGamma(alphaSum) - logGamma(varGammaSum);
		for ( int k = 0; k < this.numTopics; k++ ) {
			likelihood += - logGamma(this.alpha) + (this.alpha - 1) * (dig[k] - digsum) + logGamma(varGamma[k]) - (varGamma[k] - 1) * (dig[k] - digsum);

			for ( int n = 0; n < this.sizeVocab; n++ )
				if ( phi[n][k] > 0 )
						likelihood += instance.value(this.attributes[n]) * (phi[n][k] * ((dig[k] - digsum) - log(phi[n][k]) + this.logProbW[k][n]));
				}

		return likelihood;
	}

	/**
	 * SLDA compute likelihood.
	 * @param instance instance
	 * @param instanceTotal total value
	 * @param phi phi
	 * @param varGamma var gamma
	 * @return likelihood
	 */
	private double sldaComputeLikelihood(Instance instance, double instanceTotal, double[][] phi, double[] varGamma) {
		double[] dig = new double[this.numTopics];
		double varGammaSum = 0, alphaSum = this.numTopics * this.alpha;

		for ( int k = 0; k < this.numTopics; k++ ) {
			dig[k] = digamma(varGamma[k]);
			varGammaSum += varGamma[k];
		}

		double digsum = digamma(varGammaSum);
		double likelihood = logGamma(alphaSum) - logGamma(varGammaSum);
		double t = 0.0;
		for ( int k = 0; k < this.numTopics; k++ ) {
			likelihood += -logGamma(this.alpha) + (this.alpha - 1) * (dig[k] - digsum) + logGamma(varGamma[k]) - (varGamma[k] - 1) * (dig[k] - digsum);

			for ( int n = 0; n < this.sizeVocab; n++ )
				if ( phi[n][k] > 0 ) {
					likelihood += instance.value(this.attributes[n]) * (phi[n][k] * ((dig[k] - digsum) - log(phi[n][k]) + this.logProbW[k][n]));
					t += this.eta[(int)instance.classValue()][k] * instance.value(this.attributes[n]) * phi[n][k];
				}
		}
		likelihood += t / instanceTotal;//eta_k*\bar{\phi}

		t = 1.0;//the class model->num_classes-1
		for ( int l = 0; l < this.numClasses; l++ ) {
			double t1 = 1.0;
			for ( int n = 0; n < this.sizeVocab; n++ ) {
				if ( instance.value(this.attributes[n]) == 0.0 )
					continue;

				double t2 = 0.0;
				for ( int k = 0; k < this.numTopics; k++ )
					t2 += phi[n][k] * exp(this.eta[l][k] * instance.value(this.attributes[n]) / instanceTotal);

				t1 *= t2;
			}

			t += t1;
		}

		likelihood -= log(t);		
		return likelihood;
	}

	/**
	 * Map index;
	 * @param row row
	 * @param col col
	 * @param dim dim
	 * @return index
	 */
	private int mapIdx(int row, int col, int dim) {
		if ( row > col ) {
			int swap = row;
			row = col;
			col = swap;
		}

		return (2 * dim - row + 1) * row / 2 + col - row;
	}

	/**
	 * Sets all values to zero for the given sufficient statistic.
	 * @param ss sufficient statistic to set to zero
	 * @return sufficient statistic with zero values
	 */
	private SuffStats zeroInitializeSuffStats(SuffStats ss) {
		ss.wordTotalSS = new double[numTopics];
		ss.wordSS = new double[numTopics][sizeVocab];

		ss.zBarM = new double[ss.numDocs][numTopics];
		ss.zBarVar = new double[ss.numDocs][numTopics * (numTopics + 1) / 2];

		ss.numDocs = 0;

		return ss;
	}

	/**
	 * sufficient statistic
	 */
	class SuffStats {
		/**
		 * number documents
		 */
		public int numDocs;
		/**
		 * labels
		 */
		public int[] labels;
		/**
		 * total labels
		 */
		public int[] totLabels;
		/**
		 * word total sufficient statistic
		 */
		public double[] wordTotalSS;
		/**
		 * word sufficient statistic
		 */
		public double[][] wordSS;
		/**
		 * z bar m
		 */
		public double[][] zBarM;
		/**
		 * z bar var
		 */
		public double[][] zBarVar;

		/**
		 * Creates new sufficient statistic.
		 * @param numDocs number of documents
		 */
		public SuffStats(int numDocs) {
			this.numDocs = numDocs;
			this.wordTotalSS = new double[numTopics];
			this.wordSS = new double[numTopics][sizeVocab];

			this.zBarM = new double[this.numDocs][numTopics];
			this.zBarVar = new double[this.numDocs][numTopics * (numTopics + 1) / 2];

			this.labels = new int[numDocs];
			this.totLabels = new int[numClasses];
		}
	}

	class OptimizeEta implements Optimizable.ByGradientValue {
		private SuffStats ss;
		private double[] x;

		public OptimizeEta(SuffStats ss, double[] x) {
			this.ss = ss;
			this.x = x;
		}

		@Override
		public int size() { return this.x.length; }

		@Override
		public void getX(double[] buffer) { System.arraycopy(this.x, 0, buffer, 0, this.x.length); }

		@Override
		public double getX(int index) { return this.x[index]; }

		@Override
		public void setX(double[] params) { System.arraycopy(params, 0, this.x, 0, params.length); }

		@Override
		public void setX(int index, double value) { this.x[index] = value; }

		/**
		 * Calculates the new gradient values.
		 * @param buffer store the new gradient values
		 */
		@Override
		public void getValueGradient(double[] buffer) {
			double[] df = new double[this.x.length];
			double[] tmp = new double[this.x.length];

			for ( int l = 0; l < numClasses; l++ )
				for ( int k = 0; k < numTopics; k++ )
					df[l * numTopics + k] = -penalty * this.x[l * numTopics + k];

			for ( int d = 0; d < this.ss.numDocs; d++ ) {
				for ( int k = 0; k < numTopics; k++ )
					df[this.ss.labels[d] * numTopics + k] += this.ss.zBarM[d][k];

				double t = 0.0;//in log space, 1+exp()+exp()+....
				System.arraycopy(df, 0, tmp, 0, df.length);
				Arrays.fill(df, 0);

				for ( int l = 0; l < numClasses; l++ ) {
					double[] eta_aux = new double[numTopics];
					double a1 = 0.0;//\eta_k^T * \bar{\phi}_d
					double a2 = 0.0;//1 + 0.5*\eta_k^T * Var(z_bar)\eta_k

					for ( int k = 0; k < numTopics; k++ ) {
						a1 += this.x[l * numTopics + k] * this.ss.zBarM[d][k];

						for ( int j = 0; j < numTopics; j++ ) {
							a2 += this.x[l * numTopics + k] * ss.zBarVar[d][mapIdx(k, j, numTopics)] * this.x[l * numTopics + j];
							eta_aux[k] += this.ss.zBarVar[d][mapIdx(k, j, numTopics)] * this.x[l * numTopics + j];
						}
					}

					a2 = 1.0 + 0.5 * a2;
					t = logSum(t, a1 + log(a2));

					for ( int k = 0; k < numTopics; k++ )
						df[l * numTopics + k] -= exp(a1) * (this.ss.zBarM[d][k] * a2 + eta_aux[k]);
				}

				for ( int i = 0; i < df.length; i++ )
					df[i] = df[i] * exp(-t) + tmp[i];
			}

			System.arraycopy(df, 0, buffer, 0, df.length);
		}

		/**
		 * Calculates the gradient value.
		 * @return gradient value
		 */
		@Override
		public double getValue() {
			double fRegularization = 0.0;
			for ( int l = 0; l < numClasses; l++ )
				for ( int k = 0; k < numTopics; k++ )
					fRegularization -= pow(this.x[l * numTopics + k], 2) * penalty / 2.0;

			double f = 0.0;//log likelihood
			for ( int d = 0; d < this.ss.numDocs; d++ ) {
				for ( int k = 0; k < numTopics; k++ )
					if ( this.ss.labels[d] < numClasses )
						f += this.x[this.ss.labels[d] * numTopics + k] * this.ss.zBarM[d][k];

				double t = 0.0;//in log space, 1+exp()+exp()...
				for ( int l = 0; l < numClasses; l++ ) {
					double a1 = 0.0;//\eta_k^T * \bar{\phi}_d
					double a2 = 0.0;//1 + 0.5 * \eta_k^T * Var(z_bar)\eta_k

					for ( int k = 0; k < numTopics; k++ ) {
						a1 += this.x[l * numTopics + k] * ss.zBarM[d][k];
						for ( int j = 0; j < numTopics; j++ )
							a2 += this.x[l * numTopics + k] * this.ss.zBarVar[d][mapIdx(k, j, numTopics)] * this.x[l * numTopics + j];
					}

					a2 = 1.0 + 0.5 * a2;
					t = logSum(t, a1 + log(a2));
				}
				  f -= t;
	    }

			return (f + fRegularization);
		}
	}

	/**
	 * Limited Memory BFGS
	 */
	class LimitedMemoryBFGS {
		/**
		 * maximum iterations
		 */
		private final int MAX_ITER = 1000;

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
		 * parameters
		 */
		private double[] parameters;
		/**
		 * sufficient statistic
		 */
		private SuffStats ss;

		/**
		 * Creates a new limited memory BFGS.
		 * @param ss sufficient statistic
		 * @param parameters parameters to optimize
		 */
		public LimitedMemoryBFGS(SuffStats ss, double[] parameters) {
			this.ss = ss;
			this.parameters = parameters;
		}

		/**
		 * @return <code>true</code> if converged
		 */
		public boolean isConverged () {
			return this.converged;
		}

		/**
		 * Returns the optimized parameter
		 * @param index index
		 * @return optimized value
		 */
		public double getOptimizedParameter(int index) {
			return this.parameters[index];
		}

		/**
		 * @param tolerance tolerance to set
		 */
		public void setTolerance(double tolerance) {
			this.tolerance = tolerance;
		}

		/**
		 * Runs the optimization.
		 * @param numIterations number of iterations.
		 * @return <code>true</code> if converged
		 */
		public boolean optimize(int numIterations) {
			int iterations = 0;
			double initialValue = this.getValue(), step = 1.0;
			double[] alpha, g, oldg, direction, p, oldp;
			LinkedList s = new LinkedList();
			LinkedList y = new LinkedList();
			LinkedList rho = new LinkedList();

			Logger.getLogger(LimitedMemoryBFGS.class).debug("Entering L-BFGS.optimize(). Initial Value=" + initialValue );
			Logger.getLogger(LimitedMemoryBFGS.class).debug("First time through L-BFGS");
			alpha = new double[this.M];

			p = new double[this.parameters.length];
			oldp = new double[this.parameters.length];
			g = new double[this.parameters.length];
			oldg = new double[this.parameters.length];
			direction = new double[this.parameters.length];

			System.arraycopy(this.parameters, 0, p, 0, this.parameters.length);
			System.arraycopy(p, 0, oldp, 0, p.length);

			this.getValueGradient(g);
			System.arraycopy(g, 0, oldg, 0, g.length);
			System.arraycopy(g, 0, direction, 0, g.length);

			if ( absNorm(direction) == 0 ) {
				Logger.getLogger(LimitedMemoryBFGS.class).warn("L-BFGS initial gradient is zero; saying converged");
				this.converged = true;
				return true;
			}

			if ( Logger.getRootLogger().isDebugEnabled() )
				Logger.getLogger(LimitedMemoryBFGS.class).debug("direction.2norm: " + twoNorm(direction));
			timesEquals(direction, 1.0 / twoNorm(direction));
			//make initial jump
			if ( Logger.getRootLogger().isDebugEnabled() )
				Logger.getLogger(LimitedMemoryBFGS.class).debug("before initial jump:" +
								"\ndirection.2norm: " + twoNorm(direction) +
								"\ngradient.2norm: " + twoNorm(g) +
								"\nparameters.2norm: " + twoNorm(p));

			step = this.optimizeLine(direction, step);
			if ( step == 0.0 ) {//could not step in this direction.
				//give up and say converged.
				step = 1.0;
				throw new OptimizationException("Line search could not step in the current direction. " +
								"(This is not necessarily cause for alarm. Sometimes this happens close to the maximum," +
								" where the function may be very flat.)");
			}

			System.arraycopy(this.parameters, 0, p, 0, this.parameters.length);
			this.getValueGradient(g);
			if ( Logger.getRootLogger().isDebugEnabled() )
				Logger.getLogger(LimitedMemoryBFGS.class).debug("after initial jump:" +
								"\ndirection.2norm: " + twoNorm(direction) +
								"\ngradient.2norm: " + twoNorm(g));

			for ( int iterationCount = 0; iterationCount < numIterations; iterationCount++ ) {
				double value = this.getValue();
				if ( Logger.getRootLogger().isDebugEnabled() )
					Logger.getLogger(LimitedMemoryBFGS.class).debug("L-BFGS iteration=" + iterationCount +
									", value=" + value + " g.twoNorm: " + twoNorm(g) +
									" oldg.twoNorm: " + twoNorm(oldg));

				//get difference between previous 2 gradients and parameters
				double sy = 0.0;
				double yy = 0.0;
				for ( int i = 0; i < oldp.length; i++ ) {
					if ( Double.isInfinite(p[i]) && Double.isInfinite(oldp[i]) && (p[i] * oldp[i] > 0) )
						oldp[i] = 0.0;
					else
						oldp[i] = p[i] - oldp[i];

					if ( Double.isInfinite(g[i]) && Double.isInfinite(oldg[i]) && (g[i] * oldg[i] > 0) )
						oldg[i] = 0.0;
					else
						oldg[i] = g[i] - oldg[i];

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
					Logger.getLogger(LimitedMemoryBFGS.class).debug("before linesearch: direction.gradient.dotprod: " + dotProduct(direction, g) +
									"direction.2norm: " +	twoNorm(direction) +
									"parameters.2norm: " + twoNorm(p));

				step = this.optimizeLine(direction, step);
				if ( step == 0.0 ) {//could not step in this direction.
					step = 1.0;
					throw new OptimizationException("Line search could not step in the current direction. " +
									"(This is not necessarily cause for alarm. Sometimes this happens close to the maximum," +
									" where the function may be very flat.)");
				}

				System.arraycopy(this.parameters, 0, p, 0, this.parameters.length);
				this.getValueGradient(g);
				if ( Logger.getRootLogger().isDebugEnabled() )
					Logger.getLogger(LimitedMemoryBFGS.class).debug("after linesearch: direction.2norm: " + twoNorm(direction));
				double newValue = this.getValue();

				//Test for terminations
				if ( 2.0 * abs(newValue-value) <= this.tolerance * (abs(newValue) + abs(value) + this.eps) ) {
					Logger.getLogger(LimitedMemoryBFGS.class).debug("Exiting L-BFGS on termination #1:\nvalue difference below tolerance (oldValue: " + value + " newValue: " + newValue);
					this.converged = true;
					return true;
				}

				double gg = twoNorm(g);
				if ( gg < this.gradientTolerance ) {
					Logger.getLogger(LimitedMemoryBFGS.class).debug("Exiting L-BFGS on termination #2:\ngradient=" + gg + " < " + this.gradientTolerance);
					this.converged = true;
					return true;
				}

				if ( gg == 0.0 ) {
					Logger.getLogger(LimitedMemoryBFGS.class).debug("Exiting L-BFGS on termination #3:\ngradient==0.0");
					this.converged = true;
					return true;
				}

				Logger.getLogger(LimitedMemoryBFGS.class).debug("Gradient = " + gg);
				iterations++;
				if ( iterations > this.MAX_ITER ) {
					Logger.getLogger(LimitedMemoryBFGS.class).error("Too many iterations in L-BFGS.java. Continuing with current parameters.");
					this.converged = true;
					return true;
				}
			}

			return false;
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
		 * @param toadd double value to push onto queue
		 */
		private void push(LinkedList l, double toadd) {
			if ( l.size() == this.M ) {//pop old double and add new
				l.removeFirst();
				l.addLast(toadd);
			}
			else
				l.addLast(toadd);
		}

		/**
		 * Calculates the new gradient values.
		 * @param buffer store the new gradient values
		 */
		public void getValueGradient(double[] buffer) {
			double[] df = new double[this.parameters.length];
			double[] tmp = new double[this.parameters.length];

			for ( int l = 0; l < numClasses; l++ )
				for ( int k = 0; k < numTopics; k++ )
					df[l * numTopics + k] = -penalty * this.parameters[l * numTopics + k];

			for ( int d = 0; d < this.ss.numDocs; d++ ) {
				for ( int k = 0; k < numTopics; k++ )
					df[this.ss.labels[d] * numTopics + k] += this.ss.zBarM[d][k];

				double t = 0.0;//in log space, 1+exp()+exp()+....
				System.arraycopy(df, 0, tmp, 0, df.length);
				Arrays.fill(df, 0);

				for ( int l = 0; l < numClasses; l++ ) {
					double[] eta_aux = new double[numTopics];
					double a1 = 0.0;//\eta_k^T * \bar{\phi}_d
					double a2 = 0.0;//1 + 0.5*\eta_k^T * Var(z_bar)\eta_k

					for ( int k = 0; k < numTopics; k++ ) {
						a1 += this.parameters[l * numTopics + k] * this.ss.zBarM[d][k];

						for ( int j = 0; j < numTopics; j++ ) {
							a2 += this.parameters[l * numTopics + k] * ss.zBarVar[d][mapIdx(k, j, numTopics)] * this.parameters[l * numTopics + j];
							eta_aux[k] += this.ss.zBarVar[d][mapIdx(k, j, numTopics)] * this.parameters[l * numTopics + j];
						}
					}

					a2 = 1.0 + 0.5 * a2;
					t = logSum(t, a1 + log(a2));

					for ( int k = 0; k < numTopics; k++ )
						df[l * numTopics + k] -= exp(a1) * (this.ss.zBarM[d][k] * a2 + eta_aux[k]);
				}

				for ( int i = 0; i < df.length; i++ )
					df[i] = df[i] * exp(-t) + tmp[i];
			}

			System.arraycopy(df, 0, buffer, 0, df.length);
		}

		/**
		 * Calculates the gradient value.
		 * @return gradient value
		 */
		public double getValue() {
			double fRegularization = 0.0;
			for ( int l = 0; l < numClasses; l++ )
				for ( int k = 0; k < numTopics; k++ )
					fRegularization -= pow(this.parameters[l * numTopics + k], 2) * penalty / 2.0;

			double f = 0.0;//log likelihood
			for ( int d = 0; d < this.ss.numDocs; d++ ) {
				for ( int k = 0; k < numTopics; k++ )
					if ( this.ss.labels[d] < numClasses )
						f += this.parameters[this.ss.labels[d] * numTopics + k] * this.ss.zBarM[d][k];

				double t = 0.0;//in log space, 1+exp()+exp()...
				for ( int l = 0; l < numClasses; l++ ) {
					double a1 = 0.0;//\eta_k^T * \bar{\phi}_d
					double a2 = 0.0;//1 + 0.5 * \eta_k^T * Var(z_bar)\eta_k

					for ( int k = 0; k < numTopics; k++ ) {
						a1 += this.parameters[l * numTopics + k] * ss.zBarM[d][k];
						for ( int j = 0; j < numTopics; j++ )
							a2 += this.parameters[l * numTopics + k] * this.ss.zBarVar[d][mapIdx(k, j, numTopics)] * this.parameters[l * numTopics + j];
					}

					a2 = 1.0 + 0.5 * a2;
					t = logSum(t, a1 + log(a2));
				}
				  f -= t;
	    }

			return (f + fRegularization);
		}

		/**
		 * Line search and backtracking.
		 * @param line line
		 * @param initialStep initial step
		 * @return 0.0 if could not step in direction
		 */
		public double optimizeLine(double[] line, double initialStep) {
			int maxLineIterations = 200, iteration;
			double slope, temp, test, alamin, alam, alam2, tmplam, rhs1, rhs2, a, b, disc, oldAlam, f, fold, f2;
			double stpmax = 100;
			double relTolx = 1e-7;
			double absTolx = 1e-4;//tolerance on absolute value difference
			double ALF = 1e-4;
			double[] g, x, oldx;

			g = new double[this.parameters.length];//gradient
			x = new double[this.parameters.length];//parameters
			oldx = new double[this.parameters.length];

			System.arraycopy(this.parameters, 0, x, 0, this.parameters.length);
			System.arraycopy(x, 0, oldx, 0, x.length);

			this.getValueGradient(g);
			alam2 = tmplam = 0.0;
			f2 = fold = this.getValue();

			Logger.getLogger(LimitedMemoryBFGS.class).debug("Entering backtrack.");
			if ( Logger.getRootLogger().isDebugEnabled() )
				Logger.getLogger(LimitedMemoryBFGS.class).debug("Entering backtrack line search, value=" + fold +
								"\ndirection.oneNorm: " + oneNorm(line) +
								"\ndirection.infNorm: " + infNorm(line));

			double sum = twoNorm(line);
			if ( sum > stpmax ) {
				Logger.getLogger(LimitedMemoryBFGS.class).warn("Attempted step too big. scaling: sum=" + sum + ", stpmax=" + stpmax);
				timesEquals(line, stpmax / sum);
			}

			slope = dotProduct(g, line);
			Logger.getLogger(LimitedMemoryBFGS.class).debug("slope=" + slope);

			if ( slope < 0 )
				throw new OptimizationException("Slope = " + slope + " is negative");
			if ( slope == 0 )
				throw new OptimizationException("Slope = " + slope + " is zero");

			//find maximum lambda
			//converge when (delta x) / x < REL_TOLX for all coordinates.
			//the largest step size that triggers this threshold is
			//precomputed and saved in alamin
			test = 0.0;
			for ( int i = 0; i < oldx.length; i++ ) {
				temp = abs(line[i]) / max(abs(oldx[i]), 1.0);
				if ( temp > test )
					test = temp;
			}

			alamin = relTolx / test;
			alam = 1.0;
			oldAlam = 0.0;
			for ( iteration = 0; iteration < maxLineIterations; iteration++ ) {//look for step size in direction given by "line"
				//x = oldParameters + alam * line
				//initially, alam = 1.0, i.e. take full Newton step
				Logger.getLogger(LimitedMemoryBFGS.class).debug("BackTrack loop iteration " + iteration +
								"\nalam=" + alam + ". oldAlam=" + oldAlam);
				if ( Logger.getRootLogger().isDebugEnabled() )
					Logger.getLogger(LimitedMemoryBFGS.class).debug("before step, x.1norm: " + oneNorm(x) +
									"\nalam: " + alam + ", oldAlam: " + oldAlam);

				plusEquals(x, line, alam - oldAlam);//step
				if ( Logger.getRootLogger().isDebugEnabled() )
					Logger.getLogger(LimitedMemoryBFGS.class).debug("after step, x.1norm: " + oneNorm(x));

				//check for convergence
				//convergence on delta x
				if ( (alam < alamin) || smallAbsDiff(oldx, x, absTolx) ) {
					System.arraycopy(oldx, 0, this.parameters, 0, oldx.length);
					f = this.getValue();
					Logger.getLogger(LimitedMemoryBFGS.class).debug("Exiting backtrack: Jump too small (alamin=" + alamin + ").\nExiting and using xold. Value=" + f);
					return 0.0;
				}

				System.arraycopy(x, 0, this.parameters, 0, x.length);
				oldAlam = alam;
				f = this.getValue();
				Logger.getLogger(LimitedMemoryBFGS.class).debug("value=" + f);

				//sufficient function increase (Wolf condition)
				if ( f >= (fold + ALF * alam * slope) ) {
					Logger.getLogger(LimitedMemoryBFGS.class).debug("Exiting backtrack: value=" + f);

					if ( f < fold )
						throw new IllegalStateException("Function did not increase: f = " + f + " < " + fold + " = fold");
					return alam;
				}
				else if ( Double.isInfinite(f) || Double.isInfinite(f2) ) {
					Logger.getLogger(LimitedMemoryBFGS.class).debug("Value is infinite after jump " + oldAlam + ". f=" + f + ", f2=" + f2 + ". Scaling back step size...");
					tmplam = 0.2 * alam;
					if ( alam < alamin ) {//convergence on delta x
						System.arraycopy(oldx, 0, this.parameters, 0, oldx.length);
						f = this.getValue();
						Logger.getLogger(LimitedMemoryBFGS.class).debug("Exiting backtrack: Jump too small. Exiting and using xold. Value=" + f);
						return 0.0;
					}
				}
				else {//backtrack
					if ( alam == 1.0 )//first time through
						tmplam = -slope / (2.0 * (f - fold - slope));
					else {
						rhs1 = f - fold - alam * slope;
						rhs2 = f2 - fold - alam2 * slope;

						a = (rhs1 / (alam * alam) - rhs2 / (alam2 * alam2)) / (alam - alam2);
						b = (-alam2 * rhs1 / (alam * alam) + alam * rhs2 / (alam2 * alam2)) / (alam - alam2);

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

				Logger.getLogger(LimitedMemoryBFGS.class).debug("tmplam:" + tmplam);
				alam = max(tmplam, 0.1 * alam);//lambda >= 0.1 * lambda_1
			}

			if ( iteration >= maxLineIterations )
				throw new IllegalStateException("Too many iterations.");

			return 0.0; 
		}
	}

	public static void main(String[] args) throws Exception {
		FastVector classes = new FastVector();
		classes.addElement("A");
		classes.addElement("B");
		classes.addElement("C");
		classes.addElement("D");
		
		FastVector atts = new FastVector();
		atts.addElement(new Attribute("a"));
		atts.addElement(new Attribute("b"));
		atts.addElement(new Attribute("c"));
		atts.addElement(new Attribute("d"));
		atts.addElement(new Attribute("e"));
		atts.addElement(new Attribute("f"));
		atts.addElement(new Attribute("g"));
		atts.addElement(new Attribute("h"));
		atts.addElement(new Attribute("i"));
		atts.addElement(new Attribute("class", classes));

		Instances data = new Instances("test", atts, 0);
		data.setClassIndex(data.numAttributes() - 1);

		Instance instance = new Instance(atts.size());
		instance.setValue((Attribute)atts.elementAt(0), 1);
		instance.setValue((Attribute)atts.elementAt(1), 2);
		instance.setValue((Attribute)atts.elementAt(2), 2);
		instance.setValue((Attribute)atts.elementAt(3), 3);
		instance.setValue((Attribute)atts.elementAt(4), 0);
		instance.setValue((Attribute)atts.elementAt(5), 1);
		instance.setValue((Attribute)atts.elementAt(6), 0);
		instance.setValue((Attribute)atts.elementAt(7), 0);
		instance.setValue((Attribute)atts.elementAt(8), 2);
		instance.setValue((Attribute)atts.elementAt(9), "A");
		data.add(instance);

		instance = new Instance(atts.size());
		instance.setValue((Attribute)atts.elementAt(0), 2);
		instance.setValue((Attribute)atts.elementAt(1), 3);
		instance.setValue((Attribute)atts.elementAt(2), 2);
		instance.setValue((Attribute)atts.elementAt(3), 1);
		instance.setValue((Attribute)atts.elementAt(4), 0);
		instance.setValue((Attribute)atts.elementAt(5), 0);
		instance.setValue((Attribute)atts.elementAt(6), 1);
		instance.setValue((Attribute)atts.elementAt(7), 0);
		instance.setValue((Attribute)atts.elementAt(8), 3);
		instance.setValue((Attribute)atts.elementAt(9), "A");
		data.add(instance);

		instance = new Instance(atts.size());
		instance.setValue((Attribute)atts.elementAt(0), 5);
		instance.setValue((Attribute)atts.elementAt(1), 5);
		instance.setValue((Attribute)atts.elementAt(2), 5);
		instance.setValue((Attribute)atts.elementAt(3), 1);
		instance.setValue((Attribute)atts.elementAt(4), 1);
		instance.setValue((Attribute)atts.elementAt(5), 1);
		instance.setValue((Attribute)atts.elementAt(6), 1);
		instance.setValue((Attribute)atts.elementAt(7), 1);
		instance.setValue((Attribute)atts.elementAt(8), 6);
		instance.setValue((Attribute)atts.elementAt(9), "A");
		data.add(instance);

		instance = new Instance(atts.size());
		instance.setValue((Attribute)atts.elementAt(0), 1);
		instance.setValue((Attribute)atts.elementAt(1), 0);
		instance.setValue((Attribute)atts.elementAt(2), 0);
		instance.setValue((Attribute)atts.elementAt(3), 0);
		instance.setValue((Attribute)atts.elementAt(4), 2);
		instance.setValue((Attribute)atts.elementAt(5), 1);
		instance.setValue((Attribute)atts.elementAt(6), 2);
		instance.setValue((Attribute)atts.elementAt(7), 3);
		instance.setValue((Attribute)atts.elementAt(8), 0);
		instance.setValue((Attribute)atts.elementAt(9), "B");
		data.add(instance);

		instance = new Instance(atts.size());
		instance.setValue((Attribute)atts.elementAt(0), 0);
		instance.setValue((Attribute)atts.elementAt(1), 0);
		instance.setValue((Attribute)atts.elementAt(2), 1);
		instance.setValue((Attribute)atts.elementAt(3), 0);
		instance.setValue((Attribute)atts.elementAt(4), 3);
		instance.setValue((Attribute)atts.elementAt(5), 1);
		instance.setValue((Attribute)atts.elementAt(6), 3);
		instance.setValue((Attribute)atts.elementAt(7), 2);
		instance.setValue((Attribute)atts.elementAt(8), 0);
		instance.setValue((Attribute)atts.elementAt(9), "B");
		data.add(instance);

		instance = new Instance(atts.size());
		instance.setValue((Attribute)atts.elementAt(0), 3);
		instance.setValue((Attribute)atts.elementAt(1), 2);
		instance.setValue((Attribute)atts.elementAt(2), 1);
		instance.setValue((Attribute)atts.elementAt(3), 1);
		instance.setValue((Attribute)atts.elementAt(4), 1);
		instance.setValue((Attribute)atts.elementAt(5), 2);
		instance.setValue((Attribute)atts.elementAt(6), 3);
		instance.setValue((Attribute)atts.elementAt(7), 2);
		instance.setValue((Attribute)atts.elementAt(8), 0);
		instance.setValue((Attribute)atts.elementAt(9), "C");
		data.add(instance);

		instance = new Instance(atts.size());
		instance.setValue((Attribute)atts.elementAt(0), 4);
		instance.setValue((Attribute)atts.elementAt(1), 2);
		instance.setValue((Attribute)atts.elementAt(2), 0);
		instance.setValue((Attribute)atts.elementAt(3), 0);
		instance.setValue((Attribute)atts.elementAt(4), 1);
		instance.setValue((Attribute)atts.elementAt(5), 3);
		instance.setValue((Attribute)atts.elementAt(6), 3);
		instance.setValue((Attribute)atts.elementAt(7), 5);
		instance.setValue((Attribute)atts.elementAt(8), 0);
		instance.setValue((Attribute)atts.elementAt(9), "C");
		data.add(instance);

		instance = new Instance(atts.size());
		instance.setValue((Attribute)atts.elementAt(0), 5);
		instance.setValue((Attribute)atts.elementAt(1), 3);
		instance.setValue((Attribute)atts.elementAt(2), 0);
		instance.setValue((Attribute)atts.elementAt(3), 0);
		instance.setValue((Attribute)atts.elementAt(4), 2);
		instance.setValue((Attribute)atts.elementAt(5), 4);
		instance.setValue((Attribute)atts.elementAt(6), 4);
		instance.setValue((Attribute)atts.elementAt(7), 6);
		instance.setValue((Attribute)atts.elementAt(8), 0);
		instance.setValue((Attribute)atts.elementAt(9), "C");
		data.add(instance);

		instance = new Instance(atts.size());
		instance.setValue((Attribute)atts.elementAt(0), 1);
		instance.setValue((Attribute)atts.elementAt(1), 1);
		instance.setValue((Attribute)atts.elementAt(2), 3);
		instance.setValue((Attribute)atts.elementAt(3), 2);
		instance.setValue((Attribute)atts.elementAt(4), 2);
		instance.setValue((Attribute)atts.elementAt(5), 4);
		instance.setValue((Attribute)atts.elementAt(6), 1);
		instance.setValue((Attribute)atts.elementAt(7), 2);
		instance.setValue((Attribute)atts.elementAt(8), 0);
		instance.setValue((Attribute)atts.elementAt(9), "D");
		data.add(instance);

		instance = new Instance(atts.size());
		instance.setValue((Attribute)atts.elementAt(0), 0);
		instance.setValue((Attribute)atts.elementAt(1), 2);
		instance.setValue((Attribute)atts.elementAt(2), 3);
		instance.setValue((Attribute)atts.elementAt(3), 2);
		instance.setValue((Attribute)atts.elementAt(4), 2);
		instance.setValue((Attribute)atts.elementAt(5), 5);
		instance.setValue((Attribute)atts.elementAt(6), 1);
		instance.setValue((Attribute)atts.elementAt(7), 1);
		instance.setValue((Attribute)atts.elementAt(8), 0);
		instance.setValue((Attribute)atts.elementAt(9), "D");
		data.add(instance);

		instance = new Instance(atts.size());
		instance.setValue((Attribute)atts.elementAt(0), 0);
		instance.setValue((Attribute)atts.elementAt(1), 0);
		instance.setValue((Attribute)atts.elementAt(2), 0);
		instance.setValue((Attribute)atts.elementAt(3), 0);
		instance.setValue((Attribute)atts.elementAt(4), 0);
		instance.setValue((Attribute)atts.elementAt(5), 0);
		instance.setValue((Attribute)atts.elementAt(6), 0);
		instance.setValue((Attribute)atts.elementAt(7), 0);
		instance.setValue((Attribute)atts.elementAt(8), 0);
		instance.setValue((Attribute)atts.elementAt(9), "D");
		data.add(instance);

		System.out.println("data");
		System.out.println(data);
		System.out.println();

		SLDA slda = new SLDA();
		slda.setOptions(new String[]{"-T", "3", "-mstep-iter", "10"});
		long start = System.currentTimeMillis();
		slda.buildClassifier(data);
		long time = System.currentTimeMillis() - start;
		System.out.println("#####################################################################");

		System.out.println("classifing instances:");

		int num = 0;
		Enumeration e = data.enumerateInstances();
		while ( e.hasMoreElements() ) {
			Instance inst = (Instance)e.nextElement();
			System.out.println(inst);
			System.out.println("class value: " + inst.classValue());

			double label = slda.classifyInstance(inst);
			System.out.println("predicted label: " + label);
			if ( label == inst.classValue() )
				num++;

			double[] dis = slda.distributionForInstance(inst);
			System.out.println("distribution: ");
			for ( int i = 0; i < dis.length; i++ )
				System.out.print(i + " = " + dis[i] + ", ");
			System.out.println("\n#####################################################################");
		}
		System.out.println("predicted right: " + num + " of " + data.numInstances());
		System.out.println("time: " + time);
		System.out.println("\n#####################################################################");

		System.out.println("top words:");
		String[][] topWords = slda.getTopWords();
		for ( int i = 0; i < topWords.length; i++ ) {
			System.out.print("class " + topWords[i][0] + ": ");
			for ( int j = 1; j < topWords[i].length; j++ )
				System.out.print(topWords[i][j] + " ");
			System.out.println();
		}
	}
}
