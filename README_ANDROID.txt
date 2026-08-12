JDR LUNARIA — APPLICATION ANDROID NATIVE
=========================================

Cette app Android ouvre directement :
https://foxydreyard-dotcom.github.io/JDR/

Conséquence :
- les modifications HTML/CSS/JS du JDR restent publiées sur GitHub Pages ;
- les joueurs voient ces changements sans réinstaller l'APK ;
- l'application native (icône, permissions, fonctions Android) se met à jour via un nouvel APK.

PREMIER TEST
------------
1. Copie les dossiers "android-app" et ".github" à la racine du dépôt GitHub JDR.
2. Commit.
3. GitHub > Actions > "Build JDR Lunaria Android" > Run workflow.
4. À la fin, ouvre le workflow et télécharge l'artifact "JDR-Lunaria-Android-TEST".
5. Installe l'APK sur ton téléphone Android.

VERSION À PARTAGER AUX JOUEURS
-------------------------------
1. Configure les 4 secrets indiqués dans LUNARIA_SIGNING_SECRETS_A_GARDER.txt.
2. Crée un tag GitHub, par exemple :
   android-v1.0.0
3. Le workflow construit JDR-Lunaria.apk signé et crée automatiquement une Release GitHub.
4. Tes amis installent ce JDR-Lunaria.apk.

MISES À JOUR
------------
- Interface JDR : automatiquement, car l'app ouvre le site GitHub Pages.
- Nouvelle icône / nouveau code Android :
  crée un nouveau tag, ex. android-v1.0.1.
- L'app vérifie automatiquement la dernière Release GitHub au lancement.
  Si une version plus récente existe, elle propose "Télécharger".

IMPORTANT ANDROID
-----------------
Pour une installation hors Google Play, Android demandera toujours à l'utilisateur
d'autoriser/confirmer l'installation du nouvel APK. Une app normale distribuée sur
Google Play peut être mise à jour par le Play Store.

Le projet utilise un WebView Android avec :
- JavaScript
- localStorage / DOM storage
- vidéos autoplay
- upload de portraits via <input type="file">
- Supabase / WebSocket via HTTPS
- navigation externe dans le navigateur

Package Android :
com.jdrlunaria.app
