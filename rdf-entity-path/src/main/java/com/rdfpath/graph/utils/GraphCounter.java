/**
 * 
 */
package com.rdfpath.graph.utils;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;


/**
 *
 * @author Cristóbal Torres G.
 * @github Tinslim
 *
 */
public class GraphCounter extends AbstractRDFHandler {	
	private Boolean debug = false;
	private int countedStatements = 0;
	private int countedLines = 0;
	
	HashMap<Integer, LinkedList<Integer>> nodes;
	HashMap<Integer, int[]> edges;
	
	private long startTime;
	private long actualTime;
	private int minute;
	
	private int nodesLoaded;
	private int edgesLoaded;
	
	public GraphCounter () {
		nodes = new HashMap<Integer, LinkedList<Integer>>();
		edges = new HashMap<Integer, int[]>();
		
		startTime = System.currentTimeMillis();
		actualTime = System.currentTimeMillis();
		minute = 0;
		
		nodesLoaded = 0;
		edgesLoaded = 0;
	}
	
	public void handleStatement(Statement st) {
		countedLines += 1;
		long timeA = System.currentTimeMillis();
		
		// TODO AVISO
		if ((((timeA - actualTime)/1000) / 60) >= 10) { // Minutos
			actualTime = timeA;
			minute += 10;
			System.out.println("Minutos: " + minute);
			System.out.println("Fila: " + countedLines);
			if (System.getProperty("tg-token") != null && System.getProperty("tg-user") != null) {
				try {
					Utils.peticionHttpGet("https://api.telegram.org/bot"+System.getProperty("tg-token") + "/sendMessage?chat_id="+System.getProperty("tg-user")+"&text=Minutos:"+minute+"_Fila:"+countedLines);
					Utils.peticionHttpGet("https://api.telegram.org/bot"+System.getProperty("tg-token") + "/sendMessage?chat_id="+System.getProperty("tg-user")+"&text=Minutos:"+"Nodos:"+nodes.size());
				
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}


		Value object;
		Value subject;
		IRI predicate;
		//try {
		object = st.getObject();
		subject = st.getSubject();
		predicate = st.getPredicate();
		//}
		//catch (Exception e) {
		//	throw new IOException(e);
		//	return;
		//}
		
		// Transforma a String
		String strSubject = subject.toString();
		String strPredicate = predicate.toString();
		String strObject = object.toString();
		
		// Obtiene IDs de Sujeto, Objeto, Predicado
		int subjectKey;
		int ObjectKey;
		Integer PredicateKey;

		String str = "http://www.wikidata.org/entity/Q";
		if(!strSubject.startsWith(str) || !strObject.startsWith(str)){
			return;
		};

		//try {
		subjectKey = getObjectId(strSubject);
		ObjectKey = getObjectId(strObject);
		PredicateKey = getPredicateId(strPredicate);
		//}
		//catch (Exception e) {
		//	return;
		//}

		countedStatements++;
		
		// Crea Nodo
		if (nodes.containsKey(subjectKey)) {
			nodes.get(subjectKey).add(countedStatements);
		}
		else {
			LinkedList<Integer> adjList = new LinkedList<Integer>();
			adjList.add(countedStatements);
			nodes.put(subjectKey,adjList);
			nodesLoaded += 1;
		}
		
		if (nodes.containsKey(ObjectKey)) {
			nodes.get(ObjectKey).add(countedStatements);
		}
		else {
			LinkedList<Integer> adjList = new LinkedList<Integer>();
			adjList.add(countedStatements);
			nodes.put(ObjectKey,adjList);
			nodesLoaded += 1;
		}
		// ---
	
	
		// Añade arista
		int[] triplet = {subjectKey, PredicateKey,ObjectKey};
		edges.put(countedStatements,triplet);
		edgesLoaded += 1; //Cuenta arista

		if (debug) System.out.println(subjectKey + "->" + getPredicateId(strPredicate) + "->" + ObjectKey);
	}

	public void printCounters() {
		System.out.println("Lineas contadas:" + countedLines);
		System.out.println("Lineas Agregadas:" + countedStatements);
		System.out.println("Nodos Creados:" + nodesLoaded);
		System.out.println(nodes.size());
		System.out.println("Aristas Creadas:" + edgesLoaded);
		
	}
	
	private Integer getObjectId(String obj) {
			return Integer.parseInt(obj.substring(32, obj.length()));	
		}
	
	private Integer getPredicateId(String pred) {
		if(pred.equals("http://www.w3.org/2002/07/owl#sameAs"))
			return -1;
		else return Integer.parseInt(pred.substring(37, pred.length()));	
	}

	 public int getCountedStatements() {
	   return countedStatements;
	 }
	 
	 public int getCountedLines () {
		 return countedLines;
	 }

	/**
	 * @return
	 */
	public HashMap getNodes() {
		return nodes;
	}
	
	public HashMap getEdges() {
		return edges;
	}
}
