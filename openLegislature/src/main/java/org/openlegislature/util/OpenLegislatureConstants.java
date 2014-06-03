package org.openlegislature.util;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Simple constants class to be used by the main application. 
 * Most of the constants can be set at JVM startup time using a JVM param like -Dopenlegislature.XXX.
 * The XXX should of course be replaced by the parameter name.
 * 
 * @author dhaeb
 *
 */
@Singleton
public class OpenLegislatureConstants {
	
	private static final String MONGO_COL_DEFAULT_NAME = "plenarprotokolle";
	private static final String PREFIX = "openlegislature";
	private static final String VM_PARAM_CLEAN = PREFIX + ".clean";
	private static final String VM_PARAM_MAXPERIOD = PREFIX + ".maxPeriod";
	private static final String VM_PARAM_MAXTHREADS = PREFIX + ".maxThreads";
	private static final String VM_PARAM_MAXSESSION = PREFIX + ".maxSession";
	private static final String VM_THREAD_IDLE_TIME = PREFIX + ".threadIdleTime";
    private static final String VM_PARAM_XML = PREFIX + ".processXml";
	private static final String VM_PARAM_ENCODING = PREFIX + ".encoding";
	private static final String VM_XPATH_IN_MEM = PREFIX + ".xpathinmemory";
	private static final String VM_PARAM_PERSISTENCE_DIR = PREFIX + ".persistenceDir";
	private static final String VM_PARAM_MONGO_COL = PREFIX + ".mongoCol";
    
	private static final String DEFAULT_ENCODING = "UTF-8";
    public static final String BUNDESTAG_DEFAULT_DIR = "/data/bundestag";
    public static final String BUNSTAG_PROTOKOLL_RSS = "http://www.bundestag.de/rss_feeds/plenarprotokolle.rss";
    
    private int maxPeriod;
	private int maxSession;
	private int maxThreads;
	private Map<Integer, Integer> sessionMap = new HashMap<Integer, Integer>();
	private boolean clean;
    private boolean processXml;
    private boolean xpathInMemory;
	private int threadIdleTime;
    private String encodingName;
	private String persistenceDir;
    private String collectionName;
    
	@Inject
	public OpenLegislatureConstants() {
		maxPeriod = Helpers.initparamInt(18, VM_PARAM_MAXPERIOD);
		maxSession = Helpers.initparamInt(500, VM_PARAM_MAXSESSION);
		threadIdleTime = Helpers.initparamInt(30, VM_THREAD_IDLE_TIME); // in seconds
		sessionMap.put(1, 282);
		sessionMap.put(2, 227);
		sessionMap.put(3, 168);
		sessionMap.put(4, 198);
		sessionMap.put(5, 247);
		sessionMap.put(6, 199);
		sessionMap.put(7, 259);
		sessionMap.put(8, 230);
		sessionMap.put(9, 142);
		sessionMap.put(10, 256);
		sessionMap.put(11, 236);
		sessionMap.put(12, 243);
		sessionMap.put(13, 248);
		sessionMap.put(14, 253);
		sessionMap.put(15, 187);
		sessionMap.put(16, 233);
		sessionMap.put(17, 253);
		sessionMap.put(18, 37);
		this.clean = initBoolean(VM_PARAM_CLEAN, false);
        this.processXml = initBoolean(VM_PARAM_XML, false);
        this.xpathInMemory = initBoolean(VM_XPATH_IN_MEM, false);
        String encoding = System.getProperty(VM_PARAM_ENCODING);
        this.encodingName = encoding == null ? DEFAULT_ENCODING : encoding;
        String col = System.getProperty(VM_PARAM_MONGO_COL);
        this.collectionName = col == null ? MONGO_COL_DEFAULT_NAME : col;
        initPersistenceDir();
		maxThreads = Helpers.initparamInt(4, VM_PARAM_MAXTHREADS);
        Logger.getInstance().debug("Constant-Values:");
        Logger.getInstance().debug(toString());
	}
	
	private boolean initBoolean(String propertyName, boolean defaultBool) {
		String bool = System.getProperty(propertyName);
		return bool == null ? defaultBool : bool.equals("true");
	}

	private void initPersistenceDir() {
		String pathToPersistenceDir = System.getProperty(VM_PARAM_PERSISTENCE_DIR);
        if(pathToPersistenceDir == null){
        	this.persistenceDir = Helpers.getUserDir() + BUNDESTAG_DEFAULT_DIR;
        } else {
        	this.persistenceDir = pathToPersistenceDir + BUNDESTAG_DEFAULT_DIR;
        }
	}
	
	@Override
	public String toString() {
		return String
				.format("OpenLegislatureConstants [maxPeriod=%s, maxSession=%s, maxThreads=%s, sessionMap=%s, clean=%s, processXml=%s, xpathInMemory=%s, threadIdleTime=%s, encodingName=%s, persistenceDir=%s]",
						maxPeriod, maxSession, maxThreads, sessionMap, clean, processXml, xpathInMemory, threadIdleTime, encodingName, persistenceDir);
	}

	public int getMaxPeriod() {
		return maxPeriod;
	}

	public int getMaxSession() {
		return maxSession;
	}

	public Map<Integer, Integer> getSessionMap() {
		return sessionMap;
	}
	
	public boolean isClean() {
		return clean;
	}
	
	public int getMaxThreads() {
		return maxThreads;
	}
	
	public int getThreadIdleTime() {
		return threadIdleTime;
	}

    public boolean isProcessXml() {
        return processXml;
    }

    public String getEncoding(){
        return encodingName;
    }
    
    public boolean isXpathInMemory() {
		return xpathInMemory;
	}

	public String getPersistenceDir() {
		return persistenceDir;
	}
	
	public String getCollectionName() {
		return collectionName;
	}
}
