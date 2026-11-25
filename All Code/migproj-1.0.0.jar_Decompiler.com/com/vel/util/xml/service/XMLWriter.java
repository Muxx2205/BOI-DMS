package com.vel.util.xml.service;

import com.vel.util.xml.dto.XMLElement;
import java.io.FileWriter;

public class XMLWriter {
   public static void writeXml(XMLElement root, String filePath) throws Exception {
      FileWriter out = new FileWriter(filePath);
      out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n".toCharArray());
      out.write(root.asXml().toCharArray());
      out.close();
   }
}
