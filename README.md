# SVEGLIATI

Prima base del progetto Android nativo SVEGLIATI.

## Prima versione
- Interfaccia nera con testo bianco e dettagli verdi.
- Ora nel formato 24 ore `HH:mm`.
- Data italiana nel formato `Mer 02 SET`.
- Primo widget Android (App Widget) con ora e data.
- Compilazione APK automatica tramite GitHub Actions.

## Compilazione
La workflow GitHub Actions si avvia automaticamente a ogni push su `main`, oppure manualmente da Actions > Build SVEGLIATI APK > Run workflow.

L'APK debug viene pubblicato come artifact `SVEGLIATI-debug`.

Nota: questa è la base iniziale. Allarmi, snooze, registrazione vocale, selezione suoni e personalizzazione saranno implementati nei passaggi successivi e testati su un dispositivo Android.
