/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;


import apigatewayottopay.connection.DatabaseUtilitiesPgsql;
import apigatewayottopay.controller.ControllerEncDec;
import apigatewayottopay.controller.ControllerLog;
import apigatewayottopay.entity.EntityConfig;
import com.google.common.base.Throwables;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hasan
 */
public class Utilities {

    private Cache cache;
    private EntityConfig entityConfig;
    private final ControllerEncDec encDec;
    private final ControllerLog controllerLog;

    public Utilities() {
        CacheManager cacheManager = CacheManager.getInstance();
        //get config cache
        this.cache = cacheManager.getCache("TrueMoneyApp");
        Element config = this.cache.get("config");
        this.entityConfig = (EntityConfig) config.getObjectValue();
        //getMasterCache to process in module
        this.cache = cacheManager.getCache("TrueMoneyAppMaster");
        this.controllerLog = new ControllerLog();
        this.encDec = new ControllerEncDec(this.entityConfig.getEncIv(), this.entityConfig.getEncKey());
    }

    public int insertLog(String idTrx, JSONObject objJson) {
        //<editor-fold defaultstate="collapsed" desc="query insertLog">
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        int idTemp = 0;
        int idResultTmp = 0;
        //remove password object before insert to log
        JSONObject objJsonRecreate = new JSONObject(objJson);
//        for (Iterator iterator = objJson.keySet().iterator(); iterator.hasNext();) {
//            String key = (String) iterator.next();
//            if (!key.equals("password")) {
//                objJsonRecreate.put(key, objJson.get(key));
//            }
//        }
        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String query = "insert "
                    + "    into"
                    + "        \"tr_trx_token_log\""
                    + "        (\"id_trx\", \"request\", \"request_time\") "
                    + "    values"
                    + "        (?, ?, ?);";
            stPgsql = connPgsql.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stPgsql.setString(1, idTrx);
            stPgsql.setString(2, objJsonRecreate.toString());
            stPgsql.setTimestamp(3, this.getDate());
            idTemp = stPgsql.executeUpdate();
            rsPgsql = stPgsql.getGeneratedKeys();
            while (rsPgsql.next()) {
                idResultTmp = rsPgsql.getInt(1);
            }
        } catch (ParserConfigurationException ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
        } catch (SQLException ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
        } finally {
            try {
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                String s = Throwables.getStackTraceAsString(ex);
                controllerLog.logErrorWriter(s);
            }
        }
        //</editor-fold>

