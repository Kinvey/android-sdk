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

import android.os.Build;
import android.util.Base64;
import android.util.Log;
import com.kinvey.android.secure.PRNGFixes;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Calendar;

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

    private static final int JELLYBEAN43 = 18;
    private static final String AES = "AES";
    private static final String SECRET_KEY = "kinvey-keys";


    /**
     * Public wrapper method to encrypt a string
     *
     * @param text the string to encrypt
     * @return the encrypted string
     * @throws Exception
     */
    public final static String encrypt(String text) throws Exception{
        String key = initKeys();
        return encrypt(text, key);
    }

    /**
     * Public wrapper method to decrypt a string, will load key
     *
     * @param data the encrypted string to decrypt
     * @return a decrypted version of the string
     * @throws Exception
     */
    public final static String decrypt(String data) throws Exception{
        String key = initKeys();
        return decrypt(data, key);
    }


    /**
     * This method will generate a secret key
     * @throws Exception
     */
    private static String generateKey() throws NoSuchAlgorithmException {
        PRNGFixes.apply();
        SecureRandom sr = new SecureRandom();
        sr.setSeed(Calendar.getInstance().getTimeInMillis());
        KeyGenerator kg = KeyGenerator.getInstance(AES);
        kg.init(128, sr);
        return toHex(new SecretKeySpec((kg.generateKey()).getEncoded(), AES).getEncoded());

    }

    /**
     * Encrypt a string with the public key
     * @param text - text to encrypt
     * @param secretKey - key to use to encrypt
     * @return - an encrypted representation of the input text
     * @throws Exception
     */
    private static String encrypt(String text, String secretKey) throws Exception{
        return toHex(getCipher(secretKey, Cipher.ENCRYPT_MODE).doFinal(text.getBytes()));

    }



    /**
     * Decrypt a string using the private key
     *
     * @param data - the string to decrypt
     * @param secretKey - key to use to decrypt
     * @return a decrypted byte[]
     * @throws Exception
     */
    private static String decrypt(String data, String secretKey) throws Exception{
        return new String(getCipher(secretKey, Cipher.DECRYPT_MODE).doFinal(toByte(data)));
    }


    /**
     * Create or load public/private keys for encryption.
     *
     * Returns that secret key
     */
    private static String initKeys() throws NoSuchAlgorithmException{
        KeyStore keystore;
        //get store wrapper dependant on runtime version
        if(Build.VERSION.SDK_INT >= JELLYBEAN43){
            keystore = KeyStoreJb43.getInstance();
        }else{
            keystore = KeyStore.getInstance();
        }

        //check if the keys exist in the store
        if(keystore.contains(SECRET_KEY)){
            //load bytes
            byte[] secretSpec = keystore.get(SECRET_KEY);
            return toHex(secretSpec);
        }else{
            //generate and save
            String secret = generateKey();
            keystore.put(SECRET_KEY, toByte(secret));
            return secret;
        }
    }

    public static void deleteKeys(){
        KeyStore keystore;
        //get store wrapper dependant on runtime version
        if(Build.VERSION.SDK_INT >= JELLYBEAN43){
            keystore = KeyStoreJb43.getInstance();
        }else{
            keystore = KeyStore.getInstance();
        }
        keystore.delete(SECRET_KEY);
    }

    public static CipherInputStream encryptInputStream(InputStream stream) throws Exception{
        return new CipherInputStream(stream, getCipher(initKeys(), Cipher.ENCRYPT_MODE));
    }

    public static CipherOutputStream encryptOutputStream(OutputStream stream) throws Exception{
        return new CipherOutputStream(stream, getCipher(initKeys(), Cipher.ENCRYPT_MODE));
    }

    public static InputStream decryptInputStream(CipherInputStream stream) throws Exception{
        return new CipherInputStream(stream, getCipher(initKeys(), Cipher.DECRYPT_MODE));
    }

    public static OutputStream decryptOutputStream(CipherOutputStream stream) throws Exception{
        return new CipherOutputStream(stream, getCipher(initKeys(), Cipher.DECRYPT_MODE));
    }

    private static Cipher getCipher(String secretKey, int mode) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException{
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), secretKey.getBytes(), 128, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey key = new SecretKeySpec(tmp.getEncoded(), AES);

        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(mode, key);

        return cipher;
    }



    private static byte[] toByte(String hexString) {
        int length = hexString.length()/2;

        byte[] res = new byte[length];

        for (int i = 0; i < length; i++)
            res[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return res;
    }

    public static String toHex(byte[] stringBytes) {
        StringBuffer res = new StringBuffer(2*stringBytes.length);

        for (int i = 0; i < stringBytes.length; i++) {
            res.append(HEX.charAt((stringBytes[i] >> 4)&0x0f)).append(HEX.charAt(stringBytes[i]&0x0f));
        }

        return res.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

}





