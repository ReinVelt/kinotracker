/*
  Software serial multple serial test
 
 Receives from the hardware serial, sends to software serial.
 Receives from software serial, sends to hardware serial.
 
 The circuit: 
 * RX is digital pin 10 (connect to TX of other device)
 * TX is digital pin 11 (connect to RX of other device)
 
 Note:
 Not all pins on the Mega and Mega 2560 support change interrupts, 
 so only the following can be used for RX: 
 10, 11, 12, 13, 50, 51, 52, 53, 62, 63, 64, 65, 66, 67, 68, 69
 
 Not all pins on the Leonardo support change interrupts, 
 so only the following can be used for RX: 
 8, 9, 10, 11, 14 (MISO), 15 (SCK), 16 (MOSI).
 
 created back in the mists of time
 modified 25 May 2012
 by Tom Igoe
 based on Mikal Hart's example
 
 This example code is in the public domain.
 
 */
#include <avr/wdt.h>
#include <TinyGPS++.h>
#include <SoftwareSerial.h>

//user configurable stuff
#define  pinLed                13
#define  pinGpsRX              8
#define  pinGpsTX              9
#define  wirelessBaudrate      57600
#define  GpsBaudrate           9600

//system config stuff
#define appName                "KinoTracker"
#define appVersion             "1.0"
#define id                     "Kino"


//global vars
SoftwareSerial gpsDevice(pinGpsRX, pinGpsTX); // RX, TX
TinyGPSPlus gps;


void setup()  
{
  //set pinmodes
  pinMode(pinGpsRX,INPUT);
  pinMode(pinGpsTX,OUTPUT);
  pinMode(pinLed,OUTPUT);

  // Open serial communications and wait for port to open:
  Serial.begin(wirelessBaudrate);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }


  //send appname & version to wireless device
  Serial.print(appName);
  Serial.print(" v");
  Serial.println(appVersion);


  // set the gps baudrate
  gpsDevice.begin(GpsBaudrate);
  
  //enable watchdog timer
  //wdt_enable(WDTO_8S);
  printHeader();
}

void loop() // run over and over
{
  //wdt_reset();
  getGpsData();
} //end-loop

void printHeader()
{
   Serial.println( "YYYY-MM-DD\thh:mm:ss\tago_\tlatitude__\tlongitude__\tcourse\tspeed\tsats\tbattery");
}

void getGpsData()
{
   printDateTime(gps.date,gps.time);
   printString(id);
   printFloat(gps.location.lat(),gps.location.isValid(),11,6);
   printFloat(gps.location.lng(),gps.location.isValid(),12,6);
   printFloat(gps.course.deg(),gps.course.isValid(),7,2);
   printFloat(gps.speed.kmph(),gps.speed.isValid(),6,2);
   printInt(gps.satellites.value(),gps.satellites.isValid(),5);
   printInt(5000,true,5);
   Serial.println();
   smartDelay(1000);
   if (millis()>5000 && gps.charsProcessed()<10)
   {
      Serial.println(F("ERROR: NO DATA"));
   }
   
   
}

// This custom version of delay() ensures that the gps object
// is being "fed".
static void smartDelay(unsigned long ms)
{
  unsigned long start = millis();
  do 
  {
    while (gpsDevice.available())
      gps.encode(gpsDevice.read());
  } while (millis() - start < ms);
}

static void printFloat(float val, bool valid, int len, int prec)
{
  
    Serial.print(val, prec);
    Serial.print("\t");
  smartDelay(0);
}

static void printInt(unsigned long val, bool valid, int len)
{
  char sz[32];
  sprintf(sz, "%ld", val);
  Serial.print(sz);
  Serial.print("\t");
  smartDelay(0);
}

static void printString(String text)
{
   Serial.print(text);
   Serial.print("\t");
   smartDelay(0);
}

static void printDateTime(TinyGPSDate &d, TinyGPSTime &t)
{
  if (!d.isValid())
  {
    Serial.print(F("**********\t"));
  }
  else
  {
    char sz[32];
    sprintf(sz, "%02d-%02d-%02d\t", d.year(), d.month(), d.day());
    Serial.print(sz);
  }
  
  if (!t.isValid())
  {
    Serial.print(F("********\t"));
  }
  else
  {
    char sz[32];
    sprintf(sz, "%02d:%02d:%02d\t", t.hour(), t.minute(), t.second());
    Serial.print(sz);
  }

  printInt(d.age(), d.isValid(), 5);
  smartDelay(0);
}

static void printStr(const char *str, int len)
{
  int slen = strlen(str);
  for (int i=0; i<slen; ++i)
    Serial.print(str[i]);
  Serial.print("\t");
  smartDelay(0);
}

