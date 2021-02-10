package com.amazon.customskill;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatenbankTest {


	private static Connection con = null;
	private static Statement stmt = null;
	public static String Wortgruppe; // Correct Answer 
	public static String correctAnswer = null;
	public static String Word;


	public static void main (String[] args) throws URISyntaxException {

	//	System.out.println(DatenbankTest.class.getClassLoader().getResource("utterances.txt").toURI());
		
		Word word = new Word("schwer");

		Word = word.selectWord();

		correctAnswer = word.getWortgruppe();
	
			System.out.println(Word+"\t"+correctAnswer);
		
	}


}
