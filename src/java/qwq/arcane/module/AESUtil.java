package qwq.arcane.module;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;

public class AESUtil {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128; // 认证标签长度
    private static final int IV_LENGTH = 12; // GCM推荐12字节IV
    private static final int SALT_LENGTH = 16; // 盐值长度
    private static final int ITERATIONS = 65536; // PBKDF2迭代次数
    private static final int KEY_LENGTH = 256; // 密钥长度

    // 主密码（实际部署时应从安全配置源获取）
    private static final char[] MASTER_PASSWORD = "MySuperSecretPass".toCharArray();
    private static final byte[] SALT = "FixedSaltForDemo".getBytes(); // 示例用固定盐

    public static String encrypt(String plaintext) throws Exception {
        // 1. 生成随机IV
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        // 2. 派生密钥
        SecretKey secretKey = deriveKey();

        // 3. 初始化加密器
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        // 4. 加密并组合IV+密文
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String base64Ciphertext) throws Exception {
        // 1. 解码Base64
        byte[] combined = Base64.getDecoder().decode(base64Ciphertext);

        // 2. 分离IV和密文
        byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

        // 3. 派生密钥
        SecretKey secretKey = deriveKey();

        // 4. 初始化解密器
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        // 5. 解密
        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext);
    }

    private static SecretKey deriveKey() throws Exception {
        // 使用PBKDF2增强密钥
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(
                MASTER_PASSWORD,
                SALT,
                ITERATIONS,
                KEY_LENGTH
        );
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }
}