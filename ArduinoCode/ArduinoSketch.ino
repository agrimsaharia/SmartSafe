#include <LiquidCrystal.h>
#include <Servo.h>
#include <Keypad.h>
#include <string.h>

#define STATUS_LOCKER_LOCKED 0
#define STATUS_LOCKER_UNLOCKED 90

const int MAX_PIN_LENGTH = 6;
const int MAX_WIFI_SSID_LENGTH = 4;
const int MAX_WIFI_PASSWORD_LENGTH = 6;
const int PAUSE_TIME_AFTER_TRIGGER = 10; // Seconds
const int NUM_INCORRECT_ATTEMPTS_ALLOWED = 2;

const int rs = 2, en = 3;
const int pot = 5;
LiquidCrystal lcd(rs, en, A0, A1, A2, A3);

Servo servo;

const byte ROWS = 4; 
const byte COLS = 3; 

char hexaKeys[ROWS][COLS] = {
  {'1', '2', '3'},
  {'4', '5', '6'},
  {'7', '8', '9'},
  {'*', '0', '#'}
};

byte rowPins[ROWS] = {13, 12, 11, 10}; 
byte colPins[COLS] = {9, 8, 7}; 

Keypad customKeypad = Keypad(makeKeymap(hexaKeys), rowPins, colPins, ROWS, COLS); 

int failed_attempts;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);

  pinMode(A0, OUTPUT);
  pinMode(A1, OUTPUT);
  pinMode(A2, OUTPUT);
  pinMode(A3, OUTPUT);
  pinMode(A4, OUTPUT);
  
  pinMode(pot, OUTPUT);
  analogWrite(pot, 120);
  
  lcd.begin(16, 2);
  
  servo.attach(6);
  setLocker(STATUS_LOCKER_LOCKED);

  failed_attempts = 0;  
}

void loop() {
  // put your main code here, to run repeatedly:
 // scan from 0 to 180 degrees
 bool unlocked = startApplication();
 delay(1000);
 if (unlocked) waitForLock();
}

String getInputFromUser(String prompt, const int length)
{
  lcd.clear();
  lcd.setCursor(0, 1);
  lcd.print("*:clear  #:enter");

  lcd.setCursor(0, 0);
  lcd.print(prompt + ":");

  String input;
  for (int i = 0; i < length; i++) {
    char key = getInputKey();
    if (key == '*') return "";
    if (key == '#') return input;
    lcd.print(key);
    input += key;
  }
  while (true) {
    char key = getInputKey();
    if (key == '*') return "";
    if (key == '#') return input;
  }
}

void waitForLock()
{
  // ask user to lock the locker
  askUserToLock();
  char key = ' ';
  while (key != '#')
  {
    key = getInputKey();
  }
  setLocker(STATUS_LOCKER_LOCKED);
}

void askUserToLock()
{
  lcd.setCursor(0, 1);
  lcd.print("Press # to lock");
}

bool startApplication()
{
  // ask user for PIN
  String PIN = "";
  while(PIN == "") PIN = getInputFromUser("PIN", MAX_PIN_LENGTH);
  bool isValid = validatePIN(PIN);
  if (isValid) 
  {
    correctPinHandler();
  }
  else 
  {
    incorrectPinHandler();
  }
  return isValid;
}

void setLocker(int status)
{
  servo.write(status);
}

void correctPinHandler()
{
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Access Granted!");
  setLocker(STATUS_LOCKER_UNLOCKED);
}

void incorrectPinHandler()
{
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Access Denied!");

  lcd.setCursor(0, 1);
  lcd.print("Retries Left: ");
  lcd.print(NUM_INCORRECT_ATTEMPTS_ALLOWED - failed_attempts);
  
  failed_attempts++;
  if (failed_attempts > NUM_INCORRECT_ATTEMPTS_ALLOWED)
  {
    // notify user  
    requestESP("alertUser", false); 
    pauseApplication(PAUSE_TIME_AFTER_TRIGGER);
    failed_attempts = 0;
  }
}

void pauseApplication(uint16_t time)
{
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Locker Paused");
  lcd.setCursor(0, 1);
  lcd.print("Wait sec:");

  unsigned long refTime = millis();
  while(time)
  {
    if (millis() - refTime > 1000)
    {
      refTime = millis();
      lcd.setCursor(9, 1);
      lcd.print("       ");
      lcd.setCursor(9, 1);
      lcd.print(time--);
    }
  }
}

bool validatePIN(String recvPIN)
{
  return requestESP("recvPIN=" + String(recvPIN), true);
}

bool requestESP(String message, bool waitForResponse)
{
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Please Wait");

  if (waitForResponse) message = "[WAIT]" + message;

  while (Serial.available()) Serial.read();
  Serial.println(message);

  if (!waitForResponse) return true;  
  
  while(!Serial.available());
  
  lcd.setCursor(0, 0);
  lcd.print("Checking...");
  
  String response = Serial.readStringUntil('\n');
  response.trim();
  if (response.endsWith("OK")) return true;
  return false;  
}

char getInputKey()
{
  while(true)
  {
    char customKey = customKeypad.getKey();  
    if (customKey){
      return customKey;
    }
  }
}
