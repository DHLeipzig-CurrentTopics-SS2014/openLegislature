package org.openlegislature.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
	private final ExecutorService executorService;
	private final List<String> resultFiles = new LinkedList<>();
	private final List<Object[]> speeches = new LinkedList<>();
	private final FastVector classes = new FastVector();
	private final StopwordFilter stopwordFilter;
	private String[] tokens;
	private FastVector attrs;
	private Instances instances;
	private String inputFolder;
	private String outputFolder;
	private int finishedThreads = 0;
	private int sldas = 0;
	private int finishedSLDAs = 0;

	private SLDA(int threads) throws IOException {
		this.executorService = Executors.newFixedThreadPool(threads);
		this.stopwordFilter = new StopwordFilter(Helpers.getUserDir() + "/data/nlp/stopwordlist_german");
	}

	private synchronized FastVector getClasses() {
		return this.classes;
	}

	private String[] getTokens() {
		return this.tokens;
	}

	private synchronized void incrementFinishedThreads() {
		this.finishedThreads++;
	}

	private synchronized void incrmentFinishedSLDAs() {
		this.finishedSLDAs++;
	}

	private synchronized FastVector getAttrs() {
		return this.attrs;
	}

	private synchronized Instances getInstances() {
		return this.instances;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		SLDA slda = new SLDA(7);
		slda.inputFolder = Helpers.getUserDir() + "/data/bundestag/18/";
		slda.outputFolder = Helpers.getUserDir() + "/data/slda/";
		if ( !new File(slda.outputFolder).exists() )
			new File(slda.outputFolder).mkdirs();

		String[] xmls = slda.getXMLFiles();

		String first = xmls[0].substring(xmls[0].lastIndexOf("/") + 1, xmls[0].indexOf(".", xmls[0].lastIndexOf("/")));
		String last = xmls[xmls.length - 1].substring(xmls[xmls.length - 1].lastIndexOf("/") + 1, xmls[xmls.length - 1].indexOf(".", xmls[xmls.length - 1].lastIndexOf("/")));

		for ( String xml : xmls ) {
			String name = slda.createSpeakerARFF(xml);
			slda.clear();
			slda.analyse(name);
		}

		while ( slda.sldas != slda.finishedSLDAs )
			synchronized ( slda ) {
				Logger.getInstance().info(SLDA.class, "Finished SLDA " + slda.finishedSLDAs + " of " + slda.sldas + ".");
				slda.wait();
			}
		Logger.getInstance().info(SLDA.class, "Finished all SLDAs.");

		slda.sldas = 0;
		slda.finishedSLDAs = 0;
		slda.clear();

		String file = slda.createResultARFF(first, last);
		slda.analyse(file);

		while ( slda.sldas != slda.finishedSLDAs )
			synchronized ( slda ) {
				Logger.getInstance().info(SLDA.class, "Finished SLDA " + slda.finishedSLDAs + " of " + slda.sldas + ".");
				slda.wait();
			}
		Logger.getInstance().info(SLDA.class, "Finished all SLDAs.");
		slda.executorService.shutdown();
	}

	private String[] getXMLFiles() {
		Set<String> files = new LinkedHashSet<>();
		for ( File file : new File(this.inputFolder).listFiles() ) {
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

	private void clear() {
		this.attrs = null;
		this.classes.removeAllElements();
		this.finishedThreads = 0;
		this.instances = null;
		this.speeches.clear();
		this.tokens = null;
	}

	private String createSpeakerARFF(String xml) throws InterruptedException, IOException {
		String name = "speeches_speaker_" + xml.substring(xml.lastIndexOf("/") + 1, xml.indexOf(".", xml.lastIndexOf("/")));

		if ( !new File(this.outputFolder + name + ".arff").exists() ) {
			this.loadXMLs(xml);
			return this.createARFF(name);
		}
		else
			return this.outputFolder + name + ".arff";
	}

	private String createResultARFF(String first, String last) throws InterruptedException, IOException {
		String name = "speakers_" + first + "_" + last;

		if ( !new File(this.outputFolder + name + ".arff").exists() ) {
			this.loadResultFiles();
			return this.createARFF(name);
		}
		else
			return this.outputFolder + name + ".arff";
	}

	private void loadXMLs(String xml) throws IOException {
		Logger.getInstance().info(SLDA.class, "Loading file: " + xml.substring(xml.lastIndexOf("/") + 1));

		String text = FileReader.read(xml);

		Matcher matches = Pattern.compile("<speech>(.+?)</speech>", Pattern.DOTALL | Pattern.MULTILINE).matcher(text);
		while ( matches.find() ) {
			Matcher m = Pattern.compile("<speaker>.+?<name>(.+?)</name>.+?</speaker>", Pattern.DOTALL | Pattern.MULTILINE).matcher(matches.group(1));
			String speaker = (m.find() ? m.group(1) : null);
			String speech = matches.group(1).replaceAll("(?s)<[^>]+>[^<]+?</[^>]+>", " ").replaceAll("(?s)<[^>]+>[^<]+?</[^>]+>", " ").replaceAll("\\s+\n\\s+", " ").replaceAll("\\s\\s+", " ").trim();

			String[] tokens = Tokenizer.tokenize(speech.toLowerCase());
			tokens = this.stopwordFilter.filter(tokens);

			if ( speaker == null || tokens.length == 0 )
				continue;

			Map<String, Integer> counts = new LinkedHashMap<>();
			for ( String token : tokens )
				counts.put(token, (counts.containsKey(token) ? counts.get(token) + 1 : 1));

			this.speeches.add(new Object[]{speaker, counts});
			if ( !this.classes.contains(speaker) )
				this.classes.addElement(speaker);
		}

		Logger.getInstance().info(SLDA.class, "speeches: " + this.speeches.size());
		Logger.getInstance().info(SLDA.class, "classes: " + this.classes.size());
	}

	private void loadResultFiles() throws InterruptedException {
		int threads = 0;
		this.finishedThreads = 0;
		List<ResultLoader> loaders = new LinkedList<>();
		for ( String resultFile : this.resultFiles ) {
			ResultLoader loader = new ResultLoader(this, resultFile);
			loaders.add(loader);
			this.executorService.execute(loader);
			threads++;
		}

		while ( this.finishedThreads != threads )
			synchronized ( this ) {
				Logger.getInstance().info(SLDA.class, "Finished thread " + this.finishedThreads + " of " + threads + ".");
				this.wait();
			}
		Logger.getInstance().info(SLDA.class, "Finished all threads.");

		for ( ResultLoader loader : loaders ) {
			this.speeches.addAll(loader.getResults());
			for ( String clazz : loader.getClasses() )
				if ( !this.classes.contains(clazz) )
					this.classes.addElement(clazz);
		}

		Logger.getInstance().info(SLDA.class, "speeches: " + this.speeches.size());
		Logger.getInstance().info(SLDA.class, "classes: " + this.classes.size());
	}

	private String createARFF(String name) throws InterruptedException, IOException {
		Logger.getInstance().info(SLDA.class, "Start creating ARFF.");

		Set<String> tmp = new LinkedHashSet<>();
		for ( Object[] s : this.speeches )
			tmp.addAll(((Map<String, Integer>)s[1]).keySet());
		this.tokens = tmp.toArray(new String[tmp.size()]);
		tmp = null;
		Arrays.sort(this.tokens);
		Logger.getInstance().info(SLDA.class, "tokens: " + this.tokens.length);

		this.attrs = new FastVector(this.tokens.length + 1);
		this.attrs.addElement(new Attribute("class_attribute", this.classes));
		for ( String token : this.tokens )
			this.attrs.addElement(new Attribute(token));

		this.instances = new Instances("speeches", this.attrs, this.speeches.size());
		this.instances.setClassIndex(0);

		int threads = 0;
		this.finishedThreads = 0;
		for ( Object[] speech : this.speeches ) {
			executorService.execute(new InstanceCreator(this, speech));
			threads++;
		}

		while ( this.finishedThreads != threads )
			synchronized ( this ) {
				Logger.getInstance().info(SLDA.class, "Finished thread " + this.finishedThreads + " of " + threads + ".");
				this.wait();
			}
		Logger.getInstance().info(SLDA.class, "Finished all threads.");

		Logger.getInstance().info(SLDARunnable.class, "Saved ARFF file: " + name + ".", "classes: " + this.instances.numClasses(), "attributes: " + this.instances.numAttributes(), "speeches: " + this.instances.numInstances());

		String file = this.outputFolder + name + ".arff";
		FileWriter.write(file, this.instances.toString(), "UTF-8");

		return file;
	}

	private void analyse(String file) {
		String name = file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf("."));
		String resultFile = this.outputFolder + "topwords_" + name + ".csv";

		if ( !new File(resultFile).exists() ) {
			this.executorService.execute(new SLDARunnable(this, file, resultFile));
			this.sldas++;
		}
		else
			this.resultFiles.add(resultFile);
	}

	class ResultLoader implements Runnable {
		private final SLDA parent;
		private final String resultFile;
		private final List<Object[]> results;
		private final Set<String> classes;

		public ResultLoader(SLDA parent, String resultFile) {
			this.parent = parent;
			this.resultFile = resultFile;
			this.results = new LinkedList<>();
			this.classes = new LinkedHashSet<>();
		}

		public List<Object[]> getResults() {
			return this.results;
		}

		public Set<String> getClasses() {
			return this.classes;
		}

		@Override
		public void run() {
			Logger.getInstance().info(ResultLoader.class, "Loading file: " + this.resultFile.substring(this.resultFile.lastIndexOf("/") + 1));

			List<String[]> csv = new LinkedList<>();
			try {
				FileReader.readCSV(this.resultFile, csv, ";");
			}
			catch ( IOException e ) {
				Logger.getInstance().error(ResultLoader.class, "Error while loading file: " + this.resultFile, e.toString());
				this.finnish();
				return;
			}

			for ( String[] result : csv ) {
				Map<String, Integer> counts = new LinkedHashMap<>();
				for ( int i = 1; i < result.length; i++ )
					counts.put(result[i], (counts.containsKey(result[i]) ? counts.get(result[i]) + 1 : 1));

				this.results.add(new Object[]{result[0], counts});
				this.classes.add(result[0]);
			}

			this.finnish();
		}

		private void finnish() {
			this.parent.incrementFinishedThreads();
			synchronized ( this.parent ) {
				this.parent.notify();
			}
		}
	}

	class InstanceCreator implements Runnable {
		private final SLDA parent;
		private final Object[] speech;

		public InstanceCreator(SLDA parent, Object[] speech) {
			this.parent = parent;
			this.speech = speech;
		}

		@Override
		public void run() {
			Instance instance = new Instance(this.parent.getAttrs().size());
			instance.setValue((Attribute)this.parent.getAttrs().elementAt(0), this.speech[0].toString());

			Map<String, Integer> counts = (Map<String, Integer>)this.speech[1];
			for ( int i = 0; i < this.parent.getTokens().length; i++ )
				instance.setValue((Attribute)this.parent.getAttrs().elementAt(i + 1), (counts.containsKey(this.parent.getTokens()[i]) ? counts.get(this.parent.getTokens()[i]) : 0));

			this.parent.getInstances().add(instance);
			this.parent.incrementFinishedThreads();
			synchronized ( this.parent ) {
				this.parent.notify();
			}
		}
	}

	class SLDARunnable implements Runnable {
		private final SLDA parent;
		private final String file;
		private final String resultFile;

		public SLDARunnable(SLDA parent, String file, String resultFile) {
			this.parent = parent;
			this.file = file;
			this.resultFile = resultFile;
		}

		@Override
		public void run() {
			Logger.getInstance().info(SLDA.class, "Starting SLDA.");

			Instances instances;
			try {
				instances = new Instances(new BufferedReader(new java.io.FileReader(new File(this.file))));
				instances.setClassIndex(0);
			}
			catch ( FileNotFoundException e ) {
				Logger.getInstance().error(SLDARunnable.class, "ARFF file not found: " + this.file, e.toString());
				this.finish();
				return;
			} catch ( IOException e ) {
				Logger.getInstance().error(SLDARunnable.class, "Error while loading ARFF file: " + this.file, e.toString());
				this.finish();
				return;
			}

			Logger.getInstance().info(SLDARunnable.class, "Starting SLDA for " + this.file.substring(this.file.lastIndexOf("/") + 1, this.file.lastIndexOf(".")), "classes: " + instances.numClasses(), "attributes: " + instances.numAttributes(), "speeches: " + instances.numInstances());

			weka.classifiers.topicmodelling.SLDA slda = new weka.classifiers.topicmodelling.SLDA();
			try {
				slda.setOptions(new String[]{"-T", "30", "-mstep-iter", "1000", "-var-iter", "200", "-em-iter", "500"});
				slda.buildClassifier(instances);
			}
			catch ( Exception e ) {
				Logger.getInstance().error(SLDARunnable.class, e.toString());
				this.finish();
				return;
			}

			String[][] topWords = slda.getTopWords();
			String out = "";
			for ( String[] words : topWords ) {
				out += "topic: " + words[0] + "\n";
				for ( int i = 1; i < words.length; i++ )
					out += words[i] + " ";
				out += "\n";
			}
			Logger.getInstance().info(SLDARunnable.class, "Top words of slda for " + this.file.substring(this.file.lastIndexOf("/") + 1, this.file.lastIndexOf(".")) + ": ", out);

			try {
				FileWriter.writeCSV(this.resultFile, topWords, ";", "UTF-8");
				this.parent.resultFiles.add(this.resultFile);
			}
			catch ( IOException e ) {
				Logger.getInstance().error(SLDARunnable.class, "Error while saving result file.", e.toString());
			}

			this.finish();
		}

		private void finish() {
			this.parent.incrmentFinishedSLDAs();
			synchronized ( this.parent ) {
				this.parent.notify();
			}
		}
	}
}