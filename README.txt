FitWeek Version 2 – Drop-in-Update fuer dein bestehendes GitHub-Projekt

1. Die Datei app/src/main/java/com/fitweek/app/MainActivity.kt im Repository durch die gleichnamige Datei aus diesem ZIP ersetzen.
2. Die AndroidManifest.xml muss android:name=".MainActivity" enthalten.
3. Sicherstellen, dass app/build.gradle.kts namespace = "com.fitweek.app" und applicationId = "com.fitweek.app" verwendet.
4. Git commit/push, GitHub Actions assembleDebug ausfuehren, neues APK installieren.

Funktionsumfang: Trainings hinzufuegen/loeschen, Plan editieren, RPE und Dauer, TRX/Bodyweight-Trainingsvorlagen mit Uebungen, Ist-Saetze dokumentieren, absolviert markieren; Mahlzeiten mit Zutaten und Grammangaben editieren, Tages-Ist-Makros; lokales Speichern.

Einschraenkungen: Kein automatischer Trainingsgenerator oder echte progressive Wochenanpassung, keine vollstaendige Rezeptdatenbank, keine Trainingskalorienberechnung, keine Cloud/Google-Calendar-Synchronisation. App wurde hier nicht auf Android kompiliert oder getestet; GitHub Actions Build und Geraetetest erforderlich.
