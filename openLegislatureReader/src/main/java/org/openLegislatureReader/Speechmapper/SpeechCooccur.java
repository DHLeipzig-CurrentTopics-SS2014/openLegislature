package org.openLegislatureReader.Speechmapper;

import org.openLegislatureReader.prepro.CooccurMatrix;

public class SpeechCooccur {
	
	private CooccurMatrix cooc;
	private String[] wordlist;
	private String sessionNumber;
	private String speakername;
	
	SpeechCooccur (CooccurMatrix cooc,String[] wordlist,String sessionNumber,String speakername){
		this.cooc=cooc;
		this.wordlist=wordlist;
		this.sessionNumber=sessionNumber;
		this.speakername=speakername;
	}
	
	SpeechCooccur(){}

	public CooccurMatrix getCooc() {
		return cooc;
	}

	public void setCooc(CooccurMatrix cooc) {
		this.cooc = cooc;
	}

	public String[] getWordlist() {
		return wordlist;
	}

	public void setWordlist(String[] wordlist) {
		this.wordlist = wordlist;
	}

	public String getSessionNumber() {
		return sessionNumber;
	}

	public void setSessionNumber(String sessionNumber) {
		this.sessionNumber = sessionNumber;
	}

	public String getSpeakername() {
		return speakername;
	}

	public void setSpeakername(String speakername) {
		this.speakername = speakername;
	}
}
