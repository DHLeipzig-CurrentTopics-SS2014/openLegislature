package org.openLegislatureReader.Speechmapper;

import org.openLegislatureReader.MongoCon;

import com.mongodb.BasicDBObject;

public class MappedSpeeches {

	
	public MappedSpeeches(){
		
		MongoCon.getInstance().setCursor("ollspeeches");
		
	}
	
	public SpeechCooccur getNext(){
		if(MongoCon.getInstance().hasNextDocument()){
			
			BasicDBObject dbo=MongoCon.getInstance().getNextDocument();
			SpeechCooccur  sco = new SpeechMapper(dbo).getSc();
			return sco;
		}
		else return null;
	}
}
