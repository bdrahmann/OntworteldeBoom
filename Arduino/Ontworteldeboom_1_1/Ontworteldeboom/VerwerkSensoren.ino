/*	Hier worden de Raindropsensoren uitgelezen
**	Uitgelezen waarde = 0 wil zeggen: drijfnat
**	Uitgelezen waarde hoog wil zeggen: droog
**
** aanpassing 20170330:
** als PIG==1 en in staus 4 dan pas droog melden als sensor gedurende 12 minuten droog staat
**
*/

void LeesRegenSensor() {		// de routine om regensensor RG-11 uit te lezen
	if (digitalRead(RG_sensor) == HIGH) {	// dan komt er geen water op de sensor
		Serial.println("sensor staat droog");
		if (PompStatus == 4 && Pig == Pomp1) {	// de aanpassing zoals hierboven beschreven
			Serial.println("droog, status 4 en Pig = 1 ");
			if (!sw_Droog) {
				Serial.println("de switch stond uit ");
				sw_Droog = true;			// set de switch
				Droog = false;				// trucje
				syncTimeDroog = millis();	// start de looptijd
			}
			else {	// de tijd loopt al
				Serial.println("de switch stond aan ");
				if ((millis() - syncTimeDroog) > LOG_D_INTERVAL) {	// de tijd is om
					Serial.println("de tijd is om ");
					sw_Droog = false;
					Droog = true;
					SchrijfRaindrop(Droog);
					Serial.println("RG-11 is: DROOG in status 4 met pomp 1 ");
				}
				else {
					Serial.println(" de tijd loopt nog");
					Serial.print("de wachttijd is:  "); Serial.println((millis() - syncTimeDroog)/1000);
					// do nothing
				}
					
			}
		}
		else {	// de oorspronkelijke code
			Serial.println(" de oorspronkelijke code");
			Droog = true;
			SchrijfRaindrop(Droog);
			Serial.println("RG-11 is: DROOG ");
		}
	}
	else		// er staat water op de sensor
	{
		sw_Droog = false;// reset de Droog parameters
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



