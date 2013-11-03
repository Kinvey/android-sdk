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
package com.kinvey.android.secure;

import android.util.Base64;
import android.util.Log;
import com.kinvey.android.secure.PRNGFixes;

import javax.crypto.Cipher;
import java.security.*;

/**
 * This class offers publicly accessible methods for handling on-device encryption of arbitrary strings.
 * <p>
 * It assumes Keys are stored locally, which are managed by the version dependant KeyStore
 * </p>
 * <p>
 * This class applies the PRNG fixes recommended by Google to handle possible low entropy in /dev/urandom
 * </p>
 *
 * @author edwardf
 */
public class Crypto{

    private static final String TAG = "Kinvey - Crypto";
    private static final String RSA = "RSA";
    private static final int KEY_SIZE = 512;
    public static PublicKey pubKey;
    public static PrivateKey privateKey;


    /**
     * This method will generate a public and private key
     * @throws Exception
     */
    private static KeyPair generateKey() throws Exception{
        PRNGFixes.apply();
        KeyPairGenerator gen = KeyPairGenerator.getInstance(RSA);
        gen.initialize(KEY_SIZE, new SecureRandom());
        KeyPair keyPair = gen.generateKeyPair();
        pubKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
        return keyPair;
    }

    /**
     * Encrypt a string with the public key
     * @param text - text to encrypt
     * @param pubRSA - public key to use to encrypt
     * @return - an encrypted representation of the input text
     * @throws Exception
     */
    private static byte[] encrypt(String text, PublicKey pubRSA) throws Exception{
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.ENCRYPT_MODE, pubRSA);
        return cipher.doFinal(text.getBytes());
    }

    /**
     * Public wrapper method to encrypt a string, will load key
     *
     * @param text the string to encrypt
     * @return
     * @throws Exception
     */
    public final static String encrypt(String text, String pass) throws Exception{
        initKeys(pass);
        return Base64.encodeToString(encrypt(text, pubKey), Base64.DEFAULT);
    }

    /**
     * Public wrapper method to decrypt a string, will load key
     *
     * @param data the encrypted string to decrypt
     * @return a decrypted version of the string
     * @throws Exception
     */
    public final static String decrypt(String data, String pass) throws Exception{
        initKeys(pass);
        return new String(decrypt(Base64.decode(data.getBytes(), Base64.DEFAULT)));
    }

    /**
     * Decrypt a string using the private key
     *
     * @param src the byte[] representation of the string to decrypt
     * @return a decrypted byte[]
     * @throws Exception
     */
    private static byte[] decrypt(byte[] src) throws Exception{
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(src);
    }


    /**
     * Create or load public/private keys for encryption.
     *
     */
    private static void initKeys(String pass){
        if (pubKey != null && privateKey != null){
            return;
        }
        try{
            KeyPair pair = generateKey();
        }catch (Exception e){
            Log.e(TAG, "Couldn't generate keys -> " + e.getMessage());
            e.printStackTrace();

        }
    }


}





