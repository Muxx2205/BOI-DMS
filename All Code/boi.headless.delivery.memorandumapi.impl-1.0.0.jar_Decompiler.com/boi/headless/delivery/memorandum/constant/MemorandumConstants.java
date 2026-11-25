package boi.headless.delivery.memorandum.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MemorandumConstants {
   public static final String MEMORANDUM = "MEMORANDUM";
   public static final String PSRS = "PSRS";
   public static final String PAIMS = "PAIMS";
   public static final String BRANCH = "BRANCH";
   public static final String ZONE = "ZONE";
   public static final String NBG = "NBG";
   public static final String HO = "HO";
   public static final String MAKER = "MAKER";
   public static final String RECOMMENDER = "RECOMMENDER";
   public static final String APPROVAL = "APPROVAL";
   public static final String MEMOAPPROVED = "MEMOAPPROVED";
   public static final Map<String, String> MEMORANDUM_Files;
   public static final Map<String, String> PSRS_Files;
   public static final Map<String, String> PAIMS_Files;

   public static Map<String, String> getObjectFileNames(String departmentName) {
      switch (departmentName) {
         case "MEMORANDUM":
            return MEMORANDUM_Files;
         case "PSRS":
            return PSRS_Files;
         case "PAIMS":
            return PAIMS_Files;
         default:
            return new HashMap();
      }
   }

   static {
      new HashMap();
      Map<String, String> map = new HashMap();
      map.put("file", "file");
      MEMORANDUM_Files = Collections.unmodifiableMap(map);
      map = new HashMap();
      map.put("file", "file");
      PSRS_Files = Collections.unmodifiableMap(map);
      map = new HashMap();
      map.put("file", "file");
      PAIMS_Files = Collections.unmodifiableMap(map);
   }
}
