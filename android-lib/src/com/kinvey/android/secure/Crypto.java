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

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;

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
    private static final String RSA = "RSA";
    private static final int KEY_SIZE = 512;
    private static final String KINVEY_PRIVATE_MOD = "kinvey_private_mod";
    private static final String KINVEY_PUBLIC_MOD = "kinvey_public_mod";
    private static final String KINVEY_PRIVATE_EXP = "kinvey_private_exp";
    private static final String KINVEY_PUBLIC_EXP = "kinvey_public_exp";

    private static PublicKey pubKey;
    private static PrivateKey privateKey;
    private static KeyStore keystore;


    /**
     * This method will generate a public and private key
     * @throws Exception
     */
    private static KeyPair generateKey() throws NoSuchAlgorithmException{
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

        //get store wrapper dependant on runtime version
        if(Build.VERSION.SDK_INT >= JELLYBEAN43){
            keystore = KeyStoreJb43.getInstance();
        }else{
            keystore = KeyStore.getInstance();
        }


        //check if the keys exist in the store
        if(keystore.contains(KINVEY_PUBLIC_EXP) && keystore.contains(KINVEY_PRIVATE_EXP)){
            //load bytes
            byte[] privateExp = keystore.get(KINVEY_PRIVATE_EXP);
            byte[] privateMod = keystore.get(KINVEY_PRIVATE_MOD);
            byte[] publicExp = keystore.get(KINVEY_PUBLIC_EXP);
            byte[] publicMod = keystore.get(KINVEY_PUBLIC_MOD);
            //create keyspec from bytes
            RSAPublicKeySpec pubSpec = new RSAPublicKeySpec(new BigInteger(publicMod), new BigInteger(publicExp));
            RSAPrivateKeySpec privateSpec = new RSAPrivateKeySpec(new BigInteger(privateMod), new BigInteger(privateExp));


            KeyFactory keyFact = null;
            //create RSA keyfactory and generate keys from bytes
            try {
                keyFact = KeyFactory.getInstance(RSA);
                privateKey = keyFact.generatePrivate(privateSpec);
                pubKey = keyFact.generatePublic(pubSpec);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch(InvalidKeySpecException e){
                e.printStackTrace();
            }




        }else{
            //generate and save
            try{
                KeyPair pair = generateKey();
                RSAPrivateKey RSAprivateKey = ((RSAPrivateKey) pair.getPrivate());
                RSAPublicKey RSApublicKey = ((RSAPublicKey) pair.getPublic());

                keystore.put(KINVEY_PRIVATE_EXP, RSAprivateKey.getPrivateExponent().toByteArray());
                keystore.put(KINVEY_PRIVATE_MOD, RSAprivateKey.getModulus().toByteArray());

                keystore.put(KINVEY_PUBLIC_EXP, RSApublicKey.getPublicExponent().toByteArray());
                keystore.put(KINVEY_PUBLIC_MOD, RSApublicKey.getModulus().toByteArray());

            } catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }

        }



    }

    public static void deleteKeys(){
        keystore.delete(KINVEY_PRIVATE_MOD);
        keystore.delete(KINVEY_PRIVATE_EXP);
        keystore.delete(KINVEY_PUBLIC_MOD);
        keystore.delete(KINVEY_PUBLIC_MOD);
    }


}





