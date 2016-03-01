/* Ontwortelde Boom
** versie 0.3
** 
** uitgangspunt: Kerstboom02.2
** maar geschikt voor app: ontwortelde boom dat weer gebaseerd is op BTChat.
** Betere BT communicatie
** gelijk aan GPRS_MEGA10.0
** maar nu met twee pompen en de kraan wordt afzonderlijk bedient via een vlotterschakelaar en een accu
** aan de boom zitten drie druppelsensoren. Hiermee kan bepaald worden of er een sensor stuk is.
** De overige sensoren blijven hetzelfde
** versie 02 PompinGebruik ge�ntroduceerd.
** versie 02.1	softwarematige reset toegevoegd
** versie 02.2	verwerksensoren aangepast om kapotte sensor vast te stellen
** versie ontwortelde boom:
** vaste gegevens opslaan in EPROM en doorsturen naar Android
** versie 0.3
** SMS gegevens nu zichtbaar in Android app
** Testsignaal aangepast: nu slechts ��n melding per dag
** In Laagwater.ino wordt info verzonden om de voortgang van de delay in Android te tonen
** In DuoPompRegeling wordt info verzonden om sensordelay in Android te tonen
** versie 0.4
** Methode om vast te stellen of sensor stuk is, is buiten werking gesteld.
** Er wordt alleen een Droog signaal gegeven als is vastgesteld dat er minstens twee sensoren
** droog staan. LeesRaindropSensor is vervangen door LeesRaindropSensoropDroog
**
**
** Credit: The following example was used as a reference
** Rui Santos: http://randomnerdtutorials.wordpress.com
**
** aangepast door BDR
** datum: 20151120
** voor project ontwortelde kerstboom
**
**
*/
#include <MemoryFree.h>
#include <SD.h>				// SDcard lib
#include <SPI.h>
#include <EEPROM.h>
#include <Wire.h>
#include "RTClib.h"
#include "DHT.h"			// Temp/Hum library
#include <SFE_TSL2561.h>	// lightsensor lib
#include <Streaming.h>
#include "RunningAverage.h"	// running average
#include <avr/wdt.h>		// tbv softwarematige reset

String SMScode08 = "De bak van de ontwortelde boom is leeg. HELP!";
String SMScode10 = "Pomp 1 van ontwortelde boom is uitgeschakeld!";
String SMScode11 = "Pomp 2 van ontwortelde boom is uitgeschakeld!";
String SMScode12 = "Testbericht Ontwortelde boom";
String SMScode13 = "Alle sensoren van ontwortelde boom zijn stuk!";

#define LOG_T_INTERVAL  60000 // Temp/Hum interval 60 sec
#define LOG_L_INTERVAL  30000  // Light interval 30 sec
#define LOG_R_INTERVAL  600000 // Raindrop interval 10 min
uint32_t LOG_LL_INTERVAL = 0;  // SMS LaagWater interval bij start 0
uint32_t LOG_RD_INTERVAL = 0;   // SMS Raindrop interval bij start 0
uint32_t syncTimeT = 0; // time of last sync() Temp en Hum
uint32_t syncTimeL = 0; // time of last sync() Light
uint32_t syncTimeR = 0; // time of last sync() Raindrop
uint32_t syncTimeLL = 0; // time of last SMS LaagWater
uint32_t syncTimeRD = 0; // time of last SMS Raindrop

RTC_DS1307 RTC; // define the Real Time Clock object
const int chipSelect = 53; // 10 For Adafruit card, 53 for MEGA
File logfile; // the logging file
char filename[] = "LOGGER00.CSV";
boolean SDActionS = false;  // SD card failed
boolean SDActionF = false;  // File failed

//------------------------------------------------------------------------------
// call back for file timestamps
// Hiermee wordt de datum aan de nieuwe file gekoppeld
void dateTime(uint16_t* date, uint16_t* time) {
	DateTime now = RTC.now();

	// return date using FAT_DATE macro to format fields
	*date = FAT_DATE(now.year(), now.month(), now.day());

	// return time using FAT_TIME macro to format fields
	*time = FAT_TIME(now.hour(), now.minute(), now.second());
}
//------------------------------------------------------------------------------

DateTime now;  // now is een DataTime object

SFE_TSL2561 light; // Create an SFE_TSL2561 object, here called "light":

const int DHTPIN = 2; // gele draad aan pin digital 2
#define DHTTYPE DHT22 // de sensor van Daan
//#define DHTTYPE DHT11  // mijn sensor
//#define DHTTYPE DHT21  // weer een andere sensor
DHT dht(DHTPIN, DHTTYPE); // dht is DHT object

