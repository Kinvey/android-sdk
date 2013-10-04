/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import com.kinvey.java.Client;

/**
 * @author edwardf
 */
public class HelloWorld {

    public static void main(String[] args){
        System.out.println("Hello World");

        Client myJavaClient = new Client.Builder("kid_ePZ9kJuZMi","3b16c9a8fb8e4b90bf1c71e5b0fe87eb").build();
        Boolean ping = myJavaClient.ping();

        System.out.println("Client ping -> " + ping);
    }
}
