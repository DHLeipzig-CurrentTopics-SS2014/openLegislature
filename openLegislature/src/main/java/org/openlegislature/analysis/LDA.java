package org.openlegislature.analysis;

import cc.mallet.pipe.CharSequenceArray2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openlegislature.io.FileReader;
import org.openlegislature.nlp.tokenization.Tokenizer;
import org.openlegislature.util.Helpers;
import org.openlegislature.util.Logger;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class LDA {
	private static final int NUM_TOPICS = 100;

	public static void main(String[] args) throws IOException {
		String[] xmls = getXMLFiles();

		InstanceList instances = createInstanceList(xmls);
		instances.save(new File(Helpers.getUserDir() + "/data/speeches"));

		analyse(instances);
	}

	private static String[] getXMLFiles() {
		Set<String> files = new LinkedHashSet<>();
		for ( File file : new File(Helpers.getUserDir() + "/data/bundestag/17/").listFiles() ) {
			if ( file.isDirectory() ) {
				for ( File f : file.listFiles() )
					if  ( f.getAbsolutePath().endsWith(".xml") )
						files.add(f.getAbsolutePath());
			}
			else
				if  ( file.getAbsolutePath().endsWith(".xml") )
					files.add(file.getAbsolutePath());
		}

		String[] xmls = files.toArray(new String[files.size()]);
		Arrays.sort(xmls);
		return xmls;
	}

	private static InstanceList createInstanceList(String[] xmls) throws IOException {
		List<Pipe> pipes = new ArrayList<>();
		pipes.add(new CharSequenceArray2TokenSequence());
		pipes.add(new TokenSequenceRemoveStopwords(false).addStopWords(new File(Helpers.getUserDir() + "/data/nlp/stopwordlist_german")));
		pipes.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipes));

		int i = 1;
		for ( String xml : xmls ) {
			Logger.getInstance().info(LDA.class, "Loading file: " + xml.substring(xml.lastIndexOf("/") + 1, xml.length()));
			List<Instance> speeches = createInstancesForSpeeches(xml);
			for ( Instance instance : speeches )
				instances.addThruPipe(instance);

			if ( i > 10 )
				break;
			i++;
		}

		return instances;
	}

	private static List<Instance> createInstancesForSpeeches(String xml) throws IOException {
		List<Instance> instances = new LinkedList<>();
		String text = FileReader.read(xml);

		Matcher matches = Pattern.compile("<speech>(.+?)</speech>", Pattern.DOTALL | Pattern.MULTILINE).matcher(text);
		while ( matches.find() ) {
			Matcher m = Pattern.compile("<speaker>.+?<name>(.+?)</name>.+?</speaker>", Pattern.DOTALL | Pattern.MULTILINE).matcher(matches.group(1));
			String speaker = (m.find() ? m.group(1) : "NO_SPEAKER");
			String speech = matches.group(1).replaceAll("(?s)<[^>]+>[^<]+?</[^>]+>", " ").replaceAll("(?s)<[^>]+>[^<]+?</[^>]+>", " ").replaceAll("\\s+\n\\s+", " ").replaceAll("\\s\\s+", " ").trim();
			if ( speech.contains(">") || speech.contains("<") ) {
				System.out.println(matches.group(1));
				System.exit(1);
			}
			String[] tokens = Tokenizer.tokenize(speech);

			instances.add(new Instance(tokens, speaker, xml, speech));
		}

		return instances;
	}

	private static void analyse(InstanceList instances) throws IOException {
		Logger.getInstance().info(LDA.class, "Starting LDA.");
		ParallelTopicModel model = new ParallelTopicModel(LDA.NUM_TOPICS, 1.0, 0.01);
		model.addInstances(instances);
		model.setNumThreads(4);
		model.setNumIterations(50);
		model.estimate();

		int i = 0;
		for ( Object[] topic : model.getTopWords(20) ) {
			System.out.println("topic: " + i++);
			for ( Object word : topic ) {
				System.out.println(word);
			}
			System.out.println("##################################################");
		}
	}

	/*public static void main(String[] args) throws Exception {

        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));

        Reader fileReader = new InputStreamReader(new FileInputStream(new File(args[0])), "UTF-8");
        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                                               3, 2, 1)); // data, label, name fields

        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        int numTopics = 100;
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(50);
        model.estimate();

        // Show the words and topics in the first instance

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();
        
        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        LabelSequence topics = model.getData().get(0).topicSequence;
        
        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        for (int position = 0; position < tokens.getLength(); position++) {
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
        }
        System.out.println(out);
        
        // Estimate the topic distribution of the first instance, 
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        
        // Show top 5 words in topics with proportions for the first document
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            
            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < 5) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                rank++;
            }
            System.out.println(out);
        }
        
        // Create a new instance with high probability of topic 0
        StringBuilder topicZeroText = new StringBuilder();
        Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

        int rank = 0;
        while (iterator.hasNext() && rank < 5) {
            IDSorter idCountPair = iterator.next();
            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
            rank++;
        }

        // Create a new instance named "test instance" with empty target and source fields.
        InstanceList testing = new InstanceList(instances.getPipe());
        testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));

        TopicInferencer inferencer = model.getInferencer();
        double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
        System.out.println("0\t" + testProbabilities[0]);
    }

}*/
}