package com.amazon.customskill;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatenbankTest {


	private static Connection con = null;
	private static Statement stmt = null;


	public static void main (String[] args) throws URISyntaxException {

	//	System.out.println(DatenbankTest.class.getClassLoader().getResource("utterances.txt").toURI());
		
		try {
			con = DBConnection.getConnection();
			stmt = con.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM Words");
			String lvl = rs.getString("Schwierigkeit");
			String word = rs.getString("Wort");
			String correctAnswer = rs.getString("Wortgruppe");
			System.out.println(word+"\t"+correctAnswer);
		} catch (Exception e){
			e.printStackTrace();
		}
	}


}
