package com.amazon.customskill;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class DatenbankTest {


	private static Connection con = null;
	private static Statement stmt = null;
	public static String Wortgruppe; // Correct Answer 
	public static String correctAnswer = null;
	public static Word word;
public static ArrayList<String> myList;

	public static void main (String[] args) throws URISyntaxException {

	//	System.out.println(DatenbankTest.class.getClassLoader().getResource("utterances.txt").toURI());
		
		 word = new Word("leicht");

		 //myList = word.selectWord();

/*
		correctAnswer = word.getWortgruppe();
	
			System.out.println(Word+"\t"+correctAnswer);
			myList = new ArrayList<>();
			
	*/		
		//AlexaSkillSpeechlet as =  new AlexaSkillSpeechlet();
		//as.createRandList(9, myList);
			
		System.out.println(word.checkAnswer("arbeiten"));
			
		
	}
	
	
	public void testFunc(ArrayList<String> myList) {
		for(String s : myList)
		{
			System.out.println(s + "\n");
		}
		
	}


}
