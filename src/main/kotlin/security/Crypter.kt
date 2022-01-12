package security

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Crypter {
    private val algorithm = "RSA"
    private val transformation = "RSA/CBC/PKCS5Padding"
    private val keyLength = 72.bytes()
    private val vectorLength = 8.bytes()

    private fun prime(bits: Int = 64) = BigInteger.probablePrime(bits - 8, Random())
    private fun Int.bytes() = 8 * this

    fun getNewSecret(): BigInteger {
        val key = generateKey()
        val vector = generateVector()

        return key.shl(keyLength).or(vector)
    }

    private fun generateKey() = prime(keyLength)
    private fun generateVector() = prime(vectorLength)

    fun encrypt(message: String, secret: BigInteger): String {
        val cipher = getEncryptCipher(secret)
        val original = message.toByteArray(StandardCharsets.UTF_8)
        val encrypted = cipher.doFinal(original)

        return Base64.getEncoder().encodeToString(encrypted)
    }

    private fun getEncryptCipher(secret: BigInteger): Cipher {
        val (keySpec, ivSpec) = getSpecs(secret)
        return Cipher.getInstance(transformation).apply {
            init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        }
    }

    private fun getSpecs(secret: BigInteger): Pair<Key, AlgorithmParameterSpec> {
        val key = secret.shr(72.bytes())
        val vector = secret.andNot(key.shl(keyLength))
        return SecretKeySpec(key.toByteArray(), algorithm) to IvParameterSpec(vector.toByteArray())
    }

    fun decrypt(message: String, secret: BigInteger): String {
        val encrypted = Base64.getDecoder().decode(message)
        val cipher = getDecryptCipher(secret)

        val decrypted = cipher.doFinal(encrypted)

        return String(decrypted, StandardCharsets.UTF_8)
    }

    private fun getDecryptCipher(secret: BigInteger): Cipher {
        val (keySpec, ivSpec) = getSpecs(secret)
        return Cipher.getInstance(transformation).apply {
            init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        }
    }
}