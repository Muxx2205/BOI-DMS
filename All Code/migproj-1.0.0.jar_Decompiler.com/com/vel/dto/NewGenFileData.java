package com.vel.dto;

import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.vel.util.xml.dto.XMLElement;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class NewGenFileData {
   private String fileName;
   private String mimetype;
   private String content;
   private String extension;
   private long size;
   private Date data;
   private String uuid;
   private long fileId;
   private InputStream stream;
   byte[] bt;

   public NewGenFileData() {
   }

   public NewGenFileData(XMLElement ele) {
      try {
         this.fileName = this.parseName(((XMLElement)((List)ele.getChildMap().get("documentName")).get(0)).getText());
         if (this.fileName.contains("/")) {
            this.fileName = this.fileName.substring(this.fileName.lastIndexOf("/") + 1);
         }

         this.fileName = this.fileName.replace(":", "").replace("&", "").replace("\\", "").replace(";", "").replace('|', ' ').replace('"', ' ').replace("'", "");
         this.content = ((XMLElement)((List)ele.getChildMap().get("docContent")).get(0)).getText();
         this.size = Long.parseLong(((XMLElement)((List)ele.getChildMap().get("documentSize")).get(0)).getText());
         this.extension = ((XMLElement)((List)ele.getChildMap().get("createdByAppName")).get(0)).getText();
         this.parseContent();
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private String parseName(String name) {
      if (name.contains("/")) {
         name = name.substring(name.lastIndexOf("/") + 1);
      }

      if (name.contains("\\")) {
         name = name.substring(name.lastIndexOf("\\") + 1);
      }

      return name;
   }

   private void parseContent() {
      this.bt = Base64.getDecoder().decode(this.content);
      this.stream = new ByteArrayInputStream(this.bt);
      if (!this.fileName.endsWith(this.extension)) {
         this.fileName = this.fileName + "." + this.extension;
      }

      this.mimetype = MimeTypesUtil.getContentType(this.fileName);
   }

   public byte[] getBytes() {
      return this.bt;
   }

   public InputStream getStream() {
      return this.stream;
   }

   public void setStream(InputStream stream) {
      this.stream = stream;
   }

   public String getMimetype() {
      return this.mimetype;
   }

   public void setMimetype(String mimetype) {
      this.mimetype = mimetype;
   }

   public String getFileName() {
      return this.fileName;
   }

   public void setFileName(String fName) {
      this.fileName = this.parseName(fName);
      if (this.fileName.contains("/")) {
         this.fileName = this.fileName.substring(this.fileName.lastIndexOf("/") + 1);
      }

      this.fileName = this.fileName.replace(":", "").replace("&", "").replace("\\", "").replace(";", "").replace('|', ' ').replace('"', ' ');
   }

   public String getContent() {
      return this.content;
   }

   public void setContent(String content) {
      this.content = content;
   }

   public String getExtension() {
      return this.extension;
   }

   public void setExtension(String extension) {
      this.extension = extension;
   }

   public long getSize() {
      return this.size;
   }

   public void setSize(long size) {
      this.size = size;
   }

   public Date getData() {
      return this.data;
   }

   public void setData(Date data) {
      this.data = data;
   }

   public String getUuid() {
      return this.uuid;
   }

   public void setUuid(String uuid) {
      this.uuid = uuid;
   }

   public long getFileId() {
      return this.fileId;
   }

   public void setFileId(long fileId) {
      this.fileId = fileId;
   }

   public static void main(String[] args) {
      String fileName = "sdfs/asfdsafd/asfsaf/asas";
      if (fileName.contains("/")) {
         fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
         System.out.println(fileName);
      }

   }
}
