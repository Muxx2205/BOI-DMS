package com.newgen.dao;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileVersion;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFileEntryLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFileEntryMetadataLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFileVersionLocalServiceUtil;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.vel.dto.NewGenFileData;
import com.vel.util.xml.dto.XMLElement;
import com.vel.util.xml.service.XMLParserService;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NewGenDbService extends Thread {
   private Connection con;
   String[] userNames = new String[]{"rr227081", "ap187008", "de227045", "hb227077", "ag171815", "ht209754", "ts227224", "gm227138", "mp227080", "ns227262", "mk197189"};
   String[] passwords = new String[]{"Rakhimukhi$9901", "Krishna@01092025", "September@987654321", "November##12345", "Boiindia!@#$%^", "January##12345", "Boistar@01092025", "Bankofindia@131313", "Varalakshmi@1986", "Chillbro@123456789", "CholaTigers@092025"};
   Random r = new Random();
   private DLFolder defaultFolder = null;
   private Long grpId;
   ServiceContext ctx;
   String newgenId;
   private Map<Long, Long> rmap = new HashMap();

   public List<Map<String, String>> getSubFoldersPrnding(Long folderid, Connection conn) throws Exception {
      List<Map<String, String>> list = new ArrayList();
      Map<String, String> rw = null;

      try {
         Statement stm = conn.createStatement();
         ResultSet set = stm.executeQuery("SELECT p.folderindex,p.name,p.fid,u.userid, pr.fid as pfid FROM zz_pdbfolder p  inner join zz_pdbfolder pr on (pr.folderindex=p.parentfolderindex and pr.fid is not null) left join zz_pdbuser u on(p.owner=u.userindex) WHERE  p.OBJECTID=0 and p.fid is null and p.parentfolderindex>1 ");

         while(set.next()) {
            rw = new HashMap();
            rw.put("folderindex", set.getString("folderindex"));
            rw.put("name", this.validateName(set.getString("name")));
            rw.put("fid", set.getString("fid"));
            rw.put("pfid", set.getString("pfid"));
            rw.put("userid", set.getString("userid"));
            list.add(rw);
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return list;
   }

   public List<Map<String, String>> getSubFolders(Long folderid, Connection conn) throws Exception {
      List<Map<String, String>> list = new ArrayList();
      Map<String, String> rw = null;

      try {
         Statement stm = conn.createStatement();
         ResultSet set = stm.executeQuery("SELECT p.folderindex,p.name,p.fid,u.userid FROM zz_pdbfolder p left join zz_pdbuser u on(p.owner=u.userindex) WHERE  OBJECTID=0 AND PARENTFOLDERINDEX = " + String.valueOf(folderid));

         while(set.next()) {
            rw = new HashMap();
            rw.put("folderindex", set.getString("folderindex"));
            rw.put("name", this.validateName(set.getString("name")));
            rw.put("fid", set.getString("fid"));
            rw.put("userid", set.getString("userid"));
            list.add(rw);
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return list;
   }

   private String validateName(String str) {
      if (str == null) {
         str = "name";
      } else if (str.trim().equalsIgnoreCase("null")) {
         int var10000 = this.r.nextInt(10000);
         str = var10000 + "-" + this.r.nextInt(100000);
      } else {
         str = str.replace("{", "").replace("}", "").replace("<", "").replace("#", "").replace(":", "-").replace(">", "").replace(";", "").replace(".", "").replace("@", "").replace("/", "").replace("\\", "").trim();
      }

      return str;
   }

   public void updateFolderIds(Long folderid, String uuid, long fid, Connection conn) throws Exception {
      try {
         Statement stm = conn.createStatement();
         stm.executeUpdate(" update zz_pdbfolder set uuid='" + uuid + "', fid=" + fid + " WHERE folderindex = " + String.valueOf(folderid));
         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public void updateFileIds(String docid, String uuid, String fid, String docname, String mtype, Connection conn) throws Exception {
      try {
         Statement stm = conn.createStatement();
         stm.executeUpdate("update ZZ_PDBDocument set uuid='" + uuid + "', fid=" + fid + ",docname='" + docname + "',mimetype='" + mtype + "' where DocumentIndex=" + docid);
         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public void updateFileIds(String docid, String uuid, String fid, Connection conn) throws Exception {
      try {
         Statement stm = conn.createStatement();
         stm.executeUpdate("update ZZ_PDBDocument set uuid='" + uuid + "', fid=" + fid + " where DocumentIndex=" + docid);
         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public void insertFileOrgLocation(long docid, String uuid, String prevfid, Connection conn) throws Exception {
      try {
         Statement stm = conn.createStatement();
         stm.executeUpdate("insert into tt_documentorglocation(docid,uuid,prefolderid) values(" + docid + "'" + uuid + "'," + prevfid + ")");
         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public List<Map<String, Object>> getFilesFromDataClassParent(int dataclass, String fromDate, String toDate, Connection conn) throws Exception {
      List<Map<String, Object>> list = new ArrayList();
      Map<String, Object> rw = null;

      try {
         Statement stm = conn.createStatement();
         ResultSet set = stm.executeQuery("SELECT d.DOCUMENTINDEX ,d.NAME,f.PARENTFOLDERINDEX,d.CREATEDDATETIME ,d.DOCUMENTSIZE, f.name,pf.fid,u.userid FROM ZZ_PDBDOCUMENT d LEFT JOIN ZZ_PDBDOCUMENTCONTENT c ON (c.DOCUMENTINDEX =d.DOCUMENTINDEX ) LEFT JOIN ZZ_PDBFOLDER f ON (c.PARENTFOLDERINDEX =f.FOLDERINDEX ) LEFT JOIN ZZ_PDBFOLDER pf ON (f.PARENTFOLDERINDEX =pf.FOLDERINDEX ) LEFT JOIN ZZ_PDBUSER u ON (d.OWNER =u.USERINDEX ) where d.datadefinitionindex=" + dataclass + "  AND d.uuid IS null AND  d.CREATEDDATETIME between to_date('" + fromDate + "','YYYY-MM-DD') and to_date('" + toDate + "','YYYY-MM-DD')");

         while(set.next()) {
            rw = new HashMap();
            rw.put("DID", set.getLong(1));
            rw.put("DNAME", set.getString(2));
            rw.put("FLDID", set.getLong(3));
            rw.put("DATE", set.getDate(4));
            rw.put("SIZE", set.getLong(5));
            rw.put("REF_NO", set.getString(6));
            rw.put("DLFLD", set.getLong(7));
            rw.put("USERID", set.getLong(8));
            list.add(rw);
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return list;
   }

   public List<Map<String, Object>> getFilesFromDataClassParent2(int dataclass, String fromDate, String toDate, Connection conn) throws Exception {
      List<Map<String, Object>> list = new ArrayList();
      Map<String, Object> rw = null;

      try {
         Statement stm = conn.createStatement();
         ResultSet set = stm.executeQuery("SELECT d.DOCUMENTINDEX ,d.NAME,f.PARENTFOLDERINDEX,d.CREATEDDATETIME ,d.DOCUMENTSIZE, f.name,pf.fid,u.userid,d.docname,d.mimetype FROM ZZ_PDBDOCUMENT d LEFT JOIN ZZ_PDBDOCUMENTCONTENT c ON (c.DOCUMENTINDEX =d.DOCUMENTINDEX ) LEFT JOIN ZZ_PDBFOLDER f ON (c.PARENTFOLDERINDEX =f.FOLDERINDEX ) LEFT JOIN ZZ_PDBFOLDER pf ON (f.PARENTFOLDERINDEX =pf.FOLDERINDEX ) LEFT JOIN ZZ_PDBUSER u ON (d.OWNER =u.USERINDEX ) where d.datadefinitionindex=" + dataclass + "  AND d.fid =1  AND  d.CREATEDDATETIME between to_date('" + fromDate + "','YYYY-MM-DD') and to_date('" + toDate + "','YYYY-MM-DD')");

         while(set.next()) {
            rw = new HashMap();
            rw.put("DID", set.getLong(1));
            rw.put("DNAME", set.getString(2));
            rw.put("FLDID", set.getLong(3));
            rw.put("DATE", set.getDate(4));
            rw.put("SIZE", set.getLong(5));
            rw.put("REF_NO", set.getString(6));
            rw.put("DLFLD", set.getLong(7));
            rw.put("USERID", set.getLong(8));
            rw.put("DOCNAME", set.getString(9));
            rw.put("MTYPE", set.getString(10));
            list.add(rw);
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return list;
   }

   public List<Map<String, Object>> getFilesFromDataClassDir(int dataclass, String fromDate, String toDate, Connection conn) throws Exception {
      List<Map<String, Object>> list = new ArrayList();
      Map<String, Object> rw = null;

      try {
         Statement stm = conn.createStatement();
         ResultSet set = stm.executeQuery("SELECT d.DOCUMENTINDEX ,d.NAME,c.PARENTFOLDERINDEX,d.CREATEDDATETIME ,d.DOCUMENTSIZE,f.fid, u.userid,f.name FROM ZZ_PDBDOCUMENT d LEFT JOIN ZZ_PDBDOCUMENTCONTENT c ON (c.DOCUMENTINDEX =d.DOCUMENTINDEX ) LEFT JOIN ZZ_PDBFOLDER f ON (c.PARENTFOLDERINDEX =f.FOLDERINDEX ) LEFT JOIN ZZ_PDBUSER u ON (d.OWNER =u.USERINDEX ) WHERE d.FID is null and  d.datadefinitionindex=" + dataclass + " AND d.CREATEDDATETIME between to_date('" + fromDate + "','YYYY-MM-DD') and to_date('" + toDate + "','YYYY-MM-DD')");

         while(set.next()) {
            rw = new HashMap();
            rw.put("DID", set.getLong(1));
            rw.put("DNAME", set.getString(2));
            rw.put("FLDID", set.getLong(3));
            rw.put("DATE", set.getDate(4));
            rw.put("SIZE", set.getLong(5));
            rw.put("DLFLD", set.getLong(6));
            rw.put("USERID", set.getLong(7));
            rw.put("REF_NO", set.getString(8));
            list.add(rw);
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return list;
   }

   public List<Map<String, Object>> getFilesFromDataClassDir2(int dataclass, String fromDate, String toDate, Connection conn) throws Exception {
      List<Map<String, Object>> list = new ArrayList();
      Map<String, Object> rw = null;

      try {
         Statement stm = conn.createStatement();
         ResultSet set = stm.executeQuery("SELECT d.DOCUMENTINDEX ,d.NAME,c.PARENTFOLDERINDEX,d.CREATEDDATETIME ,d.DOCUMENTSIZE,f.fid, u.userid,f.name , d.docname,d.mimetype  FROM ZZ_PDBDOCUMENT d LEFT JOIN ZZ_PDBDOCUMENTCONTENT c ON (c.DOCUMENTINDEX =d.DOCUMENTINDEX ) LEFT JOIN ZZ_PDBFOLDER f ON (c.PARENTFOLDERINDEX =f.FOLDERINDEX ) LEFT JOIN ZZ_PDBUSER u ON (d.OWNER =u.USERINDEX ) WHERE d.FID=1 and  d.datadefinitionindex=" + dataclass + " AND d.CREATEDDATETIME between to_date('" + fromDate + "','YYYY-MM-DD') and to_date('" + toDate + "','YYYY-MM-DD')");

         while(set.next()) {
            rw = new HashMap();
            rw.put("DID", set.getLong(1));
            rw.put("DNAME", set.getString(2));
            rw.put("FLDID", set.getLong(3));
            rw.put("DATE", set.getDate(4));
            rw.put("SIZE", set.getLong(5));
            rw.put("DLFLD", set.getLong(6));
            rw.put("USERID", set.getLong(7));
            rw.put("REF_NO", set.getString(8));
            rw.put("DOCNAME", set.getString(9));
            rw.put("MTYPE", set.getString(10));
            list.add(rw);
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return list;
   }

   public List<Map<String, Object>> getFilesDir2(String fromDate, String toDate, Connection conn) throws Exception {
      List<Map<String, Object>> list = new ArrayList();
      Map<String, Object> rw = null;

      try {
         Statement stm = conn.createStatement();
         ResultSet set = stm.executeQuery("SELECT d.DOCUMENTINDEX ,d.NAME,c.PARENTFOLDERINDEX,d.CREATEDDATETIME ,d.DOCUMENTSIZE,f.fid,u.userid , d.docname,d.mimetype FROM ZZ_PDBDOCUMENT d LEFT JOIN ZZ_PDBDOCUMENTCONTENT c ON (c.DOCUMENTINDEX =d.DOCUMENTINDEX ) LEFT JOIN ZZ_PDBFOLDER f ON (c.PARENTFOLDERINDEX =f.FOLDERINDEX ) LEFT JOIN ZZ_PDBUSER u ON (d.OWNER =u.USERINDEX ) WHERE d.FID=1 and  d.datadefinitionindex=0 AND  d.CREATEDDATETIME between to_date('" + fromDate + "','YYYY-MM-DD')  and to_date('" + toDate + "','YYYY-MM-DD')");

         while(set.next()) {
            rw = new HashMap();
            rw.put("DID", set.getLong(1));
            rw.put("DNAME", set.getString(2));
            rw.put("FLDID", set.getLong(3));
            rw.put("DATE", set.getDate(4));
            rw.put("SIZE", set.getLong(5));
            rw.put("DLFLD", set.getLong(6));
            rw.put("USERID", set.getLong(7));
            rw.put("DOCNAME", set.getLong(8));
            rw.put("MTYPE", set.getLong(9));
            list.add(rw);
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return list;
   }

   public List<Map<String, Object>> getFilesDir(String fromDate, String toDate, Connection conn) throws Exception {
      List<Map<String, Object>> list = new ArrayList();
      Map<String, Object> rw = null;

      try {
         Statement stm = conn.createStatement();
         ResultSet set = stm.executeQuery("SELECT d.DOCUMENTINDEX ,d.NAME,c.PARENTFOLDERINDEX,d.CREATEDDATETIME ,d.DOCUMENTSIZE,f.fid,u.userid FROM ZZ_PDBDOCUMENT d LEFT JOIN ZZ_PDBDOCUMENTCONTENT c ON (c.DOCUMENTINDEX =d.DOCUMENTINDEX ) LEFT JOIN ZZ_PDBFOLDER f ON (c.PARENTFOLDERINDEX =f.FOLDERINDEX ) LEFT JOIN ZZ_PDBUSER u ON (d.OWNER =u.USERINDEX ) WHERE d.FID is null and  d.datadefinitionindex=0 AND  d.CREATEDDATETIME between to_date('" + fromDate + "','YYYY-MM-DD')  and to_date('" + toDate + "','YYYY-MM-DD')");

         while(set.next()) {
            rw = new HashMap();
            rw.put("DID", set.getLong(1));
            rw.put("DNAME", set.getString(2));
            rw.put("FLDID", set.getLong(3));
            rw.put("DATE", set.getDate(4));
            rw.put("SIZE", set.getLong(5));
            rw.put("DLFLD", set.getLong(6));
            rw.put("USERID", set.getLong(7));
            list.add(rw);
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return list;
   }

   public List<Map<String, Object>> getFiles(String folderid, Connection conn) throws Exception {
      List<Map<String, Object>> list = new ArrayList();
      Map<String, Object> rw = null;

      try {
         Statement stm = conn.createStatement();
         ResultSet set = stm.executeQuery("select a.DocumentIndex, a.DocumentSize,a.Name,a.CreatedDateTime,b.FiledDatetime,b.ParentFolderIndex from ZZ_PDBDocument a, ZZ_PDBDocumentContent b  where a.DocumentIndex=b.DocumentIndex and b.ParentFolderIndex=" + folderid);

         while(set.next()) {
            rw = new HashMap();
            rw.put("DID", set.getString(1));
            rw.put("SIZE", set.getLong(2));
            rw.put("DATE", set.getDate(4));
            rw.put("FLDID", set.getLong(6));
            list.add(rw);
         }

         stm.close();
      } catch (Exception e) {
         e.printStackTrace();
      }

      return list;
   }

   public static void disableSSLVerification() throws Exception {
      TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
         public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
         }

         public void checkClientTrusted(X509Certificate[] certs, String authType) {
         }

         public void checkServerTrusted(X509Certificate[] certs, String authType) {
         }
      }};
      SSLContext sc = SSLContext.getInstance("TLS");
      sc.init((KeyManager[])null, trustAllCerts, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
   }

   public NewGenFileData callApi(String flId, int userId) throws Exception {
      NewGenFileData file = null;
      disableSSLVerification();
      String url = "https://172.1.2.41/OmniDocsRestWS/rest/services/getDocument";
      XMLElement xml = new XMLElement();
      xml.setName("NGOGetDocumentBDO");
      XMLElement ch = new XMLElement();
      ch.setName("cabinetName");
      ch.setValues("boidms".toCharArray());
      xml.addElement(ch);
      ch = new XMLElement();
      ch.setName("docIndex");
      ch.setValues(flId.toCharArray());
      xml.addElement(ch);
      ch = new XMLElement();
      ch.setName("siteId");
      ch.setValues("1".toCharArray());
      xml.addElement(ch);
      ch = new XMLElement();
      ch.setName("volumeId");
      ch.setValues("1".toCharArray());
      xml.addElement(ch);
      ch = new XMLElement();
      ch.setName("userName");
      ch.setValues(this.userNames[userId].toCharArray());
      xml.addElement(ch);
      ch = new XMLElement();
      ch.setName("userPassword");
      ch.setValues(this.passwords[userId].toCharArray());
      xml.addElement(ch);
      ch = new XMLElement();
      ch.setName("userDBId");
      ch.setValues("".toCharArray());
      xml.addElement(ch);
      ch = new XMLElement();
      ch.setName("locale");
      ch.setValues("en_US".toCharArray());
      xml.addElement(ch);
      String requestBody = xml.asXml();
      Map<String, String> headers = new HashMap();
      headers.put("Content-Type", "application/xml");
      headers.put("Authorization", "Bearer your_token");
      String responseStr = postXmlRequest(url, requestBody, headers);
      XMLElement root = XMLParserService.rootElement(responseStr);
      String status = ((XMLElement)((List)root.getChildMap().get("statusCode")).get(0)).getText();
      System.out.println(((XMLElement)((List)root.getChildMap().get("statusCode")).get(0)).getText());
      if (status.contentEquals("0")) {
         file = new NewGenFileData(root);
      } else {
         String var10001 = this.userNames[userId];
         System.out.println(var10001 + "   " + ((XMLElement)((List)root.getChildMap().get("message")).get(0)).getText());
      }

      return file;
   }

   public static String postXmlRequest(String urlString, String xmlPayload, Map<String, String> headers) throws Exception {
      disableSSLVerification();
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setConnectTimeout(10000);
      conn.setReadTimeout(15000);

      for(Map.Entry<String, String> entry : headers.entrySet()) {
         conn.setRequestProperty((String)entry.getKey(), (String)entry.getValue());
      }

      Throwable var29 = null;
      Iterator var31 = null;

      try {
         OutputStream os = conn.getOutputStream();

         try {
            byte[] input = xmlPayload.getBytes("UTF-8");
            os.write(input);
         } finally {
            if (os != null) {
               os.close();
            }

         }
      } catch (Throwable var28) {
         if (var29 == null) {
            var29 = var28;
         } else if (var29 != var28) {
            var29.addSuppressed(var28);
         }

         throw var29;
      }

      var29 = null;
      var31 = null;

      try {
         InputStream is = conn.getInputStream();

         String var10000;
         try {
            var10000 = new String(is.readAllBytes(), "UTF-8");
         } finally {
            if (is != null) {
               is.close();
            }

         }

         return var10000;
      } catch (Throwable var26) {
         if (var29 == null) {
            var29 = var26;
         } else if (var29 != var26) {
            var29.addSuppressed(var26);
         }

         throw var29;
      }
   }

   public void createFolder(DLFolder pf, String fid) throws Exception {
      if (pf == null) {
         pf = this.defaultFolder;
      }

      for(Map<String, Object> map : this.getFiles(fid, this.con)) {
         NewGenFileData file = this.callApi("" + String.valueOf(map.get("DID")), 0);
         if (file != null) {
            file.setSize((Long)map.get("SIZE"));
            file.setData((Date)map.get("DATE"));
            this.createFile(pf, file);
         }
      }

   }

   private String uuid() {
      return PortalUUIDUtil.generate();
   }

   public void getMigFolder(long fid) {
      try {
         this.defaultFolder = DLFolderLocalServiceUtil.getDLFolder(fid);
         this.grpId = this.defaultFolder.getGroupId();
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   private void createFile(DLFolder pf, NewGenFileData fileData) {
      try {
         Date dt = fileData.getData();
         DLFileEntry fl = null;
         fl = DLFileEntryLocalServiceUtil.addFileEntry(this.uuid(), pf.getUserId(), pf.getGroupId(), pf.getRepositoryId(), pf.getFolderId(), fileData.getFileName(), fileData.getMimetype(), fileData.getFileName(), fileData.getFileName(), fileData.getFileName(), (String)null, 0L, (Map)null, (File)null, fileData.getStream(), fileData.getSize(), dt, (Date)null, dt, this.ctx);
         DLFileEntryMetadataLocalServiceUtil.updateFileEntryMetadata(fl.getExternalReferenceCode(), fl.getFileEntryTypeId(), fl.getFileEntryId(), fl.getFileVersion().getFileVersionId(), (Map)null, this.ctx);
         fl.setLastPublishDate(dt);
         DLFileEntryLocalServiceUtil.updateDLFileEntry(fl);
         DLFileVersion dv = fl.getFileVersion();
         dv.setStatus(0);
         DLFileVersionLocalServiceUtil.updateDLFileVersion(dv);
         this.setResourcePermission(fl);
      } catch (Exception e) {
         PrintStream var10000 = System.out;
         String var10001 = fileData.getFileName();
         var10000.println("File Name " + var10001 + " Already Exists  " + String.valueOf(e));
      }

   }

   public String setValues(ServiceContext ctx, long fId, String newgenId) throws Exception {
      String str = "success";
      this.ctx = ctx;
      this.newgenId = newgenId;
      this.getMigFolder(fId);
      return str;
   }

   public void run() {
      try {
         this.createFolder(this.defaultFolder, this.newgenId);
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public String getMimeType(File f) throws Exception {
      String fname = f.getName().toLowerCase();
      return MimeTypesUtil.getContentType(fname);
   }

   public void setResourcePermission(DLFileEntry fl) throws PortalException {
      Long roleid = 0L;
      if (this.rmap.containsKey(fl.getCompanyId())) {
         roleid = (Long)this.rmap.get(fl.getCompanyId());
      } else {
         Role userRole = RoleLocalServiceUtil.getRole(fl.getCompanyId(), "User");
         roleid = userRole.getRoleId();
         this.rmap.put(fl.getCompanyId(), roleid);
      }

      ResourcePermissionLocalServiceUtil.setResourcePermissions(fl.getCompanyId(), DLFileEntry.class.getName(), 4, String.valueOf(fl.getFileEntryId()), roleid, new String[]{"VIEW"});
   }
}
