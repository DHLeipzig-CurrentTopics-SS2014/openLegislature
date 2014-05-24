package org.openlegislature.process;

import com.google.inject.Inject;
import com.stumbleupon.async.Callback;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;

public class XPathQueryEngineCallback implements Callback<File, File> {

    private XPathQueryEngine engine;

    @Inject
    public XPathQueryEngineCallback(XPathQueryEngine engine) throws ParserConfigurationException {
        this.engine = engine;
    }

    @Override
    public File call(File xmlFile) throws Exception {
        engine.add(xmlFile);
        return xmlFile;
    }
}
