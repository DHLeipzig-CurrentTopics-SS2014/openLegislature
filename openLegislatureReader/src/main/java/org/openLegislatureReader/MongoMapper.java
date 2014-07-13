package org.openLegislatureReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;




import com.mongodb.DBObject;

public class MongoMapper {

	private static Map mp;
	public MongoMapper(DBObject dbo){
		this.mp = dbo.toMap();
		
	}
	
	public static void printMap() {
	    Iterator it = mp.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getValue().getClass());
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	}
}
