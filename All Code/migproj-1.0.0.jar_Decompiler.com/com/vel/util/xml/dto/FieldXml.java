package com.vel.util.xml.dto;

import java.util.HashMap;
import java.util.Map;

public class FieldXml {
   private String name;
   private String value;
   private Map<String, FieldXml> fields;

   public FieldXml() {
   }

   public FieldXml(XMLElement ch) {
      this.name = ch.getText();
      this.value = ch.getName();
      if (ch.getChileds() != null && ch.getChileds().size() > 0) {
         this.name = ch.getAttribute("lray");
         this.fields = new HashMap();

         for(XMLElement c : ch.getChileds()) {
            FieldXml fx = new FieldXml(c);
            this.fields.put(fx.getName(), fx);
         }
      }

   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public Map<String, FieldXml> getFields() {
      return this.fields;
   }

   public void setFields(Map<String, FieldXml> fields) {
      this.fields = fields;
   }

   public void addChild(FieldXml ch) {
      if (this.fields == null) {
         this.fields = new HashMap();
      }

      this.fields.put(ch.getName(), ch);
   }

   public FieldXml getChild(String name) {
      return this.fields == null ? null : (FieldXml)this.fields.get(name);
   }
}
