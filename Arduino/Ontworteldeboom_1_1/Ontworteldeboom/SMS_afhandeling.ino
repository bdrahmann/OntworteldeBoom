/*	Afhandeling SMS en schrijven naar SD card

code	omschrijving				SMS	SD
00		Bestand aangemaakt			x	00
01
02		temp,hum					x	02
03
04		raindropwaarde 1			x	04
05		
06		
07		
08		bak is leeg					08	08
09		light						09	09	TODO wordt dit nog gebruikt?
10		Pomp 1 uitgeschakeld		10	10
11		Pomp 2 uitgeschakeld		11	11
12		testsignaal gestuurd		12	12
13		


*/
void StuurBericht(String kode) {
	String SMSstatus = "SMS gaat verstuurd worden!";
	if (SMScode == '1') { // stuur SMS
		Serial3.print("27" + SMSstatus + "#");
		// TODO dit moet mooier met een array
		if (kode == "08") Serial3.print("28" + SMScode08 + "#");	// zet SMS bericht op Android
		if (kode == "10") Serial3.print("28" + SMScode10 + "#");
		if (kode == "11") Serial3.print("28" + SMScode11 + "#");
		if (kode == "12") Serial3.print("28" + SMScode12 + "#");
		if (kode == "13") Serial3.print("28" + SMScode13 + "#");

		SIM900power();  // schakel SIM in
		SMSstatus = "Log on to netwerk, 10 sec delay";
		Serial.println(SMSstatus);
		Serial3.print("27" + SMSstatus + "#");
		delay(10000);   // time to log on to the network of 20000?
		sendSMS(kode);      // stuur SMS bericht
		SIM900power();  // schakel SIM uit
		Serial3.print("27 #");		// zet SMS status uit
		Serial3.print("28 #");		// zet SMS bericht op Android uit
	}

	WriteSDcard1(kode);  // later aanpassen aan wie het verstuurd is
	if (kode == "08") logfile.println(F("0,0,LL SMS gestuurd"));
	if (kode == "10") {
		if (SMScode == '1') logfile.println(F("0,0,Pomp1 uit: SMS gestuurd"));
		else logfile.println(F("0,0,Pomp1 uit: geen SMS gestuurd"));
	}
	if (kode == "11") {
		if (SMScode == '1') logfile.println(F("0,0,Pomp2 uit: SMS gestuurd"));
		else logfile.println(F("0,0,Pomp2 uit: geen SMS gestuurd"));
	}
	if (kode == "12") logfile.println(F("0,0,Testmessage SMS gestuurd"));
	if (kode == "13") {
		if (SMScode == '1') logfile.println(F("0,0,Alle Sensoren stuk: SMS gestuurd"));
		else logfile.println(F("0,0,Alle sensoren stuk: geen SMS gestuurd"));
	}
	logfile.flush();

}  // einde StuurBericht

void SIM900power() { // equivalent of pressing the GSM shield "power" button
	String SMSstatus = "Belmodule schakelen, 6 sec delay.";
	Serial.println(SMSstatus);
	Serial3.print("27" + SMSstatus + "#");
	digitalWrite(Simpower, HIGH);
	delay(1000);
	digitalWrite(Simpower, LOW);
	delay(5000);
	SMSstatus = "Belmodule schakelen is klaar.";
	Serial.println(SMSstatus);
	Serial3.print("27" + SMSstatus + "#");
}

void sendSMS(String kode) {
	String SMSstatus = "";
	SMSstatus = "Arduino belt";
	Serial.println(SMSstatus);
	Serial3.print("27" + SMSstatus + "#");
	String slash = "\"";
	String beginString = "AT + CMGS = ";
	String totaalString = beginString + slash + telefoonnummer + slash;

	Serial1.print("AT+CMGF=1\r"); // AT command to send SMS message
	delay(100);
	Serial1.println(totaalString);

	//Serial1.println("AT + CMGS = \"+31653169253\""); // Bernard mobile number, in international format
	delay(100);
	if (kode == "08") {		// lege bak
		Serial1.println(SMScode08);
		Serial.println(SMScode08);
	}
	if (kode == "10") {		// Pomp1 uitgeschakeld
		Serial1.println(SMScode10);
		Serial.println(SMScode10);
	}
	if (kode == "11") {		// Pomp2 uitgeschakeld
		Serial1.println(SMScode11);
		Serial.println(SMScode11);
	}
	if (kode == "12") {		// Testbericht
		Serial1.println(SMScode12);
		Serial.println(SMScode12);
	}
	if (kode == "13") {		// alle sensoren zijn stuk
		Serial1.println(SMScode13);
		Serial.println(SMScode13);
	}
	delay(100);
	Serial1.println((char)26);  // End AT command with a ^Z, ASCII code 26
	delay(100);
	Serial1.println();
	delay(5000);            // give module time to send SMS
	Serial.print("Gebeld naar: ");
	Serial.println(totaalString);
}



