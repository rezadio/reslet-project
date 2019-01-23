/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

/**
 *
 * @author DPPH
 */
public interface EncryptDecryptService {

    byte[] encrypt(String plainText) throws Exception;

    String decrypt(byte[] cipherText) throws Exception;
}
