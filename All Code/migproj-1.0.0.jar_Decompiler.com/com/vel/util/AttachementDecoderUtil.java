package com.vel.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AttachementDecoderUtil {
   private static final int FIXED_HEADER = 16;
   private int fileSize;
   private int attachmentNameLength;
   private String attachmentName;
   private byte[] decodedAttachment;

   public AttachementDecoderUtil(String strBase64EncodedString) throws Exception {
      this.getFile(strBase64EncodedString);
   }

   private String getFile(String strBase64EncodedString) {
      byte[] theData = Base64.getDecoder().decode(strBase64EncodedString);

      try {
         Throwable var3 = null;
         Object var4 = null;

         try {
            ByteArrayInputStream ms = new ByteArrayInputStream(theData);

            String var10000;
            try {
               DataInputStream theReader = new DataInputStream(ms);

               try {
                  var10000 = this.decodeAttachment(theReader, strBase64EncodedString);
               } finally {
                  if (theReader != null) {
                     theReader.close();
                  }

               }
            } catch (Throwable var20) {
               if (var3 == null) {
                  var3 = var20;
               } else if (var3 != var20) {
                  var3.addSuppressed(var20);
               }

               if (ms != null) {
                  ms.close();
               }

               throw var3;
            }

            if (ms != null) {
               ms.close();
            }

            return var10000;
         } catch (Throwable var21) {
            if (var3 == null) {
               var3 = var21;
            } else if (var3 != var21) {
               var3.addSuppressed(var21);
            }

            throw var3;
         }
      } catch (IOException e) {
         e.printStackTrace();
         return "Exception: " + e.getMessage();
      }
   }

   private String decodeAttachment(DataInputStream theReader, String fileContentBase) {
      try {
         byte[] headerData = new byte[16];
         theReader.readFully(headerData);
         this.fileSize = Integer.reverseBytes(theReader.readInt());
         this.attachmentNameLength = Integer.reverseBytes(theReader.readInt()) * 2;
         byte[] fileNameBytes = new byte[this.attachmentNameLength];
         theReader.readFully(fileNameBytes);
         this.attachmentName = new String(fileNameBytes, 0, this.attachmentNameLength - 2, StandardCharsets.UTF_16LE);
         this.decodedAttachment = new byte[this.fileSize];
         theReader.readFully(this.decodedAttachment);
         byte[] theData = Base64.getDecoder().decode(fileContentBase);
         byte[] fileContent = new byte[theData.length - (24 + this.attachmentNameLength)];
         System.arraycopy(theData, 24 + this.attachmentNameLength, fileContent, 0, fileContent.length);
         this.decodedAttachment = fileContent;
      } catch (Exception ex) {
         String var10001 = ex.getMessage();
         this.attachmentName = "Exception: " + var10001 + " " + String.valueOf(ex.getStackTrace());
      }

      return this.attachmentName;
   }

   public int getFileSize() {
      return this.fileSize;
   }

   public void setFileSize(int fileSize) {
      this.fileSize = fileSize;
   }

   public int getAttachmentNameLength() {
      return this.attachmentNameLength;
   }

   public void setAttachmentNameLength(int attachmentNameLength) {
      this.attachmentNameLength = attachmentNameLength;
   }

   public String getAttachmentName() {
      return this.attachmentName;
   }

   public void setAttachmentName(String attachmentName) {
      this.attachmentName = attachmentName;
   }

   public byte[] getDecodedAttachment() {
      return this.decodedAttachment;
   }

   public void setDecodedAttachment(byte[] decodedAttachment) {
      this.decodedAttachment = decodedAttachment;
   }
}
