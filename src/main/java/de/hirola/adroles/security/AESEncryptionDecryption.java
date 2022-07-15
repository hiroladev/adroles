package de.hirola.adroles.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotNull;


/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A simple util to encrypt and decrypt strings.
 * The secret key is read from the configuration.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public final class AESEncryptionDecryption {
    private static final String ALGORITHM = "AES";

    public static String encrypt(@NotNull String stringToEncrypt) throws GeneralSecurityException {
        if (stringToEncrypt == null) {
            throw new GeneralSecurityException("Argument must be not null!");
        }
        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getSecreteKey());
        return Base64.getEncoder().encodeToString(cipher.doFinal(stringToEncrypt.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decrypt(@NotNull String stringToDecrypt) throws GeneralSecurityException {
        if (stringToDecrypt == null) {
            throw new GeneralSecurityException("Argument must be not null!");
        }
        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getSecreteKey());
        return new String(cipher.doFinal(Base64.getDecoder().decode(stringToDecrypt)));
    }

    private static SecretKeySpec getSecreteKey() throws NoSuchAlgorithmException {
        final String myKey = "123"; //TODO: load from config
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, "AES");
    }
}
