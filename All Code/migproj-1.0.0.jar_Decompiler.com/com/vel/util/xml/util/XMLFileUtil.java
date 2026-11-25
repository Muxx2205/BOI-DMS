package com.vel.util.xml.util;

import com.vel.util.xml.dto.XMLElement;
import com.vel.util.xml.service.XMLParserService;
import com.vel.util.xml.service.XMLWriter;
import java.io.File;
import java.util.Map;

public class XMLFileUtil {
   public static void createFile(String path, String folderName, String fileName, Map<String, String> data) throws Exception {
      try {
         String fpath = path + "/" + folderName + "/" + fileName;
         File file = new File(fpath);
         XMLElement root = XMLParserService.rootElement(file);
         root.addElement("my:AuthorFLD", (String)data.get("Author"));
         root.addElement("my:CreatedFLD", (String)data.get("Created"));
         root.addElement("my:LoginFLD", (String)data.get("Login"));
         XMLWriter.writeXml(root, fpath);
         System.out.println(fpath + " File updated");
      } catch (Exception ex) {
         System.out.println("Exception in >> " + fileName);
         ex.printStackTrace();
         throw ex;
      }
   }

   public static XMLElement parseFile(String path) throws Exception {
      try {
         return XMLParserService.rootElement(new File(path));
      } catch (Exception ex) {
         ex.printStackTrace();
         throw ex;
      }
   }

   public static void main(String[] args) {
      String s = "?TSR,OPINION, RECOVERY�MATTERS";
      char[] ch = s.toCharArray();

      for(char c : ch) {
         System.out.println(c + "  " + c);
      }

   }
}
