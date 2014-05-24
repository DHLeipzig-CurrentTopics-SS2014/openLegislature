package org.openlegislature.process;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.util.List;
import java.util.Vector;

import static javax.xml.parsers.DocumentBuilderFactory.newInstance;

@Singleton
public class XPathQueryEngine {

    private static final DocumentBuilderFactory FACTORY = newInstance();

    private ThreadLocal<DocumentBuilder> builderLocale = new ThreadLocal<DocumentBuilder>() {

        @Override
        protected DocumentBuilder initialValue() {
            try {
                return FACTORY.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException("Can't initialize XML Parser");
            }
        }

    };


    private List<Document> fileList = new Vector<Document>();
    private XPathFactory xPathfactory = XPathFactory.newInstance();
    private Transformer nodePrinter;

    @Inject
    public XPathQueryEngine() throws ParserConfigurationException, TransformerConfigurationException {
        nodePrinter = TransformerFactory.newInstance().newTransformer();
    }

    public void add(File xmlFile) throws IOException, SAXException {
        try {
            Document doc = builderLocale.get().parse(xmlFile);
            fileList.add(doc);
            System.err.println(fileList.size());
        } catch (Exception e) {
            PrintWriter pw = new PrintWriter(new FileOutputStream(new File("fail-files.txt"), true));
            pw.println(xmlFile.toString());
            pw.println(e.toString());
            pw.close();
            throw e;
        } finally{
            builderLocale.get().reset();
        }
    }

    public void query(String xpath) throws XPathExpressionException, TransformerException {
        System.out.println(xpath);
        System.out.println(fileList.size());
        XPath xpathExpr = xPathfactory.newXPath();
        XPathExpression expr = xpathExpr.compile(xpath);
        for(Document doc : fileList){
            NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for(int i = 0; i < nodeList.getLength(); i++){
                nodePrinter.transform(new DOMSource(nodeList.item(i)), new StreamResult(new BufferedWriter(new PrintWriter(System.out))));
            }

        }
    }

}
