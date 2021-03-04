package com.amazon.customskill;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import com.amazon.customskill.DBConnection;


public class Word {
	public static String Artikel;
	public static String Wortgruppe; // Correct Answer 
	public static String AlexaCorrectAnswer = null;
	public static String Word;
	public static String Level;
	public static int WordID;
	private static Connection con = null;
	private static Statement stmt = null;// Sql statement 
	Random random = new Random(); // fuer Randomisierte Auswahl der Woerter 
    int rand; // int mit Aktuellen rand num 
    int randOld;
    ArrayList<String> rnlist;  

	public Word(String Level) {
		this.Level = Level;
	}

	public Word(int wordID2) {
		this.WordID = WordID;
	}

	/*
* selects the content of a row in the question tables for the needed question based on the levelId from Words table
	 * */
	public ArrayList<String> selectWord() {
		
		rnlist = new ArrayList<String>();

		switch (Level) {
		case "leicht": {
			int max = 49;
	        int min = 1; 
			rand = (int)(random.nextInt((max - min) + 1) + min);
			
			randOld = rand; 
			break;
		}
		case "schwer": {
			int max = 68;
	        int min = 50; 
			rand = (int)(random.nextInt((max - min) + 1) + min);
			break;
		}
		case "sehrschwer": {
			int max = 69;
	        int min = 88; 
			rand = (int)(random.nextInt((max - min) + 1) + min);
			break;
		}

		}

		try {
			con = DBConnection.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT *  FROM Words where Schwierigkeit = '" + Level + "'");
			while (rs.next()) {
				Word = rs.getString("Wort");
				WordID = rs.getInt("WordID");
				// change all dependencies
				Wortgruppe = rs.getString("Wortgruppe");
				Artikel = rs.getString("Artikel");
				
				rnlist.add(Word);
				
				
			
			}
			/*
			
			for(String s : rnlist)
			{
				System.out.println(s + "\n");
			}*/
			//String ID = rs.getString("NiveauID");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rnlist;
	}

	
	public String checkAnswer(String currentWord) {

		try {
			con = DBConnection.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT *  FROM Words where Wort = '" + currentWord + "'");
			while (rs.next()) {
				Word = rs.getString("Wort");
				WordID = rs.getInt("WordID");
				// change all dependencies
				Wortgruppe = rs.getString("Wortgruppe");
				Artikel = rs.getString("Artikel");
				
			}
			
			
		
			
			//String ID = rs.getString("NiveauID");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Wortgruppe;
	}
	
	public ArrayList<String> getRnlist() {
		return rnlist;
	}

	public void setRnlist(ArrayList<String> rnlist) {
		this.rnlist = rnlist;
	}

	public String getArtikel() {
		return Artikel;
	}

	public static void setArtikel(String artikel) {
		Artikel = artikel;
	}
//
	public String getWortgruppe() {
		return Wortgruppe;
	}

	public void setWortgruppe(String wortgruppe) {
		Wortgruppe = wortgruppe;
	}

	public String getAlexaCorrectAnswer() {
		return AlexaCorrectAnswer;
	}

	public void setAlexaCorrectAnswer(String alexaCorrectAnswer) {
		AlexaCorrectAnswer = alexaCorrectAnswer;
	}

	public void setWordID(int wordID) {
		WordID = wordID;
	}

	public int getWordID() {
		return WordID;
	}

	public String getWord() {
		return Word;
	}
}
