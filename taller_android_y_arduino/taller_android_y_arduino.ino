#include <WiFi.h>
#include <FirebaseESP32.h>
#include <NewPing.h>

// Configura las credenciales de Wi-Fi
#define WIFI_SSID "Wifi"
#define WIFI_PASSWORD "password"

// Configura las credenciales de Firebase
#define FIREBASE_HOST ""  // Reemplaza por tu URL de Firebase
#define FIREBASE_API_KEY "" // Reemplaza por el API KEY de tu proyecto

#define USER_EMAIL "" // Ingresa el correo del usuario que creaste en Firebase
#define USER_PASSWORD "" // Ingresa la contraseña del usuario que creaste en Firebase

#define TRIGGER_PIN 2
#define ECHO_PIN 4
#define MAX_DISTANCE 400

NewPing sonar(TRIGGER_PIN, ECHO_PIN, MAX_DISTANCE);

FirebaseData firebaseData;
FirebaseAuth auth;
FirebaseConfig config;

int ledPin = 5;
int lastDistance = 0;

void setup() {
  // Configura el pin del LED
  pinMode(ledPin, OUTPUT);

  // Inicia la comunicación serial
  Serial.begin(115200);

  Serial.print("Conectando a WiFi: ");
  Serial.println(WIFI_SSID);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
    attempts++;
    if (attempts > 20) {  // Intentar conectarse durante 20 segundos
      Serial.println("\nNo se pudo conectar a WiFi.");
      return;
    }
  }
  Serial.println();
  Serial.println("Conectado a WiFi");

  // Configuración de Firebase
  config.host = FIREBASE_HOST;
  config.api_key = FIREBASE_API_KEY;

  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  if (Firebase.signUp(&config, &auth, "", "")) {
    Serial.println("Autenticación exitosa");
  } else {
    Serial.printf("Error de autenticación: %s\n", config.signer.signupError.message.c_str());
  }
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {
    unsigned int distance = sonar.ping_cm();

    Serial.print("Distancia: ");
    Serial.print(distance);
    Serial.println(" cm");

    if (Firebase.ready()) {
      if (lastDistance != distance) {
        lastDistance = distance;
        if (Firebase.setInt(firebaseData, "/sensor/currentDistance", distance)) {
          Serial.println("Datos enviados a Firebase");
        } else {
          Serial.println("Error enviando datos a Firebase");
          Serial.println(firebaseData.errorReason());
        }
      } else {
        Serial.println("La distancia registrada es la misma en Firebase");
      }
      if (Firebase.getInt(firebaseData, "/sensor/detectionDistance")) {
        if (firebaseData.intData() >= distance) {
          if (Firebase.setBool(firebaseData, "/sensor/ledState", true)) {
            Serial.println("Datos de led enviados a Firebase");
          } else {
            Serial.println("Error enviando datos del led a Firebase");
            Serial.println(firebaseData.errorReason());
          }
        } else {
          if (Firebase.setBool(firebaseData, "/sensor/ledState", false)) {
            Serial.println("Datos de led enviados a Firebase");
          } else {
            Serial.println("Error enviando datos del led a Firebase");
            Serial.println(firebaseData.errorReason());
          }
        }
      }
      if (Firebase.getBool(firebaseData, "/sensor/ledState")) {
        if (firebaseData.boolData()) {
          digitalWrite(ledPin, HIGH);
        } else {
          digitalWrite(ledPin, LOW);
        }
      }
    } else {
      Serial.println("Firebase no está listo");
    }
  } else {
    Serial.println("WiFi no está conectado");
  }

  delay(1000);
}
