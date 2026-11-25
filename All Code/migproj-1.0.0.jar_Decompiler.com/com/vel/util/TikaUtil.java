package com.vel.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TikaUtil {
   public static String callTika(String encodedData) throws Exception {
      String type = "text/plain";
      URL u = new URL("http://localhost:8789/tikka/ext");
      HttpURLConnection conn = (HttpURLConnection)u.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", type);
      conn.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
      OutputStream os = conn.getOutputStream();
      os.write(encodedData.getBytes());
      os.flush();
      os.close();
      int responseCode = conn.getResponseCode();
      System.out.println("POST Response Code :: " + responseCode);
      StringBuffer response = new StringBuffer();
      if (responseCode == 200) {
         BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

         String inputLine;
         while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
         }

         in.close();
      }

      return response.toString();
   }

   public static String callTikaFile(String encodedData) throws Exception {
      String type = "text/plain";
      URL u = new URL("http://localhost:8789/tikka/cnt");
      HttpURLConnection conn = (HttpURLConnection)u.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", type);
      conn.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
      OutputStream os = conn.getOutputStream();
      os.write(encodedData.getBytes());
      os.flush();
      os.close();
      int responseCode = conn.getResponseCode();
      System.out.println("POST Response Code :: " + responseCode);
      StringBuffer response = new StringBuffer();
      if (responseCode == 200) {
         BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

         String inputLine;
         while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
         }

         in.close();
      }

      return response.toString();
   }

   public static void main(String[] args) throws Exception {
      System.out.println(callTika("abcd.pdf"));
   }
}
