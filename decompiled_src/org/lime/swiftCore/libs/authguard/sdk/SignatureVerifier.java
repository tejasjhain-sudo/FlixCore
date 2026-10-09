package org.lime.swiftCore.libs.authguard.sdk;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

class SignatureVerifier {
   private static final Logger LOGGER = Logger.getLogger(SignatureVerifier.class.getName());
   private static final long DEFAULT_WINDOW_SECONDS = 120L;

   private SignatureVerifier() {
   }

   static SignatureResult verify(JsonObject var0, String var1, String var2, long var3) {
      if (var3 <= 0L) {
         var3 = 120L;
      }

      if (var0.has("verification") && !var0.get("verification").isJsonNull()) {
         try {
            JsonObject var5 = var0.getAsJsonObject("verification");
            JsonObject var6 = var5.getAsJsonObject("payload");
            if (var6 == null) {
               return SignatureResult.failure("Verification block has no payload");
            } else {
               String var7 = getJsonString(var6, "nonce");
               if (var7 != null && var7.equals(var1)) {
                  String var8 = getJsonString(var6, "timestamp");
                  if (var8 == null) {
                     return SignatureResult.failure("Verification payload has no timestamp");
                  } else {
                     Instant var9;
                     try {
                        var9 = Instant.parse(var8);
                     } catch (Exception var18) {
                        return SignatureResult.failure("Invalid timestamp format: " + var8);
                     }

                     long var10 = Math.abs(Duration.between(var9, Instant.now()).getSeconds());
                     if (var10 > var3) {
                        return SignatureResult.failure("Signed response is too old: " + var10 + "s (max " + var3 + "s)");
                     } else {
                        String var12 = getJsonString(var5, "algorithm");
                        if (var12 == null) {
                           var12 = "SHA256withRSA";
                        }

                        String var13 = getJsonString(var5, "signedPayload");
                        String var14 = getJsonString(var5, "signature");
                        if (var13 != null && var14 != null) {
                           PublicKey var15 = fetchPublicKey(var2);
                           if (var15 == null) {
                              return SignatureResult.failure("Failed to fetch server public key from " + var2);
                           } else {
                              Signature var16 = Signature.getInstance(var12);
                              var16.initVerify(var15);
                              var16.update(var13.getBytes(StandardCharsets.UTF_8));
                              boolean var17 = var16.verify(Base64.getDecoder().decode(var14));
                              return !var17
                                 ? SignatureResult.failure("RSA signature verification failed — response may be tampered")
                                 : SignatureResult.success();
                           }
                        } else {
                           return SignatureResult.failure("Verification block missing signedPayload or signature");
                        }
                     }
                  }
               } else {
                  return SignatureResult.failure("Nonce mismatch: expected " + var1 + " but got " + var7);
               }
            }
         } catch (Exception var19) {
            LOGGER.log(Level.WARNING, "Signature verification error", (Throwable)var19);
            return SignatureResult.failure("Signature verification error: " + var19.getMessage());
         }
      } else {
         LOGGER.fine("Server did not return a signed verification block — skipping signature check");
         return SignatureResult.unsupported();
      }
   }

   private static PublicKey fetchPublicKey(String var0) {
      HttpURLConnection var1 = null;

      Object var21;
      try {
         String var2 = var0 + "/api/v1/verify/public-key";
         var1 = (HttpURLConnection)URI.create(var2).toURL().openConnection();
         var1.setRequestMethod("GET");
         var1.setRequestProperty("User-Agent", "AuthGuard-SDK/1.0.0");
         var1.setRequestProperty("Accept", "application/json");
         var1.setConnectTimeout(8000);
         var1.setReadTimeout(8000);
         int var17 = var1.getResponseCode();
         if (var17 < 200 || var17 >= 300) {
            LOGGER.warning("Public key endpoint returned HTTP " + var17);
            return null;
         }

         BufferedReader var5 = new BufferedReader(new InputStreamReader(var1.getInputStream(), StandardCharsets.UTF_8));

         String var4;
         try {
            StringBuilder var6 = new StringBuilder();

            while ((var7 = var5.readLine()) != null) {
               var6.append(var7);
            }

            var4 = var6.toString();
         } catch (Throwable var14) {
            try {
               var5.close();
            } catch (Throwable var13) {
               var14.addSuppressed(var13);
            }

            throw var14;
         }

         var5.close();
         JsonObject var19 = JsonParser.parseString(var4).getAsJsonObject();
         String var20 = getJsonString(var19, "publicKey");
         if (var20 != null) {
            return parsePublicKey(var20);
         }

         LOGGER.warning("Public key endpoint did not return a 'publicKey' field");
         var21 = null;
      } catch (Exception var15) {
         LOGGER.log(Level.WARNING, "Failed to fetch public key", (Throwable)var15);
         return null;
      } finally {
         if (var1 != null) {
            var1.disconnect();
         }
      }

      return (PublicKey)var21;
   }

   private static PublicKey parsePublicKey(String var0) throws Exception {
      String var1 = var0.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replaceAll("\\s", "");
      byte[] var2 = Base64.getDecoder().decode(var1);
      return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(var2));
   }

   private static String getJsonString(JsonObject var0, String var1) {
      return var0.has(var1) && !var0.get(var1).isJsonNull() ? var0.get(var1).getAsString() : null;
   }
}
