/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;


import service.EncryptDecryptService;
import service.EncryptDecryptServiceImpl;
import com.google.common.base.Throwables;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.net.util.Base64;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;

/**
 *
 * @author DPPH
 */
public class ControllerEncDec {

    EncryptDecryptService service;
    private String decrypt;
    private byte[] encrypt;
    private final ControllerLog controllerLog;

    public ControllerEncDec(String iv, String key) {
        this.service = new EncryptDecryptServiceImpl(iv, key);
        this.controllerLog = new ControllerLog();
    }

    public String getDecrypt(byte[] decodedBytes) throws ClassNotFoundException, Exception {
        this.decrypt = this.service.decrypt(decodedBytes);
        return this.decrypt;
    }

    public byte[] getEncrypt(String str) throws ClassNotFoundException, Exception {
        this.encrypt = this.service.encrypt(str);
        return this.encrypt;
    }

    //--------------------dari sistem sebelumnya, untuk enc dec pin di database
    private byte[] konversiKeByte(String Kunci) {
        byte[] array_byte = new byte[32];
        int i = 0;
        while (i < Kunci.length()) {
            array_byte[i] = (byte) Kunci.charAt(i);
            i++;
        }
        if (i < 32) {
            while (i < 32) {
                array_byte[i] = (byte) i;
                i++;
            }
        }
        return array_byte;
    }

    private Key generateKey(String kunciEnkripsi) throws Exception {
        Key key = new SecretKeySpec(konversiKeByte(kunciEnkripsi), "AES");
        return key;
    }

    public String encryptDbVal(String text, String Kunci) throws Exception {
        // SecretKeySpec spec = getKeySpec();
        Key key = generateKey(Kunci);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        BASE64Encoder enc = new BASE64Encoder();
        return enc.encode(cipher.doFinal(text.getBytes())).toString();
    }

    public String decryptDbVal(String text, String Kunci) throws Exception {
        // SecretKeySpec spec = getKeySpec();
        String result = null;
        try {
            Key key = generateKey(Kunci);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            BASE64Decoder dec = new BASE64Decoder();
            result = new String(cipher.doFinal(dec.decodeBuffer(text)));
        } catch (Exception e) {
            String s = Throwables.getStackTraceAsString(e);
            controllerLog.logErrorWriter(s);
        } finally {
            // optional, use this block if necessary
        }

        return result;
    }
    public String createSignature(String text,String key){
        // SecretKeySpec spec = getKeySpec();
        String result = null;
        try {
//            text = text.replace(";;;;", "");
//            text = text.replace("\"", "");
//            text = text.replace("\\", "");
//            text = text.replace("+", "");
//            text = text.replaceAll("\\s","");
            String regx = ",.\" \n";
            char[] ca = text.toCharArray();
            for (char c : ca) {
                String ab= c+"";
                if(!ab.matches("[A-Za-z0-9{}:,.]+")){
                    text = text.replace(""+c, "");
                }
                // input = input.replace(""+c, "");
            }
            String signature = text.trim().toUpperCase();
            System.out.println("signatureRaw: "+signature);
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String hexString = Hex.encodeHexString(sha256_HMAC.doFinal(signature.getBytes()));
            System.out.println("hexString: "+hexString);

            BigInteger bigint = new BigInteger(hexString, 16);

            StringBuilder sb = new StringBuilder();
            byte[] ba = Base64.encodeInteger(bigint);
            for (byte b : ba) {
                sb.append((char)b);
            }
            result = sb.toString();

            }
        catch (Exception e){
            System.out.println("Error");
        }
        System.out.println("createSignature: "+result);

        return result;
    }

    public String encryptSha256 (String text){
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes());
            StringBuffer res = new StringBuffer();
            for (byte b : md.digest()) res.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));

            result =  res.toString();
        }catch (Exception e){
            controllerLog.logErrorWriter("encryptSha256 :  " + e.getMessage());

        }
        return  result;
    }
    public static String encryptMd5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }
    public static void main(String[] args){

        ControllerEncDec encDec = new ControllerEncDec("1234567890123456", "1234567890123456");
        byte[] decodedBytes = Base64.decodeBase64("fBLm1U7wmTENuxtJF3RP9JU5V7oe6SAMqhpqWPkxSXa1a4bJOIr+ePRJqQyaynXSxdCBmtBO5kZy/CiSZdw4KFkiRmoVQ6nVSjCtRNw3oc8g+sF0duN/ptO++qENoW1T");

        try {
            encDec.getDecrypt(decodedBytes);
            String queryJson = encDec.getDecrypt(decodedBytes);
            System.out.println(queryJson);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //----------------------parsing the json message-------------------------------

    }
}
