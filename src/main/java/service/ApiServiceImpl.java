package service;

import apigatewayottopay.connection.DatabaseUtilitiesPgsql;
import apigatewayottopay.connection.DatabaseUtilitiesPgsqlLog;
import apigatewayottopay.controller.ControllerEncDec;
import apigatewayottopay.controller.ControllerLog;
import apigatewayottopay.entity.*;
import apigatewayottopay.helper.Helper;
import apigatewayottopay.helper.SendRequest;
import apigatewayottopay.helper.Utilities;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.xml.parsers.ParserConfigurationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

/**
 * @author Reza Dio Nugraha
 */
public class ApiServiceImpl implements ApiService {
    EntityConfig entityConfig;
    Helper hlp;
    JSONParser parser = new JSONParser();
    JSONObject jsonObject;
    private final ControllerEncDec encDec;
    private final Utilities utilities;
    static SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy");
    private final ControllerLog controllerLog;
    private final SendRequest sendRequest = new SendRequest(entityConfig);

    public ApiServiceImpl(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        this.encDec = new ControllerEncDec(this.entityConfig.getEncIv(),this.entityConfig.getEncKey());
        this.hlp = new Helper();
        this.utilities = new Utilities();
        this.controllerLog = new ControllerLog();
    }

    @Override
    public InquiryResponse getInquiry(InquiryRequest request,JSONObject jsonReq) {
        InquiryResponse response = new InquiryResponse();
        InquiryRequest reqs = new InquiryRequest();
        reqs.setMerchantId(request.getMerchantId());
        Gson gson= new Gson();
        try {

//            String resp = sendRequest.curl_post(entityConfig, entityConfig.getOttopayInqurl(),gson.toJson(reqs),request,null);
            String resp = sendRequest.curl_post(entityConfig, entityConfig.getOttopayInqurl(),gson.toJson(reqs),request,null);
            response = gson.fromJson(resp,InquiryResponse.class);
            System.out.println(gson.toJson(resp));
            //<editor-fold defaultstate="collapsed" desc="query insert log tr_otopay_log ">
            String idtransaksi = "";
            DatabaseUtilitiesPgsqlLog databaseUtilitiesPgsqlLog = new DatabaseUtilitiesPgsqlLog();
            Connection connPgsql = null;
            PreparedStatement stPgsql = null;
            ResultSet rsPgsql = null;

            try {

                String account_id = "";
                String id_trx = "";
                String id_trx_supplier = "";
                String payment_otopay = "";
                String request_content = "";
                String response_content = "";
                String response_code = "";
                String trx_timestamp = "";
                String type_trx = "";

                if(jsonReq.get("customerId") !=null){account_id = jsonReq.get("customerId").toString();};
                if(response.getOrderId()!=null){id_trx=response.getOrderId();};
                if(response.getMerchantId() !=null){id_trx_supplier = response.getMerchantId();}
                if(jsonReq.get("customerId") !=null){payment_otopay = jsonReq.get("customerId").toString();}
                if(reqs!=null){request_content = gson.toJson(reqs);}
                if(response!=null){response_content= gson.toJson(response);}
                if(response.getResponseCode() !=null){response_code = response.getResponseCode();}
                // Update ke Tabel Transaksi
                connPgsql = databaseUtilitiesPgsqlLog.getConnection(this.entityConfig);
                String codalog = "INSERT INTO \"tr_ottopay_log\" (\n" +
                        "	\"account_id\",\n" +
                        "	\"id_trx\",\n" +
                        "	\"id_trx_supplier\",\n" +
                        "	\"payment_otopay\",\n" +
                        "	\"request_content\",\n" +
                        "	\"response_content\",\n" +
                        "	\"response_code\",\n" +
                        "	\"trx_timestamp\",\n" +
                        "	\"type_trx\"\n" +
                        ")\n" +
                        "VALUES\n" +
                        "	(\n" +
                        "		?,\n" +
                        "		?,\n" +
                        "		?,\n" +
                        "		?,\n" +
                        "		?,\n" +
                        "		?,\n" +
                        "		?,\n" +
                        "		now(),\n" +
                        "		'INQUIRY'\n" +
                        "	)";
                stPgsql = connPgsql.prepareStatement(codalog);
                stPgsql.setString(1, account_id);
                stPgsql.setString(2, id_trx);
                stPgsql.setString(3, id_trx_supplier);
                stPgsql.setString(4, payment_otopay);
                stPgsql.setString(5, request_content);
                stPgsql.setString(6, response_content);
                stPgsql.setString(7, response_code);

                int result = stPgsql.executeUpdate();

                System.out.println("result :"+result);

            } catch (SQLException ex) {
                String s = Throwables.getStackTraceAsString(ex);
                controllerLog.logErrorWriter(s);
                ex.printStackTrace();
            } catch (Exception ex) {
                String s = Throwables.getStackTraceAsString(ex);
                controllerLog.logErrorWriter(s);
                ex.printStackTrace();
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
                    String s = Throwables.getStackTraceAsString(ex);
                    controllerLog.logErrorWriter(s);
                    ex.printStackTrace();

                }
            }
            //</editor-fold>
        }catch (Exception e){
            controllerLog.logErrorWriter(e.getMessage());
            e.printStackTrace();

        }
        return response;
    }

    @Override
    public PaymentResponse getPayment(BaseApiRequest baseApiRequest,JSONObject jsonReq) {
       BaseApiResponse baseApiResponse = new BaseApiResponse();
       PaymentRequest paymentRequest = new PaymentRequest();
       PaymentResponse response = new PaymentResponse();
       String timestamp = this.hlp.getUnixTimestamp();
       Gson gson = new Gson();
       String rc = "";

//        try {
//           String baseApiResult = sendRequest.curl_postBaseApi(this.entityConfig.getBaseApiUrl(), gson.toJson(baseApiRequest));
//           baseApiResponse = gson.fromJson(baseApiResult, BaseApiResponse.class);
//           rc = baseApiResponse.getRc();
//
//       }catch (Exception e) {
//
//       }
           try {
               if(baseApiRequest.getNohp()!=null && !baseApiRequest.getNohp().equalsIgnoreCase("")){
                   paymentRequest.setCustomerId(this.entityConfig.getOttopayInitiator()+";;;;"+baseApiRequest.getNohp());
               }else {
                   paymentRequest.setCustomerId(this.entityConfig.getOttopayInitiator()+";;;;"+baseApiRequest.getAccountid());
               }
               paymentRequest.setAmount(baseApiRequest.getAmount());
               paymentRequest.setCurrency("IDR");
               paymentRequest.setCustomerName(baseApiRequest.getName());
               paymentRequest.setIssuerInstitutionId(this.entityConfig.getOttopayInitiator());
               paymentRequest.setIssuerInstitutionName("TRUEMONEY");
               paymentRequest.setMerchantId(baseApiRequest.getCustomerId());
               paymentRequest.setOrderId(baseApiRequest.getOrderid());
               paymentRequest.setPaymentStatus(baseApiRequest.getTrxstatus());
               paymentRequest.setReferenceNumber(baseApiRequest.getTransid());
               paymentRequest.setTransactionDate(this.hlp.parsingDateNow());
               paymentRequest.setAdditionalData(this.entityConfig.getOttopayAdditionalData());
               String reqs = gson.toJson(paymentRequest);
               String signature = encDec.createSignature(gson.toJson(paymentRequest) + ":"+timestamp, entityConfig.getOttopayKey());
               paymentRequest.setSignature(signature);

               paymentRequest.setTimestamp(timestamp);
               String paymentResult = sendRequest.curl_post(this.entityConfig, this.entityConfig.getOttopayPayurl(),reqs,null,paymentRequest);
               response = gson.fromJson(paymentResult,PaymentResponse.class);
               //<editor-fold defaultstate="collapsed" desc="query insert log tr_otopay_log ">
               String idtransaksi = "";
               DatabaseUtilitiesPgsqlLog databaseUtilitiesPgsqlLog = new DatabaseUtilitiesPgsqlLog();
               Connection connPgsql = null;
               PreparedStatement stPgsql = null;
               ResultSet rsPgsql = null;

               try {
                   // Update ke Tabel Transaksi
                   connPgsql = databaseUtilitiesPgsqlLog.getConnection(this.entityConfig);
                   String codalog = "INSERT INTO \"tr_ottopay_log\" (\n" +
                           "	\"account_id\",\n" +
                           "	\"id_trx\",\n" +
                           "	\"id_trx_supplier\",\n" +
                           "	\"payment_otopay\",\n" +
                           "	\"request_content\",\n" +
                           "	\"response_content\",\n" +
                           "	\"response_code\",\n" +
                           "	\"trx_timestamp\",\n" +
                           "	\"type_trx\",\n" +
                           "	\"status_trx\",\n" +
                           "	\"response_code_tmn\""+
                           ")\n" +
                           "VALUES\n" +
                           "	(\n" +
                           "		?,\n" +
                           "		?,\n" +
                           "		?,\n" +
                           "		?,\n" +
                           "		?,\n" +
                           "		?,\n" +
                           "		?,\n" +
                           "		'now',\n" +
                           "		'PAYMENT',\n" +
                           "		?,\n" +
                           "		?\n" +
                           "	)";
//               System.out.println(codalog);
                   stPgsql = connPgsql.prepareStatement(codalog);
                   stPgsql.setString(1, jsonReq.get("accountId").toString());
                   stPgsql.setString(2, baseApiRequest.getTransid());
                   stPgsql.setString(3, response.getReceiptNumber());
                   stPgsql.setString(4, response.getReceiptNumber());
                   stPgsql.setString(5, gson.toJson(paymentRequest));
                   stPgsql.setString(6, gson.toJson(response));
                   stPgsql.setString(7, response.getResponseCode());
                   stPgsql.setString(8, response.getResponseDescription());
                   stPgsql.setString(9, baseApiResponse.getRc());
//               System.out.println(stPgsql.toString());
                   int result = stPgsql.executeUpdate();
//               System.out.println("result :"+result);

               } catch (SQLException ex) {
                   String s = Throwables.getStackTraceAsString(ex);
                   controllerLog.logErrorWriter(s);
                   ex.printStackTrace();
               } catch (Exception ex) {
                   String s = Throwables.getStackTraceAsString(ex);
                   controllerLog.logErrorWriter(s);
                   ex.printStackTrace();
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
                       String s = Throwables.getStackTraceAsString(ex);
                       controllerLog.logErrorWriter(s);
                       ex.printStackTrace();

                   }
               }
               //</editor-fold>
//            baseApiRequest.setAccountid(re);
           }catch (Exception e){
               String s = Throwables.getStackTraceAsString(e);
               controllerLog.logErrorWriter(s);
               e.printStackTrace();
           }
        return response;
    }

    @Override
    public ReversePaymentResponse getReverse(ReversePaymentRequest request, JSONObject jsonReq) {
        BaseApiResponse baseApiResponse = new BaseApiResponse();
        BaseApiRequest baseApiRequest = new BaseApiRequest();
        ReversePaymentResponse response = new ReversePaymentResponse();
        String timestamp = this.hlp.getUnixTimestamp();
        Gson gson = new Gson();
        String rc = "";
        String customerId = "";
        try {

            customerId = request.getCustomerId().split(";;;;")[1];

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //<editor-fold defaultstate="collapsed" desc="query get TrTransaksi ">
            try {
                DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
                Connection connPgsql = null;
                PreparedStatement stPgsql = null;
                ResultSet rsPgsql = null;
                try {
                    connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
                    String query = "SELECT id_transaksi,id_trx,id_trxsupplier,\"StatusTRX\" from \"TrTransaksi\" WHERE id_trx = ?";
                    stPgsql = connPgsql.prepareStatement(query);
                    stPgsql.setString(1, request.getReferenceNumber());
                    rsPgsql = stPgsql.executeQuery();
                    System.out.println(stPgsql);

                    while (rsPgsql.next()) {
                        baseApiRequest.setTransaksiId(rsPgsql.getString("id_transaksi"));
                        baseApiRequest.setTransid( rsPgsql.getString("id_trx"));
                        baseApiRequest.setIdTrxsupplier(rsPgsql.getString("id_trxsupplier"));
                        baseApiRequest.setStatusTrx(rsPgsql.getString("StatusTRX"));
                    }
                    if(baseApiRequest.getStatusTrx().equalsIgnoreCase("GAGAL")){
                        response.setResponseCode("02");
                        response.setResponseDescription("Already Refund");
                        return response;
                    }
                } catch (ParserConfigurationException ex) {
                    String s = Throwables.getStackTraceAsString(ex);
                    controllerLog.logErrorWriter(s);
                } catch (SQLException ex) {
                    String s = Throwables.getStackTraceAsString(ex);
                    controllerLog.logErrorWriter(s);
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
                        String s = Throwables.getStackTraceAsString(ex);
                        controllerLog.logErrorWriter(s);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            //</editor-fold>

            try {
                baseApiRequest.setStatusTrx("GAGAL");
                String paymentResult = sendRequest.curl_postBaseApi(this.entityConfig.getBaseApiReverseUrl(),gson.toJson(baseApiRequest));
                baseApiResponse = gson.fromJson(paymentResult,BaseApiResponse.class);

                if(baseApiResponse!=null && baseApiResponse.getRc().equalsIgnoreCase("00")){
                    response.setResponseCode("00");
                    response.setResponseDescription("Success");
                }else{
                    response.setResponseCode("01");
                    response.setResponseDescription("Failed");
                }

                //<editor-fold defaultstate="collapsed" desc="query insert log tr_otopay_log ">
                String idtransaksi = "";
                DatabaseUtilitiesPgsqlLog databaseUtilitiesPgsqlLog = new DatabaseUtilitiesPgsqlLog();
                Connection connPgsql = null;
                PreparedStatement stPgsql = null;
                ResultSet rsPgsql = null;

                try {
                    // Update ke Tabel Transaksi
                    connPgsql = databaseUtilitiesPgsqlLog.getConnection(this.entityConfig);
                    String codalog = "INSERT INTO \"tr_ottopay_log\" (\n" +
                            "	\"account_id\",\n" +
                            "	\"id_trx\",\n" +
                            "	\"id_trx_supplier\",\n" +
                            "	\"payment_otopay\",\n" +
                            "	\"request_content\",\n" +
                            "	\"response_content\",\n" +
                            "	\"response_code\",\n" +
                            "	\"trx_timestamp\",\n" +
                            "	\"type_trx\",\n" +
                            "	\"status_trx\",\n" +
                            "	\"response_code_tmn\""+
                            ")\n" +
                            "VALUES\n" +
                            "	(\n" +
                            "		?,\n" +
                            "		?,\n" +
                            "		?,\n" +
                            "		?,\n" +
                            "		?,\n" +
                            "		?,\n" +
                            "		?,\n" +
                            "		'now',\n" +
                            "		'REFUND',\n" +
                            "		?,\n" +
                            "		?\n" +
                            "	)";
//               System.out.println(codalog);

                    stPgsql = connPgsql.prepareStatement(codalog);
                    stPgsql.setString(1, customerId);
                    stPgsql.setString(2, baseApiRequest.getTransid());
                    stPgsql.setString(3, baseApiRequest.getIdTrxsupplier());
                    stPgsql.setString(4, request.getReferenceNumber());
                    stPgsql.setString(5, gson.toJson(request)+","+gson.toJson(baseApiRequest));
                    stPgsql.setString(6, gson.toJson(response));
                    stPgsql.setString(7, response.getResponseCode());
                    stPgsql.setString(8, response.getResponseDescription());
                    stPgsql.setString(9, baseApiResponse.getRc());
//               System.out.println(stPgsql.toString());
                    int result = stPgsql.executeUpdate();
//               System.out.println("result :"+result);

                } catch (SQLException ex) {
                    String s = Throwables.getStackTraceAsString(ex);
                    controllerLog.logErrorWriter(s);
                    ex.printStackTrace();
                } catch (Exception ex) {
                    String s = Throwables.getStackTraceAsString(ex);
                    controllerLog.logErrorWriter(s);
                    ex.printStackTrace();
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
                        String s = Throwables.getStackTraceAsString(ex);
                        controllerLog.logErrorWriter(s);
                        ex.printStackTrace();

                    }
                }
            }catch (Exception e){
                String s = Throwables.getStackTraceAsString(e);
                controllerLog.logErrorWriter(s);
                e.printStackTrace();

            }

            //</editor-fold>
//            baseApiRequest.setAccountid(re);
        }catch (Exception e){
            String s = Throwables.getStackTraceAsString(e);
            controllerLog.logErrorWriter(s);
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public ReversePaymentResponse getRefund(RefundPaymentRequest request, JSONObject jsonReq) {
        BaseApiResponse baseApiResponse = new BaseApiResponse();
        BaseApiRequest baseApiRequest = new BaseApiRequest();
        ReversePaymentResponse response = new ReversePaymentResponse();
        String timestamp = this.hlp.getUnixTimestamp();
        Gson gson = new Gson();
        String rc = "";
        String customerId = "";
        try {

            customerId = request.getCustomerId().split(";;;;")[1];

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //<editor-fold defaultstate="collapsed" desc="query get TrTransaksi ">
            try {
                DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
                Connection connPgsql = null;
                PreparedStatement stPgsql = null;
                ResultSet rsPgsql = null;
                try {
                    connPgsql = databaseUtilitiesPgsql.getConnection(this.entityConfig);
                    String query = "SELECT\n" +
                            "	id_transaksi,\n" +
                            "	id_trx,\n" +
                            "	id_trxsupplier,\n" +
                            "	\"StatusTRX\",\n" +
                            "	\"TimeStamp\",\n" +
                            "	\"Nominal\",\n" +
                            "	id_feesupplier,\n" +
                            "	\"Keterangan\",\n" +
                            "	id_agentaccount,\n" +
                            "	id_memberaccount,\n" +
                            "	\"Biaya\",\n" +
                            "	hargajual,\n" +
                            "	hargabeli,\n" +
                            "	id_pelanggan\n" +
                            "FROM\n" +
                            "	\"TrTransaksi\"\n" +
                            "WHERE\n" +
                            "	id_trx = ? ;";
                    stPgsql = connPgsql.prepareStatement(query);
                    stPgsql.setString(1, request.getReferenceNumber());
                    rsPgsql = stPgsql.executeQuery();
                    System.out.println(stPgsql);

                    while (rsPgsql.next()) {
                        baseApiRequest.setAmount(rsPgsql.getString("Nominal"));
                        baseApiRequest.setTransid(rsPgsql.getString("id_trx"));
                        baseApiRequest.setTransidtmn(rsPgsql.getString("id_transaksi"));
                        baseApiRequest.setFeesupplierid(rsPgsql.getString("id_feesupplier"));
                        baseApiRequest.setDescription(rsPgsql.getString("Keterangan"));
                        if(rsPgsql.getString("id_memberaccount")!=null && !rsPgsql.getString("id_memberaccount").equalsIgnoreCase("")){
                            baseApiRequest.setType("MEMBER");
                            baseApiRequest.setAccountid(rsPgsql.getString("id_memberaccount"));
                        }else {
                            baseApiRequest.setType("AGENT");
                            baseApiRequest.setAccountid(rsPgsql.getString("id_agentaccount"));
                        }
                        baseApiRequest.setBiayaadmin(rsPgsql.getString("Biaya"));
                        baseApiRequest.setHargajual(rsPgsql.getString("hargajual"));
                        baseApiRequest.setIdAgentAccount(rsPgsql.getString("id_agentaccount"));
                        baseApiRequest.setIdTrxsupplier(rsPgsql.getString("id_trxsupplier"));
                        baseApiRequest.setIdpelanggan(rsPgsql.getString("id_pelanggan"));
                        baseApiRequest.setHargabeli(rsPgsql.getString("hargabeli"));
                        baseApiRequest.setStatusTrx(rsPgsql.getString("StatusTRX"));
                        baseApiRequest.setUsername(entityConfig.getBaseApiUsername());



//                        baseApiRequest.setTransaksiId(rsPgsql.getString("id_transaksi"));
//                        baseApiRequest.setTransid( rsPgsql.getString("id_trx"));
//                        baseApiRequest.setIdTrxsupplier(rsPgsql.getString("id_trxsupplier"));
//                        baseApiRequest.setStatusTrx(rsPgsql.getString("StatusTRX"));



                        response.setTransactionDate(utilities.getSimpleDate(rsPgsql.getString("TimeStamp")));
                        response.setReferenceNumber( rsPgsql.getString("id_trx"));
                        response.setCurrency("IDR");
                        response.setAmount(rsPgsql.getString("Nominal"));
                    }
                    if(baseApiRequest.getStatusTrx().equalsIgnoreCase("GAGAL")){
                        response.setResponseCode("02");
                        response.setResponseDescription("Already Refund");
                        return response;
                    }
                } catch (ParserConfigurationException ex) {
                    String s = Throwables.getStackTraceAsString(ex);
                    controllerLog.logErrorWriter(s);
                } catch (SQLException ex) {
                    String s = Throwables.getStackTraceAsString(ex);
                    controllerLog.logErrorWriter(s);
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
                        String s = Throwables.getStackTraceAsString(ex);
                        controllerLog.logErrorWriter(s);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            //</editor-fold>

            try {
                baseApiRequest.setTrxstatus("UPDATEGAGAL");
                baseApiRequest.setSignature(utilities.signActive(baseApiRequest.getTransid(),entityConfig.getBaseApiKey(),baseApiRequest.getAccountid(),baseApiRequest.getUsername()));
                String paymentResult = sendRequest.curl_postBaseApi(this.entityConfig.getBaseApiReverseUrl(),gson.toJson(baseApiRequest));
                baseApiResponse = gson.fromJson(paymentResult,BaseApiResponse.class);

                if(baseApiResponse!=null && baseApiResponse.getRc().equalsIgnoreCase("00")){
                    response.setResponseCode("00");
                    response.setResponseDescription("Success");
                }else{
                    response.setResponseCode("01");
                    response.setResponseDescription("Failed");
                }

                //<editor-fold defaultstate="collapsed" desc="query insert log tr_otopay_log ">
                String idtransaksi = "";
                DatabaseUtilitiesPgsqlLog databaseUtilitiesPgsqlLog = new DatabaseUtilitiesPgsqlLog();
                Connection connPgsql = null;
                PreparedStatement stPgsql = null;
                ResultSet rsPgsql = null;

                try {
                    // Update ke Tabel Transaksi
                    connPgsql = databaseUtilitiesPgsqlLog.getConnection(this.entityConfig);
                    String codalog = "INSERT INTO \"tr_ottopay_log\" (\n" +
                            "	\"account_id\",\n" +
                            "	\"id_trx\",\n" +
                            "	\"id_trx_supplier\",\n" +
                            "	\"payment_otopay\",\n" +
                            "	\"request_content\",\n" +
                            "	\"response_content\",\n" +
                            "	\"response_code\",\n" +
                            "	\"trx_timestamp\",\n" +
                            "	\"type_trx\",\n" +
                            "	\"status_trx\",\n" +
                            "	\"response_code_tmn\""+
                            ")\n" +
                            "VALUES\n" +
                            "	(\n" +
                            "		?,\n" +
                            "		?,\n" +
                            "		?,\n" +
                            "		?,\n" +
                            "		?,\n" +
                            "		?,\n" +
                            "		?,\n" +
                            "		'now',\n" +
                            "		'REFUND',\n" +
                            "		?,\n" +
                            "		?\n" +
                            "	)";
//               System.out.println(codalog);

                    stPgsql = connPgsql.prepareStatement(codalog);
                    stPgsql.setString(1, customerId);
                    stPgsql.setString(2, baseApiRequest.getTransid());
                    stPgsql.setString(3, baseApiRequest.getIdTrxsupplier());
                    stPgsql.setString(4, request.getReferenceNumber());
                    stPgsql.setString(5, gson.toJson(request)+","+gson.toJson(baseApiRequest));
                    stPgsql.setString(6, gson.toJson(response));
                    stPgsql.setString(7, response.getResponseCode());
                    stPgsql.setString(8, response.getResponseDescription());
                    stPgsql.setString(9, baseApiResponse.getRc());
//               System.out.println(stPgsql.toString());
                    int result = stPgsql.executeUpdate();
//               System.out.println("result :"+result);

                } catch (SQLException ex) {
                    String s = Throwables.getStackTraceAsString(ex);
                    controllerLog.logErrorWriter(s);
                    ex.printStackTrace();
                } catch (Exception ex) {
                    String s = Throwables.getStackTraceAsString(ex);
                    controllerLog.logErrorWriter(s);
                    ex.printStackTrace();
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
                        String s = Throwables.getStackTraceAsString(ex);
                        controllerLog.logErrorWriter(s);
                        ex.printStackTrace();

                    }
                }
            }catch (Exception e){
                String s = Throwables.getStackTraceAsString(e);
                controllerLog.logErrorWriter(s);
                e.printStackTrace();
            }

            //</editor-fold>
//            baseApiRequest.setAccountid(re);
        }catch (Exception e){
            String s = Throwables.getStackTraceAsString(e);
            controllerLog.logErrorWriter(s);
            e.printStackTrace();
        }
        return response;
    }



}
