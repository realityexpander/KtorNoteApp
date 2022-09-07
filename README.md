# KtorNoteApp
Android Notes app that uses Ktor back end server

Back End server: https://github.com/realityexpander/ktor-note-app

[<img src="https://user-images.githubusercontent.com/5157474/180708684-10c5e065-ad66-466a-8212-7942192952ae.png" width="240"/>](https://user-images.githubusercontent.com/5157474/180708684-10c5e065-ad66-466a-8212-7942192952ae.png)
[<img src="https://user-images.githubusercontent.com/5157474/180708820-5c73b5d2-a149-4948-8597-964ee16ec360.png" width="240"/>](https://user-images.githubusercontent.com/5157474/180708820-5c73b5d2-a149-4948-8597-964ee16ec360.png)
[<img src="https://user-images.githubusercontent.com/5157474/180708928-b93d1c9b-46c7-42c4-842f-b709f45f017b.png" width="240"/>](https://user-images.githubusercontent.com/5157474/180708928-b93d1c9b-46c7-42c4-842f-b709f45f017b.png)
[<img src="https://user-images.githubusercontent.com/5157474/180709033-9ce0813c-3733-4a53-a2fd-a6fba771830e.png" width="240"/>](https://user-images.githubusercontent.com/5157474/180709033-9ce0813c-3733-4a53-a2fd-a6fba771830e.png)
[<img src="https://user-images.githubusercontent.com/5157474/180709143-5088d696-3da6-42c3-828d-2617104b3179.png" width="240"/>](https://user-images.githubusercontent.com/5157474/180709143-5088d696-3da6-42c3-828d-2617104b3179.png)
[<img src="https://user-images.githubusercontent.com/5157474/180709243-02091594-ceb4-4c4d-a3dd-3b8ebaa6f2a0.png" width="240"/>](https://user-images.githubusercontent.com/5157474/180709243-02091594-ceb4-4c4d-a3dd-3b8ebaa6f2a0.png)
[<img src="https://user-images.githubusercontent.com/5157474/180709312-f6cd2742-6eac-4f64-9e17-af682c82f428.png" width="240"/>](https://user-images.githubusercontent.com/5157474/180709312-f6cd2742-6eac-4f64-9e17-af682c82f428.png)

Allows users to:
- Add notes
- Delete notes
- Share notes with other users
- Add, read, and delete notes while offline
- Change colors of notes
- Add markup to notes

# Technologies employed:

* Kotlin 
* MVVM
* Coroutines
* Custom REST API built with Ktor
* Responds to custom HTTP & HTTPS endpoints with JSON, HTML and CSS
* MongoDB Database
* Custom authentication mechanism, so only logged in users can make requests to authenticated endpoints
* A complete registration and login system
* NoSQL database scheme
* Retrofit on Android to communicate with Ktor server
* Efficiently handles network errors
* Efficient local database cache that saves notes in a Room database for offline use
* Dependency injection with Dagger-Hilt
* User logged in even while offline
* Encrypted SharedPreferences
* Edit & Display Markdown formatted notes in Android
* Synchronized local notes with your Ktor server when online
* Securely saves passwords using modern encryption standards
* Encrypted HTTPS traffic
* Deployable Ktor server accessable from anywhere
* Animated Motion Views

Note : this repository only contains the android app part, Ktor API is in this repo: https://github.com/realityexpander/ktor-note-app

To install the Apk:

1. Open this link on your Android device:
   https://github.com/realityexpander/KtorNoteApp/blob/master/ktor-note-app_1.0.apk
2. Tap the "skewer" menu and tap the "download"

   [![](https://user-images.githubusercontent.com/5157474/147434050-57102a30-af32-46ed-a90b-d94e0c4a4f35.jpg)]()
3. Allow the file to download (DO NOT click "show details")
4. After the file is downloaded, click "OK" to install
5. Click "OK" to install
6. Click "OK" to launch

If you have developer options turned on, you may need to turn off "USB Debugging" if the "Waiting for debugger" dialog is displayed.
