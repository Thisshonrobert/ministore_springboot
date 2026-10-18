//member 3
package com.codewithmosh.store;

import com.codewithmosh.store.services.PasswordHasher;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHasherTest {
    private final PasswordHasher passwordHasher = new PasswordHasher();

    @Test
    void hashesWithRandomSaltAndVerifiesThePassword() {
        String first = passwordHasher.hash("StorePass123!");
        String second = passwordHasher.hash("StorePass123!");

        assertThat(first).startsWith("pbkdf2_sha256$210000$");
        assertThat(first).isNotEqualTo(second);
        assertThat(first).doesNotContain("StorePass123!");
        assertThat(passwordHasher.matches("StorePass123!", first)).isTrue();
        assertThat(passwordHasher.matches("StorePass123!", second)).isTrue();
        assertThat(passwordHasher.matches("wrong-password", first)).isFalse();
    }

    @Test
    void rejectsMissingAndMalformedHashes() {
        assertThat(passwordHasher.matches("StorePass123!", null)).isFalse();
        assertThat(passwordHasher.matches(null, "pbkdf2_sha256$210000$abc$def")).isFalse();
        assertThat(passwordHasher.matches("StorePass123!", "plain-text")).isFalse();
        assertThat(passwordHasher.matches("StorePass123!", "pbkdf2_sha256$not-a-number$abc$def")).isFalse();
        assertThat(passwordHasher.matches("StorePass123!", "pbkdf2_sha256$1000$abc$def")).isFalse();
    }
}
