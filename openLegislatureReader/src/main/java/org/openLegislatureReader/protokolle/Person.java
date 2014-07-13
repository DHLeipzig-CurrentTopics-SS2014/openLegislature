package org.openLegislatureReader.protokolle;

import java.util.Iterator;
import java.util.Map;

import com.mongodb.BasicDBObject;

public class Person {
	
	public Person(){}
	public Person(BasicDBObject bdbo){
		Iterator<?> it = bdbo.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry en = (Map.Entry) it.next();
			if(en.getKey().equals("public_office")){
				this.public_office=en.getValue().toString();
			}
			else if(en.getKey().equals("name")){
				this.name=en.getValue().toString();
			}
			else if(en.getKey().equals("party")){
				this.party=en.getValue().toString();
			}
			else{
				System.err.println(en.getKey()+":"+en.getValue());
			}
		}
		
	}
	
	String name= new String();
	String party = new String();
	String public_office = new String();
	
	public String getPublic_office() {
		return public_office;
	}
	public void setPublic_office(String public_office) {
		this.public_office = public_office;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParty() {
		return party;
	}
	public void setParty(String party) {
		this.party = party;
	}
	public String toString(){
		return this.public_office+" "+this.name+" ("+this.party+")";
	}
	
	
}
