package org.openLegislatureReader.protokolle;

import java.util.ArrayList;
import java.util.List;

import org.openLegislatureReader.prepro.CooccurMatrix;

public class Speech {

	private List<String> content = new ArrayList<String>();
	private Person speaker;
	private List<String> interjections = new ArrayList<String>();
	
	private CooccurMatrix cooc;
	
	public CooccurMatrix getCooc() {
		return cooc;
	}
	public void setCooc(CooccurMatrix cooc) {
		this.cooc = cooc;
	}


	public Person getSpeaker() {
		return speaker;
	}

	public void setSpeaker(Person speaker) {
		this.speaker = speaker;
	}

	public List<String> getContent() {
		return content;
	}
	
	public List<String> getInterjections() {
		return interjections;
	}

	public void addContent(String content) {
		this.content.add(content);
	}
	
	public void addAllContent(List content) {
		this.content.addAll(content);
	}
	
	public void addInterjection(String interjection) {
		this.interjections.add(interjection);
	}
	
	public void addAllInterjection(List interjection) {
		this.interjections.addAll(interjection);
	}
	
}
