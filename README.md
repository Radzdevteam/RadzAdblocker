# RadzAdblocker Android WebView Blocker

[![](https://jitpack.io/v/Radzdevteam/RadzAdblocker.svg)](https://jitpack.io/#Radzdevteam/RadzAdblocker)

RadzWeb is an Android app that uses a customized WebView to load websites with enhanced functionality. It features video playback, ad blocking, URL redirection prevention, and more.

## Features

- **WebView Integration**: Displays webpages in a `WebView` with support for JavaScript, DOM storage, and video playback.
- **Ad Blocking**: Blocks known ad hosts and redirect links dynamically fetched from a remote URL.
- **Video Playback**: Supports fullscreen video playback, including screen rotation and maintaining screen-on behavior during video.
- **Custom Error Pages**: Displays a custom `404.html` error page for network-related issues.
- **Swipe-to-Refresh**: Allows users to reload the webpage with a swipe gesture.
- **WakeLock Management**: Keeps the screen on during video playback and fullscreen mode.
- **SSL Error Handling**: Automatically proceeds with SSL errors (for cases where SSL certificate validation is not required).

## Prerequisites

- Android Studio (latest stable version)
- Android 5.0 (API level 21) or higher

## Setup

### 1. Add Repository to `settings.gradle`
In your project’s `settings.gradle` file, add the following repository configuration:

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add Dependency in `build.gradle`
In your app module’s `build.gradle` file, add the following dependency under `dependencies`:

```groovy
dependencies {
    implementation ("com.github.Radzdevteam:RadzAdblocker:Tag")
}
```

Replace `Tag` with the version or tag of the library you want to use.

### 3. Launch `radzweb` Activity from `MainActivity`
In your `MainActivity`, add the following code to start the `radzweb` activity and pass a URL:

```kotlin
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, radzadblocker::class.java)
        intent.putExtra("url", "https://google.com/")
        startActivity(intent)
        finish()
    }
}
```

## Usage

Once the app is set up and running, it will:

1. Load the provided URL (`https://google.com/` in the example above) in a `WebView`.
2. Block unwanted ads and redirection links using predefined patterns.
3. Show a custom error page if the page fails to load.
4. Handle fullscreen video playback when watching videos inside the `WebView`.

## Customization

- **URL Customization**: You can pass any URL to the `radzadblocker` activity by changing the value passed in the `Intent`.
- **Ad Block List**: The list of ad hosts is dynamically fetched from a remote URL. You can customize this list by modifying the source of the ad host file.
