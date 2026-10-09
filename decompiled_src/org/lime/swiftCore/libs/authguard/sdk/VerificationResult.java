package org.lime.swiftCore.libs.authguard.sdk;

public class VerificationResult {
   private final boolean valid;
   private final String message;
   private final String productId;
   private final String expiresAt;
   private final String ipUsage;
   private final String hwidUsage;
   private final String discordUsername;
   private final boolean signatureVerified;

   public VerificationResult(boolean var1, String var2, String var3, String var4, String var5, String var6, String var7) {
      this(var1, var2, var3, var4, var5, var6, var7, false);
   }

   public VerificationResult(boolean var1, String var2, String var3, String var4, String var5, String var6, String var7, boolean var8) {
      this.valid = var1;
      this.message = var2;
      this.productId = var3;
      this.expiresAt = var4;
      this.ipUsage = var5;
      this.hwidUsage = var6;
      this.discordUsername = var7;
      this.signatureVerified = var8;
   }

   public VerificationResult(boolean var1, String var2) {
      this(var1, var2, null, null, null, null, null, false);
   }

   public boolean isValid() {
      return this.valid;
   }

   public String getMessage() {
      return this.message;
   }

   public String getProductId() {
      return this.productId;
   }

   public String getExpiresAt() {
      return this.expiresAt;
   }

   public String getIpUsage() {
      return this.ipUsage;
   }

   public String getHwidUsage() {
      return this.hwidUsage;
   }

   public String getDiscordUsername() {
      return this.discordUsername;
   }

   public boolean isSignatureVerified() {
      return this.signatureVerified;
   }
}
