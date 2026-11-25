package com.vel.util;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ValueDto2 {
   public Map<String, Serializable> values = new HashMap();
   public Long userId;
   public Date date;
   public Long folderIndex;
   public Long objectEntryId;
   List<Map<String, String>> docs;
}
