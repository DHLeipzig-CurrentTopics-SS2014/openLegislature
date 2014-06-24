package org.openLegislatureReader.prepro;

import java.util.HashSet;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import cern.colt.matrix.tint.IntMatrix2D;
import cern.colt.matrix.tint.impl.SparseIntMatrix2D;



public class CooccurMatrix {

	
	
	IntMatrix2D co;
	String[] wordindex;
	
	public CooccurMatrix(){}
	
	public CooccurMatrix(IntMatrix2D co,	String[] wordindex){
		this.co=co;
		this.wordindex=wordindex;
	}
	
	public void addMatrix(IntMatrix2D co,	String[] wordindex){
		
		if(this.co==null){
			this.co=co;
			this.wordindex=wordindex;
		}
		else{
			merge(co,wordindex);
		}
	}
	
	private void merge(IntMatrix2D co,	String[] wordindex){
		Set<String> wordset = new HashSet<String>();
		for(String s:this.wordindex){wordset.add(s);}
		for(String s:wordindex){wordset.add(s);}
		
		String[] wordindexNew =new String[wordset.size()];
		//wordset.toArray(wordindexNew);
		IntMatrix2D coNew = new SparseIntMatrix2D(wordset.size(),wordset.size());
		
		//old matrix
		for(int i=0;i<this.wordindex.length;i++){
			wordindexNew[i]=this.wordindex[i];
			int x=findWord(this.wordindex, this.wordindex[i]);
			for(int j=0;j<this.wordindex.length;j++){
				int y=findWord(this.wordindex, this.wordindex[j]);
				coNew.setQuick(x, y, this.co.getQuick(x, y));
			}
		}
		
		int counter = this.wordindex.length;
		
		//add new matrix
		for(int i=0;i<wordindex.length;i++){
			
			if( findWord(wordindexNew, wordindex[i]) ==-1){
				wordindexNew[counter]=wordindex[i];
				counter++;
			}
			else{
				wordindexNew[findWord(wordindexNew, wordindex[i])]=wordindex[i];
			}
			
			int xold=findWord(wordindex, wordindex[i]);
			int xnew=findWord(wordindexNew, wordindex[i]);
			for(int j=0;j<wordindex.length;j++){
				int yold=findWord(wordindex, wordindex[j]);
				int ynew=findWord(wordindexNew, wordindex[j]);
				coNew.setQuick(xnew, ynew, coNew.getQuick(xnew, ynew) + co.getQuick(xold, yold));
			}
		}
		
		
		
		//override old
		this.co=coNew;
		this.wordindex=wordindexNew;
	}
	
	
	public String[] getWordindex() {
		return this.wordindex;
	}
	
	public IntMatrix2D getCo() {	
		return this.co;
	}
	
	private int findWord(String[] index,String word){
		//if(word==null ){System.err.println("findword get null");return -1;}
		for(int i=0;i<index.length;i++)
		{
			if(word.equals((index[i])))
			{
				return i;
			}
		}
		return -1;
	}
	
	public void testPrint(){

		for(int i=0;i<co.rows();i++){
			System.out.println("");
			for(int j=0;j<co.rows();j++){
					System.out.print(co.getQuick(i, j));
			}
		}
	}
	
	public String getJson(){
		String ret="";
		
		for(int i=0;i< wordindex.length;i++){
			if(i!=0){ret+=",";}
			String key=wordindex[i];
			String value="[";
			for(int j=0;j<wordindex.length;j++){
				if(j!=0){value+=",";}
				value+="{\""+wordindex[j]+"\":\""+co.getQuick(i, j)+"\"}";
			}
			value+="]";
			ret+=""+key+"\":"+value;
		}
		ret+="";
		
		return ret;
	}
	
	public BasicDBObjectBuilder getDBObject(String[] keys, String[] values){
		if(keys.length!=values.length){return null;}
		
		
		BasicDBObjectBuilder root = BasicDBObjectBuilder.start();
		for(int i=0;i<keys.length;i++){
			root.add(keys[i], values[i]);
		}
                 
        BasicDBObjectBuilder w = BasicDBObjectBuilder.start();
        for(int i=0;i< wordindex.length;i++){
 			
 			String key=wordindex[i];
 			BasicDBObjectBuilder ww = BasicDBObjectBuilder.start();
 			for(int j=0;j<wordindex.length;j++){
 				if(co.getQuick(i, j)>0){
 					ww.add(wordindex[j], co.getQuick(i, j));
 				}
 			}
 			w.add(key, ww.get());
 		}
                 
		root.add("data",w.get());
		return root;
	}
}
