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
package com.kinvey.android;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import java.security.*;

/**
 * @author edwardf
 */
public class Crypto{

    private static final String TAG = "Kinvey - Crypto";
    private static final String RSA = "RSA";
    public static PublicKey pubKey;
    public static PrivateKey privateKey;


    public static void generateKey() throws Exception{
        PRNGFixes.apply();
        KeyPairGenerator gen = KeyPairGenerator.getInstance(RSA);
        gen.initialize(512, new SecureRandom());
        KeyPair keyPair = gen.generateKeyPair();
        pubKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }
    private static byte[] encrypt(String text, PublicKey pubRSA) throws Exception{
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.ENCRYPT_MODE, pubRSA);
        return cipher.doFinal(text.getBytes());
    }
    public final static String encrypt(String text) throws Exception{
        initKeys();
        return Base64.encodeToString(encrypt(text, pubKey), Base64.DEFAULT);
    }

    public final static String decrypt(String data) throws Exception{
        initKeys();
        return new String(decrypt(Base64.decode(data.getBytes(), Base64.DEFAULT)));
    }

    private static byte[] decrypt(byte[] src) throws Exception{
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(src);
    }

    private static void initKeys(){
        if (pubKey != null && privateKey != null){
            return;
        }
        try{
            generateKey();

        }catch (Exception e){
            Log.e(TAG, "Couldn't generate keys -> " + e.getMessage());
            e.printStackTrace();

        }
    }
}





