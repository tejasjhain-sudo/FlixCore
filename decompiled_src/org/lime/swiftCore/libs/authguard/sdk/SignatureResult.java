package org.lime.swiftCore.libs.authguard.sdk;

class SignatureResult {
   private final boolean verified;
   private final boolean serverSupportsSignature;
   private final String failureReason;

   private SignatureResult(boolean var1, boolean var2, String var3) {
      this.verified = var1;
      this.serverSupportsSignature = var2;
      this.failureReason = var3;
   }

   static SignatureResult success() {
      return new SignatureResult(true, true, null);
   }

   static SignatureResult unsupported() {
      return new SignatureResult(false, false, "Server does not support response signing");
   }

   static SignatureResult failure(String var0) {
      return new SignatureResult(false, true, var0);
   }

   boolean isVerified() {
      return this.verified;
   }

   boolean isServerSupportsSignature() {
      return this.serverSupportsSignature;
   }

   String getFailureReason() {
      return this.failureReason;
   }
}
