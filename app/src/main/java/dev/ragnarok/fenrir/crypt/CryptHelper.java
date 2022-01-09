package dev.ragnarok.fenrir.crypt;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.util.Base64;

import com.google.gson.Gson;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import dev.ragnarok.fenrir.util.Utils;

public class CryptHelper {

    @MessageType
    public static int analizeMessageBody(String text) {
        @MessageType
        int type = MessageType.NORMAL;

        if (Utils.safeIsEmpty(text)) {
            type = MessageType.NORMAL;
        } else if (isKeyExchangeServiceMessage(text)) {
            type = MessageType.KEY_EXCHANGE;
        } else if (isAes(text)) {
            type = MessageType.CRYPTED;
        }

        // Exestime.log("analizeMessageBody", start, "length: " + (Objects.isNull(text) ? 0 : text.length()), "type: " + type);
        // 0 ms
        return type;
    }

    /**
     * Является ли сообщение служебным (для обмена ключами шифрования)
     *
     * @param text текст сообщения
     * @return true - если сообщение является служебным, использовалось для обмена ключами шифрования
     */
    private static boolean isKeyExchangeServiceMessage(String text) {
        if (isEmpty(text)) {
            return false;
        }

        try {
            if (!text.endsWith("}") || !text.startsWith("RSA{")) {
                return false;
            }

            String exchangeMessageBody = text.substring(3); // without RSA on start
            ExchangeMessage message = new Gson().fromJson(exchangeMessageBody, ExchangeMessage.class);
            return nonNull(message)
                    && 0 < message.getSessionId()
                    && 0 < message.getVersion()
                    && 0 < message.getSenderSessionState();
        } catch (Exception e) {
            return false;
        }
    }

    // проверяем удовлетворяет ли текст формату AES{$key_location_policy}{$session_id}:{$encrypted_body}
    // (А-аптемезацея)
    private static boolean isAes(String text) {
        if (isNull(text) || 0 == text.length()) {
            return false;
        }

        int digitsCount = 0;
        boolean yesAes = false;
        boolean hasDivider = false;

        out:
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            switch (i) {
                case 0:
                    if ('A' == c) {
                        yesAes = true;
                    } else {
                        break out;
                    }
                    break;
                case 1:
                    if ('E' != c) {
                        yesAes = false;
                    }
                    break;
                case 2:
                    if ('S' != c) {
                        yesAes = false;
                    }
                    break;

                default:
                    boolean digit = Character.isDigit(c);

                    if (digit) {
                        digitsCount++;
                    } else {
                        if (':' == c) {
                            hasDivider = true;
                            break out;
                        } else {
                            return false;
                        }
                    }

                    break;
            }

            if (!yesAes) {
                break;
            }
        }

        return yesAes && 1 < digitsCount && hasDivider;
    }

    public static String encryptWithAes(String body, String key, String ifError, long sessionId,
                                        @KeyLocationPolicy int keyLocationPolicy) {
        try {
            return "AES" + keyLocationPolicy + sessionId
                    + ":" + AESCrypt.encrypt(key, body);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return ifError;
        }
    }

    public static String decryptWithAes(String body, String key) throws GeneralSecurityException {
        return AESCrypt.decrypt(key, body);
    }

    public static EncryptedMessage parseEncryptedMessage(String body) throws EncryptedMessageParseException {
        if (isEmpty(body)) {
            return null;
        }

        // AES{$key_location_policy}{$session_id}:{$encrypted_body}
        try {
            int dividerLocation = body.indexOf(':');

            @KeyLocationPolicy
            int keyLocationPolicy = Character.getNumericValue(body.charAt(3));

            long sessionId = Long.parseLong(body.substring(4, dividerLocation));
            String originalBody = body.substring(dividerLocation + 1);

            return new EncryptedMessage(sessionId, originalBody, keyLocationPolicy);
        } catch (Exception e) {
            throw new EncryptedMessageParseException();
        }
    }

    public static PublicKey createRsaPublicKeyFromString(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] byteKey = Base64.decode(key.getBytes(), Base64.DEFAULT);
        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(X509publicKey);
    }

    public static String generateRandomAesKey(int keysize) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(keysize); // for example
        SecretKey secretKey = keyGen.generateKey();
        byte[] encoded = secretKey.getEncoded();
        return Base64.encodeToString(encoded, Base64.DEFAULT);
    }

    public static KeyPair generateRsaKeyPair(int keysize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keysize);
        return keyGen.genKeyPair();
    }

    /**
     * Encrypt the plain text using public key.
     *
     * @param text : original plain text
     * @param key  :The public key
     * @return Encrypted text
     * @throws java.lang.Exception
     */
    public static byte[] encryptRsa(String text, PublicKey key) throws BadPaddingException, IllegalBlockSizeException,
            InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        byte[] cipherText;

        // get an RSA cipher object and print the provider
        Cipher cipher = Cipher.getInstance("RSA");
        // encrypt the plain text using the public key
        cipher.init(Cipher.ENCRYPT_MODE, key);
        cipherText = cipher.doFinal(text.getBytes());
        return cipherText;
    }

    /**
     * Decrypt text using private key.
     *
     * @param text :encrypted text
     * @param key  :The private key
     * @return plain text
     * @throws java.lang.Exception
     */
    public static String decryptRsa(byte[] text, PrivateKey key) throws InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException {
        byte[] dectyptedText;

        // get an RSA cipher object and print the provider
        Cipher cipher = Cipher.getInstance("RSA");

        // decrypt the text using the private key
        cipher.init(Cipher.DECRYPT_MODE, key);
        dectyptedText = cipher.doFinal(text);
        return new String(dectyptedText);
    }
}