        return idResultTmp;
    }

    public void updateLog(int idTmp, String resp) {
        JSONParser parser = new JSONParser();
        JSONObject jsonResp = null;
        try {
            jsonResp = (JSONObject) parser.parse(resp);
        } catch (org.json.simple.parser.ParseException ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);;
        }
        //<editor-fold defaultstate="collapsed" desc="query updateTmp">
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String query = "update \"tr_trx_token_log\" set \"response\" = ?, \"response_time\" = ? where id_trx_token_log = ?;";
            stPgsql = connPgsql.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stPgsql.setString(1, jsonResp.toString());
            stPgsql.setTimestamp(2, this.getDate());
            stPgsql.setLong(3, idTmp);
            stPgsql.executeUpdate();
        } catch (ParserConfigurationException ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
        } catch (SQLException ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
        } finally {
            try {
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                String s = Throwables.getStackTraceAsString(ex);
                controllerLog.logErrorWriter(s);
            }
        }
        //</editor-fold>
    }

    public String convertFormatDate(String formatOld, String formatNew, String dateString) {
        String bulanTahun = "";
        try {
            DateFormat df = new SimpleDateFormat(formatOld);
            Date bulanTahunDate = df.parse(dateString);
            df = new SimpleDateFormat(formatNew);
            bulanTahun = df.format(bulanTahunDate);
        } catch (ParseException e) {
            String s = Throwables.getStackTraceAsString(e);
            controllerLog.logErrorWriter(s);
        }
        return bulanTahun;
    }

    public String curlPost(String url, String data, boolean encode) {
        JSONObject responseObj = null;
        try {

            BufferedReader rd = null;
            String line = null;
            if (encode) {
                data = URLEncoder.encode(data.toString(), "UTF-8");
            }
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
            con.setRequestProperty("Content-Language", "en-US");
            con.setRequestProperty("Content-Type", "application/json");

            ControllerLog ctrlLog = new ControllerLog();
            ctrlLog.logStreamWriter("Upline -> Req : " + url + " | data = " + data.toString());

            con.setConnectTimeout(120000);
            con.setReadTimeout(120000);

            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            String code = String.valueOf(con.getResponseCode());

            //System.err.println(code);
            // Get Response
            InputStream is = con.getInputStream();
            rd = new BufferedReader(new InputStreamReader(is));

            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            if (response.toString().trim().length() > 10) {
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(response.toString());
                responseObj = (JSONObject) obj;
                responseObj.put("responseCode", code);
            } else {
                responseObj = new JSONObject();
                responseObj.put("responseCode", "500");
                responseObj.put("ack", "NOK");
                responseObj.put("message", "Mohon maaf untuk sementara transaksi tidak dapat dilakukan");
                responseObj.put("MSG", "Mohon maaf untuk sementara transaksi tidak dapat dilakukan");
            }
            ctrlLog.logStreamWriter("Upline -> Resp : data = " + responseObj.toString());

        } catch (java.net.SocketTimeoutException e) {
            responseObj = new JSONObject();
            responseObj.put("responseCode", "504");
            responseObj.put("ack", "NOK");
            responseObj.put("message", "TIME OUT!!");
            String s = Throwables.getStackTraceAsString(e);
            controllerLog.logErrorWriter(s);

            return (responseObj.toString());
        } catch (Exception e) {
            String s = Throwables.getStackTraceAsString(e);
            controllerLog.logErrorWriter(s);

            responseObj = new JSONObject();
            responseObj.put("responseCode", "500");
            responseObj.put("ack", "NOK");
            responseObj.put("message", "EXT : INTERNAL SERVER ERROR");

            return (responseObj.toString());

        }
        return (responseObj.toString());
    }


    public synchronized String createStan() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(999999999);
        String formatted = String.format("%012d", num);
        return formatted;
    }

    public JSONObject removePasswordObj(JSONObject jsonReq) {
        JSONObject objJsonRecreate = new JSONObject();
        for (Iterator iterator = jsonReq.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            if (!key.equals("password")) {
                objJsonRecreate.put(key, jsonReq.get(key));
            }
        }
        return objJsonRecreate;
    }

    public String GeneratePassword() {
        final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final int N = alphabet.length();
        Random r = new Random();
        String password = "";
        for (int i = 0; i < 6; i++) {
            password = password + alphabet.charAt(r.nextInt(N));
        }
        return password;
    }

    public String GeneratePIN() {
        final String alphabet = "0123456789";
        final int N = alphabet.length();
        Random r = new Random();
        String password = "";
        for (int i = 0; i < 6; i++) {
            password = password + alphabet.charAt(r.nextInt(N));
        }
        return password;
    }

    public Timestamp getDate() {
        Timestamp timestamp = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = format.format(new Date()).toString();
            Date date1 = format.parse(date);
            timestamp = new Timestamp(date1.getTime());
        } catch (ParseException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        return timestamp;
    }

    public String getDate(String format) {
        // "yyyy-MM-dd H:i:s"
        Date now = new Date();
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat(format);
        String strDate = sdf.format(now);

        sdf = null;
        return strDate;
    }
    public String getSimpleDate(String input) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date  date = null;
        String  strDate = "";
        try {
            date = format.parse(input);
            strDate = sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDate;
    }

    public String getRcMessage(String rc) {
        //cari status message
        Element elem = this.cache.get("msresponseCode");
        ArrayList<HashMap<String, Object>> msResponseCode = (ArrayList<HashMap<String, Object>>) elem.getObjectValue();
        int found = 0;
        String message = "RESPONSE TIDAK TERDAFTAR";
        //find the data
        for (HashMap<String, Object> map : msResponseCode) {
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String key = pair.getKey().toString();
                String value = pair.getValue().toString();
                if (key.equals("response_code") && value.equals(rc)) {
                    found = 1;
                }
                if (key.equals("response_detail")) {
                    message = value;
                }
            }
            if (found == 1) {
                break;
            }
        }
        return message;
    }

    public String getWording(String wordContent) {
        //cari status message
        Element elem = this.cache.get("msWording");
        ArrayList<HashMap<String, Object>> msResponseCode = (ArrayList<HashMap<String, Object>>) elem.getObjectValue();
        int found = 0;
        String message = "WORDING TIDAK TERDAFTAR";
        //find the data
        for (HashMap<String, Object> map : msResponseCode) {
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String key = pair.getKey().toString();
                String value = pair.getValue().toString();
                if (key.equals("wording_content_name") && value.equals(wordContent)) {
                    found = 1;
                }
                if (key.equals("wording_content")) {
                    message = value;
                }
            }
            if (found == 1) {
                break;
            }
        }
        return message;
    }

    public JSONObject curl_post_inq(String url, String data, boolean encode) {
        JSONObject responseObj = null;
        try {
            BufferedReader rd = null;
            String line = null;
            if (encode) {
                data = URLEncoder.encode(data.toString(), "UTF-8");
            }
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
            con.setRequestProperty("Content-Language", "en-US");
            con.setRequestProperty("Content-Type", "text/plain");

            con.setConnectTimeout(30000);
            con.setReadTimeout(30000);

            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            String code = String.valueOf(con.getResponseCode());

            //System.err.println(code);
            // Get Response
            InputStream is = con.getInputStream();
            rd = new BufferedReader(new InputStreamReader(is));

            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            if (response.toString().trim().length() > 10) {
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(response.toString());
                responseObj = (JSONObject) obj;
                responseObj.put("responseCode", code);
            } else {
                responseObj = new JSONObject();
                responseObj.put("responseCode", "500");
                responseObj.put("ack", "NOK");
                responseObj.put("message", "Mohon maaf untuk sementara transaksi tidak dapat dilakukan");
                responseObj.put("MSG", "Mohon maaf untuk sementara transaksi tidak dapat dilakukan");
            }

        } catch (java.net.SocketTimeoutException e) {
            responseObj = new JSONObject();
            responseObj.put("responseCode", "504");
            responseObj.put("ack", "NOK");
            responseObj.put("message", "TIME OUT!!");
            e.printStackTrace();

            return responseObj;
        } catch (Exception e) {
            e.printStackTrace();

            responseObj = new JSONObject();
            responseObj.put("responseCode", "500");
            responseObj.put("ack", "NOK");
            responseObj.put("message", "EXT : INTERNAL SERVER ERROR");

            return responseObj;

        }
        return responseObj;
    }

    public JSONObject checkDeviceMember(JSONObject jsonReq) {
        JSONObject jobjResp = new JSONObject();
//        JSONObject jObjDataDiri = (JSONObject) jsonReq.get("dataPribadi");
        jobjResp.put("ack", "NOK");
        jobjResp.put("message", "device changed");

        //<editor-fold defaultstate="collapsed" desc="query checkDevice">
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        int count = 0;
        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String query = "select count(*) "
                    + "from \"MsMemberAccount\" "
                    + "where \"device_info\" = ?"
                    + "and \"Handphone\" = ?";
            stPgsql = connPgsql.prepareStatement(query);
            stPgsql.setString(1, jsonReq.get("deviceInfo").toString());
            stPgsql.setString(2, jsonReq.get("username").toString());
            rsPgsql = stPgsql.executeQuery();
            while (rsPgsql.next()) {
                count = rsPgsql.getInt("count");
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rsPgsql != null) {
                    rsPgsql.close();
                }
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //</editor-fold>

        if (count > 0) {
            jobjResp.put("ack", "OK");
            jobjResp.put("message", "device not changed");
        }

        return jobjResp;
    }

    public JSONObject checkVersion(JSONObject objJson) {
        JSONObject jsonResp = new JSONObject();
        jsonResp.put("ack", "NOK");
        jsonResp.put("message", "Terdapat Update aplikasi");
        //<editor-fold defaultstate="collapsed" desc="query checkVersion">
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        try {
            String packageName = objJson.get("packageName").toString();
            String versionCode = objJson.get("versionCode").toString();
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String query = "select \"AppsName\",\"Version\",\"TimeStamp\",\"IsActive\" from "
                    + "\"MsAndroidVersion\" where \"TimeStamp\" = (select max(\"TimeStamp\") from "
                    + "\"MsAndroidVersion\" where \"AppsName\" = ?) and "
                    + "\"Version\" = ?";
            stPgsql = connPgsql.prepareStatement(query);
            stPgsql.setString(1, packageName);
            stPgsql.setString(2, versionCode);
            rsPgsql = stPgsql.executeQuery();
            while (rsPgsql.next()) {
                String version = rsPgsql.getString("Version");

                if (!versionCode.equalsIgnoreCase(version)) {
                    jsonResp.put("ack", "NOK");
                    jsonResp.put("message", "Terdapat Update aplikasi");
                } else {
                    if (rsPgsql.getBoolean("IsActive") == true) {
                        jsonResp.put("ack", "OK");
                        jsonResp.put("message", "");
                    } else {
                        jsonResp.put("ack", "NOK");
                        jsonResp.put("message", "Under Construction");
                    }
                }
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rsPgsql != null) {
                    rsPgsql.close();
                }
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //</editor-fold>
        return jsonResp;
    }

    public String insertTmp(JSONObject objJson) {
        //<editor-fold defaultstate="collapsed" desc="query insertTmp">
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        int idTemp = 0;
        String idResultTmp = "0";
        //remove password object before insert to log
        JSONObject objJsonRecreate = new JSONObject();
        for (Iterator iterator = objJson.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            if (!key.equals("password")) {
                objJsonRecreate.put(key, objJson.get(key));
            }
        }
        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String query = "insert "
                    + "    into"
                    + "        \"Temp\""
                    + "        (\"Request\", \"RequestTime\") "
                    + "    values"
                    + "        (?, ?);";
            stPgsql = connPgsql.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stPgsql.setString(1, objJsonRecreate.toString());
            stPgsql.setTimestamp(2, this.getDate());
            idTemp = stPgsql.executeUpdate();
            rsPgsql = stPgsql.getGeneratedKeys();
            while (rsPgsql.next()) {
                idResultTmp = rsPgsql.getString(1);
            }
        } catch (ParserConfigurationException ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
//            Logger.getLogger(ImpleNotif.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
//            Logger.getLogger(ImpleNotif.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                String s = Throwables.getStackTraceAsString(ex);
                controllerLog.logErrorWriter(s);
//                Logger.getLogger(ImpleNotif.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //</editor-fold>
        controllerLog.logDebugWriter("updateTmp OK");
        return idResultTmp;
    }

    public void updateTmp(String idTmp, String resp) {
        JSONParser parser = new JSONParser();
        JSONObject jsonResp = null;
        try {
            jsonResp = (JSONObject) parser.parse(resp);
        } catch (org.json.simple.parser.ParseException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        //<editor-fold defaultstate="collapsed" desc="query updateTmp">
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String query = "update \"Temp\" set \"Response\" = ?, \"ResponseTime\" = ? where id_temp = ?;";
            stPgsql = connPgsql.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stPgsql.setString(1, jsonResp.toString());
            stPgsql.setTimestamp(2, this.getDate());
            stPgsql.setInt(3, Integer.parseInt(idTmp));
            stPgsql.executeUpdate();
        } catch (ParserConfigurationException ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
//            Logger.getLogger(ImpleNotif.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
//            Logger.getLogger(ImpleNotif.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                String s = Throwables.getStackTraceAsString(ex);
                controllerLog.logErrorWriter(s);
//                Logger.getLogger(ImpleNotif.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //</editor-fold>
        controllerLog.logDebugWriter("updateTmp OK");
    }

    public String constructResponse(String resp) {
        byte[] encryptByte = null;
        try {
            encryptByte = this.encDec.getEncrypt(resp);
        } catch (Exception ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        resp = new String(Base64.encodeBase64(encryptByte));
        return resp;
    }

    public String formatTanggal(String strtime) {
        String strbulan = "";
        String strtanggal = "";
        String strtahun = "";
        String strjam = "";
        if (strtime != null && !strtime.isEmpty()) {
            strtahun = strtime.substring(0, 4);
            strbulan = strtime.substring(5, 7);
            if (strbulan.equals("01")) {
                strbulan = "Januari";
            } else if (strbulan.equals("02")) {
                strbulan = "Februari";
            } else if (strbulan.equals("03")) {
                strbulan = "Maret";
            } else if (strbulan.equals("04")) {
                strbulan = "April";
            } else if (strbulan.equals("05")) {
                strbulan = "Mei";
            } else if (strbulan.equals("06")) {
                strbulan = "Juni";
            } else if (strbulan.equals("07")) {
                strbulan = "Juli";
            } else if (strbulan.equals("08")) {
                strbulan = "Agustus";
            } else if (strbulan.equals("09")) {
                strbulan = "September";
            } else if (strbulan.equals("10")) {
                strbulan = "Oktober";
            } else if (strbulan.equals("11")) {
                strbulan = "November";
            } else if (strbulan.equals("12")) {
                strbulan = "Desember";
            }
            strtanggal = strtime.substring(8, 10);
            if (strtime.length() > 10) {
                strjam = " " + strtime.substring(11, 16);
            }
            return strtanggal + " " + strbulan + " " + strtahun + strjam;
        } else {
            return strtime;
        }
    }

    public boolean moveFile(String to, String fileName) {
        boolean sukses = false;
        String pathTarget = to + fileName;
        try {
            File afile = new File(entityConfig.getWorkDir() + "tmp/" + fileName);
            if (afile.renameTo(new File(pathTarget))) {
                sukses = true;
            } else {
                sukses = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sukses;
    }

    public String getID_MemberNonEDC(String nohp) {
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        String id_memberaccount = null;
        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String query = "SELECT \"MsMemberAccount\".id_memberaccount \n"
                    + "FROM public.\"MsMemberAccount\" \n"
                    + "WHERE \"MsMemberAccount\".\"Handphone\" = ? OR \"MsMemberAccount\".id_memberaccount = ?";
            stPgsql = connPgsql.prepareStatement(query);
            stPgsql.setString(1, nohp);
            stPgsql.setString(2, nohp);
            rsPgsql = stPgsql.executeQuery();
            while (rsPgsql.next()) {
                id_memberaccount = rsPgsql.getString("id_memberaccount");
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rsPgsql != null) {
                    rsPgsql.close();
                }
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return id_memberaccount;
    }

    public String getPIN(String memberaccount) {
        String pin = "";
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String query = "SELECT  " + "  \"MsMemberAccount\".\"PIN\" " + "FROM  " + "  public.\"MsMemberAccount\" "
                    + "WHERE  " + "  \"MsMemberAccount\".id_memberaccount = ? OR \"MsMemberAccount\".\"Handphone\" = ?";
            stPgsql = connPgsql.prepareStatement(query);
            stPgsql.setString(1, memberaccount);
            stPgsql.setString(2, memberaccount);
            rsPgsql = stPgsql.executeQuery();
            while (rsPgsql.next()) {
                pin = rsPgsql.getString("PIN");
            }
            pin = this.encDec.decryptDbVal(pin, this.entityConfig.getEncKeyDb());
        } catch (ParserConfigurationException | SQLException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rsPgsql != null) {
                    rsPgsql.close();
                }
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return pin;
    }

    public int[] getIDAgenKomisi(int tipetransaksi, String NoKartu, String nominal) throws SQLException {
        int[] comission = new int[8];

        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String query = "SELECT "
                    + " A.id_supplier, "
                    + " A.\"ProductCode\", "
                    + " A.\"HargaBeli\", "
                    + " A.\"HargaJual\", "
                    + " A.\"HargaCetak\", "
                    + " A.\"HargaCetakMember\", "
                    + " A.\"BiayaAdmin\", "
                    + " A.\"TrueSpareFee\", "
                    + " A.\"SupplierPrice\", "
                    + " A.\"AgentCommType\", "
                    + " A.\"AgentCommNom\", "
                    + " A.\"DealerCommType\", "
                    + " A.\"DealerCommNom\", "
                    + " A.\"TrueFeeType\", "
                    + " A.\"TrueFeeNom\", "
                    + " A.\"MemberDiscType\", "
                    + " CASE WHEN A.\"MemberDiscType\" = 'PERCENTAGE' THEN ((A.\"BiayaAdmin\" - A.\"SupplierPrice\") * A.\"MemberDiscNom\") / 100 ELSE A.\"MemberDiscNom\" END, "
                    + " A.\"BiayaAdmin\" - A.\"TrueSpareFee\" AS \"MarginGross\" "
                    + "FROM "
                    + " \"MsFeeSupplier\" A "
                    + "JOIN \"MsDenom\" ON A.id_denom = \"MsDenom\".id_denom "
                    + "JOIN \"MsOperator\" ON A.id_operator = \"MsOperator\".id_operator "
                    + "JOIN \"MsFee\" ON \"MsFee\".id_fee = A.id_fee "
                    + "WHERE "
                    + " \"MsDenom\".\"Nominal\" = ?"
                    + "AND \"MsOperator\".id_operator = 13 "
                    + "AND A.\"FlagActive\" = 'TRUE' "
                    + "AND A.\"HargaBeli\" IS NOT NULL "
                    + "AND \"MsFee\".id_tipeaplikasi = 3";
            stPgsql = connPgsql.prepareStatement(query);
            stPgsql.setString(1, nominal);
            rsPgsql = stPgsql.executeQuery();
            while (rsPgsql.next()) {
                comission[0] = rsPgsql.getInt("BiayaAdmin"); 	// BiayaAdmin
                comission[1] = rsPgsql.getInt("TrueSpareFee"); 	// TrueSpareFee
                comission[2] = rsPgsql.getInt("SupplierPrice"); // SupplierPrice
                comission[3] = rsPgsql.getInt("AgentCommNom"); 	// AgentCommNom
                comission[4] = rsPgsql.getInt("DealerCommNom"); // DealerCommNom
                comission[5] = rsPgsql.getInt("TrueFeeNom"); 	// TrueFeeNom
                comission[6] = rsPgsql.getInt("MemberDiscNom"); // MemberDiscNom
                comission[7] = rsPgsql.getInt("id_supplier"); 	// idSupplier
            }
        } catch (ParserConfigurationException | SQLException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rsPgsql != null) {
                    rsPgsql.close();
                }
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return comission;
    }

    public boolean CekIsEmptyPostpaid(String idPelanggan, String id_operator, String idaccount) {
        boolean cek = false;
        int count = 0;

        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String query = "SELECT \"count\"(*) from \"TrTransaksi\" WHERE \"id_pelanggan\" = ? "
                    + " and id_operator = ? AND \"TimeStamp\"::DATE = now()::DATE "
                    + " AND (id_memberaccount = ? OR id_agentaccount = ?)"
                    + " AND \"upper\"(\"StatusTRX\") in ('SUKSES', 'OPEN', 'PENDING')";

            stPgsql = connPgsql.prepareStatement(query);
            stPgsql.setString(1, idPelanggan);
            stPgsql.setString(2, id_operator);
            stPgsql.setString(3, idaccount);
            stPgsql.setString(4, idaccount);
            rsPgsql = stPgsql.executeQuery();

            while (rsPgsql.next()) {
                count = rsPgsql.getInt("count");
            }

            if (count != 0) {
            } else {
                cek = true;
            }
        } catch (ParserConfigurationException | SQLException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rsPgsql != null) {
                    rsPgsql.close();
                }
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return cek;
    }

    public boolean[] getConfigUnferify(String id_MemberNonEDC, String hargaCetak) {
        boolean value_config[] = new boolean[3];
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;

        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String query = "WITH memberacc AS ( "
                    + "	SELECT "
                    + "		id_memberaccount, "
                    + "		id_statusverifikasi, "
                    + "		\"LastBalance\" "
                    + "	FROM "
                    + "		\"MsMemberAccount\" "
                    + "	WHERE "
                    + "		\"Handphone\" =  ? "
                    + "	OR \"id_memberaccount\" =  ? "
                    + "	OR \"id_member\" =  ? "
                    + "	OR \"NomorKartu\" =  ? "
                    + "), "
                    + " statusver AS ( "
                    + "	SELECT "
                    + "		\"NamaStatusVerifikasi\", "
                    + "		\"LimitBalance\", "
                    + "		\"LimitTransaksi\", "
                    + "		\"FlagActive\", "
                    + "		\"JumlahMaxTransaksi\", "
                    + "		\"DaySizeMaxTransaksi\" "
                    + "	FROM "
                    + "		\"MsStatusVerifikasi\", "
                    + "		memberacc "
                    + "	WHERE "
                    + "		\"MsStatusVerifikasi\".id_statusverifikasi = memberacc.id_statusverifikasi "
                    + "	AND \"FlagActive\" = 't' "
                    + "), "
                    + " transaksi AS ( "
                    + "	SELECT "
                    + "		\"sum\" (\"TrStock\".\"Nominal\") AS total "
                    + "	FROM "
                    + "		memberacc, "
                    + "		\"TrStock\" "
                    + "	JOIN \"TrTransaksi\" ON \"TrTransaksi\".id_transaksi = \"TrStock\".id_transaksi "
                    + "	WHERE "
                    + "		\"TrStock\".id_stock = memberacc.id_memberaccount "
                    + "	AND \"upper\" (\"TrTransaksi\".\"StatusTRX\") <> 'GAGAL' "
                    + "	AND \"upper\" (\"TrStock\".\"TypeTrx\") = 'WITHDRAW' "
                    + "	AND EXTRACT ( "
                    + "		MONTH "
                    + "		FROM "
                    + "			\"TrTransaksi\".\"TimeStamp\" :: TIMESTAMP "
                    + "	) = EXTRACT (MONTH FROM now()) "
                    + "	AND EXTRACT ( "
                    + "		YEAR "
                    + "		FROM "
                    + "			\"TrTransaksi\".\"TimeStamp\" :: TIMESTAMP "
                    + "	) = EXTRACT (YEAR FROM now()) "
                    + ") SELECT "
                    + "	CASE "
                    + "WHEN transaksi.total :: INT IS NULL THEN "
                    + "	0 "
                    + "ELSE "
                    + "	transaksi.total :: INT + ? "
                    + "END <= statusver.\"LimitTransaksi\" AS status_trx_nominal "
                    + "FROM "
                    + "	statusver, "
                    + "	transaksi, "
                    + "	memberacc";
            stPgsql = connPgsql.prepareStatement(query);
            stPgsql.setString(1, id_MemberNonEDC);
            stPgsql.setString(2, id_MemberNonEDC);
            stPgsql.setString(3, id_MemberNonEDC);
            stPgsql.setString(4, id_MemberNonEDC);
            stPgsql.setInt(5, Integer.parseInt(hargaCetak));
            rsPgsql = stPgsql.executeQuery();
            while (rsPgsql.next()) {
                value_config[2] = rsPgsql.getBoolean("status_trx_nominal"); // 20 JT
            }

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rsPgsql != null) {
                    rsPgsql.close();
                }
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return value_config;
    }

    public String getLastBalanceMember(String idAccount) {
        String Amount = null;
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String getAmount = "SELECT \"MsMemberAccount\".\"LastBalance\" FROM  "
                    + " public.\"MsMemberAccount\"  WHERE  \"Handphone\" = ? OR "
                    + "\"id_memberaccount\"= ?";
            stPgsql = connPgsql.prepareStatement(getAmount);
            stPgsql.setString(1, idAccount);
            stPgsql.setString(2, idAccount);
            rsPgsql = stPgsql.executeQuery();

            while (rsPgsql.next()) {
                Amount = rsPgsql.getString("LastBalance");
            }

            if (Amount == null && Amount.isEmpty()) {
                Amount = "0";
            }
        } catch (SQLException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rsPgsql != null) {
                    rsPgsql.close();
                }
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return Amount;
    }

    public boolean isPromo(int i) throws SQLException {
        boolean status = false;
        int count = 0;
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;

        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String sql = "SELECT " + "	\"count\"(*) " + "FROM " + "	\"MsPercentage\" "
                    + "JOIN \"MsPromo\" ON \"MsPercentage\".id_promo = \"MsPromo\".id_promo " + "WHERE "
                    + "	now() :: DATE <= \"MsPromo\".tgl_selesai " + "AND now() :: DATE >= \"MsPromo\".tgl_mulai "
                    + "AND \"MsPromo\".id_promo = ?";

            stPgsql = connPgsql.prepareStatement(sql);
            stPgsql.setInt(1, i);
            rsPgsql = stPgsql.executeQuery();

            while (rsPgsql.next()) {
                count = rsPgsql.getInt("count");
            }

            if (count != 0) {
                status = true;
            }

        } catch (SQLException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rsPgsql != null) {
                    rsPgsql.close();
                }
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return status;
    }

    public int[] getPromoKITA_BISA_2(int id_operator, int denom) throws SQLException {
        int promo[] = new int[8];
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            String sql = "SELECT " + " A.\"ProductCode\", " + " A.\"HargaBeli\", " + " A.\"HargaJual\", "
                    + " A.\"HargaCetak\", " + " A.\"HargaCetakMember\", " + " A.\"BiayaAdmin\", "
                    + " A.\"TrueSpareFee\", " + " A.\"SupplierPrice\", " + " A.\"AgentCommType\", "
                    + " A.\"AgentCommNom\", " + " A.\"DealerCommType\", " + " A.\"DealerCommNom\", "
                    + " A.\"TrueFeeType\", " + " A.\"TrueFeeNom\", " + " A.\"MemberDiscType\", "
                    + " CASE WHEN A.\"MemberDiscType\" = 'PERCENTAGE' THEN ((A.\"BiayaAdmin\" - A.\"SupplierPrice\") * A.\"MemberDiscNom\") / 100 ELSE A.\"MemberDiscNom\" END, "
                    + " A.\"BiayaAdmin\" - A.\"TrueSpareFee\" AS \"MarginGross\", "
                    + " A.\"MemberDiscNom\" as percentage " + "FROM " + " \"MsFeeSupplier\" A "
                    + "JOIN \"MsDenom\" ON A.id_denom = \"MsDenom\".id_denom "
                    + "JOIN \"MsOperator\" ON A.id_operator = \"MsOperator\".id_operator "
                    + "JOIN \"MsFee\" ON \"MsFee\".id_fee = A.id_fee " + "WHERE " + " \"MsDenom\".\"Nominal\" = ? "
                    + "AND \"MsOperator\".id_operator = ? AND A.\"FlagActive\" = 'TRUE' " + " AND A.\"HargaBeli\" "
                    + "IS NOT NULL AND \"MsFee\".id_tipeaplikasi = 3";

            stPgsql = connPgsql.prepareStatement(sql);
            stPgsql.setInt(1, denom);
            stPgsql.setInt(2, id_operator);
            rsPgsql = stPgsql.executeQuery();

            while (rsPgsql.next()) {
                promo[0] = rsPgsql.getInt("BiayaAdmin"); // BiayaAdmin
                promo[1] = rsPgsql.getInt("TrueSpareFee"); // TrueSpareFee
                promo[2] = rsPgsql.getInt("SupplierPrice"); // SupplierPrice
                promo[3] = rsPgsql.getInt("AgentCommNom"); // AgentCommNom
                promo[4] = rsPgsql.getInt("DealerCommNom"); // DealerCommNom
                promo[5] = rsPgsql.getInt("TrueFeeNom"); // TrueFeeNom
                promo[6] = rsPgsql.getInt("MemberDiscNom"); // MemberDiscNom
                promo[7] = rsPgsql.getInt("percentage"); // percentage
            }
        } catch (SQLException | ParserConfigurationException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rsPgsql != null) {
                    rsPgsql.close();
                }
                if (stPgsql != null) {
                    stPgsql.close();
                }
                if (connPgsql != null) {
                    connPgsql.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return promo;
    }

    public String getStatusMessage(String Status) {
        //cari status message
        int rc = Integer.parseInt(Status);
        HashMap<Integer, String> hasm = new HashMap<Integer, String>();
        String message = "";
        try {
            hasm.put(0, "SUCCESS");
            hasm.put(1, "Sender and recipient account are same");
            hasm.put(2, "Recipient account is not found");
            hasm.put(3, "Reach the transaction limit per day");
            hasm.put(4, "Reach the transaction limit per month");
            hasm.put(5, "Recipient balance is over limit");
            hasm.put(6, "Sender balance is not enough");
            hasm.put(7, "Failed in deduct Sender balance");
            hasm.put(8, "Failed in deposit to Recipient balance");
            hasm.put(11, "API access denied");
            hasm.put(12, "Invalid request format");
            hasm.put(13, "Invalid username");
            hasm.put(14, "Invalid signature");
            hasm.put(15, "Duplicate Transaction ID");
            hasm.put(16, "Error database connection");
            hasm.put(17, "Invalid amount (min 10 K IDR – max 10 Mio IDR)");
            hasm.put(18, "Time Out");
            hasm.put(19, "Session expired");
            hasm.put(25, "Transaksi sedang di proses");

            message = hasm.get(rc);
        } catch (Exception ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
        }
        return message;
    }
    public String signActive(String transid, String key, String accountid, String username) {
        String Signature = null;
        //<editor-fold defaultstate="collapsed" desc="signature for transaksi">
        try {
            String sha256 = SHA256(key);
            Signature = md5Generate(transid + sha256.toLowerCase() + accountid + username);
//            controllerLog.logDebugWriter("signature " + Signature + " accountid " + accountid);
//            controllerLog.logDebugWriter("key " + key + " transid " + transid + " sha256" + sha256);

        } catch (Exception ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
            Signature = null;
        }
        //</editor-fold>
        controllerLog.logDebugWriter("isSignature OK " + Signature);
        return Signature;
    }
    public String md5Generate(String string) {
        String hasil = "";
        controllerLog.logDebugWriter("isSignature " + string);
        try {

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());

            byte byteData[] = md.digest();

            // convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            hasil = sb.toString();
        } catch (Exception ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
        }
        controllerLog.logDebugWriter("isSignature OK " + hasil);
        return hasil;
    }
    public String SHA256(String string) {
        String hasil = "";
        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(string.getBytes());

            byte byteData[] = md.digest();

            // convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            hasil = sb.toString();
        } catch (Exception ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
        }
        controllerLog.logDebugWriter("isSignature OK SHA256 " + hasil);
        return hasil;
    }
}
