# I am no longer working on this but PRs opened will be reviewed!

[![IMAGE ALT TEXT](https://img.youtube.com/vi/8ls11w08g_Y/maxresdefault.jpg)](https://www.youtube.com/watch?v=8ls11w08g_Y&t=54s)

This repo represents an android app with webRTC functionality that let's you communicate with a realtime open.ai model, utilizes Room for local route storing and auth0 for handling user credentials when entering the app.

This app is very barebones as my focus was just on functionality, ideally I would've made it prettier at some point but the functionality list kept getting longer and longer, some notable areas that would be of interest are:

[`webRTC`](https://github.com/BBBmau/realtime-tour-guide/tree/main/mobile/src/main/java/com/mau/exploreai/webrtc)- this directory implements webRTC connection between the client and the openai server. This took quite sometime to setup as there is no simple way to setup webRTC on android. Nonetheless this should be helpful for anyone who's interested in adding similar functionality to their own app.
`

[`ConversationDatabase.kt`](https://github.com/BBBmau/realtime-tour-guide/blob/main/mobile/src/main/java/com/mau/exploreai/ConversationDataBase.kt) - this is a good starting point for those interested in how i implemented local storage using Room, you would start here for conversations and enter individual storing of the messages.

[`RetrofitClient.kt`](https://github.com/BBBmau/realtime-tour-guide/blob/main/mobile/src/main/java/com/mau/exploreai/RetrofitClient.kt) - a good starting point for those interested in setting up HTTP requests on android using `okhttp3` as well as converting data in a way that can be sent/read through `retrofit`

For login functionality you'll need to create an auth0 account and provide the necessary credentials [here](https://github.com/BBBmau/realtime-tour-guide/blob/694598473bb6e7b776de07163b1ae5e0f8c4bfe4/automotive/src/main/res/values/strings.xml#L3). I recommend reading up on [setting up auth0 on android native](https://auth0.com/docs/quickstart/native/android/interactive) as well

You'll also want to setup a backend that will provide you an ephemeral key from openai to gain access to the realtime open.ai model, I've provided the backend source code as well [here](https://github.com/BBBmau/realtime-tour-guide-backend).
