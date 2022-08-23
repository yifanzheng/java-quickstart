package top.yifan;

import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * DESUtil
 *
 * @author Star Zheng
 */
@SuppressWarnings("restriction")
public class DESUtil {

    private DESUtil() {
    }

    public static String encrypt(String ciphertext, String key) throws GeneralSecurityException {
        return encrypt(Base64.getEncoder().encode(ciphertext.getBytes()), key.getBytes());
    }

    public static String decrypt(String ciphertext, String key) throws GeneralSecurityException {
        return decrypt(Base64.getDecoder().decode(ciphertext), key.getBytes());
    }

    /**
     * DES解密
     *
     * @param data
     * @param key
     * @return
     * @throws GeneralSecurityException
     */
    private static String decrypt(byte[] data, byte[] key) throws GeneralSecurityException {
        SecureRandom sr = new SecureRandom();
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
        byte[] bytes = cipher.doFinal(data);
        byte[] bytes2 = Base64.getDecoder().decode(new String(bytes));
        return new String(bytes2);
    }

    /**
     * 进行DES加密
     *
     * @param data 明文密码的base64字符的byte数组
     * @param key  key值的byte数组
     * @throws GeneralSecurityException
     */
    private static String encrypt(byte[] data, byte[] key) throws GeneralSecurityException {
        SecureRandom sr = new SecureRandom();
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);
        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
        byte[] bytes = cipher.doFinal(data);
        return new BASE64Encoder().encode(bytes);
    }
}
