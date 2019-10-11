# Adhash-Ask-SDK

### How to add

1. Add latest version of SDK to your app `build.gradle`. 

```xml
 dependencies {
        implementation 'org.adhash.sdk:adhashsdk:<version>'
  }
```

2. Latest version [here](https://github.com/RuslanNelipa/Adhash-Ask-SDK/releases "Releases")
3. SDK uses external libraries. Keep in mind that you don't need to add those to `build.gradle`. You will have to resolve conflicts otherways

```xml
dependencies{
    implementation 'com.google.code.gson:gson:2.8.5' //JSON deserealizer

    def retrofitVersion = "2.6.2"
    implementation 'com.squareup.retrofit2:retrofit:' + retrofitVersion //API calls
    implementation 'com.squareup.retrofit2:converter-gson:' + retrofitVersion
    implementation 'com.squareup.retrofit2:converter-scalars:' + retrofitVersion
}
```

### How to use
1) Add to layout. `publisherId` is required to make library work properly.
```xml
<org.adhash.sdk.adhashask.view.AdHashView
        ....
        app:publisherId="0x36016ae83df471d11332e5d2c490c804a45ca9b" />
```
2) Other available parameters:

| Property | Mandatory | Example | Explanation  |
| ------------ | ------------ | ------------ | ------------ |
|publisherId | + | 0x36016ae83df47035679f2e5d2c490c804a67ca9b | Publisher ID |
|errorDrawable | - | @drawable/ic_error | Image resource that will be placed if any error occured during ad loading  |
|screenshotUrl | - |http://website.com | URL to chish user will redirected when screenshot taken. This works only if READ_EXTERNAL_STORAGE permission is given |
|version | - | 1 | Version of SKD usage. Set by user |
|adTagId | - |middle of screen | Text form. Identifier for location of Ad on screen |
|adOrder | - |1 | Order of the Ad on screen |
|analyticsUrl | + |http://website.com | URL which will be called as GET request with advertiser parameters |
|timezone | - |-3 | Timezone, stands for GMT+3:00 |
|location | - |com.package.net | unique application identifier |
|screenWidth | - |1366 | Screen width |
|screenHeight | - |768 | Screen height |
|platform | - |ARM | Device platform, something like 'ARM','iPad','iPhone','Linux aarch64','Linux armv7l','Linux i686',... |
|language | - |en-US | Locale settings |
|device | - |Samsung | Device brand name |
|model | - |Galaxy J7 Pro | Device model name |
|type | - |mobile | "mobile" or "tablet" |
|connection | - |WiFi | Connection type - WiFi, LTE, EDGE, HSDPA, etc. |
|isp | - |CoolProvider | Internet service provider / carrier ID |
|orientation | - |portrait | screen orientation |
|gps | - |30.3030303, 40.4040404 | coordinates. SDK tries to fetch it automatically of LOCATION permission is granted |
|creativesSize | - |300x250 | size in pixels of requested Ad |
|adHashUrl | - |http://website.com | URL which will be opened when user clicks on AH icon |
| loadAdOnStart | - | true\false | trigger to load first ad as only view attached |

### Public methods
1. Setters for parameters from table above
2. `AdView.requestNewAd()` - requests new Ad with all latest paramters
3. `AdView.setLoadingCallback { isLoading -> }` - callback for loading state of the view
4. `AdView.setErrorCallback { error -> }` - callback for errors received if any
