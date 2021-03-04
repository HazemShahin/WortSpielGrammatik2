/** 

    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved. 

  

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at 

  

        http://aws.amazon.com/apache2.0/ 

  

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License. 

*/

package com.amazon.customskill;

import com.amazon.customskill.Word;

import java.io.File;

import java.io.IOException;

import java.nio.file.Files;

import java.nio.file.Path;

import java.nio.file.Paths;

import java.sql.Connection;

import java.sql.ResultSet;

import java.sql.Statement;

import java.util.ArrayList;

import java.util.Arrays;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import java.util.Random;

import java.util.regex.Matcher;

import java.util.regex.Pattern;

import java.util.stream.IntStream;

import org.apache.commons.io.IOUtils;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;

import com.amazon.speech.slu.Intent;

import com.amazon.speech.speechlet.IntentRequest;

import com.amazon.speech.speechlet.LaunchRequest;

import com.amazon.speech.speechlet.SessionEndedRequest;

import com.amazon.speech.speechlet.SessionStartedRequest;

import com.amazon.speech.speechlet.SpeechletResponse;

import com.amazon.speech.speechlet.SpeechletV2;

import com.amazon.speech.ui.PlainTextOutputSpeech;

import com.amazon.speech.ui.Reprompt;

import com.amazon.speech.ui.SsmlOutputSpeech;

/* Zu machen 18.01.21
 * evaluateYesNO Method
 * evaluateYesNoWiederholung Method
 * evaluateAnswer Method
 * erstelle eine Verbindung mit der neuen DB von Gruppe in  MsTeams.
 * Github Problem lösen
 * 19.01
 * evaluateWordnumChice Method: Denk daran zwei Variabeln zu implementieren für Werte der Zufälligen Zahl(Alt und Aktuell). Grenze der Zahl ist wordnum 10,20,30 
 * Brauchst du unbedingt 2 Methoden askUserResponse-Word&Question oder reicht eine Methode für die Auswahl von DB 
 * Utterance.txt umändern. Besser als alles in Strings zu speichern.
 * Logger Classe überprüfen und Ordentlich testen
 * */


/* 

* This class is the actual skill. Here you receive the input and have to produce the speech output.  

*/

public class AlexaSkillSpeechlet

		implements SpeechletV2

{

// Initialisiert den Logger. Am besten möglichst of Logmeldungen erstellen, hilft hinterher bei der Fehlersuche! 

	static Logger logger = LoggerFactory.getLogger(AlexaSkillSpeechlet.class);

// Variablen, die wir auch schon in DialogOS hatten 

// Var Number of Words (Zahl) 

	//private Question question;
	private Word word;
	public static int  counter = 0;
	static int wordnum;
	static boolean newWord = false;
	static boolean repeat;
	//static String Question = "";
	static String Word = "";
	static boolean beendet = false;
	static String correctAnswer = "";

	private String thisWord;

	private String Level;

	private int WordID;
	public Random rand;
	ArrayList<String> templist;
	ArrayList<String> nextwordlist;
   int punkte = 0;


	public static String userRequest;

// In welchem Spracherkennerknoten sind wir? 

	static enum RecognitionState {
		Answer, YesNo, YesNoWiederholung, YesNoWord
	}; // 4 Methoden für die evaluierung jedes einzige Status

	RecognitionState recState;

// Was hat der User grade gesagt. (Die "Semantic Tags"aus DialogOS) 

	private static enum UserIntent {

		Yes, No, Nomen, Verben, Adjektiven, Präpositionen, leicht, schwer, sehrschwer, beenden, question, Word, //Level,
		zehn, zwanzig, dreizig, nochmal // question,Word Sehlohom

	};
	//obj dec

	UserIntent ourUserIntent;

// Was das System sagen kann 

	Map<String, String> utterances;

// Baut die Systemäußerung zusammen 

	String buildString(String msg, String replacement1, String replacement2) {

		return msg.replace("{replacement}", replacement1).replace("{replacement2}", replacement2);

	}

// Liest am Anfang alle Systemäußerungen aus Datei ein 

	Map<String, String> readSystemUtterances() {

		Map<String, String> utterances = new HashMap<String, String>();

		try {

			for (String line : IOUtils
					.readLines(this.getClass().getClassLoader().getResourceAsStream("utterances.txt"))) {

				if (line.startsWith("#")) {

					continue;

				}

				String[] parts = line.split("=");

				String key = parts[0].trim();

				String utterance = parts[1].trim();

				utterances.put(key, utterance);

			}

			logger.info("Read " + utterances.keySet().size() + "utterances");

		} catch (IOException e) {

			logger.info("Could not read utterances: " + e.getMessage());

			System.err.println("Could not read utterances: " + e.getMessage());

		}

		return utterances;
	}

// Datenbank für Quizfragen // del

	static String DBName = "WortspielDB.db";

	private static Connection con = null;

// Vorgegebene Methode wird am Anfang einmal ausgeführt, wenn ein neuer Dialog startet: 

// * lies Nutzeräußerungen ein 

// * Initialisiere Variablen 

	@Override

	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope)

	{

		logger.info("Alexa session begins");

		utterances = readSystemUtterances();
		recState = RecognitionState.Answer;
		counter = 0;

	}

