package com.vel.util.xml.service;

import com.vel.util.xml.dto.XMLElement;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class UserXMLParserService {
   static long fId;
   private static String rootName = "emp";

   public static Map<String, String> getUserData(String path) {
      Map<String, String> data = null;

      try {
         XMLElement ele = rootElement(new File(path));
         if (ele != null && ele.getChileds() != null) {
            data = new HashMap();

            for(XMLElement e : ele.getChileds()) {
               data.put(e.getName(), e.getText());
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      return data;
   }

   private static XMLElement rootElement(File xmlFile) throws Exception {
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
      String str = sb.substring(sb.indexOf("<" + rname) + lngth, sb.indexOf("</" + rname + ">"));
      str = str.substring(str.indexOf(">") + 1);
      ele.setName(rootName);
      ele.setValues(str.toCharArray());
      new ChildElementParser(ele);
      return ele;
   }
}
