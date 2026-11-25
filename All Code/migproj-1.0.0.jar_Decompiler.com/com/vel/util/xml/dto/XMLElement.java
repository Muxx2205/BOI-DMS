package com.vel.util.xml.dto;

import com.vel.util.xml.service.ChildElementParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLElement {
   private String name;
   private char[] values;
   private List<XMLElement> chileds;
   private Map<String, List<XMLElement>> childMap;
   private Map<String, String> attrMap;

   public void addAttribute(String key, String val) {
      if (this.attrMap == null) {
         this.attrMap = new HashMap();
      }

      this.attrMap.put(key, val);
   }

   public String getAttribute(String key) {
      return this.attrMap != null ? (String)this.attrMap.get(key) : null;
   }

   public boolean isIgnore() {
      return this.attrMap != null && this.attrMap.containsKey("ignore") && ((String)this.attrMap.get("ignore")).equalsIgnoreCase("true");
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public char[] getValues() {
      return this.values;
   }

   public String getText() {
      return this.values == null || this.chileds != null && this.chileds.size() != 0 ? "" : (new String(this.values)).trim();
   }

   public void setValues(char[] values) {
      this.values = values;
   }

   public List<XMLElement> getChileds() {
      return this.chileds;
   }

   public void setChileds(List<XMLElement> chileds) {
      this.chileds = chileds;
   }

   public Map<String, List<XMLElement>> getChildMap() {
      return this.childMap;
   }

   public void setChildMap(Map<String, List<XMLElement>> childMap) {
      this.childMap = childMap;
   }

   public void addElement(String name, String value) {
      XMLElement ch = new XMLElement();
      ch.setName(name);
      ch.setValues(value.toCharArray());
      this.addElement(ch);
   }

   public void addElement(XMLElement ch) {
      if (this.chileds == null) {
         this.chileds = new ArrayList();
      }

      this.chileds.add(ch);
      if (this.childMap == null) {
         this.childMap = new HashMap();
      }

      if (!this.childMap.containsKey(ch.getName())) {
         this.childMap.put(ch.name, new ArrayList());
      }

      ((List)this.childMap.get(ch.getName())).add(ch);
   }

   public String asXml() {
      StringBuffer sb = new StringBuffer();
      if (this.chileds != null) {
         sb.append("<").append(this.name).append(">").append("\n");

         for(XMLElement ch : this.chileds) {
            sb.append(ch.asXml()).append("\n");
         }

         sb.append("</").append(this.name).append(">");
      } else {
         sb.append("<").append(this.name).append(">");
         if (this.values != null) {
            sb.append(new String(this.values));
         }

         sb.append("</").append(this.name).append(">");
      }

      return sb.toString();
   }

   public void parseChilds() throws Exception {
      new ChildElementParser(this);
   }
}
