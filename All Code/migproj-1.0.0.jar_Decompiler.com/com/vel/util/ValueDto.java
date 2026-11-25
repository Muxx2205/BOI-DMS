package com.vel.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ValueDto {
   public Map<String, Object> values = new HashMap();
   public String username;
   public Long userId;
   public Date date;
   public Long folderIndex;
   public Long objectEntryId;
   List<Map<String, String>> docs;
}