// Wir starten den Dialog: 

// * Hole die erste Frage aus der Datenbank 

// * Lies die Welcome-Message vor, dann die Frage 

// * Dann wollen wir eine Antwort erkennen 

	@Override

	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope)

	{
		logger.info("onLaunch");
		return askUserResponse(utterances.get("welcomeMsg"));
	}

// Ziehe eine Wort aus der Datenbank, abhängig vom NiveauID 

	public void selectWord(String l) {

		switch (l) {

		case "leicht": {

			word = new Word("leicht");

			templist = word.selectWord();

			//thisWord = word.checkAnswer(thisWord);

			//Level = "leicht";

			break;

		}

		case "schwer": {

			Word word = new Word("schwer");

			//thisWord = word.selectWord();

			WordID = word.getWordID();

			break;

		}

		case "sehrschwer": {

			Word word = new Word("sehrschwer");

			//thisWord = word.selectWord();

			WordID = word.getWordID();

			break;

		}

		default:

		}

	}
	

// Hier gehen wir rein, wenn der User etwas gesagt hat 

// Wir speichern den String in userRequest, je nach recognition State reagiert das System unterschiedlich 

	@Override

	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope)

	{

		IntentRequest request = requestEnvelope.getRequest();

		Intent intent = request.getIntent();

		userRequest = intent.getSlot("anything").getValue();

		logger.info("Received following text: [" + userRequest + "]");

		logger.info("recState is [" + recState + "]");
//initially
		SpeechletResponse resp = null;
// handling user intents 
		switch (recState) {

		case Answer:
			resp = evaluateAnswer(userRequest);
			break;

		case YesNo:
			resp = evaluateYesNo(userRequest);
			recState = RecognitionState.Answer;
			break;
		case YesNoWiederholung:
			beendet = true;
			resp = evaluateYesNoWiederholung(userRequest);
			recState = RecognitionState.Answer;
			break;
		case YesNoWord:
			resp = evaluateYesNoWord(userRequest);
			recState = RecognitionState.Answer;
			break;
			

		

//case YesNoWiederholung: resp =  evaluateYesNoWiederholung(userRequest); break; create the Method  

		default:
			resp = tellUserAndFinish("Erkannter Text: " + userRequest);

		}

		return resp;

	}

// Ja/Nein-Fragen kommen genau dann vor, wenn wir wissen wollen, ob der User weitermachen will. hier I want to ask that  

// only if the User said  a  wrong answer  or  got the full points of the Game.*** 

// Wenn Ja, stelle die nächste Frage 

// Wenn Nein, nenne die Gewinncounterme und verabschiede den user 
// Auswahl der Worter falls Ja 
	private SpeechletResponse evaluateYesNo(String userRequest) {

		SpeechletResponse res = null;

		recognizeUserIntent(userRequest);

		switch (ourUserIntent) {

		case Yes: {

			selectWord(Level);
			newWord= true;
			//res = askUserResponseQuestion();//
			recState = RecognitionState.Answer;
			break;
			// where is Question defined ?

		}
		case No: {

			res = tellUserAndFinish(buildString(utterances.get("counterMsg"), String.valueOf(counter), "") + " "
					+ utterances.get("goodbyeMsg"));
			break;

		}
		default: {

			res = askUserResponse(utterances.get(""));

		}

		}

		return res;

	}

	
	
