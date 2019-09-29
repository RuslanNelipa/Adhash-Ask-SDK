# Adhash-Ask-SDK

### How to add

1. Add latest version of SDK to your app `build.gradle`. 

```xml
 dependencies {
        implementation 'org.adhash.sdk:adhashsdk:<version>'
  }
```

2. Latest version [here](https://github.com/RuslanNelipa/Adhash-Ask-SDK/releases "Releases")
3. SDK uses external libraries. Therefore you need to add them to app `build.gradle`. Version names might be different.

```xml
dependencies{
    implementation("io.coil-kt:coil:0.7.0") //Image loaded library
    implementation 'com.google.code.gson:gson:2.8.5' //JSON deserealizer

    def retrofitVersion = "2.6.2"
    implementation 'com.squareup.retrofit2:retrofit:' + retrofitVersion //API calls
    implementation 'com.squareup.retrofit2:converter-gson:' + retrofitVersion
    implementation 'com.squareup.retrofit2:converter-scalars:' + retrofitVersion
}
```

### How to use
1) Add to layout. `PublishedId` is required to make library work properly.
```xml
<org.adhash.sdk.adhashask.view.AdHashView
        ....
        app:publisherId="0x36016ae83df471d11332e5d2c490c804a45ca9b" />
```
2) Other available parameters:
```xml
<org.adhash.sdk.adhashask.view.AdHashView
       ...
        app:adOrder="1"
        app:adTagId="name of ad slot on screen"
        app:placeholderDrawable="@drawable/ic_launcher_foreground"
        app:errorDrawable="@drawable/ic_cross_24"
        app:version="1.0"
        app:analyticsUrl="http://website.com"
		app:screenshotUrl="http://website.com"/>
```

| Property  | Explanation  |
| ------------ | ------------ |
| placeholderDrawable  |  drawable resource. Will be used as a placeholder before Ad loaded  |
| errorDrawable  |  drawable resource. Will be displayed if Ad failed to load  |
| screenshotUrl | URL for redirection when user takes screenshot
|version|version of library usage. Set by user
|analyticsUrl| URL for which analytics will be sent
|adTagId| explanation of Ad placement on screen. Free text form

### Public methods
1. Setters for properties
2. Callback for Analytics sent result
3. Request new Ad
