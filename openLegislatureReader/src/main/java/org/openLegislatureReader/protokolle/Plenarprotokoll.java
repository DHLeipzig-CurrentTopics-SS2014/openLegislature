package org.openLegislatureReader.protokolle;

import java.util.ArrayList;
import java.util.List;

public class Plenarprotokoll {

	private String oid = new String();
	private String session_number;
	private String header = new String();
	private List<Speech> speeches = new ArrayList<Speech>();
	private List<Agenda> agendas = new ArrayList<Agenda>();
	
	public String getHeader() {
		return header;
	}

	public void setSpeeches(List<Speech> speeches) {
		this.speeches = speeches;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getSession_number() {
		return session_number;
	}

	public void setSession_number(String session_number) {
		this.session_number = session_number;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public List<Speech> getSpeeches() {
		return speeches;
	}
	
	public void addSpeech(Speech speech){
		this.speeches.add(speech);
	}
	
	public void addAgenda(Agenda agenda){
		this.agendas.add(agenda);
	}
	
	public List<Agenda> getAgendas() {
		return agendas;
	}
	
	public void testOutput(){
		System.out.println("####");
		System.out.println("sessionnumber: "+session_number);
		System.out.println("------------------------");
		for(Speech s : speeches){
			System.out.println("Speaker: "+s.getSpeaker().toString());
			System.out.println("Contentsize: "+s.getContent().size());
			System.out.println("Interjectionssize: "+s.getInterjections().size());
		}
		System.out.println("");
		System.out.println("####");
	}
}
