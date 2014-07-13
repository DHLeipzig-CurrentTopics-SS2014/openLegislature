package org.openLegislatureReader.Speechmapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openLegislatureReader.prepro.CooccurMatrix;

import cern.colt.matrix.tint.IntMatrix2D;
import cern.colt.matrix.tint.impl.SparseIntMatrix2D;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class SpeechMapper {
	
	SpeechCooccur sc = new SpeechCooccur();
	
	public SpeechMapper(DBObject dbo){
		mapDbobject(dbo);
	}

	private void mapDbobject(DBObject dbo) {
		// TODO Auto-generated method stub
		Map<?, ?> map = dbo.toMap();
		
		
		Iterator<?> it = map.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry en = (Map.Entry) it.next();
			
			if(en.getKey().equals("sessionnumber")){
				sc.setSessionNumber( en.getValue().toString());
				
			}
			else if(en.getKey().equals("speaker")){
				sc.setSpeakername(en.getValue().toString());
			}
			else if(en.getKey().equals("data")){
				sc = processData(en.getValue(),sc);
			}
			
		}
	}
	
	private SpeechCooccur processData(Object o,SpeechCooccur sc ) {
		// TODO Auto-generated method stub
		
		Map<String,String> kvlist = new HashMap<String, String>();
		List<String> wlist = new ArrayList<String>();
		BasicDBObject dbo = (BasicDBObject) o;
		
		Iterator<?> it = dbo.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry en = (Map.Entry) it.next();
			
			BasicDBObject db = (BasicDBObject) en.getValue();
			
			kvlist.put(en.getKey().toString(),db.getString((String) en.getKey()));
			wlist.add(en.getKey().toString());
		}
		String[] wordlist = new String[wlist.size()];
		sc.setWordlist( wlist.toArray(wordlist) );
		
		//hauptdiagonale eintragen
		IntMatrix2D coNew = new SparseIntMatrix2D(kvlist.size(),kvlist.size());
		for(int i=0;i<wordlist.length;i++){
			
			coNew.setQuick(i, i, Integer.parseInt(kvlist.get(wordlist[i])));
		}
		//rest befüllen
		for(int i=0;i<coNew.rows();i++){
			for(int j=0;j<coNew.rows();j++){
				if(i!=j){
					int val = coNew.getQuick(i, i) * coNew.getQuick(j, j); 
					coNew.setQuick(i, j, val);
				}
			}
		}
		CooccurMatrix cooc = new CooccurMatrix(coNew,wordlist);
		sc.setCooc(cooc);
		
		return sc;
	}

	private boolean valueIsList(Object o){
		if(o.getClass().toString().equals("class com.mongodb.BasicDBList")) return true;
		return false;
	}
	
	private boolean valueIsString(Object o){
		if(o.getClass().toString().equals("class java.lang.String")) return true;
		return false;
	}
	
	private boolean valueIsBasicDBObject(Object o){
		if(o.getClass().toString().equals("class com.mongodb.BasicDBObject")) return true;
		return false;
	}

	public SpeechCooccur getSc() {
		return sc;
	}
}
