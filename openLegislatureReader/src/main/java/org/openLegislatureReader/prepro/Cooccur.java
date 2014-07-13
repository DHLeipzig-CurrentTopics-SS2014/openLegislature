package org.openLegislatureReader.prepro;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.openLegislatureReader.protokolle.Speech;

import cern.colt.matrix.tint.IntMatrix2D;
import cern.colt.matrix.tint.impl.SparseIntMatrix2D;

public class Cooccur {
//IntMatrix2D coNew = new SparseIntMatrix2D(wordset.size(),wordset.size());
	Speech speech;
	List<String> text = new ArrayList<String>();
	List<String> textToken = new LinkedList<String>();
	private String[] crapList={"“","„","■","\"","\\(","\\)","\\[","\\]","-","–","\\?","\\.","\\,",":","!","\n"};
	String[] wordarray;
	CooccurMatrix com;
	
	public Cooccur(Speech speech){
		this.speech = speech;
	}
	
	public void docCooC(){
		cleanUp();
		createWordArray();
		this.com = new CooccurMatrix(fillDocCooc(),wordarray);
	}
	
	public void docCooCCount(){
		cleanUp();
		createWordArray();
		this.com = new CooccurMatrix(fillDocCoocCount(),wordarray);
	}
	
	public CooccurMatrix getCom() {
		return com;
	}

	private void cleanUp(){
		if(speech.getContent()!=null){
			try{
				for(String l : speech.getContent()){
					
					l=removeCrap(l);
					this.text.add(l);
				}		
			}catch(Exception e){}
		}
	}
	
	public String[] getWordarray() {
		return wordarray;
	}

	private String[] createWordArray(){
		HashSet<String> words = new HashSet<String>();
		
		for(String t:text){
			String[] tmp= t.split(" ");
			for( String s:tmp){
				if(s.trim().equalsIgnoreCase(" ")==false && s.trim().isEmpty()==false ){
					if(s.length() > 1) {
						//set for wordarray
						words.add(s.toLowerCase());
						
						textToken.add(s.toLowerCase());
					}
				}
			}
		}
		
		String[] a = new String[words.size()];
		words.toArray(a);
		wordarray=a;
		return a;
	}
	
	private String removeCrap(String s){
		
		for(String crap:crapList)
		{
			s=s.replaceAll(crap, "");
		}
		
		return s;
		
	}
	
	private int findWord(String word)
	{
		for(int i=0;i<wordarray.length;i++)
		{
			if(word.equals(wordarray[i]))
			{
				return i;
			}
		}
		return -1;
	}
	
	
	private IntMatrix2D fillDocCooc(){
		IntMatrix2D co =new SparseIntMatrix2D(this.wordarray.length,this.wordarray.length);
		for(int i=0;i<textToken.size();i++){
			String s= textToken.get(i);
			int x=findWord(s);
			if(x>=0){
				co.setQuick(x, x, co.getQuick(x, x)+1);
				for(int j=0;j<textToken.size();j++){
					String t=textToken.get(j);
					int y = findWord(t);
					if(y>=0){
						//System.out.println(co.getQuick(x, y)+1);
						
						if(x!=y){
							co.setQuick(x, y, co.getQuick(x, y)+1);
						}
					}
				}
			}
			
		}
		
		return co;
	}
	
	private IntMatrix2D fillDocCoocCount(){
		IntMatrix2D co =new SparseIntMatrix2D(this.wordarray.length,this.wordarray.length);
		for(int i=0;i<textToken.size();i++){
			String s= textToken.get(i);
			int x=findWord(s);
			if(x>=0){
				co.setQuick(x, x, co.getQuick(x, x)+1);
			}
		}
		return co;
	}
}
