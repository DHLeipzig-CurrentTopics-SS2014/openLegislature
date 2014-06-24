package org.openLegislatureReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openLegislatureReader.MongoCon;
import org.openLegislatureReader.Speechmapper.MappedSpeeches;
import org.openLegislatureReader.Speechmapper.SpeechCooccur;
import org.openLegislatureReader.prepro.Cooccur;
import org.openLegislatureReader.prepro.Speaker;
import org.openLegislatureReader.protokolle.Plenarprotokoll;
import org.openLegislatureReader.protokolle.Speech;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;

public class App {

	
	/*
	 * todo:
	 * speech: list of speaker??
	 * some query gedöhns
	 * 
	 */
	
	public static void main(String[] args) throws InterruptedException, IOException {
		// TODO Auto-generated method stub
		
		MongoCon.getInstance().connect("");
		
		//test
		//test();
		
		//speeches
		//processPlenarprotokolleSpeeches();
		
		//speakers
		//processPlenarprotokolleSpeakers();
		
		//speech mapper
		getSpeechesAndMap();
	}

	private static void getSpeechesAndMap() {
		// TODO Auto-generated method stub
		
		//init
		MappedSpeeches ms = new MappedSpeeches();
		SpeechCooccur sc = ms.getNext();
		if(sc!=null){
			//do something
			sc.getCooc().testPrint();
		}
		
	}

	private static void  processPlenarprotokolleSpeeches(){
		
		int counter=0;
		int maxdocuments=2;
		
		MongoCon.getInstance().setCursor("plenarprotokolle");
		while(MongoCon.getInstance().hasNextDocument()){
			counter++;
			if(counter>=maxdocuments){break;}
			BasicDBObject dbo=MongoCon.getInstance().getNextDocument();
			Plenarprotokoll p = new PlenarProtokollMapper(dbo).getPlenarprotokoll();
			p=cooccur(p);
			writeSpeechToDB(p);
		}
	}
	
	private static void  test(){
		
		int counter=0;
		int maxdocuments=4;
		
		MongoCon.getInstance().setCursor("plenarprotokolle");
		while(MongoCon.getInstance().hasNextDocument()){
			counter++;
			if(counter>=maxdocuments){break;}
			BasicDBObject dbo=MongoCon.getInstance().getNextDocument();
			
			MongoMapper mp = new MongoMapper(dbo);
			mp.printMap();
		}
	}
	
	private static void  processPlenarprotokolleSpeakers(){
		
		Set<String> speakerset = new HashSet<String>();
		List<Speaker> speakerlist = new ArrayList<Speaker>(); 
		int counter=0;
		int maxdocuments=4;
		
		//create set of all speakers
		MongoCon.getInstance().setCursor("plenarprotokolle");
		while(MongoCon.getInstance().hasNextDocument()){
			counter++;
			if(counter>=maxdocuments){break;}
			BasicDBObject dbo=MongoCon.getInstance().getNextDocument();
			Plenarprotokoll p = new PlenarProtokollMapper(dbo).getPlenarprotokoll();
			
			for(Speech speech:p.getSpeeches()){
				speakerset.add(speech.getSpeaker().getName());
			}
		}
		
		//set to list
		for(String s:speakerset){
			speakerlist.add(new Speaker(s));
		}
		
		//getCooc for each speaker
		for(Speaker s:speakerlist){
			
			writeSpeakerToDB( processSpeaker(s) );
		}
	}
	
	private static Speaker processSpeaker(Speaker s){
		
		int counter=0;
		int maxdocuments=4;
		
		MongoCon.getInstance().setCursor("plenarprotokolle");
		while(MongoCon.getInstance().hasNextDocument()){
			counter++;
			if(counter>=maxdocuments){break;}
			BasicDBObject dbo=MongoCon.getInstance().getNextDocument();
			Plenarprotokoll p = new PlenarProtokollMapper(dbo).getPlenarprotokoll();
			
			for(Speech speech:p.getSpeeches()){
				if(speech.getSpeaker().getName().equalsIgnoreCase(s.getName())){
					Cooccur co = new Cooccur(speech);
					co.docCooC();
					s.addCooc(co.getCom());
					
				}
			}
		}
		return s;
	}
	
	private static Plenarprotokoll cooccur(Plenarprotokoll p ){
		
		
			System.out.println("process:"+p.getOid());
			List<Speech> speeches = p.getSpeeches();
			for(int i=0;i<speeches.size();i++){
				
				Cooccur co = new Cooccur(speeches.get(i));
				co.docCooCCount();
				speeches.get(i).setCooc(co.getCom());
			}
			p.setSpeeches(speeches);
		
		
		return p;
	}
	
	private static List<Speaker> cooccurSpeaker(List<Plenarprotokoll> protokolle, List<Speaker> speakerlist){
	
		
		//System.out.println("Anz speaker: "+speakerlist.size());
		for(Plenarprotokoll p :protokolle ){
			for(Speech speech:p.getSpeeches()){
				if(speech.getSpeaker()!=null){
					
					for(int i=0;i<speakerlist.size();i++){
						if(speakerlist.get(i).getName().equals(speech.getSpeaker().getName())){
							speakerlist.get(i).addCooc(speech.getCooc());
						}
					}
					
				}
				//else{System.out.println("bla");}
			}
		}
		return speakerlist;
	}
	
	private static void writeSpeechToDB(Plenarprotokoll p){
	
		
			
			List<Speech> speeches = p.getSpeeches();
			for(int i=0;i<speeches.size();i++){
				try{
				
					String[] keys =new String[3];
					String[] values = new String[3];
					keys[0]="docid";
					values[0]=p.getOid();
					keys[1]="speaker";
					values[1]=speeches.get(i).getSpeaker().getName().replaceAll("\\.", "\uff0E");
					keys[2]="sessionumber";
					values[2]=p.getSession_number();
					
					
					BasicDBObjectBuilder dbo = speeches.get(i).getCooc().getDBObject(keys,values);
					MongoCon.getInstance().insert("ollspeeches", dbo);
					
				}catch(Exception e){}
				
			}
		
		
	}
	
	
	private static void writeSpeakerToDB(Speaker speaker){
		
		
		if(speaker.getCooc().getCo()!=null){
			//System.out.println(speakerlist.get(i).getName()+","+speakerlist.get(i).getCooc().getCo().rows());
			
			String[] keys =new String[1];
			String[] values = new String[1];
			keys[0]="speaker";
			values[0]=speaker.getName().replaceAll("\\.", "\uff0E");
		
			
			BasicDBObjectBuilder dbo = speaker.getCooc().getDBObject( keys,values);
			MongoCon.getInstance().insert("ollspeaker", dbo);
			
		}
	

	}
	
	
}
