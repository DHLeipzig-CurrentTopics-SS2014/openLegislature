package weka.classifiers.topicmodelling;

import static org.junit.Assert.assertEquals;

import java.util.Enumeration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
public class SLDATest {
	private Instances data;
	@Before
	public void setUp() {
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

		this.data = new Instances("test", atts, 0);
		this.data.setClassIndex(this.data.numAttributes() - 1);

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
		this.data.add(instance);

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
		this.data.add(instance);

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
		this.data.add(instance);

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
		this.data.add(instance);

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
		this.data.add(instance);

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
		this.data.add(instance);

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
		this.data.add(instance);

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
		this.data.add(instance);

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
		this.data.add(instance);

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
		this.data.add(instance);

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
		this.data.add(instance);

		System.out.println("data");
		System.out.println(this.data);
		System.out.println();
	}

	@After
	public void tearDown() {}

	@Test
	public void testSLDA() throws Exception {
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
		for ( String[] topWord : topWords ) {
			System.out.print("class " + topWord[0] + ": ");
			for (int j = 1; j < topWord.length; j++)
				System.out.print(topWord[j] + " ");
			System.out.println();
		}

		assertEquals(10, num);
	}
}