package org.openLegislatureReader.protokolle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class Agenda {

	
	public Agenda(){}
	public Agenda(BasicDBObject dbo){
		Iterator<?> it = dbo.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry en = (Map.Entry) it.next();
			
			
			if(en.getKey().equals("id")){
				this.id=Integer.valueOf(en.getValue().toString());
			}
			else if(en.getKey().equals("speaker")){
				//System.out.println(en.getKey()+":"+en.getValue());
				
				if(en.getValue().getClass().toString().equals("class com.mongodb.BasicDBObject")){
					this.speaker.add( new Person( (BasicDBObject) en.getValue()));
				}
				else if(en.getValue().getClass().toString().equals("class com.mongodb.BasicDBList")){
					BasicDBList list = (BasicDBList)  en.getValue();
					for(int i=0;i<list.size();i++){
						if(list.get(i).getClass().toString().equals("class com.mongodb.BasicDBObject")){
							this.speaker.add( new Person( (BasicDBObject) list.get(i)));
						}
						else{System.err.println(list.get(i));}
					}
				}
				
				//
			}
			else if(en.getKey().equals("content")){
				this.content=en.getValue().toString();
			}
			else{
				System.err.println(en.getKey()+":"+en.getValue());
			}
		}
	}
	
	private int id;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public List<Person> getSpeaker() {
		return speaker;
	}
	public void setSpeaker(Person speaker) {
		this.speaker.add( speaker );
	}
	private String content = new String();
	private List<Person> speaker = new ArrayList<Person>();
}