const int VlotterLaag = 8;      // pin 8 is alarmniveau: geeft signaal als HIGH wordt gemeten
const int Pomp1 = 9;			// pin 9 is aansturen pomp 1
const int Pomp2 = 10;			// Pin 10 is aansturen pomp 2
int Pig = Pomp1;				// Starten met PominGebruik = pomp1

const int druppel1 = 0;			// Pin A0 is uitlezen druppelsensor1
const int druppel2 = 1;			// Pin A1 is uitlezen druppelsensor2
const int druppel3 = 2;			// Pin A2 is uitlezen druppelsensor3

// Globale waarde pompregeling
int PompStatus = 0;				// toestand van de Pompstatus
int PompStatusoud = 0;			// de vorige Pompstatus
boolean sw_laagwater = true;	// begin met laagwater
boolean laagwateroud;			// vorige meting laagwater
int laagwater_delay = 0;	// tijdsvertraging in laagwater om dender te voorkomen (10000)
uint32_t looptijdLL = 0;		// loopt tijdens het testen van de laagwatervlotter

// Globale waarde humidity en temperatuur
float h, t, f;    // humidity, temp C, temp f
char buffer[10];   // buffer voor float to char omzetting
String Hum, TempC;
char inChar = 0;

// Global variables voor Light
boolean gain = false;    // Gain setting, 0 = X1, 1 = X16;
unsigned int ms;  // Integration ("shutter") time in milliseconds
double lux;    // Resulting lux value
boolean good;  // True if neither sensor is saturated
String Light;

// Global variables voor Raindrop

int ra = 20;				// aantal samples voor running average
RunningAverage RA1(ra);		// create object voor running average sensor waarde 1
RunningAverage RA2(ra);
RunningAverage RA3(ra);

int Rain1;		// Uitgelezen waarde raindrop sensor 1
int Rain2;
int Rain3;
int RIG = 1;	// Rain sensor in gebruik
int Rain1Gem;	// gemiddelde waarde Rain1
int Rain2Gem;
int Rain3Gem;

// variabelen voor sensorcontrole
int Teller = 0;					// aantal malen dat geteld is
int FoutTeller1 = 0;			// aantal malen dat een fout geteld is voor sensor 1
int FoutTeller2 = 0;
int FoutTeller3 = 0;

boolean Rain1Pos = false;
boolean Rain2Pos = false;
boolean Rain3Pos = false;

boolean Rain1_stuk = false;	// geeft aan of druppelsensor 1 stuk is
boolean Rain2_stuk = false;	// geeft aan of druppelsensor 2 stuk is
boolean Rain3_stuk = false;	// geeft aan of druppelsensor 3 stuk is
boolean Rain_SMS_Gestuurd = false;	// stuur slechts ��n keer een SMS als alle sensoren stuk zijn

int Drooglevel1 = 0;	// onder deze waarde staat er geen of te weinig water op de sensor(50). Via BT aan te passen.
int Drooglevel2 = 0;
int Drooglevel3 = 0;

boolean Droog = true;			// variable om droog vast te stellen
uint32_t Droogtijd = 0;			// tijd om druppelsensoren nat te laten worden; van buiten instelbaar (20000)
uint32_t DroogtijdLL = 0;		// loopt tijdens het testen van de Droogtijd
int Druppelspeling = 0;			// toegestane verschil rond het gemiddelde (5). Via BT instelbaar

// Global variable for SMS yes or no
char SMScode = '0';			//stuur sms bij alarmsituaties, default uit
String telefoonnummer = "";
boolean bericht_gestuurd = false;	// om te voorkomen dat sms testbericht meer dan ��n per minuut gestuurd wordt
const int Simpower = 7;		// voor de sim900 kaart Shield B-v1.1
// const int Simpower = 9;  // voor de "oude" Sim900 kaart

