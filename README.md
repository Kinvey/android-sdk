Kinvey Java Library
======

This library is a standalone library designed for all java environments.
The library acts as a client for the Kinvey REST api and can be used for
building Android apps and Java6 server applications.

It is recommended to use either IntelliJ or Android Studio. Eclipse is NOT recommended.

## Documentation
Refer https://devcenter.kinvey.com/android-v2 for complete documentation of the library APIs and usage.

## Overview of the Library -

The codebase is made of the following key projects at the top level (under java-library): 

### java-api-core 
The core of the library. Most of the library logic is written here. This project contains most of the underlying networking, user management, caching logic. Things that are platform specific (android-specific or standalone-java-specific) are represented as interfaces / abstract classes, and implemented in the other libraries described below.

### android-lib
The wrapper library for android, built on top of java-api-core. All the android specific implementation goes here. Most of the classes in this library extend from the ones in java-api-core.

### java-lib
The wrapper library for java, built on top of java-api-core. All the standalone-java specific implementation goes here. Most of the classes in this library extend from the ones in java-api-core.

###android-secure
Encryption module built on top of android-lib. Rarely used; not compiled into the standard build process. This may be requested by certain customers who need encryption in their app.

### samples 
Samples built on top of the libraries. This is a submodule, the full source for samples is under https://github.com/Kinvey/java-library/tree/ver2.x/samples

## Build
Pre-requisites:

* [android sdk](http://developer.android.com/sdk/index.html)
* Set JAVA_HOME
* Set ANDROID_HOME
* Download android_sdk/platforms/android-19, android_sdk/platforms/android-10

```
./gradlew clean release
```

After this .zip with generated .aar and .jar files should be in the directory: /release/zipped

## How to enable TLSv1.1/TLSv1.2 on the Android 4.1 - 4.4 versions
The Kinvey backend doesn't support TLSv1.0 connections anymore. Devices with Android 5.0+ support TLSv1.1/TLSv1.2 by default, so for these versions it doesn't need to add any changes. But it's disabled by default at the devices with Android 4.1 - 4.4. To enable TLSv1.1/TLSv1.2 on the Android 4.1 - 4.4 versions you need to:
- set up minSdkVersion = 16
- create a custom SSLSocketFactory that is going to proxy all calls to a default  SSLSocketFactory implementation. Override all createSocket methods and callsetEnabledProtocols on the returned SSLSocket to enable TLS 1.1 and TLS 1.2. For example:
```java
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


class KinveySocketFactory extends SSLSocketFactory {

    private static final String TLS = "TLS";
    private static final String TLSv1_1 = "TLSv1.1";
    private static final String TLSv1_2 = "TLSv1.2";

    private SSLSocketFactory internalSSLSocketFactory;

    KinveySocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext context = SSLContext.getInstance(TLS);
        context.init(null, null, null);
        internalSSLSocketFactory = context.getSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return internalSSLSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return internalSSLSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket());
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(address, port, localAddress, localPort));
    }

    private Socket enableTLSOnSocket(Socket socket) {
        if(socket != null && (socket instanceof SSLSocket)) {
            ((SSLSocket)socket).setEnabledProtocols(new String[] {TLSv1_1, TLSv1_2});
        }
        return socket;
    }
}
```
- set custom SSLSocketFactory to the NetHttpTransport.Builder in the Client.java class
 ```java       
private static HttpTransport newCompatibleTransport(){
    return android.os.Build.VERSION.SDK_INT >= 16 && android.os.Build.VERSION.SDK_INT <= 19 ?
            buildSupportHttpTransport() :
            new NetHttpTransport();
}

private static HttpTransport buildSupportHttpTransport() {
    NetHttpTransport httpTransport;
    try {
        httpTransport = new NetHttpTransport.Builder()
                .setProxy(null)
                .setHostnameVerifier(null)
                .setSslSocketFactory(new KinveySocketFactory())
                .build();
    } catch (KeyManagementException | NoSuchAlgorithmException e) {
        e.printStackTrace();
        httpTransport = new NetHttpTransport();
    }
    return httpTransport;
}
```
- make new build 

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

