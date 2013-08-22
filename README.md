Kinvey Java Library
======

This library is a standalone library designed for all java evnironments.
The library acts as a client for the Kinvey REST api and can be used for
building Android apps and Java6 server applications.

## Build
Pre-requisites:

* [android sdk](http://developer.android.com/sdk/index.html)
* [maven 3.0.3](http://maven.apache.org/download.cgi)
   
```
mvn install
```

### Regenerate Javadocs

```
rm -r <devcenter.home>/content/reference/android/api/*
cd <project.home> 
mvn -Pdev javadoc:javadoc install
```

### Release

```
mvn -Prelease clean install
```

###Explicit release steps (including the above)
```
find and replace on version number (all poms.xml and RequestHeader version)
check in
double check/update devcenter.home location (in parent pom) relative to trunk/

git pull devcenter
remove current android javadocs (rm -r <devcenter.home>/content/reference/android/api/*)

rm -r <devcenter.home>/content/reference/android/api/*
cd <trunk> 
mvn -Pdev javadoc:javadoc install

node . to run at localhost:3000

//strange errors from above?
nvm is at ~/.nvm
rm -r node_modules
(npm install)
(npm update)

mvn -Prelease clean install

cd devcenter/content/downloads/android-changelog.md
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

svn up at root

svn merge -rLastRevisionMergedFromTrunkToBranch:HEAD url/of/trunk path/to/branch/wc
(merge any changes on trunk into correct branch or create new one for major release)
svn cp from branch/2.2.x to tag2.2.2 (tag is snapshot of release)

check it all in
```



## License

    Copyright 2013 Kinvey, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

