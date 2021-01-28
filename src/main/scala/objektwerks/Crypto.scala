package objektwerks

object Crypto {
  import javax.crypto.Cipher
  import javax.crypto.SecretKeyFactory
  import javax.crypto.spec.IvParameterSpec
  import javax.crypto.spec.PBEKeySpec
  import javax.crypto.spec.SecretKeySpec
  import java.util.Base64

  import scala.util.Try

  private val iv = Array[Byte](0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
  private val ivParamSpec = new IvParameterSpec(iv)

  def encrypt(sharedSecret: String,
              sharedSalt: String,
              text: String): Option[String] =
    Try {
      val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
      val keySpec = new PBEKeySpec(sharedSecret.toCharArray, sharedSalt.getBytes, 65536, 256)
      val secret = keyFactory.generateSecret(keySpec)
      val secretKey = new SecretKeySpec(secret.getEncoded, "AES")
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParamSpec)
      Base64.getEncoder.encodeToString( cipher.doFinal(text.getBytes("UTF-8")) )
    }.toOption

  def decrypt(sharedSecret: String,
              sharedSalt: String,
              encryptedText: String): Option[String] =
    Try {
      val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
      val keySpec = new PBEKeySpec(sharedSecret.toCharArray, sharedSalt.getBytes, 65536, 256)
      val secret = keyFactory.generateSecret(keySpec)
      val secretKey = new SecretKeySpec(secret.getEncoded, "AES")
      val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParamSpec)
      cipher.doFinal( Base64.getDecoder.decode(encryptedText) ).mkString
    }.toOption
}