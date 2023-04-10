#include <ESP8266WiFi.h>
#include <Firebase_ESP_Client.h>
#include <addons/TokenHelper.h>

/* 1. Define the WiFi credentials */
#define WIFI_SSID "YOUR WIFI SSID"
#define WIFI_PASSWORD "YOUR WIFI PASSWORD"

#define DATABASE_URL "smartsafe-56597-default-rtdb.firebaseio.com" //<databaseName>.firebaseio.com or <databaseName>.<region>.firebasedatabase.app

/* 2. Define the Service Account credentials (required for token generation) */
#define FIREBASE_PROJECT_ID "smartsafe-56597"
#define FIREBASE_CLIENT_EMAIL "esp8266@smartsafe-56597.iam.gserviceaccount.com"
const char PRIVATE_KEY[] PROGMEM = "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDRb+6Kw7VZl7cg\n64Q1ddO3ye4pCbE6nP06w9xfwS9L9+zHb44sdSHaiTipyiZWVIgiYq/AKDMHnrGe\ngRpizPo86/NE9QwyNeVteO3Z9qnA4ynFbWFKh8H0DyqEmxx7AqG/YjXm5qa1vQmi\n85ZtaGzAFbXhr0R/luyh+KfUUSgkge5howC6p3YvHSnJ/7W0RoTRm0ZdrCBYm8aA\nJ6VnBSs8UptQbXRStg68XEBQhWZ9wnXt2gluyo6Gu3BTnV/J+aH+ocOrdzOlO/Hx\n5tsm+3v07vxvn//oQ6zE7LhxktaqnKZT8ELvb8T6w32bkG0uqCJ/gYepeYW9z64h\nrehUN7dtAgMBAAECggEAWy+21mf7b3Sk4Qpp17TjZ2Pd87Qqm/B6OLnKG07LPvJE\n/1hMuIKPKM0SBKrlIjVPyFjbWHioLysi/nYLfPTuBtRcaKugkcPxjkZWmM3Sfw7o\nwvKqp36QLajkxDOcpNoyfldOTKgj8YDKz03rMtuCbeeF9ysOUdbuVF8lA0YKiknV\njsQ1ZpBGDcjTHtAOJKrL4OZSotac7G4AnAGxaDbCehUfWDyZ+Gqn4eqMlfW4ssEb\nnQcSpns5G0ZDIpyz5Dg4+QkjEXcIJZv3PULT5l3EqQwQXaKE1duGmyEddwF6E6+s\nWnGJ52Kd58l5zfdO2NasgYhXaxwCWWTHcBQOmg34aQKBgQD8uo+ComWkGeSoNZPV\n5dsXAO04ZvKrPAoJNz31bxhqo7J9z4vePaykYClhdPkL+XCpVxFc+xggchbthl8D\nskPX2ErSSrbHwtqNMb0GnThBS4gwLDIefi0FM2vRmj8AJHlqzLljYFxl0bPN9hRP\n+Yyo72/hcBgPVOwyFZRT2vdDXwKBgQDUJeu/Bn73cNUXTbs608a9Ewh+dYm2QU8t\nd7BYXn0HpRmLSZ2dSUzzv5oTu4jG5DGD+G0g9o9j2S76MKJWIImMV050ZWFTBtvL\n6i+9vTpB1PGmZ8HDH1Uly4kK8T9WN6xESJes5WWvWKcrOnWzcnXhT23H2u5lTAZ8\nIRkt2o3kswKBgQC5PXUa5zVgxqnWsQ6e5U9k2QUHJk4QPH2Oq7L6Rme7IikeFhYK\n22HDSciK6lXw9PAi7vKHGHRis70idC8IYo1fk+Wjpae7W5MdkqteQbaOBXLY/Evw\nFcOh4ELS6x/qVtG3+j6YHHUdHEuSVyAkTmFPIdD7iFHaSwCk5TRHEmPSOQKBgQCB\nNtuhDSbWNY5E1F5+lTDORsJ8q8CGFr9QFT9+g4ap2mB1mw/6FgRdzkzdSfMkGScj\nl3N4iVSK32SilG68ALNmnGK3uGwP8vR6/he3/IJ4SGgrD7VP0Ey/aXn+BTSrods1\n9/USJOLLazbeuIuqEcREyGk4OlvLrQOiJVK9wAl+ywKBgAlb7cCmRCjIMM+OE8kd\n1EuCr1Om4hUJQ8V+dVJjoBLa3rTzkfgWEuZmoqS8439GW21R4DuYjvRQ2z2XxJwv\ne9soAMJU66Is0MgEpkQfh9QembaQjltWUZqAxY04bmMlby0O9tYYLk8LHPtp7Xr7\n8Ae6LR/T5tPP/yQrcmBiKsjD\n-----END PRIVATE KEY-----\n";
/* 3. Define the ID token for client or device to send the message */

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config_fcm, config_rtdb;

