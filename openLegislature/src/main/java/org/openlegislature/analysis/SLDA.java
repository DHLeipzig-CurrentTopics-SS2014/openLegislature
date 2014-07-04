package org.openlegislature.analysis;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openlegislature.io.FileReader;
import org.openlegislature.io.FileWriter;
import org.openlegislature.nlp.stopwords.StopwordFilter;
import org.openlegislature.nlp.tokenization.Tokenizer;
import org.openlegislature.util.Helpers;
import org.openlegislature.util.Logger;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class SLDA {
	private final List<Object[]> speeches = new LinkedList<>();
	private final FastVector classes = new FastVector();
	private Set<String> allTokens = new LinkedHashSet<>();
	private StopwordFilter stopwordFilter;
	private int finishedThreads = 0;

	private SLDA() {}

	private synchronized List<Object[]> getSpeeches() {
		return this.speeches;
	}

	private synchronized FastVector getClasses() {
		return this.classes;
	}

	private synchronized Set<String> getAllTokens() {
		return this.allTokens;
	}

	private synchronized void incrementFinishedThreads() {
		this.finishedThreads++;
	}

	public static void main(String[] args) throws IOException, Exception {
		SLDA slda = new SLDA();
		String[] xmls = slda.getXMLFiles();

		Instances instances = slda.createARFF(xmls);
		FileWriter.write(Helpers.getUserDir() + "/data/speeches.arff", instances.toString(), "UTF-8");
		//Instances instances = new Instances(new BufferedReader(new java.io.FileReader(new File(Helpers.getUserDir() + "/data/speeches.arff"))));
		//instances.setClassIndex(instances.numAttributes() - 1);
		analyse(instances);
	}

	private String[] getXMLFiles() {
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

	private Instances createARFF(String[] xmls) throws IOException, InterruptedException {
		this.stopwordFilter = new StopwordFilter(Helpers.getUserDir() + "/data/nlp/stopwordlist_german");

		ExecutorService executorService = Executors.newFixedThreadPool(8);
		int threads = 0;
		for ( String xml : xmls ) {
			executorService.execute(new SpeechLoader(this, xml));
			threads++;
			if ( threads == 20 )
				break;
		}

		while ( this.finishedThreads != threads )
			synchronized ( this ) {
				Logger.getInstance().info(SLDA.class, "Finished thread " + this.finishedThreads + " of " + threads + ".");
				this.wait();
			}
		Logger.getInstance().info(SLDA.class, "Finished all threads.");

		Logger.getInstance().info(SLDA.class, "Start creating ARFF.");
		String[] tokens = this.allTokens.toArray(new String[this.allTokens.size()]);
		this.allTokens = null;
		Arrays.sort(tokens);

		FastVector attrs = new FastVector(tokens.length + 1);
		for ( String token : tokens )
			attrs.addElement(new Attribute(token));
		attrs.addElement(new Attribute("class_attribute", this.classes));

		Instances instances = new Instances("speeches", attrs, this.speeches.size());
		instances.setClassIndex(attrs.size() - 1);

		for ( Object[] speech : this.speeches ) {
			Instance instance = new Instance(attrs.size());

			Map<String, Integer> counts = (Map<String, Integer>)speech[1];

			for ( int i = 0; i < tokens.length; i++ ) {
				instance.setValue((Attribute)attrs.elementAt(i), (counts.containsKey(tokens[i]) ? counts.get(tokens[i]) : 0));
			}
			instance.setValue((Attribute)attrs.elementAt(tokens.length), speech[0].toString());
			instances.add(instance);
		}

		System.out.println("classes: " + instances.numClasses());
		System.out.println("attributes: " + instances.numAttributes());
		System.out.println("speeches: " + instances.numInstances());

		return instances;
	}

	private static void analyse(Instances instances) throws IOException, Exception {
		Logger.getInstance().info(SLDA.class, "Starting SLDA.");
		System.out.println("classes: " + instances.numClasses());
		System.out.println("attributes: " + instances.numAttributes());
		System.out.println("speeches: " + instances.numInstances());

		weka.classifiers.functions.SLDA slda = new weka.classifiers.functions.SLDA();
		slda.setOptions(new String[]{"-T", "30", "-mstep-iter", "1000", "-var-iter", "200", "-em-iter", "500"});
		slda.buildClassifier(instances);

		String[][] topWords = slda.getTopWords();
		for ( String[] words : topWords ) {
			System.out.println("topic: " + words[0]);
			for ( int i = 1; i < words.length; i++ ) {
				System.out.print(words[i] + " ");
			}
			System.out.println();
		}

		FileWriter.writeCSV(Helpers.getUserDir() + "/data/topwords.csv", topWords, ";", "UTF-8");
	}

	class SpeechLoader implements Runnable {
		private final SLDA parent;
		private final String xml;

		public SpeechLoader(SLDA parent, String xml) {
			this.parent = parent;
			this.xml = xml;
		}

		@Override
		public void run() {
			Logger.getInstance().info(SpeechLoader.class, "Loading file: " + this.xml.substring(this.xml.lastIndexOf("/") + 1, this.xml.length()));

			String text;
			try {
				text = FileReader.read(this.xml);
			}
			catch ( IOException e ) {
				Logger.getInstance().error(SpeechLoader.class, "Error while loading file: " + this.xml, e.toString());
				return;
			}

			Matcher matches = Pattern.compile("<speech>(.+?)</speech>", Pattern.DOTALL | Pattern.MULTILINE).matcher(text);
			while ( matches.find() ) {
				Matcher m = Pattern.compile("<speaker>.+?<name>(.+?)</name>.+?</speaker>", Pattern.DOTALL | Pattern.MULTILINE).matcher(matches.group(1));
				String speaker = (m.find() ? m.group(1) : "NO_SPEAKER");
				String speech = matches.group(1).replaceAll("(?s)<[^>]+>[^<]+?</[^>]+>", " ").replaceAll("(?s)<[^>]+>[^<]+?</[^>]+>", " ").replaceAll("\\s+\n\\s+", " ").replaceAll("\\s\\s+", " ").trim();

				String[] tokens = Tokenizer.tokenize(speech.toLowerCase());
				tokens = this.parent.stopwordFilter.filter(tokens);
				this.parent.getAllTokens().addAll(Arrays.asList(tokens));

				Map<String, Integer> counts = new LinkedHashMap<>();
				for ( String token : tokens )
					counts.put(token, (counts.containsKey(token) ? counts.get(token) + 1 : 1));
				
				this.parent.getSpeeches().add(new Object[]{speaker, counts});
				if ( !this.parent.getClasses().contains(speaker) )
					this.parent.getClasses().addElement(speaker);
			}

			this.parent.incrementFinishedThreads();
			synchronized ( this.parent ) {
				this.parent.notify();
			}
		}
	}
}