package com.data.mig.util;

import com.liferay.portal.kernel.util.FileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileStorageUtil {
   private String rootPath;
   private File rootFolder;

   public FileStorageUtil(String rootPath) {
      this.rootPath = rootPath;
      this.rootFolder = new File(rootPath);
   }

   private File getFilePath(String datadefId, String fid) {
      String path = datadefId + "/";
      if (fid.length() >= 3) {
         path = path + "/" + fid.substring(0, 3);
      }

      if (fid.length() >= 6) {
         path = path + "/" + fid.substring(3, 6);
      }

      return new File(this.rootFolder, path);
   }

   public void saveFile(String datadefId, String fid, byte[] bt) throws Exception {
      File dirpath = this.getFilePath(datadefId, fid);
      FileUtil.mkdirs(dirpath);
      FileOutputStream fout = new FileOutputStream(new File(dirpath, fid));
      fout.write(bt);
      fout.close();
   }

   public InputStream getFileData(String datadefId, String fid) throws Exception {
      return new FileInputStream(new File(this.getFilePath(datadefId, fid), fid));
   }
}
