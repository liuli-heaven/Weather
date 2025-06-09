package com.example.weather.net
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.time.ZonedDateTime
import java.util.Base64


fun generateJWT(privateKeyStr: String, sub: String, kid: String): String{
    val privateKeyBytes = Base64.getDecoder().decode(privateKeyStr)
    val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
    val keyFactory = KeyFactory.getInstance("EdDSA")
    val privateKey = keyFactory.generatePrivate(keySpec)
    //Header
    val headerJson = "{\"alg\": \"EdDSA\", \"kid\": \"$kid\"}"
    //Payload
    val iat = ZonedDateTime.now().toEpochSecond() - 30
    val exp = iat + 900
    val payloadJson = "{\"sub\": \"$sub\", \"iat\": \"$iat\", \"exp\": \"$exp\"}"
    // Base64URL
    val headerEncoded = Base64.getUrlEncoder().encodeToString(headerJson.toByteArray())
    val payloadEncoded = Base64.getUrlEncoder().encodeToString(payloadJson.toByteArray())
    val data = "$headerEncoded.$payloadEncoded"
    //Sign
    val signer = Signature.getInstance("EdDSA")
    signer.initSign(privateKey)
    signer.update(data.toByteArray())
    val signature = signer.sign()

    val signatureString = Base64.getUrlEncoder().encodeToString(signature)
    val jwt = "$data.$signatureString"

    return jwt
}

