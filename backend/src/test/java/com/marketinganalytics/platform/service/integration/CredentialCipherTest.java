package com.marketinganalytics.platform.service.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CredentialCipherTest {

    @Test
    void encryptsAndDecryptsBackToTheOriginalValue() {
        CredentialCipher cipher = new CredentialCipher("a-test-passphrase-that-is-long-enough");

        String encrypted = cipher.encrypt("super-secret-access-token");

        assertThat(encrypted).isNotEqualTo("super-secret-access-token");
        assertThat(cipher.decrypt(encrypted)).isEqualTo("super-secret-access-token");
    }

    @Test
    void producesDifferentCiphertextForTheSamePlaintextEachTime() {
        CredentialCipher cipher = new CredentialCipher("a-test-passphrase-that-is-long-enough");

        String first = cipher.encrypt("token-value");
        String second = cipher.encrypt("token-value");

        assertThat(first).isNotEqualTo(second);
        assertThat(cipher.decrypt(first)).isEqualTo("token-value");
        assertThat(cipher.decrypt(second)).isEqualTo("token-value");
    }

    @Test
    void handlesNullValuesWithoutThrowing() {
        CredentialCipher cipher = new CredentialCipher("a-test-passphrase-that-is-long-enough");

        assertThat(cipher.encrypt(null)).isNull();
        assertThat(cipher.decrypt(null)).isNull();
    }

    @Test
    void refusesToStartWithoutAnEncryptionKey() {
        assertThatThrownBy(() -> new CredentialCipher(""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_ENCRYPTION_KEY");
    }

    @Test
    void cannotDecryptWithADifferentKey() {
        CredentialCipher cipherA = new CredentialCipher("passphrase-one-long-enough-for-aes");
        CredentialCipher cipherB = new CredentialCipher("passphrase-two-long-enough-for-aes");

        String encrypted = cipherA.encrypt("token-value");

        assertThatThrownBy(() -> cipherB.decrypt(encrypted)).isInstanceOf(IllegalStateException.class);
    }
}
