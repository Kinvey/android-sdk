Kinvey Java Library
======

This library is a standalone library designed for all java evnironments.
The library acts as a client for the Kinvey REST api and can be used for
building Android apps and Java6 server applications.

It is recommended you use either IntelliJ or Android Studio. Eclipse is NOT recommended.

##Documentation
Refer http://devcenter.kinvey.com/android for complete documentation of the library APIs and usage.

##Overview of the Library -

The codebase is made of the following key projects at the top level (under java-library): 

###java-api-core 
The core of the library. Most of the library logic is written here. This project contains most of the underlying networking, user management, caching logic. Things that are platform specific (android-specific or standalone-java-specific) are represented as interfaces / abstract classes, and implemented in the other libraries described below.

###android-lib
The wrapper library for android, built on top of java-api-core. All the android specific implementation goes here. Most of the classes in this library extend from the ones in java-api-core.

###java-lib
The wrapper library for java, built on top of java-api-core. All the standalone-java specific implementation goes here. Most of the classes in this library extend from the ones in java-api-core.

###android-secure
Encryption module built on top of android-lib. Rarely used; not compiled into the standard build process. This may be requested by certain customers who need encryption in their app.

###samples 
Samples built on top of the libraries. This is a submodule, the full source for samples is under https://github.com/KinveyApps

## Build
Pre-requisites:

* [android sdk](http://developer.android.com/sdk/index.html)
* [gradle build system](http://gradle.org/)

```
gradle clean
```

```
gradle release
```

```
gradle test jacocoTestReport
```
### Regenerate Javadocs

```
<devcenter.home> should be located in this relative path ../../../<devcenter.home> relatively <java-library.home>.
cd <devcenter.home> 
git pull devcenter
cd <java-library.home>
gradle release
```

### Release

```
cd <java-library.home>
gradle release
```

###Explicit release steps (including the above)
```
find and replace on version (build.gradle in project directory and RequestHeader version)
check in
double check/update devcenter.home location

git pull devcenter

cd <java-library.home>
gradle release

cd <devcenter.home> 
npm start

node . to run at localhost:3000

//strange errors from above?
nvm is at ~/.nvm
rm -r node_modules
(npm install)
(npm update)

cd devcenter/content/downloads/android-v3.0-changelog.md
update changelog

login to AWS S3 and upload zip from trunk/release
modify links in content/downloads.json

test locally
commit
push to origin master
push to staging
check it
push to prod
check it

```



## License

    Copyright 2014 Kinvey, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

