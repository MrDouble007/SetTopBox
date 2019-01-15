package com.oeasy.stb.app.push;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public abstract class Coder {
    /**
     * MAC算法可选以下多种算法
     * <pre>
     * HmacMD5
     * HmacSHA1
     * HmacSHA256
     * HmacSHA384
     * HmacSHA512
     * </pre>
     */
    private static final String KEY_MAC = "HmacSHA256";
    private static final String KEY_SHA = "SHA";
    private static final String KEY_MD5 = "MD5";

    /**
     * MD5加密
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static String encryptMD5(String value) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
        md5.update(Coder.decryptBASE64(value));
        return new BigInteger(md5.digest()).toString(16);

    }

    /**
     * SHA加密
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static String encryptSHA(String value) throws Exception {
        MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
        sha.update(Coder.decryptBASE64(value));
        return new BigInteger(sha.digest()).toString(32);
    }

    /**
     * HMAC加密
     *
     * @param value
     * @param key
     * @return
     * @throws Exception
     */
    public static String encryptHMAC(String value, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(decryptBASE64(key), KEY_MAC);
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);
        return new BigInteger(mac.doFinal(Coder.decryptBASE64(value))).toString(32);
    }

    /**
     * BASE64解密
     *
     * @param key
     * @return
     * @throws Exception
     */
    private static byte[] decryptBASE64(String key) throws Exception {
        return new BASE64Decoder().decodeBuffer(key);
    }

    /**
     * BASE64加密
     *
     * @param key
     * @return
     * @throws Exception
     */
    private static String encryptBASE64(byte[] key) throws Exception {
        return new BASE64Encoder().encodeBuffer(key);
    }

    /**
     * 初始化HMAC密钥
     *
     * @return
     * @throws Exception
     */
//	private static String initMacKey() throws Exception {
//		KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_MAC);
//		SecretKey secretKey = keyGenerator.generateKey();
//		return encryptBASE64(secretKey.getEncoded());
//	}
    public static void main(String[] args) throws Exception {
        String inputStr = "for english it will bee good ?";
        System.err.println("原文:/n" + inputStr);

        byte[] inputData = inputStr.getBytes(Charset.defaultCharset());
        String code = Coder.encryptBASE64(inputData);

        System.err.println("BASE64加密后:/n" + code);

        byte[] output = Coder.decryptBASE64(code);

        String outputStr = new String(output, Charset.defaultCharset());

        System.err.println("BASE64解密后:/n" + outputStr);

        System.err.println("MD5:/n" + Coder.encryptMD5(inputStr));

        System.err.println("SHA:/n" + Coder.encryptSHA(inputStr));
        //"robotkey","token":"78e3e41a-33b3-11e6-879f-005056c00001
        System.err.println("HMAC:/n" + Coder.encryptHMAC("78e3e41a-33b3-11e6-879f-005056c00001", "robotkey"));
    }
}
