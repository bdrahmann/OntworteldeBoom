/* Ontwortelde Boom
** versie 1.1
** 
** 20170330
** 
** Uitgangspunt is de Ontwortelde Boom zoals die in juli 2016 in Velp draait.
** Probleem is echter daar de regensensoren. Die werken na een week al niet meer goed.
** Daarom in de USA de Hydreon RG-11 aangeschaft.
**
** Eerst de overbodige sensoren eruit gehaald
** 20170330 gebasseerd op versie 1.0
** aanpassing tbv aan/uit regeling op pomp1 12 minuten vertraging
** aanpassing in verwerksensoren.ino
**
** aanpassing op 20180430:
** via GPRS status pomp 2 uitlezen
** via GPRS arduino resetten
**
** Credit: The following example was used as a reference
** Rui Santos: http://randomnerdtutorials.wordpress.com
**
** aangepast door BDR
** datum: 20151120
** 
** wijziging om github te testen
** versie van augustus 2018
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
String SMScode10 = "RG-11 sensor droog; Pomp 1 van ontwortelde boom is uitgeschakeld!";
String SMScode11 = "RG-11 sensor droog; Pomp 2 van ontwortelde boom is uitgeschakeld!";
String SMScode12 = "Testbericht Ontwortelde boom";
String SMScode13 = "Alle sensoren van ontwortelde boom zijn stuk!";
String SMScode14 = "Status pomp2 = AAN";
String SMScode15 = "Status pomp2 = UIT";
String SMScode16 = "Arduino BOOM wordt gereset";

#define LOG_T_INTERVAL  60000 // Temp/Hum interval 60 sec
#define LOG_L_INTERVAL  30000  // Light interval 30 sec
#define LOG_R_INTERVAL  60000 // Raindrop interval 10 min
#define LOG_D_INTERVAL  60000	// Droog interval 12 minuten

uint32_t LOG_LL_INTERVAL = 0;  // SMS LaagWater interval bij start 0
//uint32_t LOG_RD_INTERVAL = 0;   // SMS Raindrop interval bij start 0
uint32_t syncTimeT = 0; // time of last sync() Temp en Hum
uint32_t syncTimeL = 0; // time of last sync() Light
//uint32_t syncTimeR = 0; // time of last sync() Raindrop
uint32_t syncTimeLL = 0; // time of last SMS LaagWater
//uint32_t syncTimeRD = 0; // time of last SMS Raindrop
uint32_t syncTimeDroog = 0;	// time of last Droog
boolean sw_Droog = false;	// sw for first Droog

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
const int Pomp1 = 3;			// pin 9 is aansturen pomp 1
const int Pomp2 = 10;			// Pin 10 is aansturen pomp 2
int Pig = Pomp1;				// Starten met PominGebruik = pomp1

// Globale waarde pompregeling
int PompStatus = 0;				// toestand van de Pompstatus
int PompStatusoud = 0;			// de vorige Pompstatus
int handpomp1 = 0;		// geeft aan of pomp1 in handmatige status uit/aan is
int handpomp2 = 0;		// geeft aan of pomp2 in handmatige status uit/aan is
boolean sw_laagwater = true;	// begin met laagwater
boolean laagwateroud;			// vorige meting laagwater
uint32_t laagwater_delay = 0;	// tijdsvertraging in laagwater om dender te voorkomen (10000)
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
const int RG_sensor = 11;		// regensensor RG-11

// variabelen voor sensorcontrole

boolean Rain_SMS_Gestuurd = false;	// stuur slechts ��n keer een SMS als alle sensoren stuk zijn

boolean Droog = true;			// variable om droog vast te stellen
uint32_t Droogtijd = 0;			// tijd om druppelsensoren nat te laten worden; van buiten instelbaar (20000)
uint32_t DroogtijdLL = 0;		// loopt tijdens het testen van de Droogtijd

// Global variable for SMS yes or no
char SMScode = '0';			//stuur sms bij alarmsituaties, default uit
String telefoonnummer = "";
boolean bericht_gestuurd = false;	// om te voorkomen dat sms testbericht meer dan ��n per minuut gestuurd wordt
// const int Simpower = 7;		// voor de sim900 kaart Shield B-v1.1
const int Simpower = 9;			// voor de "oude" Sim900 kaart en de KEYESTUDIO kaart
String textOpnieuw = "";	// tekst voor start GPRS module
String textMessage = "";	// input en output voor GPRS
String SMSstatus = "";

void setup() {
	String PS;		// is de PrintString
	
	pinMode(Pomp1, OUTPUT);
	digitalWrite(Pomp1, LOW);		// zet pomp1 uit. De pompen zijn active LOW
	pinMode(Pomp2, OUTPUT);
	digitalWrite(Pomp2, LOW);		// zet pomp2 uit

	pinMode(RG_sensor, INPUT);		// digital Pin to INPUT for the RG-11 sensor
	
	laagwateroud = digitalRead(VlotterLaag);	// lees de beginstand van de vlotter
	
	Serial.begin(9600);			// output via serial monitor
	Serial1.begin(19200);		// connection to GPRS network
	Serial3.begin(19200);		// Default connection rate BT
	Serial.println();
	PS = "In setup is pomp 1 uitgezet. In setup is pomp 2 uitgezet"; Serial.println(PS);
	dht.begin();

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
	11 - 14 = Droogtijd in sec
	15 - 18 = Laagwater_delay in sec
	*/
	
	SMScode = EEPROM.read(0); // SMScode ophalen uit EPROM op plaats 0
	telefoonnummer = "";
	telefoonnummer = LeesEprom(1, 10);
	Serial.print("telefoonnummer uit EPROM =  "); Serial.println(telefoonnummer);
	Droogtijd = LeesEprom(11, 14).toInt()*1000;
	laagwater_delay = LeesEprom(15, 18).toInt()*1000;
	
	// start GPRS module en log on
	// Automatically turn on the shield
	startModem(textOpnieuw);

	if (textMessage.indexOf("DOWN") >= 0) {	// als modem uit is gezet
		Serial.println("modem is uitgezet. Opnieuw opstarten");
		textOpnieuw = "opnieuw ";
		startModem(textOpnieuw);
	}

	// Give time to your GSM shield log on to network
	Serial.print("GPRS modem logt on...");
	delay(20000);
	Serial.println("GPRS modem ready...");
	SMSstatus = "verbonden met netwerk";
	Serial.println(SMSstatus);
	Serial3.print("27" + SMSstatus + "#");
	Serial.println("modem wordt in SMS mode gezet");
	// AT command to set Serial1 to SMS mode
	Serial1.print("AT+CMGF=1\r");
	delay(100);
	// Set module to send SMS data to serial out upon receipt 
	Serial1.print("AT+CNMI=2,2,0,0,0\r");
	delay(100);
	textMessage = Serial1.readString();
	Serial.print("textMessage: ");
	Serial.println(textMessage);
	delay(10);

	
	ReactieOpy();	// stuur bepaalde berichten opnieuw
	Sendkode29(laagwater_delay, laagwater_delay);	// stuur de status "100" om progressbar uit te zetten
	Sendkode30(Droogtijd, Droogtijd);	// stuur de status 100% om de progressbar uit te zetten

	
}  // einde Setup

