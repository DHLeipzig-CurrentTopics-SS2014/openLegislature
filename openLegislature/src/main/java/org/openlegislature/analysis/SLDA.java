package org.openlegislature.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
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
import weka.core.Utils;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class SLDA {
	public static final int SINGLE_ARFF_MODE = 0;
	public static final int MULTIPLE_ARFF_MODE = 1;
	private final ExecutorService executorService;
	private final StopwordFilter stopwordFilter;
	private final String sldaOptions;
	private String inputFolder;
	private String outputFolder;
	private int finishedThreads = 0;
	private int sldas = 0;
	private int finishedSLDAs = 0;

	private SLDA(int threads, String sldaOptions) throws IOException {
		this.executorService = Executors.newFixedThreadPool(threads);
		this.stopwordFilter = new StopwordFilter(Helpers.getUserDir() + "/data/nlp/stopwordlist_german");
		this.sldaOptions = sldaOptions;
	}

	private synchronized void incrementFinishedThreads() {
		this.finishedThreads++;
	}

	private synchronized void incrmentFinishedSLDAs() {
		this.finishedSLDAs++;
	}

	public static void main(String[] args) throws IOException {
		int threads = 1, mode = SLDA.SINGLE_ARFF_MODE;
		String sldaOptions = "", periode = "";

		Iterator<String> it = Arrays.asList(args).iterator();
		while ( it.hasNext() )
			switch ( it.next() ) {
				case "-t":
				case "-threads":
					threads = Integer.parseInt(it.next());
					break;
				case "-p":
				case "-periode":
					periode = it.next();
					break;
				case "-m":
				case "-mode":
					switch ( Integer.parseInt(it.next()) ) {
						case SLDA.SINGLE_ARFF_MODE:
							mode = SLDA.SINGLE_ARFF_MODE;
							break;
						case SLDA.MULTIPLE_ARFF_MODE:
							mode = SLDA.MULTIPLE_ARFF_MODE;
							break;
						default:
							System.out.println("Mode not recognized. Setting it to 0.");
							mode = SLDA.SINGLE_ARFF_MODE;
					}
					break;
				case "-slda":
					sldaOptions = it.next();
			}

		if ( periode.isEmpty() ) {
			System.out.println("Usage: SLDA [-t <number of threads>] -p <periode> -m <mode> [-slda <slda options>]");
			System.out.println("Default number of threads: 1");
			System.out.println("Mode:");
			System.out.println("\t0 - Single ARFF file (default)");
			System.out.println("\t1 - Multiple ARFF file");
			System.exit(1);
		}

		SLDA slda = new SLDA(threads, sldaOptions);
		slda.start(periode, mode);
	}

	public void start(String periode, int mode) {
		this.inputFolder = Helpers.getUserDir() + "/data/bundestag/" + periode + "/";
		this.outputFolder = Helpers.getUserDir() + "/data/slda/" + periode + "/";
		if ( !new File(this.outputFolder).exists() )
			new File(this.outputFolder).mkdirs();

		String[] xmls = this.getXMLFiles();
		try {
			if ( mode == SLDA.SINGLE_ARFF_MODE )
				this.runSingleARFF(xmls);
			else if ( mode == SLDA.MULTIPLE_ARFF_MODE )
				this.runMultipleARFF(xmls);
			else
				Logger.getInstance().error(SLDA.class, "Mode not recognized.");
		} catch ( IOException | InterruptedException e ) {
			Logger.getInstance().error(SLDA.class, "An error occured while performing slda classification.", e.toString());
		}

		this.executorService.shutdown();
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

	private void runSingleARFF(String[] xmls) throws InterruptedException, IOException {
		String file = this.createSpeakerARFF(xmls);
		this.analyse(file);

		while ( this.sldas != this.finishedSLDAs )
			synchronized ( this ) {
				Logger.getInstance().info(SLDA.class, "Finished SLDA " + this.finishedSLDAs + " of " + this.sldas + ".");
				this.wait();
			}
		Logger.getInstance().info(SLDA.class, "Finished all SLDAs.");
	}

	private void runMultipleARFF(String[] xmls) throws InterruptedException, IOException {
		String first = xmls[0].substring(xmls[0].lastIndexOf("/") + 1, xmls[0].indexOf(".", xmls[0].lastIndexOf("/")));
		String last = xmls[xmls.length - 1].substring(xmls[xmls.length - 1].lastIndexOf("/") + 1, xmls[xmls.length - 1].indexOf(".", xmls[xmls.length - 1].lastIndexOf("/")));

		List<String> resultFiles = new LinkedList<>();
		for ( String xml : xmls ) {
			String name = this.createSpeakerARFF(xml);
			resultFiles.add(this.analyse(name));
		}

		while ( this.sldas != this.finishedSLDAs )
			synchronized ( this ) {
				Logger.getInstance().info(SLDA.class, "Finished SLDA " + this.finishedSLDAs + " of " + this.sldas + ".");
				this.wait();
			}
		Logger.getInstance().info(SLDA.class, "Finished all SLDAs.");

		this.sldas = 0;
		this.finishedSLDAs = 0;
		String file = this.createResultARFF(resultFiles, first, last);
		this.analyse(file);

		while ( this.sldas != this.finishedSLDAs )
			synchronized ( this ) {
				Logger.getInstance().info(SLDA.class, "Finished SLDA " + this.finishedSLDAs + " of " + this.sldas + ".");
				this.wait();
			}
		Logger.getInstance().info(SLDA.class, "Finished all SLDAs.");
	}

	private String createSpeakerARFF(String xml) throws InterruptedException, IOException {
		String name = "speeches_speaker_" + xml.substring(xml.lastIndexOf("/") + 1, xml.indexOf(".", xml.lastIndexOf("/")));

		if ( !new File(this.outputFolder + name + ".arff").exists() ) {
			List<Object[]> speeches = new LinkedList<>();
			Set<String> speakers = new LinkedHashSet<>();

			this.loadXML(xml, speeches, speakers);
			return this.createARFF(name, speeches, speakers);
		}
		else
			return this.outputFolder + name + ".arff";
	}

	private String createSpeakerARFF(String[] xmls) throws InterruptedException, IOException {
		String first = xmls[0].substring(xmls[0].lastIndexOf("/") + 1, xmls[0].indexOf(".", xmls[0].lastIndexOf("/")));
		String last = xmls[xmls.length - 1].substring(xmls[xmls.length - 1].lastIndexOf("/") + 1, xmls[xmls.length - 1].indexOf(".", xmls[xmls.length - 1].lastIndexOf("/")));
		String name = "speeches_speaker_" + first + "_" + last;

		if ( !new File(this.outputFolder + name + ".arff").exists() ) {
			List<Object[]> speeches = new LinkedList<>();
			Set<String> speakers = new LinkedHashSet<>();

			int threads = 0;
			this.finishedThreads = 0;
			List<XMLLoader> loaders = new LinkedList<>();
			for ( String xml : xmls ) {
				XMLLoader loader = new XMLLoader(this, xml);
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

			for ( XMLLoader loader : loaders ) {
				speeches.addAll(loader.getSpeeches());
				speakers.addAll(loader.getSpeakers());
			}

			Logger.getInstance().info(SLDA.class, "name: " + name, "speeches: " + speeches.size(), "classes: " + speakers.size());

			return this.createARFF(name, speeches, speakers);
		}
		else
			return this.outputFolder + name + ".arff";
	}

	private String createResultARFF(List<String> resultFiles, String first, String last) throws InterruptedException, IOException {
		String name = "speakers_" + first + "_" + last;

		if ( !new File(this.outputFolder + name + ".arff").exists() ) {
			List<Object[]> topWords = new LinkedList<>();
			Set<String> speakers = new LinkedHashSet();

			this.loadResultFiles(resultFiles, topWords, speakers);
			return this.createARFF(name, topWords, speakers);
		}
		else
			return this.outputFolder + name + ".arff";
	}

	private void loadXML(String xml, List<Object[]> speeches, Set<String> speakers) throws IOException {
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

			speeches.add(new Object[]{speaker, counts});
			speakers.add(speaker);
		}

		Logger.getInstance().info(SLDA.class, "XML file loaded: " + xml.substring(xml.lastIndexOf("/") + 1), "speeches: " + speeches.size(), "speakers: " + speakers.size());
	}

	private void loadResultFiles(List<String> resultFiles, List<Object[]> topWords, Set<String> speakers) throws InterruptedException {
		int threads = 0;
		this.finishedThreads = 0;
		List<ResultLoader> loaders = new LinkedList<>();
		for ( String resultFile : resultFiles ) {
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
			topWords.addAll(loader.getResults());
			speakers.addAll(loader.getClasses());
		}

		Logger.getInstance().info(SLDA.class, "speeches: " + topWords.size(), "classes: " + speakers.size());
	}

	private String createARFF(String name, List<Object[]> data, Set<String> labels) throws InterruptedException, IOException {
		Logger.getInstance().info(SLDA.class, "Start creating ARFF: " + name);

		Set<String> tmp = new LinkedHashSet<>();
		for ( Object[] s : data )
			tmp.addAll(((Map<String, Integer>)s[1]).keySet());
		String[] words = tmp.toArray(new String[tmp.size()]);
		tmp = null;
		Arrays.sort(words);
		Logger.getInstance().info(SLDA.class, "words: " + words.length);

		FastVector classes = new FastVector();
		for ( String label : labels )
			classes.addElement(label);

		FastVector attrs = new FastVector(words.length + 1);
		attrs.addElement(new Attribute("class_attribute", classes));
		for ( String token : words )
			attrs.addElement(new Attribute(token));
		words = null;

		Instances instances = new Instances(name, attrs, data.size());
		instances.setClassIndex(0);

		int threads = 0;
		this.finishedThreads = 0;
		List<InstanceCreator> creators = new LinkedList<>();
		for ( Object[] speech : data ) {
			InstanceCreator creator = new InstanceCreator(this, speech, attrs);
			creators.add(creator);
			this.executorService.execute(creator);
			threads++;
		}

		while ( this.finishedThreads != threads )
			synchronized ( this ) {
				Logger.getInstance().info(SLDA.class, "Finished thread " + this.finishedThreads + " of " + threads + ".");
				this.wait();
			}
		Logger.getInstance().info(SLDA.class, "Finished all threads.");

		for ( InstanceCreator creator : creators )
			instances.add(creator.getInstance());

		Logger.getInstance().info(SLDARunnable.class, "Saved ARFF file: " + name + ".", "classes: " + instances.numClasses(), "attributes: " + instances.numAttributes(), "speeches: " + instances.numInstances());

		String file = this.outputFolder + name + ".arff";
		FileWriter.write(file, instances.toString(), "UTF-8");

		return file;
	}

	private String analyse(String file) {
		String name = file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf("."));
		String resultFile = this.outputFolder + "topwords_" + name + ".csv";

		if ( !new File(resultFile).exists() ) {
			this.executorService.execute(new SLDARunnable(this, file, resultFile));
			this.sldas++;
		}

		return resultFile;
	}

	class XMLLoader implements Runnable {
		private final SLDA parent;
		private final String xml;
		private final List<Object[]> speeches;
		private final Set<String> speakers;

		public XMLLoader(SLDA parent, String xml) {
			this.parent = parent;
			this.xml = xml;
			this.speeches = new LinkedList<>();
			this.speakers = new LinkedHashSet<>();
		}

		public List<Object[]> getSpeeches() {
			return this.speeches;
		}

		public Set<String> getSpeakers() {
			return this.speakers;
		}

		@Override
		public void run() {
			try {
				this.parent.loadXML(this.xml, this.speeches, this.speakers);
			}
			catch ( IOException e ) {
				Logger.getInstance().error(ResultLoader.class, "Error while loading file: " + this.xml, e.toString());
			}

			this.parent.incrementFinishedThreads();
			synchronized ( this.parent ) {
				this.parent.notify();
			}
		}
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
		private final FastVector attrs;
		private Instance instance;

		public InstanceCreator(SLDA parent, Object[] speech, FastVector attrs) {
			this.parent = parent;
			this.speech = speech;
			this.attrs = attrs;
		}

		public Instance getInstance() {
			return this.instance;
		}

		@Override
		public void run() {
			this.instance = new Instance(this.attrs.size());
			this.instance.setValue((Attribute)this.attrs.elementAt(0), this.speech[0].toString());

			Map<String, Integer> counts = (Map<String, Integer>)this.speech[1];
			Enumeration elements = this.attrs.elements(0);
			while ( elements.hasMoreElements() ) {
				Attribute a = (Attribute)elements.nextElement();
				this.instance.setValue(a, (counts.containsKey(a.name()) ? counts.get(a.name()) : 0));
			}

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
				slda.setOptions(Utils.splitOptions(this.parent.sldaOptions));
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