private SpeechletResponse evaluateYesNoWiederholung(String userRequest) {
	
	SpeechletResponse res = null;
	
	recognizeUserIntent(userRequest);
	
	switch (ourUserIntent) {
	
	
	case Yes: {
		
		counter = 0;
		createRandList(9, word.getRnlist(), beendet);
		res = askUserResponseQuestion(nextwordlist, 10, 0);

	break;
	
	} default: {
		res = responseWithFlavour(utterances.get("goodbyeMsg"), 0);
		
	res = askUserResponse(utterances.get(""));
	
	
	}
	
	}
	
	return res;
	
	}
	
/*
	private SpeechletResponse askUserResponseYesNoWiederholung(boolean again) {
		SsmlOutputSpeech speech = new SsmlOutputSpeech();

		if (again == true) {
			speech.setSsml("<speak> ok here is your question again " + question
					+ "<audio src=\\\"soundbank://soundlibrary/alarms/beeps_and_bloops/bell_02\\\"/> </speak>");

		} else {

			speech.setSsml(
					"<speak> here is your question <audio src=\"soundbank://soundlibrary/alarms/beeps_and_bloops/bell_02\"/>"
							+ question + "</speak>");
		}
		// reprompt after 8 seconds
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> you still there?</speak>");

		Reprompt rep = new Reprompt();
		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);
	}
/*
	/**
	
	 * 
	 * überprüft User YesNo Antwort, wenn nein wird ein neues Wort Randomisier
	 * ausgewählt und deren Frage gelesen.Wenn Ja liest die Frage vom Aktuellen Wort
	 * nochmal
	 **/

// Evaluation der User Antwort falls Ja  queston ii(Wiederholung) 
	private SpeechletResponse evaluateYesNoWord(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case Yes: {
			
			logger.info("Received following text: [" + userRequest + "] + counter " + counter + "arr size " + templist.size() );
							if(counter < templist.size()) {
				res = askUserResponseQuestion(templist,10,counter++);// Counter 
				recState = RecognitionState.Answer;
				} 
				else {
					recState = RecognitionState.YesNoWiederholung;
				res = responseWithFlavour("Deine Liste ist durch. Wenn du noch einmal spielen möchtest sagen Sie bitte yes oder beenden um das Spiel zu beenden", 0);
				
				}
				break;
			
		}
		case No: {
			res = responseWithFlavour(utterances.get("goodbyeMsg"), 0);
			break;


		}
		case beenden: {
			res = responseWithFlavour(utterances.get("goodbyeMsg"), 0);
			break;
		}
		default: {
			res = responseWithFlavour(utterances.get("errorYesNoMsg"), 0);
		}
		}
		return res;
	}

// Diese Methode ist noch nicht fertig implementiert.
	private SpeechletResponse evaluateAnswer(String userRequest) {

		SpeechletResponse res = null;

		recognizeUserIntent(userRequest);

		switch (ourUserIntent) {

//selectWord(Level); 

		
		case leicht: {
			selectWord("leicht");
			recState = RecognitionState.Answer;
			createRandList(9,word.getRnlist(),beendet);

			res = askUserResponseQuestion(templist,1,1);// Counter 
			break;
		}
		
		/*
		
		case schwer: {

			selectWord("schwer");
			res = askUserResponseQuestion(templist,1,1);// Counter 
			recState = RecognitionState.Answer;
			
			break;
		}
		case sehrschwer: {

			selectWord("sehrschwer");
			res = askUserResponseQuestion(templist,1,1);// Counter 
			recState = RecognitionState.Answer;
			
			break;
		}
		case beenden: {
			res = tellUserAndFinish(buildString(utterances.get("counterMsg"), String.valueOf(counter), "") + " "
					+ utterances.get("goodbyeMsg"));//  is it right ? 
			
			break;
		}
		*/
		case Yes :{
			wordnum = 10;
			createRandList(9,word.getRnlist(),beendet);
			res = askUserResponseQuestion(templist,10, 0);// Counter 
			recState = RecognitionState.Answer;
break;

		} 
		
		case zwanzig :{
			wordnum = 20;
			createRandList(20,word.getRnlist(),beendet);
			res = askUserResponseQuestion(templist,20,1);// Counter 

		} 
		/*
		case dreizig :{
			wordnum = 30;
			createRandList(30,word.getRnlist());
			res = askUserResponseQuestion(templist,30,1);// Counter 

		} 
		
		
		case Verben :{
			if (word.checkAnswer(thisWord) == "Verben") {

				logger.info("User answer recognized as correct.");
				// Counter++;
				//increasecounter();

				res = askUserResponseWord(true);

			}

		} 
*/
		default: {

			
	if (ourUserIntent.equals(UserIntent.Nomen)

			|| ourUserIntent.equals(UserIntent.Verben)

			|| ourUserIntent.equals(UserIntent.Adjektiven)

			|| ourUserIntent.equals(UserIntent.Präpositionen)
			
	) {
		
		
		

		logger.info("User answer =" + ourUserIntent.name().toLowerCase() + "/correct answer="+ word.checkAnswer(thisWord)+"this Word" + thisWord);//

		if (ourUserIntent.name().toLowerCase().equals(word.checkAnswer(thisWord).toLowerCase())) {

			logger.info("User answer recognized as correct.");
			// Counter++;
			//increasecounter();

			res = askUserResponseWord(true);

		}
	 else {
		logger.info("User answer recognized as wrong.");

		res = askUserResponseWord(false);
		 //.
	 }
		}

		}
		}
		return res;
	}



