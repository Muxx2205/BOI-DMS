package com.vel.util.xml.service;

import com.vel.util.xml.dto.FormXml;
import com.vel.util.xml.dto.XMLElement;
import java.io.File;
import java.io.FileReader;

public class XMLParserService {
   static long fId;
   private static String rootName = "NGOGetDocumentBDOResponse";

   public static XMLElement rootElement(String xmlStr) throws Exception {
      XMLElement ele = new XMLElement();
      int lngth = rootName.length() + 1;
      String str = xmlStr.replace("<my:", "<");
      str = str.replace("</my:", "</");
      StringBuffer sb = new StringBuffer(str);
      str = sb.substring(sb.indexOf("<" + rootName) + lngth, sb.indexOf("</" + rootName + ">"));
      str = str.substring(str.indexOf(">") + 1);
      ele.setName(rootName);
      ele.setValues(str.toCharArray());
      new ChildElementParser(ele);
      return ele;
   }

   public static FormXml rootElementConf(String xmlStr) throws Exception {
      FormXml frm = new FormXml();
      int lngth = rootName.length() + 1;
      String str = xmlStr.replace("<my:", "<");
      str = str.replace("</my:", "</");
      str = str.replace("\n", "");
      StringBuffer sb = new StringBuffer(str);
      str = sb.substring(sb.indexOf("<" + rootName) + lngth, sb.indexOf("</" + rootName + ">"));
      String attrstr = str.substring(0, str.indexOf(">")).trim();
      if (attrstr.length() > 4 && attrstr.startsWith("formId")) {
         String[] s = attrstr.split("=");
         fId = Long.parseLong(s[1].replace("\"", ""));
         frm.setFormId(fId);
         str = str.substring(str.indexOf(">") + 1);
         XMLElement ele = new XMLElement();
         ele.setName(rootName);
         ele.setValues(str.toCharArray());
         new ChildElementParser(ele);
         frm.populat(ele);
         return frm;
      } else {
         throw new Exception("Form id not found");
      }
   }

   private void parseAttr(String atStr, XMLElement ch) {
      if (atStr != null && atStr.trim().length() > 3) {
         String[] prm = atStr.split(" ");

         for(String atr : prm) {
            if (atr.contains("=")) {
               String[] a = atr.split("=");
               ch.addAttribute(a[0], a[1].replace("\"", ""));
            }
         }
      }

   }

   public static XMLElement rootElement(File xmlFile) throws Exception {
      XMLElement ele = new XMLElement();
      FileReader read = new FileReader(xmlFile);
      StringBuffer sb = new StringBuffer();
      String rname = rootName;
      int val = -1;

      while((val = read.read()) >= 0) {
         sb.append((char)val);
      }

      read.close();
      int lngth = rname.length() + 1;
      String str = sb.toString().replace("<my:", "<");
      str = str.replace("</my:", "</");
      sb = new StringBuffer(str);
      str = sb.substring(sb.indexOf("<" + rname) + lngth, sb.indexOf("</" + rname + ">"));
      str = str.substring(str.indexOf(">") + 1);
      ele.setName(rootName);
      ele.setValues(str.toCharArray());
      new ChildElementParser(ele);
      return ele;
   }
}