String ARDUINO_ID = "0000"; 
String appToken;

// String getPIN();
// bool checkPIN(String);
// void setLockStatus(String);
// void getAppToken();
// bool notifyIntruderAlert();

void setup()
{
  Serial.begin(115200);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(300);
  }

  /* Assign the sevice account credentials and private key (required) */
  config_fcm.service_account.data.client_email = FIREBASE_CLIENT_EMAIL;
  config_fcm.service_account.data.project_id = FIREBASE_PROJECT_ID;
  config_fcm.service_account.data.private_key = PRIVATE_KEY;
  /* Assign the callback function for the long running token generation task */
  config_fcm.token_status_callback = tokenStatusCallback; // see addons/TokenHelper.h

  config_rtdb.database_url = DATABASE_URL;
  config_rtdb.signer.test_mode = true;

  Firebase.reconnectWiFi(true);
  Firebase.begin(&config_rtdb, &auth);

  getAppToken();
}

void loop()
{
  if (Serial.available())
  {
    String command = Serial.readStringUntil('\n');
    command.trim();
    bool request = false, response = false;
    if (command.startsWith("[WAIT]")) 
    {
      request = true;
      command = command.substring(6);
    }
    if (command == "alertUser") 
    {
      response = notifyIntruderAlert();
    }
    else if (command.startsWith("setStatus=")) 
    {
      response = setLockStatus(command.substring(10));
    }
    else if (command.startsWith("recvPIN=")) 
    {
      response = checkPIN(command.substring(8));
    }
    if (request) 
    {
      if (response == true) Serial.println("OK");
      else Serial.println("FAIL");
    }
  }
}

bool notifyIntruderAlert()
{
  Firebase.begin(&config_fcm, &auth);
  // Firebase.getCurrentTime();

  // Read more details about HTTP v1 API here https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages
  FCM_HTTPv1_JSON_Message msg;

  msg.token = appToken;

  msg.notification.title = "Intruder Alert";
  msg.notification.body = "Smart Lock is being accessed using incorrect PIN";

  // For the usage of FirebaseJson, see examples/FirebaseJson/BasicUsage/Create.ino
  FirebaseJson payload;

  // all data key-values should be string
  // payload.add("temp", "28");
  // payload.add("unit", "celsius");
  // payload.add("timestamp", "1609815454");
  // msg.data = payload.raw();

  while (!Firebase.ready()) {
    delay(10);
  }
  bool sent = false;
  if (Firebase.FCM.send(&fbdo, &msg)) sent = true;

  Firebase.begin(&config_rtdb, &auth);

  return sent;
}

bool getAppToken()
{
  if (Firebase.RTDB.getString(&fbdo, "/Arduino/" + ARDUINO_ID + "/AppToken"))
  {
    appToken = fbdo.stringData();
    return true;
  }
  return false;
}

String getPIN()
{
  if (Firebase.RTDB.getString(&fbdo, "/Arduino/" + ARDUINO_ID + "/PIN"))
  {
    return fbdo.stringData();
  }
  // Serial.printf("getPIN error : %s\n", fbdo.errorReason().c_str());
  return "";
}

bool setLockStatus(String status)
{
  if (Firebase.RTDB.setString(&fbdo, "/Arduino/" + ARDUINO_ID + "/Status", status))
  {
    return true;
  }
  return false;
}

bool checkPIN(String recvPIN)
{
  if (getPIN() == recvPIN)
  {
    return true;
  }
  else 
  {
    return false;
  }
}
