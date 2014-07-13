package weka.classifiers.topicmodelling;

import weka.classifiers.Classifier;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public abstract class TopicModel extends Classifier {
	/**
	 * Returns the top words for each topic.
	 * @return 
	 */
	public abstract String[][] getTopWords();
}