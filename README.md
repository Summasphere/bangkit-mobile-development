# Summasphere App - Mobile Development Documentation
The source code of the Summasphere (Summarizer and Analyzer of Long Corpus) Apps using Kotlin as a Programming Language

- ### Project Architecture
![MD Implementation](https://github.com/Summasphere/bangkit-mobile-development/assets/123135519/affac823-3609-49ae-b2cd-9bbed39ef60c)

- ### Implementation Flow:
  - **Input Handling on Android App:** Our Android app receives input in the form of text, documents (docs), or URLs. This input is then converted into JSON format for standardized data handling.
  - **Transmission to Cloud Computing:** The JSON data is transmitted to our Cloud computing infrastructure. Here, the Cloud initiates a request to retrieve relevant data from the connected database.
  - **Summarization and Analysis Process:** The retrieved data is passed through a summarizer and analyzer module. These modules are integrated with a Machine Learning (ML) model via FastAPI for efficient processing.
  - **ML Model Integration via FastAPI:** The summarization and analysis tasks are delegated to the ML model through FastAPI endpoints. The ML model processes the data to generate a JSON response containing summaries and analyses.
  - **Data Flow Back to Cloud Computing:** The JSON response, containing summaries and analyses, is sent back to the Cloud computing environment.
  - **Integration with Database:** In the Cloud, the processed results are integrated back with the database for storage and retrieval purposes.
  - **Return to Android App:** Finally, the summarized and analyzed results are formatted into JSON format again. This JSON data is transmitted back to the Android app.
  - **User Accessibility:** Users can easily copy the text-based summary or download it as a PDF directly from the app.
  - **Conclusion:** This flow ensures that Summasphere provides users with efficient summarization and analysis of documents, text, and URLs, leveraging Cloud computing and Machine Learning to deliver accurate and accessible results. Based on this implementation there are several keys of our app :
    - Integration: Seamless integration of Android app with Cloud computing and ML via FastAPI.
    - Efficiency: Efficient processing of documents and URLs to provide quick summaries and analyses.
    - User Experience: User-friendly options to copy text or download summaries as PDFs.

- ### Mock-up
![mockup](https://github.com/Summasphere/bangkit-mobile-development/assets/123135519/f5132daf-ad52-478c-82a5-d6bb74e06513)

- ### Features of Summasphere
  * **Summarizer**
    Users can quickly obtain concise summaries of text, documents, or URLs using this feature.
  * **Analyzer**
    Users receive a detailed analysis of input data (text, documents, or URLs), enhancing their understanding and insights through Topics Descriptions, Word Cloud, and   Topics Distribution Graph

- ### Dependencies
  - [AndroidX Core KTX](https://developer.android.com/jetpack/androidx/releases/core)
  - [AndroidX AppCompat](https://developer.android.com/jetpack/androidx/releases/appcompat)
  - [Material Components for Android](https://material.io/develop/android)
  - [AndroidX Activity](https://developer.android.com/jetpack/androidx/releases/activity)
  - [AndroidX ConstraintLayout](https://developer.android.com/jetpack/androidx/releases/constraintlayout)
  - [Firebase Auth](https://firebase.google.com/docs/auth)
  - [AndroidX Legacy Support v4](https://developer.android.com/jetpack/androidx/releases/legacy)
  - [AndroidX Lifecycle Livedata KTX](https://developer.android.com/jetpack/androidx/releases/lifecycle)
  - [AndroidX Fragment KTX](https://developer.android.com/jetpack/androidx/releases/fragment)
  - [AndroidX Lifecycle ViewModel KTX](https://developer.android.com/jetpack/androidx/releases/lifecycle)
  - [AndroidX Activity KTX](https://developer.android.com/jetpack/androidx/releases/activity)
  - [AndroidX RecyclerView](https://developer.android.com/jetpack/androidx/releases/recyclerview)
  - [AndroidX Splashscreen](https://developer.android.com/jetpack/androidx/releases/splashscreen)
  - [Lottie](https://airbnb.io/lottie/#/android)
  - [Retrofit](https://square.github.io/retrofit/)
  - [Gson](https://github.com/google/gson)
  - [OkHttp Logging Interceptor](https://square.github.io/okhttp/)
  - [Firebase BoM](https://firebase.google.com/docs/projects/bom)
  - [Google Play Services Auth](https://developers.google.com/android/guides/overview)
  - [Navigation Component](https://developer.android.com/jetpack/androidx/releases/navigation)
  - [AndroidX Security Crypto](https://developer.android.com/jetpack/androidx/releases/security)
  - [Picasso](https://square.github.io/picasso/)
  - [Android PDFDocument](https://developer.android.com/reference/android/graphics/pdf/PdfDocument)

### Getting Started Application

  - ### Preparation
      - ##### Tools Sofware
        - [Android Studio](https://developer.android.com/studio)
        - [Postmant](https://www.postman.com/) to test API from Cloud Computing
