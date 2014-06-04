package org.openlegislature.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

public class MongoCon {
	private static MongoCon instance;
	private DB db;

	private MongoCon() {}

	public synchronized static MongoCon getInstance() {
		if ( instance == null )
			instance = new MongoCon();

		return instance;
	}

	public synchronized void connect(String host) throws UnknownHostException {
		if ( this.db != null )
			return;

		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		this.db = mongoClient.getDB( "local" );
	}

	public synchronized void disconnect() {
		if ( this.db == null )
			return;

		this.db = null;
	}

	public synchronized void insert( String colname,String json) {
		if ( this.db == null )
			return;
		DBObject dbObject = (DBObject) JSON.parse(json);
		DBCollection collection = db.getCollection(colname);
		collection.insert(dbObject);

	}

	public ArrayList<BasicDBObject> query(String collection , BasicDBObject query){
		ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>();
		
		try {
			DB db = createDBConnection();
			
		
			DBCollection coll = db.getCollection(collection);
			DBCursor cursor = coll.find( query);
			
			while(cursor.hasNext()) {
		       list.add( (BasicDBObject) cursor.next() );
		    }
			cursor.close();
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	public DBObject queryOne(String collection , BasicDBObject query){
		
		DBObject dbObj = null;
		try {
			DB db = createDBConnection();
			DBCollection coll = db.getCollection(collection);
			
		    dbObj = coll.findOne(query);
		    
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dbObj;
	}
	public ArrayList<String> queryS(String collection , BasicDBObject query){
		ArrayList<String> list = new ArrayList<String>();
		
		ArrayList<BasicDBObject> alist = this.query(collection, query);
		for(int i=0;i<alist.size();i++){
			list.add( JSON.serialize( alist.get(i) ));
		}
		return list;
	}
	

	private DB createDBConnection() throws UnknownHostException {
		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		DB db = mongoClient.getDB( "local" );
		return db;
	}
	
	private boolean insertOld(String col, String json){
		try {
			 
			DB db = createDBConnection();
			DBCollection collection = db.getCollection(col);
 
			// convert JSON to DBObject directly
			DBObject dbObject = (DBObject) JSON.parse(json);

			collection.insert(dbObject);
 
			//test output
			/*
			DBCursor cursorDoc = collection.find();
			while (cursorDoc.hasNext()) {
				System.out.println(cursorDoc.next());
			}
 			*/
			//System.out.println("Done");
 			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	
}