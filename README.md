# videosdk-uitookit-android-sample

## Overview

This is a simple sample implementation showcasing how to add the [Android UI Toolkit](https://developers.zoom.us/docs/video-sdk/android/ui-toolkit/) to a basic Android project using XML layouts.

## Prerequisites

- Android Studio
- An Android device or emulator
- A [Video SDK JWT](https://developers.zoom.us/docs/video-sdk/auth/)

## Download and run

1. Clone the repo

```sh
git clone https://github.com/zoom/videosdk-uitookit-android-sample
```
2. Open the `UIToolkitSample` directory in Android Studio. Do not open the repository's root directory or Gradle will not sync with the project correctly.
3. Navigate to `Constants.kt` and add valid values for the session context.
   - `NAME`: The name of the user who will be joining the session.
   - `SESSION_NAME`: The name of the session being joined. See [Sessions](https://developers.zoom.us/docs/video-sdk/android/sessions/) for more information.
   - `SDK_KEY`: Your Video SDK Key.
   - `SDK_SECRET`: Your Video SDK Secret.

    ```kotlin
    object Constants {
       const val NAME = ""
       const val SESSION_NAME = ""
       const val SDK_KEY = ""
       const val SDK_SECRET= ""
    }
    ```
4. Select a target device and click run.

## Need help?

If you're looking for help, try [Developer Support](https://devsupport.zoom.us) or our [Developer Forum](https://devforum.zoom.us). Priority support is also available with [Premier Developer Support](https://explore.zoom.us/docs/en-us/developer-support-plans.html) plans.
