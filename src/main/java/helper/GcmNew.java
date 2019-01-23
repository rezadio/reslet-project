package helper;


import apigatewayottopay.connection.DatabaseUtilitiesPgsql;
import apigatewayottopay.entity.EntityConfig;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GcmNew {

    public void sent(String idTrx, String idAccount, String pesan, int idTipeTransaksi, boolean isPremium, EntityConfig configEntity) {
        try {
            GcmThread gcm = new GcmThread(idTrx, idAccount, pesan, idTipeTransaksi, isPremium, configEntity);
            Thread t = new Thread(gcm);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class GcmThread implements Runnable {

    private String idTrx;
    private String idAccount;
    private String pesan;
    private int idTipeTransaksi;
    private boolean isPremium;
    private boolean isGcm;
    private EntityConfig configEntity;

    public GcmThread(String idTrx, String idAccount, String pesan, int idTipeTransaksi, boolean isPremium, EntityConfig configEntity) {
        this.idTrx = idTrx;
        this.idAccount = idAccount;
        this.pesan = pesan;
        this.idTipeTransaksi = idTipeTransaksi;
        this.isPremium = isPremium;
        this.configEntity = configEntity;
    }

    @Override
    public void run() {
        String data = "{ "
                + "\"id_trx\": \"" + idTrx + "\","
                + "\"id_account\": \"" + idAccount + "\","
                + "\"pesan\": \"" + pesan + "\","
                + "\"isPremium\": true,"
                + "\"isGcm\": " + isPremium + ","
                + "\"handphone\": \"085858585858\","
                + "\"id_reg\": \"" + idTrx + "\","
                + "\"idTipeTransaksi\": \"" + idTipeTransaksi + "\" }";

        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;

        try {
            connPgsql = databaseUtilitiesPgsql.getConnection(this.configEntity);
            String sql = "SELECT parameter_value from ms_parameter WHERE \"parameter_name\" = 'gcm_url'";

            stPgsql = connPgsql.prepareStatement(sql);
            rsPgsql = stPgsql.executeQuery();

            String url = null;

            while (rsPgsql.next()) {
                url = rsPgsql.getString("parameter_value");
            }
            String respon = this.curl_post(url, data, false);

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

    public String curl_post(String url, String data, boolean encode) {
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

            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();

            String code = String.valueOf(con.getResponseCode());
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
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseObj.put("responseCode", "200");
            responseObj.put("ack", "PENDING");
            responseObj.put("message", "EXT : INTERNAL SERVER ERROR");
            return (responseObj.toString());

        }
        return (responseObj.toString());
    }
}
