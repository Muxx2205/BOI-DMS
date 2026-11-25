package com.vel.util.xml.util;

import com.vel.util.xml.dto.FieldXml;
import com.vel.util.xml.dto.FormXml;
import com.vel.util.xml.dto.XMLElement;
import com.vel.util.xml.service.XMLParserService;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestUtil {
   static String path = "d:/formmetadata";

   public static void main(String[] args) throws Exception {
      FormXml xml = getMetadata("D:/stardeskform", "KRIReporting.xml");
      System.out.println(xml.getFormId());

      for(String s : xml.getFields().keySet()) {
         FieldXml fx = (FieldXml)xml.getFields().get(s);
         PrintStream var10000 = System.out;
         String var10001 = fx.getName();
         var10000.println(var10001 + " " + fx.getValue());
      }

   }

   public static FormXml getMetadata(String folder, String fname) throws Exception {
      FileReader read = new FileReader(folder + "/" + fname);
      StringBuffer sb = new StringBuffer();
      int val = -1;

      while((val = read.read()) >= 0) {
         sb.append((char)val);
      }

      read.close();
      FormXml root = XMLParserService.rootElementConf(sb.toString());
      return root;
   }

   private static Map<String, Object> getMapData(List<XMLElement> lst) {
      Map<String, Object> mp = new HashMap();

      for(XMLElement c : lst) {
         if (c.getChileds() != null && c.getChileds().size() > 0) {
            mp.put(c.getAttribute("lray"), c.getName());
            mp.put(c.getAttribute("lray") + "list", getMapData(c.getChileds()));
         } else {
            mp.put(c.getText(), c.getName());
         }
      }

      return mp;
   }
}
