package helper;

import apigatewayottopay.connection.DatabaseUtilitiesPgsql;
import apigatewayottopay.controller.ControllerLog;
import apigatewayottopay.entity.EntityConfig;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SmsHelper {

    public void sent(String idTrx, String hp, String pesan, String info, EntityConfig configEntity) {
        try {
            SmsThread sms = new SmsThread(idTrx, hp, pesan, info, configEntity);
            Thread t = new Thread(sms);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class SmsThread implements Runnable {

    private String idTrx;
    private String hp;
    private String pesan;
    private String info;
    private EntityConfig configEntity;
    private ControllerLog controllerLog;

    public SmsThread(String idTrx, String hp, String pesan, String info, EntityConfig configEntity) {
        this.idTrx = idTrx;
        this.hp = hp;
        this.pesan = pesan;
        this.info = info;
        this.configEntity = configEntity;
        this.controllerLog = new ControllerLog();
    }

    @Override
    public void run() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MSISDN", "baseapi");
        params.put("requestId", idTrx);
        params.put("NOHP", hp);
        params.put("PIN", "F4CX6KNZHJD14W7Y");
        params.put("MESSAGE", pesan);
        params.put("GROUP", info);

        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;

        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.configEntity);
            String sql = "SELECT parameter_value from ms_parameter WHERE \"parameter_name\" = 'url_sms_masking'";

            stPgsql = connPgsql.prepareStatement(sql);
            rsPgsql = stPgsql.executeQuery();

            String url = null;

            while (rsPgsql.next()) {
                url = rsPgsql.getString("parameter_value");
            }
            String respon = this.curl_post(url, params);

        } catch (Exception e) {
            e.printStackTrace();
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
    }

    public String curl_post(String url, Map<String, Object> params) {
        JSONObject responseObj = null;
        try {
            URL urlPost = new URL(url);

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) {
                    postData.append('&');
                }
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }

            controllerLog.logStreamWriter("req to sms api : " + url + " with data : " + postData.toString());
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) urlPost.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // handle url encoded form data
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            String code = String.valueOf(conn.getResponseCode());

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;) {
                sb.append((char) c);
            }
            String response = sb.toString();

            if (response.toString().trim().length() > 10) {
                this.controllerLog.logStreamWriter("resp from sms api : " + response.toString());
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(response.toString());
                responseObj = (JSONObject) obj;
                responseObj.put("responseCode", code);
            } else {
                responseObj = new JSONObject();
                responseObj.put("responseCode", "500");
                responseObj.put("ack", "NOK");
                responseObj.put("message", "Mohon maaf untuk sementara transaksi tidak dapat dilakukan");
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            responseObj.put("responseCode", "200");
            responseObj.put("ack", "PENDING");
            responseObj.put("message", "EXT : INTERNAL SERVER ERROR");
            return (responseObj.toString());

        }
        return (responseObj.toString());
    }
}
