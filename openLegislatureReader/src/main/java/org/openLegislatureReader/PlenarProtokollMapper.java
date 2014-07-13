package org.openLegislatureReader;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.openLegislatureReader.protokolle.Agenda;
import org.openLegislatureReader.protokolle.Person;
import org.openLegislatureReader.protokolle.Plenarprotokoll;
import org.openLegislatureReader.protokolle.Speech;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class PlenarProtokollMapper {
	
	Plenarprotokoll plenarprotokoll = new Plenarprotokoll();
	
	public PlenarProtokollMapper(DBObject dbo){
		mapDbobject(dbo);
	}

	
	private void mapDbobject(DBObject dbo){
		Map<?, ?> map = dbo.toMap();
		
		Iterator<?> it = map.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry en = (Map.Entry) it.next();
			
			if(en.getKey().equals("_id")){
				plenarprotokoll.setOid(en.getValue().toString());
			}
			else if(en.getKey().equals("protocol")){
				mapProtocol(en.getValue());
			}
			else{
				System.out.println("mapDbobject: "+en.getKey()+":"+en.getValue());
			}
			
		}
	}
	
	private void mapProtocol(Object o){
		BasicDBObject dbo = (BasicDBObject) o;
		Iterator<?> it = dbo.entrySet().iterator();
		while(it.hasNext()){ 
			Map.Entry en = (Map.Entry) it.next();
			if(en.getKey().equals("session_number")){
				plenarprotokoll.setSession_number( en.getValue().toString());
				
			}
			else if(en.getKey().equals("header")){
				plenarprotokoll.setHeader(en.getValue().toString());
			}
			else if(en.getKey().equals("agenda")){
				mapAgenda(en.getValue());
			}
			else if(en.getKey().equals("session")){
				
				if(valueIsBasicDBObject(en.getValue())){
					mapSession(en.getValue());
				}
				else if(valueIsList(en.getValue())){
					BasicDBList list = (BasicDBList) en.getValue();
					for(Object obj : list){
						if(valueIsBasicDBObject(obj)){
							mapSession(obj);
						}
					}
				}
				
			}
			else{
				System.err.println("mapProtokoll: "+en.getKey()+":"+en.getValue());
			}
		}
		
	}
	
	private void mapAgenda(Object o){ 
		BasicDBObject dbo = (BasicDBObject) o;
		
		Iterator<?> it = dbo.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry en = (Map.Entry) it.next();
			if(en.getKey().equals("item")){
				
				if( valueIsList(en.getValue())){
					BasicDBList bdb = (BasicDBList) en.getValue();
					for( int i=0;i<bdb.size();i++){ 
						if(valueIsBasicDBObject(bdb.get(i))){
							this.plenarprotokoll.addAgenda( new Agenda( (BasicDBObject) bdb.get(i) ) );
						}
					}
				}
			}
			else{
				System.err.println("mapAgenda: "+en.getKey()+":"+en.getValue()+" (oid: "+this.plenarprotokoll.getOid()+" )");
			}
		}
	}
	
	private void mapSession(Object o){
		BasicDBObject dbo = (BasicDBObject) o;
		
		Iterator<?> it = dbo.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry en = (Map.Entry) it.next();
			
			if(  en.getKey().equals("speech") ){
				if( valueIsList(en.getValue())) {
					BasicDBList bdb = (BasicDBList) en.getValue();
					
					for( int i=0;i<bdb.size();i++){
						mapSpeech(bdb.get(i));
					}
				}
				else if( valueIsBasicDBObject(en.getValue())) {
					mapSpeech(en.getValue());
				}
				else{
					System.err.println("mapSession-speech: "+en.getKey()+":"+en.getValue()); 
				}
				
			}
			else if(en.getKey().equals("content")){
				//do something if needed
				//System.out.println(en.getValue().toString());
			}
			else if(en.getKey().equals("attachment")){
				//do something if needed
			}
			else if(en.getKey().equals("adjustment")){
				//do something if needed
			}
			else{
				
				System.err.println("mapSession: "+en.getKey()+":"+en.getValue()); 
			}
		}
	}
	
	
	private void mapSpeech(Object o){
		BasicDBObject dbo = (BasicDBObject) o;
		
		Speech speech = new Speech();
		
		Iterator<?> it = dbo.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry en = (Map.Entry) it.next();
		
			
			if(en.getKey().equals("content")){

				if(valueIsList(en.getValue())){
					BasicDBList bdblist = (BasicDBList) en.getValue();
					speech.addAllContent(bdblist);
				}
				else if(valueIsString(en.getValue())){
					speech.addContent(en.getValue().toString());
				}
				else{
					System.err.println(en.getKey()+":"+en.getValue());
				}
			}
			else if(en.getKey().equals("interjection")){
				
				if(valueIsList(en.getValue())){
					BasicDBList bdblist = (BasicDBList) en.getValue();
					speech.addAllInterjection(bdblist);
				}
				else if(valueIsString(en.getValue())){
					speech.addInterjection(en.getValue().toString());
				}
				else{
					System.err.println(en.getKey()+":"+en.getValue());
				}
			}
			else if(en.getKey().equals("speaker")){
				if(  valueIsBasicDBObject(en.getValue())){
					Person person = new Person( (BasicDBObject) en.getValue());
					speech.setSpeaker(person);
					
				}
				else{
					System.err.println(en.getKey()+":"+en.getValue());
				}
			}
			else{
				System.err.println(en.getKey()+":"+en.getValue());
			}
		}
		plenarprotokoll.addSpeech(speech);
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
	public Plenarprotokoll getPlenarprotokoll(){return plenarprotokoll;}
	
}
