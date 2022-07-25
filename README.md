# KtorNoteApp
Android Notes app that uses Ktor back end server

Back End server: https://github.com/realityexpander/ktor-note-app

[<img src="https://user-images.githubusercontent.com/5157474/180708684-10c5e065-ad66-466a-8212-7942192952ae.png" width="200"/>](https://user-images.githubusercontent.com/5157474/180708684-10c5e065-ad66-466a-8212-7942192952ae.png)
[<img src="https://user-images.githubusercontent.com/5157474/180708820-5c73b5d2-a149-4948-8597-964ee16ec360.png" width="200"/>](https://user-images.githubusercontent.com/5157474/180708820-5c73b5d2-a149-4948-8597-964ee16ec360.png)
[<img src="https://user-images.githubusercontent.com/5157474/180708928-b93d1c9b-46c7-42c4-842f-b709f45f017b.png" width="200"/>](https://user-images.githubusercontent.com/5157474/180708928-b93d1c9b-46c7-42c4-842f-b709f45f017b.png)
[<img src="https://user-images.githubusercontent.com/5157474/180709033-9ce0813c-3733-4a53-a2fd-a6fba771830e.png" width="200"/>](https://user-images.githubusercontent.com/5157474/180709033-9ce0813c-3733-4a53-a2fd-a6fba771830e.png)
[<img src="https://user-images.githubusercontent.com/5157474/180709143-5088d696-3da6-42c3-828d-2617104b3179.png" width="200"/>](https://user-images.githubusercontent.com/5157474/180709143-5088d696-3da6-42c3-828d-2617104b3179.png)
[<img src="https://user-images.githubusercontent.com/5157474/180709243-02091594-ceb4-4c4d-a3dd-3b8ebaa6f2a0.png" width="200"/>](https://user-images.githubusercontent.com/5157474/180709243-02091594-ceb4-4c4d-a3dd-3b8ebaa6f2a0.png)
[<img src="https://user-images.githubusercontent.com/5157474/180709312-f6cd2742-6eac-4f64-9e17-af682c82f428.png" width="200"/>](https://user-images.githubusercontent.com/5157474/180709312-f6cd2742-6eac-4f64-9e17-af682c82f428.png)

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

Note : this repository only contains the android app part, Ktor API is in this repo: https://github.com/realityexpander/ktor-note-app
