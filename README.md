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
rm <devcenter.home>/content/reference/api/android
cd <project.home> 
mvn -Ddev javadoc:javadoc install
```

### Release

```
mvn -Drelease clean install
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