void startModem(String opnieuw) {
	Serial.print("GPRS modem wordt " + opnieuw + "gestart...");
	digitalWrite(9, HIGH);
	delay(1000);
	digitalWrite(9, LOW);
	delay(5000);
	Serial.println("GPRS modem is " + opnieuw + "gestart...");
	textMessage = Serial1.readString();
	Serial.print("textMessage: ");
	Serial.println(textMessage);
	delay(10);
}

void loop() {

	LeesSMS();			// kijk of er sms'jes gestuurd zijn

	DuoPompRegeling();	// regelt met twee pompen
						
	LeesHumTemp();     // lees Humidity en temperatuur
					   
	LeesLight();       // lees de lichtopbrengst
					   
	// LeesRaindropSensor();	// Lees de raindrop sensoren
	// routine vervangen door onderstaande
	// LeesRaindropSensoropDroog();  // routine ook vervangen door:
	LeesRegenSensor();	// de regensensor RG-11

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

/*void softwareReset(uint8_t prescaller) {
	// start watchdog with the provided prescaller
	wdt_enable(prescaller);
	// wait for the prescaller time to expire
	// without sending the reset signal by using
	// the wdt_reset() method
	while (1) {}
}
*/

String LeesEprom(int b, int e) {
	String result = "";
	int x;
	for (int i = b; i < e + 1; i = i + 1) {
		x = EEPROM.read(i);  // in x staat een ascii cijfer
		result = result + String(x - 48); // en nu staat er een getalstring
	}
	return result;
}




