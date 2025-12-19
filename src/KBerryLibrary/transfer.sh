# Konfiguration
PI_USER="mradle"
PI_HOST="192.168.178.164"
PI_APP_DIR="./smart-home"
APP_NAME="kberry.jar"

# 1. Neueste JAR im target-Verzeichnis finden
JAR_FILE=$(ls -t target/*.jar | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "Keine JAR-Datei im target-Verzeichnis gefunden!"
    exit 1
fi

echo "Gefundene JAR: $JAR_FILE"

echo "Kopiere JAR-Datei nach Raspberry Pi..."
scp "$JAR_FILE" $PI_USER@$PI_HOST:$PI_APP_DIR/$APP_NAME