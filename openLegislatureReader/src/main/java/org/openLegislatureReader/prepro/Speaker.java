package org.openLegislatureReader.prepro;

public class Speaker {

	
	CooccurMatrix cooc= new CooccurMatrix();
	String name;
	
	public Speaker(String name){
		this.name=name;
	}
	
	public CooccurMatrix getCooc() {
		return this.cooc;
	}
	
	
	public void addCooc(CooccurMatrix cooc) {
		this.cooc.addMatrix(cooc.getCo(), cooc.getWordindex());
	}

	public String getName() {
		return name;
	}
	
	
}
