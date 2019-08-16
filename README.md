<p align="left">
  <a href="https://www.progress.com/kinvey" style="display: inline-block;">
    <img src="logo-progresskinvey.png">
  </a>
</p>

# [Kinvey Android SDK](https://devcenter.kinvey.com/android)

![badge-jitpack] ![badge-status] ![badge-coverage]

# Overview

[Kinvey](https://www.progress.com/kinvey) is a high-productivity serverless application development platform that provides developers tools to build robust, multi-channel applications utilizing a cloud backend and front-end SDKs. As a platform, Kinvey provides many solutions to common development needs, such as a data store, data integration, single sign-on integration, and file storage. With Kinvey, developers can focus on building what provides value for their app - the user experience (UX) and business logic of the application. This approach increases developer productivity and aims to enable higher quality apps by leveraging Kinvey's pre-built components.

# Features

The Kinvey Android SDK repository represents the package that can be used to develop Android apps on the Kinvey platform. The Kinvey SDK is developed using a mix of Kotlin and Java code, with a gradual transition being made to a completely Kotlin codebase.

## Contents

The following is a high-level overview of the most important projects in the solution:

* `java-api-core` The core of the library. Most of the library functionality is written here. This project contains most of the underlying networking, user management, caching logic. Things that are Android-platform specific are represented as interfaces / abstract classes, and implemented in the `android-lib` library described below.
* `android-lib`: The wrapper library for Android, built on top of `java-api-core`. All the Android-specific implementation is located here, and most of the classes in this library extend from the ones in `java-api-core`.

## Using the SDK

Refer to the Kinvey [DevCenter](http://devcenter.kinvey.com/android) for guides and documentation on using Kinvey.

# Releases

Versioning of the Kinvey SDK follows the guidelines stated in [Semantic Version 2.0.0](http://semver.org/).

# Build Instructions

In order to build this repository, the following pre-requisites must be in place:

* [android sdk](http://developer.android.com/sdk/index.html)
* Set JAVA_HOME
* Set ANDROID_HOME

Once these are set, you can run the following commands:

```
./gradlew clean
```

```
./gradlew release
```

After performing these commands, a `.zip` file with generated `.aar` and `.jar` files should be in the `/release/zipped` diretory.

# Test Instructions

```
./gradlew test jacocoTestReport
```

Before running the tests:

 * Connect android device or start emulator with min SDK version 15
 * Add app.key and app.secret to android-lib/src/androidTest/assets/kinvey.properties
 * Create User with username: test password: test in your console app
 * Custom endpoints tests and Social networks tests should be configured additionally

```
./gradlew connectedAndroidTest --info
```

# API Reference

Documentation for using the Kinvey SDK as well as other parts of the Kinvey platform can be found in the Kinvey DevCenter [reference](https://devcenter.kinvey.com/android/reference/) guide.

# Contribute

Feedback on our SDK is welcome and encouraged. Please, use [GitHub Issues](https://github.com/Kinvey/android-sdk/issues) on this repository for reporting a bug or requesting a feature for this SDK. Please reference our [contribution guide](CONTRIBUTING.md) for more information.

We would also love to have your contributions! You may also reference our [contribution guide](CONTRIBUTING.md) for details on how to submit a pull request (PR) for this repository.

# License

See [LICENSE](LICENSE.txt) for details.

[badge-jitpack]: https://img.shields.io/jitpack/v/github/Kinvey/android-sdk
[badge-status]: https://api.cirrus-ci.com/github/Kinvey/android-sdk.svg?branch=master
[badge-coverage]: https://codecov.io/gh/Kinvey/android-sdk/graph/badge.svg