void setup() {
	
	pinMode(Pomp1, OUTPUT);
	digitalWrite(Pomp1, LOW);		// zet pomp1 uit. De pompen zijn active LOW
	pinMode(Pomp2, OUTPUT);
	digitalWrite(Pomp2, LOW);		// zet pomp2 uit

	laagwateroud = digitalRead(VlotterLaag);	// lees de beginstand van de vlotter
	
	Serial.begin(9600);			// output via serial monitor
	Serial1.begin(19200);		// connection to GPRS network
	Serial3.begin(19200);	// Default connection rate 
	dht.begin();

	// initialiseer de Running Average
	RA1.clear();
	RA2.clear();
	RA3.clear();

	// initialize the SD card
	// make sure that the default chip select pin is set to
	// output, even if you don't use it:
	pinMode(chipSelect, OUTPUT);

	// set date time callback function for date time on disk
	SdFile::dateTimeCallback(dateTime);

	// see if the card is present and can be initialized:
	if (SD.begin(chipSelect)) SDActionS = true;  // SD card herkend

	// create a new file
	for (uint8_t i = 0; i < 100; i++) {
		filename[6] = i / 10 + '0';
		filename[7] = i % 10 + '0';
		if (!SD.exists(filename)) { // only open a new file if it doesn't exist
			logfile = SD.open(filename, FILE_WRITE);
			break;  // leave the loop!
		}
	}
	if (logfile) SDActionF = true; // file aangemaakt

	Wire.begin();
	if (!RTC.begin()) {
		logfile.println("RTC failed");
	}
	WriteSDcard1("00");
	logfile.println(F("0,0,Bestand aangemaakt"));
	logfile.flush();

	light.begin();	// Initialize the SFE_TSL2561 library
	unsigned char time = 2;
	light.setTiming(gain, time, ms);
	light.setPowerUp();

	// Vaste gegevens uit EPROM ophalen
	/* EPROM plaats
	0 = SMS code ja/nee = 1/0
	1 - 10 = telefoonnummer
	11 - 14 = Drooglevel1
	15 - 18 = Drooglevel 2
	19 - 22 = Drooglevel3
	23 - 26 = Droogtijd in sec
	27 - 30 = Druppelspeling
	31 - 34 = Samples
	35 - 38 = Laagwater_delay in sec
	*/

	SMScode = EEPROM.read(0); // SMScode ophalen uit EPROM op plaats 0
	telefoonnummer = "";
	telefoonnummer = LeesEprom(1, 10);
	Drooglevel1 = LeesEprom(11, 14).toInt();
	Drooglevel2 = LeesEprom(15, 18).toInt();
	Drooglevel3 = LeesEprom(19, 22).toInt();
	Droogtijd = LeesEprom(23, 26).toInt()*1000;
	Druppelspeling = LeesEprom(27, 30).toInt();
	int Samples = LeesEprom(31, 34).toInt();	// TODO later kijken of dit wel kan
	laagwater_delay = LeesEprom(35, 38).toInt()*1000;


	//Serial.println(Drooglevel1);
	//StuurBericht("12");	// ter test

	ReactieOpy();	// stuur bepaalde berichten opnieuw
	Sendkode29(laagwater_delay, laagwater_delay);	// stuur de status "100" om progressbar uit te zetten
	Sendkode30(Droogtijd, Droogtijd);	// stuur de status 100% om de progressbar uit te zetten

	
}  // einde Setup

void loop() {

	DuoPompRegeling();	// regelt met twee pompen
						
	LeesHumTemp();     // lees Humidity en temperatuur
					   
	LeesLight();       // lees de lichtopbrengst
					   
	// LeesRaindropSensor();	// Lees de raindrop sensoren
	// routine vervangen door onderstaande
	LeesRaindropSensoropDroog();  // zijn vervanger

	// ControleerSensoren();	// controleer of de sensoren in orde zijn
 // routine uitgezet omdat met andere filosofie gewerkt wordt
	
	LaagWater();	// routine om de laagwatervlotter uit te lezen
					
	TestSignaal();     // Stuurt een teken van leven naar SMS
					   
	ReadBT();		// Lees de BlueTooth input
					
	SendBT();		// Zend info naar Android toestel	

}  //einde loop

void TestSignaal() {    // Stuurt een teken van leven naar SMS

						// stuur iedere dag om 12:00 uur een Testbericht

	if (now.hour() == 12 && now.minute() == 00) {
		if (!bericht_gestuurd) {
			StuurBericht("12");
			bericht_gestuurd = true;
		}
	}
	else bericht_gestuurd = false;

}  // einde TestSignaal

int freeRam() {
	extern int __heap_start, *__brkval;
	int v;
	return (int)&v - (__brkval == 0 ? (int)&__heap_start : (int)__brkval);
}

void softwareReset(uint8_t prescaller) {
	// start watchdog with the provided prescaller
	wdt_enable(prescaller);
	// wait for the prescaller time to expire
	// without sending the reset signal by using
	// the wdt_reset() method
	while (1) {}
}

String LeesEprom(int b, int e) {
	String result = "";
	int x;
	for (int i = b; i < e + 1; i = i + 1) {
		x = EEPROM.read(i);  // in x staat een ascii cijfer
		result = result + String(x - 48); // en nu staat er een getalstring
	}
	return result;
}