// Achtung, Reihenfolge ist wichtig! 
//WörterAnzahl Eingabe wird hier anerkannt 
	// Fehler Anfällig 
	void recognizeUserIntent(String userRequest) {
	
		userRequest = userRequest.toLowerCase();

		//String pattern1 = "(ich nehme )?(Ich wähle )?(Ich denke )?(Ich vermute )?(antwort )?(Nomen)( bitte)?"; 																			// alone
		String pattern2 = "nochmal";//"(was)?nochmal( bitte)?";
		String pattern3 = "leicht"; //"(Ich nehme )?(Niveau )?(Stufe)?(leicht)(bitte)?";
		String pattern4 = "(Ich nehme )?(Niveau )?(Stufe)?(schwer)(bitte)?";
		String pattern5 = "sehr schwer";//"(Ich nehme )?(Niveau )?(Stufe)?(sehr schwer)(bitte)?";
		String pattern6 = "\\bnein\\b";
		String pattern7 = "\\bja\\b";
		String pattern8 = "(\\bbeenden\\b) (bitte)?";
		String pattern9 = "yes";//"(Ich nehme )?(ich möchte )? zehn(Wörter)?(Worten)?( bitte)?";
		String pattern10 = "zwanzig";//"(Ich nehme )?(ich möchte )? zwanzig(Wörter)?(Worten)?( bitte)?";
		String pattern11 = "dreizig";//"(Ich nehme )?(ich möchte )? dreizig(Wörter)?(Worten)?( bitte)?";
		String pattern12 = "nomen";//"(ich nehme )?(Ich wähle )?(Ich denke )?(Ich vermute )?(antwort )?(Nomen)( bitte)?";
		String pattern13 = "(Ich nehme )?(ein )?(wort)( bitte)?";// noch nicht in Gebrauch
		String pattern14 = "verben"; //(ich nehme )?(Ich wähle )?(Ich denke )?(Ich vermute )?(antwort )?(Verben)( bitte)?";
		String pattern15 ="adjektiven";// "(ich nehme )?(Ich wähle )?(Ich denke )?(Ich vermute )?(antwort )?(Adjektiven)( bitte)?";
		String pattern16 = "präpositionen";//"(ich nehme )?(Ich wähle )?(Ich denke )?(Ich vermute )?(antwort )?(Präpositionen)( bitte)?";
		//Pattern p1 = Pattern.compile(pattern1);
		//Matcher m1 = p1.matcher(userRequest);
		Pattern p2 = Pattern.compile(pattern2);
		Matcher m2 = p2.matcher(userRequest);
		Pattern p3 = Pattern.compile(pattern3);
		Matcher m3 = p3.matcher(userRequest);
		//Pattern p4 = Pattern.compile(pattern4);
		//Matcher m4 = p4.matcher(userRequest);
		//Pattern p5 = Pattern.compile(pattern5);
		//Matcher m5 = p5.matcher(userRequest);
		//Pattern p6 = Pattern.compile(pattern6);
		//Matcher m6 = p6.matcher(userRequest);
		//Pattern p7 = Pattern.compile(pattern7);
		//Matcher m7 = p7.matcher(userRequest);
		//Pattern p8 = Pattern.compile(pattern8);
		//Matcher m8 = p8.matcher(userRequest);
		Pattern p9 = Pattern.compile(pattern9);
		Matcher m9 = p9.matcher(userRequest);
		Pattern p10 = Pattern.compile(pattern10);
		Matcher m10 = p10.matcher(userRequest);
//		Pattern p11 = Pattern.compile(pattern11);
//		Matcher m11 = p11.matcher(userRequest);
		Pattern p12 = Pattern.compile(pattern12);
		Matcher m12 = p12.matcher(userRequest);
//		Pattern p13 = Pattern.compile(pattern13);
//		Matcher m13 = p13.matcher(userRequest);
		Pattern p14 = Pattern.compile(pattern14);
		Matcher m14 = p14.matcher(userRequest);
		Pattern p15 = Pattern.compile(pattern15);
		Matcher m15 = p15.matcher(userRequest);
		Pattern p16 = Pattern.compile(pattern16);
		Matcher m16 = p16.matcher(userRequest);



		if (m2.find()) {

			ourUserIntent = UserIntent.nochmal; 

		} else if (m3.find()) {

			ourUserIntent = UserIntent.leicht;}

//		} else if (m4.find()) {
//
//			ourUserIntent = UserIntent.schwer;
//
//		} else if (m5.find()) {
//
//			ourUserIntent = UserIntent.sehrschwer;
//
//		} else if (m6.find()) {
//
//			ourUserIntent = UserIntent.Yes;
//
//		} else if (m7.find()) {
//			ourUserIntent = UserIntent.No;
//		} else if (m8.find()) {
//			ourUserIntent = UserIntent.beenden;
//
//		}
			else if (m9.find()) {
			ourUserIntent = UserIntent.Yes;
		} else if (m10.find()) {
		    ourUserIntent = UserIntent.zwanzig;
//		} else if (m11.find()) {
//			ourUserIntent = UserIntent.dreizig;
	} else if (m12.find()) {
		ourUserIntent = UserIntent.Nomen;	} 
	
	
	//else if (m13.find()) {
//			ourUserIntent = UserIntent.Word;
//		}
			else if (m14.find()) {
			ourUserIntent = UserIntent.Verben;//
		} 
 
			
		else if (m15.find()) {
		ourUserIntent = UserIntent.Adjektiven;
		} else if (m16.find()) {
			ourUserIntent = UserIntent.Präpositionen;
		}
		logger.info("set ourUserIntent to " + ourUserIntent);

	}

	@Override

	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope)

	{

		logger.info("Alexa session ends now");

	}

	/**
	 * 
	 * Tell the user something - the Alexa session ends after a 'tell'
	 * 
	 */

	private SpeechletResponse tellUserAndFinish(String text)

	{

// Create the plain text output. 

		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

		speech.setText(text);

		return SpeechletResponse.newTellResponse(speech);

	}

	/**
	 * 
	 * A response to the original input - the session stays alive after an ask
	 * request was send.
	 * 
	 * have a look on
	 * https://developer.amazon.com/de/docs/custom-skills/speech-synthesis-markup-language-ssml-reference.html
	 * 
	 * @param text
	 * 
	 * @return
	 * 
	 */

	private SpeechletResponse askUserResponse(String text) {

		SsmlOutputSpeech speech = new SsmlOutputSpeech();

		speech.setSsml("<speak>" + text + "  <audio src=\"soundbank://soundlibrary/alarms/beeps_and_bloops/bell_02\"/> </speak>");

// reprompt after 8 seconds 
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();

		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> bist du noch da?</speak>");

		Reprompt rep = new Reprompt();

		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);

	}
	
	/*
	 * Responding to the UserAnswer in case rivhtig oder Falsch.
	 * 
	 * 
	 * 
	 * */

	private SpeechletResponse askUserResponseWord(boolean correct) {

		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		if (correct == true) {
			punkte++;
			speech.setSsml("<speak>" + thisWord +" gehört zur Gruppe" +word.getWortgruppe() +" du hast insgesamt" + punkte
					+ " Punkte erreicht. möchtest du  ein anderes  Wort ?</speak>");
			recState = RecognitionState.YesNoWord;


		} else {
			speech.setSsml("<speak>das war leider Falsch möchtest du nochmal hören ? oder weiter machen</speak>");
		}

// reprompt after 8 seconds 

		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();

		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> bist du noch da?</speak>");

		Reprompt rep = new Reprompt();

		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);


		/****************************************************/ // Alexa handling responses and calling method
																// evaluateYesNoQuestion (Wiederholug)

	}
	
	
	
	public void createRandList (int numofwords,ArrayList<String> List5,boolean nextWordbool){		
		templist = new ArrayList<String>();
		nextwordlist = new ArrayList<String>();
		Random random = new Random();
		int ran;
		int max = 80;
        int min = 1; 
	    String test; 
		int index = 0;
	    if(nextWordbool == false) {
	    for( String S : List5) {
			if(templist.size() <= numofwords)
			{
				ran = (int)(random.nextInt((max - min) + 1) + min);
				test = List5.get(ran);
				templist.add(test);
				
				//System.out.println("*************************************" + test);
			}
	    }
			
		} else {
			for( String S : templist) {
				if(S != List5.get(index))
				{
					
					nextwordlist.add(List5.get(index));
					
					//System.out.println("*************************************" + test);
				}
				
				
				index++;
		    }
			
			
		}
		//DatenbankTest dt = new DatenbankTest();
		//dt.testFunc(templist);
			
	}
	
	

	/// read till all questions are reached  String 
	private SpeechletResponse askUserResponseQuestion(ArrayList<String> rnq, int counter, int index) {

		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		
		//countershouldbe deleted. 
		if (counter == 10) {
		//wordnum = 10;
			
		
		speech.setSsml("<speak>  zu welcher Wortgruppe gehört das Wort "+ rnq.get(index) + " </speak>");
	
		
		thisWord =  rnq.get(index);
		logger.info("this word has "+thisWord);
		}
		/*
		else if (counter ==20) {
			
		wordnum = 20;
		speech.setSsml("<speak> <voice name=\"Brian\"> zu welcher Wortgruppe gehört das Wort "
				+ word + " </voice> </speak>");
		}
		else if (counter ==30) {
		wordnum = 30;
		speech.setSsml("<speak> <voice name=\"Brian\"> zu welcher Wortgruppe gehört das Wort "
				+ word + " </voice> </speak>");
		

	} else 	if (counter == wordnum) {

			speech.setSsml("<speak> <voice name=\"Brian\"> zu welcher Wortgruppe gehört das Wort Besoo  </voice> </speak>");
			
			/*speech.setSsml(

					"<speak> alle fragen durch <audio src=\"soundbank://soundlibrary/alarms/beeps_and_bloops/bell_02\"/> </speak>");*/

		//} 
		
		else {



			speech.setSsml(	"<speak> mit wie vielen Wörter möchtest du anfangen 10 20 30? </speak>");

		}
			
// reprompt after 8 seconds 
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> noch da?</speak>");
		Reprompt rep = new Reprompt();
		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);

	}

	/**
	 * 
	 * formats the text in weird ways
	 * 
	 * @param text
	 * @param i
	 * @return
	 */
// einpaar Anpassungen Fehlen.
	private SpeechletResponse responseWithFlavour(String text, int i) {

		SsmlOutputSpeech speech = new SsmlOutputSpeech();

		switch (i) {

		case 0:

			speech.setSsml("<speak><amazon:effect name=\"whispered\">" + text + "</amazon:effect></speak>");
			

			break;

		case 1:

			speech.setSsml("<speak><emphasis level=\"strong\">" + text + "</emphasis></speak>");

			break;

		case 2:

			String firstNoun = "erstes Wort buchstabiert";

			String firstN = text.split(" ")[3];

			speech.setSsml(
					"<speak>" + firstNoun + "<say-as interpret-as=\"spell-out\">" + firstN + "</say-as>" + "</speak>");

			break;

		case 3:

			speech.setSsml(
					"<speak><audio src='soundbank://soundlibrary/transportation/amzn_sfx_airplane_takeoff_whoosh_01'/></speak>");

			break;

		default:

			speech.setSsml("<speak><amazon:effect name=\"whispered\">" + text + "</amazon:effect></speak>");

		}

		return SpeechletResponse.newTellResponse(speech);

	}

}