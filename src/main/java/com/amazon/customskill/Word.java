package com.amazon.customskill;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

public class Word {

	static String Word;
	static String Level;
	static int WordID;
	private static Connection con = null;
	private static Statement stmt = null;// Sql statement 
	Random random = new Random(); // fuer Randomisierte Auswahl der Woerter 
    int rand; // int mit Aktuellen rand num 
    int randOld;

	public Word(String Level) {
		this.Level = Level;
	}

	/*
* selects the content of a row in the question tables for the needed question based on the levelId from Words table
	 * */
	public String selectWord() {

		switch (Level) {
		case "leicht": {
			int max = 115;
	        int min = 100; 
			rand = (int)(random.nextInt((max - min) + 1) + min);
			randOld = rand; 
			break;
		}
		case "schwer": {
			int max = 147;
	        int min = 116; 
			rand = (int)(random.nextInt((max - min) + 1) + min);
			break;
		}
		case "sehrschwer": {
			int max = 148;
	        int min = 163; 
			rand = (int)(random.nextInt((max - min) + 1) + min);
			break;
		}

		}

		try {
			con = DBConnection.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT *  FROM Word where NiveauID = '" + Level + "' and WordID =" + rand + "");
			while (rs.next()) {
				Word = rs.getString("Word");
				WordID = rs.getInt("WordID");
			}
			//String ID = rs.getString("NiveauID");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Word;
	}

	public static void setWordID(int wordID) {
		WordID = wordID;
	}

	public int getWordID() {
		return WordID;
	}

	public String getWord() {
		return Word;
	}
}
