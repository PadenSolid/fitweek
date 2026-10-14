# FitWeek – Android MVP

Native Android-App (Kotlin + Jetpack Compose) für **prospektive Wochenplanung** von Training und Mahlzeiten.

## Funktionen in Version 0.1
- Kalenderwochen vor- und zurückblättern; pro Tag mehrere Einheiten (Rudern, Joggen, Krafttraining, Regeneration)
- Gewichts- und Zielgewichtseingabe (Startwert 77,5 kg, Ziel 74 kg)
- Drei konkrete Mahlzeitenvorschläge je Tag mit Beispiel-Nährwerten
- Unterscheidung von Abnahme und Erhaltung anhand des eingegebenen Gewichts
- Lokale Speicherung der Trainingseinträge und Gewichtsziele auf dem Gerät
- Teilen eines Tagesplans über Android

## Noch nicht implementiert
- **Google-Kalender-Synchronisation**, Gewichtshistorie und Diagramme, Rezeptdatenbank mit Zutaten/Portionsgrößen, Einkaufsliste, personalisierte Energiebedarfsrechnung, automatisch optimierte Makronährstoffplanung, Benachrichtigungen.
- Die aktuellen kcal-Werte sind **Platzhalter**, keine medizinisch oder ernährungswissenschaftlich individualisierte Empfehlung. Mahlzeiten-Summen entsprechen nicht automatisch den Tageszielwerten.

## Installation / Build
1. Dieses Repository in **Android Studio** öffnen (JDK 17, Android SDK 35).
2. Gradle-Sync abwarten.
3. Samsung-Smartphone mit USB-Debugging verbinden und **Run** ausführen; alternativ **Build > Build APK(s)**.
4. APK auf dem Smartphone installieren.

## Auf GitHub veröffentlichen
```bash
git init
git add .
git commit -m "Initial FitWeek Android MVP"
git branch -M main
git remote add origin https://github.com/DEIN-BENUTZERNAME/fitweek.git
git push -u origin main
```
Das leere Repository `fitweek` zuvor auf GitHub anlegen. Der echte Link ist erst nach dem Push verfügbar.

## Datenschutz
Keine Cloud-Anbindung; Eingaben bleiben lokal in Android SharedPreferences. Für spätere Versionen mit Gesundheitsdaten ist eine verschlüsselte Datenbank empfehlenswert.
