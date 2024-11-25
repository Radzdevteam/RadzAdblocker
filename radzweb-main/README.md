# Radz Webview
[![](https://jitpack.io/v/Radzdevteam/radzweb.svg)](https://jitpack.io/#Radzdevteam/radzweb)


## Features:

1. URL Loading Functionality
2. Ad Blocker Management 
3. Intent URL Blocking  
4. Popup and Ad Blocking  
5. Fullscreen Video Support  
6. Loading Animation  
7. Refresh WebView  
8. Back Navigation  
9. User-Agent String Management  
10. Swipe to Refresh  
11. Handle HTTP Response Codes  
12. Custom Error Pages

## How to Include
### Step 1. Add the repository to your project settings.gradle:
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

### Step 2. Add the dependency
```groovy
dependencies {
    implementation ("com.github.Radzdevteam:radzweb:Tag")
}

   ```

## Usage

In your `MainActivity`, add the following code:
```groovy
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, radzweb::class.java)
        intent.putExtra("url", "https://google.com/")
        startActivity(intent)
        finish()
    }
}

   ```


## Manifest
In your `AndroidManifest`, add the following code:

```groovy

<activity android:name="com.radzdev.radzweb.radzadblocker"
android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode"/>

   ```
