package helper;

import apigatewayottopay.controller.ControllerLog;
import apigatewayottopay.entity.EntityConfig;
import apigatewayottopay.entity.InquiryRequest;
import apigatewayottopay.entity.PaymentRequest;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import sun.misc.BASE64Encoder;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

/**
 * @author Reza Dio Nugraha
 */
public class SendRequest {
    private final ControllerLog controllerLog;
    EntityConfig entityConfig;


    public SendRequest(EntityConfig entityConfig) {
        controllerLog = new ControllerLog();
        this.entityConfig = entityConfig;

    }



    public String curl_post(EntityConfig entityConfig, String url, String data, InquiryRequest request, PaymentRequest paymentRequest) {
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };
        String resp = "";
        int responseCode = 0;
        System.out.println("[OTTOPAY][REQUEST]:"+data);
        JSONObject responseObj = null;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            BufferedReader rd = null;
            String line = null;
            String authorization = null;
            String timestamp = null;
            String signature = null;
            Gson gson = new Gson();
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            if (request != null) {
                authorization = request.getAuthorization();
                timestamp = request.getTimestamp();
                signature = request.getSignature();
            } else if (paymentRequest != null) {
                authorization = paymentRequest.getAuthorization();
                timestamp = paymentRequest.getTimestamp();
                signature = paymentRequest.getSignature();
            }
            String basicAuth = "Basic " + new String(new Base64().encode(entityConfig.getOttopayInitiator().getBytes()));
//            con.setRequestMethod("POST");
//            con.addRequestProperty("Content-Type","application/json");
////            con.setRequestProperty("Accept", "application/x-www-form-urlencoded");
////            con.setRequestProperty("Accept-Charset", "UTF-8");
////            con.setRequestProperty("Content-Length", Integer.toString(data.getBytes("utf8").length));
//
//            con.addRequestProperty("Authorization", basicAuth);
//            con.addRequestProperty("Timestamp", timestamp);
//            con.addRequestProperty("Signature", signature);
            System.out.println("[OTTOPAY][HEADER]: | Timestamp: " + timestamp + " | Signature: " + signature + " | Authorization" + basicAuth);
            controllerLog.logStreamWriter("[OTTOPAY][URL]:"+url);
            controllerLog.logStreamWriter("[OTTOPAY][HEADER]: | Timestamp: " + timestamp + " | Signature: " + signature + " | Authorization" + basicAuth);
            controllerLog.logStreamWriter("[OTTOPAY][REQUEST]:"+data);

            try {
                HttpResponse<JsonNode> response = Unirest.post(url)
                        .header("cache-control", "no-cache")
                        .header("accept", "application/json")
                        .header("Timestamp", timestamp)
                        .header("Signature", signature)
                        .header("Authorization", basicAuth)
                        .body(data)
                        .asJson();
                controllerLog.logStreamWriter("[OTTOPAY][RESPONSE] Response Code : " + response.getStatus() + " " + response.getBody());
                System.out.println("[OTTOPAY][RESPONSE] Response Code : " + response.getStatus() + " " + response.getBody());
                responseCode = response.getStatus();
                System.out.println("\nSending 'POST' request to URL : " + response.getBody() + " | " + response.getHeaders());
                System.out.println("Response Code : " + responseCode);
                resp = response.getBody() + "";
            }catch (Exception e){
                System.out.println("ERROR"+e.getMessage());
                e.printStackTrace();
            }


        } catch (ConnectTimeoutException te){
            te.printStackTrace();
            responseObj.put("responseCode", "201");
            responseObj.put("ack", "NOK");
            responseObj.put("message", "EXT : CONNECTION TIMEOUT");
            controllerLog.logStreamWriter("[RESPONSE]:"+responseObj.toString());
            return (responseObj.toString());

        } catch (Exception e) {
            e.printStackTrace();
            responseObj.put("responseCode", "200");
            responseObj.put("ack", "NOK");
            responseObj.put("message", "EXT : INTERNAL SERVER ERROR");
            controllerLog.logStreamWriter("[RESPONSE]:"+responseObj.toString());
            return (responseObj.toString());

        }
        return resp;
    }
    public String curl_postBaseApi(String url, String data) {
        System.out.println("request: "+data);
        controllerLog.logStreamWriter("[BASEAPI][REQUEST]:"+data);
        System.out.println("[BASEAPI][REQUEST]:"+data);
        JSONObject responseObj = null;
        try {
            BufferedReader rd = null;
            String line = null;

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
//                responseObj.put("responseCode", code);
            } else {
                responseObj = new JSONObject();
                responseObj.put("responseCode", "500");
                responseObj.put("ack", "NOK");
                responseObj.put("message", "Mohon maaf untuk sementara transaksi tidak dapat dilakukan");
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseObj.put("responseCode", "200");
            responseObj.put("ack", "NOK");
            responseObj.put("message", "EXT : INTERNAL SERVER ERROR");
            controllerLog.logStreamWriter("[BASEAPI][RESPONSE]:"+responseObj.toString());
            return (responseObj.toString());

        }

        System.out.println("[BASEAPI][RESPONSE]:"+responseObj.toString());
        controllerLog.logStreamWriter("[BASEAPI][RESPONSE]:"+responseObj.toString());
        return (responseObj.toString());
    }

    public String getAuthorized(EntityConfig entityConfig){
        String result = "";
        try {
            String s = entityConfig.getOttopayInitiator();
            BASE64Encoder enc = new BASE64Encoder();
            result =  enc.encode((s.getBytes()));
            System.out.println("result "+s+"|"+result);
        }catch (Exception e){
        e.printStackTrace();
        }

        return result;
    }

}
