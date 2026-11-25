package com.vel.util.xml.dto;

import java.util.HashMap;
import java.util.Map;

public class FormXml {
   private long formId;
   private Map<String, FieldXml> fields;

   public long getFormId() {
      return this.formId;
   }

   public void setFormId(long formId) {
      this.formId = formId;
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

   public void populat(XMLElement root) {
      if (root != null && root.getChileds() != null) {
         if (this.fields == null) {
            this.fields = new HashMap();
         }

         for(XMLElement c : root.getChileds()) {
            FieldXml fx = new FieldXml(c);
            this.fields.put(fx.getName(), fx);
         }
      }

   }
}
