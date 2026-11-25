package com.vel.util.xml.service;

import com.vel.util.xml.dto.XMLElement;

public class ChildElementParser {
   private XMLElement parentEle;
   private String eleText;

   public ChildElementParser(XMLElement parent) throws Exception {
      this.parentEle = parent;
      this.eleText = new String(parent.getValues());
      if (this.eleText != null) {
         this.eleText = this.eleText.trim();
      }

      this.parse();
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

   private boolean parse() throws Exception {
      boolean flag = true;
      if (this.eleText != null && this.eleText.length() > 5 && this.eleText.startsWith("<")) {
         while(flag && this.eleText.length() > 5) {
            XMLElement ch = this.getChEle();
            flag = ch != null;
            if (flag) {
               if (ch.isIgnore()) {
                  ch.parseChilds();
                  if (ch.getChileds() != null) {
                     for(XMLElement e : ch.getChileds()) {
                        this.parentEle.addElement(e);
                     }
                  }
               } else {
                  this.parentEle.addElement(ch);
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private XMLElement getChEle() {
      try {
         XMLElement ch = new XMLElement();
         String name = this.eleText.substring(0, this.eleText.indexOf(">"));
         name = name.substring(name.indexOf("<") + 1);
         if (name.endsWith("/")) {
            if (name.contains(" ")) {
               name = name.substring(0, name.indexOf(32));
            }

            this.eleText = this.eleText.substring(this.eleText.indexOf(">") + 1);
            name = name.substring(0, name.length() - 1);
         } else {
            String nam = name;
            if (name.contains(" ")) {
               String ign = name.substring(name.indexOf(32)).trim();

               try {
                  this.parseAttr(ign, ch);
               } catch (Exception e) {
                  e.printStackTrace();
                  System.out.println("Incorrect Ignore Attr");
               }

               name = name.substring(0, name.indexOf(32));
            }

            String txt = this.eleText.substring(this.eleText.indexOf("<" + nam + ">") + ("<" + nam + ">").length(), this.eleText.indexOf("</" + name + ">"));
            this.eleText = this.eleText.substring(this.eleText.indexOf("</" + name + ">") + ("</" + name + ">").length());
            ch.setValues(txt.toCharArray());
         }

         ch.setName(name.replace("my:", ""));
         return ch;
      } catch (Exception ex) {
         ex.printStackTrace();
         return null;
      }
   }
}
