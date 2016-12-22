/*	Hier worden de Raindropsensoren uitgelezen
**	Uitgelezen waarde = 0 wil zeggen: drijfnat
**	Uitgelezen waarde hoog wil zeggen: droog
*/

void LeesRegenSensor() {		// de routine om regensensor RG-11 uit te lezen
	if (digitalRead(RG_sensor) == HIGH) {	// dan komt er geen water op de sensor
		Droog = true;
		SchrijfRaindrop(Droog);
		Serial.println("RG-11 is: DROOG ");
	}
	else		// er staat water op de sensor
	{
		Droog = false;
		SchrijfRaindrop(Droog);
		Serial.println("RG-11 is: NAT ");
	}
	

}	// einde LeesRegenSensor


void SchrijfRaindrop(int R1) {	// schrijf waarde raindrop in tussenposen van R_INTERVAL naar SD card
	if ((millis() - syncTimeL) < LOG_R_INTERVAL) return;  // de intervaltijd is nog niet verstreken
	syncTimeL = millis();
	WriteSDcard1("04");
	logfile.println(R1);
	
	logfile.flush();
}



