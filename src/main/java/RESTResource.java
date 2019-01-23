/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import apigatewayottopay.controller.ControllerEncDec;
import apigatewayottopay.controller.ControllerLog;
import apigatewayottopay.controller.ControllerOttopay;
import apigatewayottopay.entity.EntityConfig;
import apigatewayottopay.helper.Constant;
import apigatewayottopay.helper.CookieHelper;
import apigatewayottopay.helper.Helper;
import apigatewayottopay.helper.Utilities;
import com.google.common.base.Throwables;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.restlet.data.*;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.*;
import org.restlet.util.Series;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class RESTResource extends ServerResource {

    JSONObject jsonObject;
    JSONParser parser = new JSONParser();
    Cache cacheConfig;
    Helper hlp;
    private final EntityConfig entityConfig;
    private final ControllerLog controllerLog;
    private String cookieClient;
    private boolean expireCookie;
    private CookieHelper cookieHelper;
    private final Utilities utilities;
    private final ControllerEncDec encDec;

    public RESTResource() {
        CacheManager cacheManager = CacheManager.getInstance();
        this.cacheConfig = cacheManager.getCache("TrueMoneyApp");
        Element config = this.cacheConfig.get("config");
        this.entityConfig = (EntityConfig) config.getObjectValue();
        this.hlp = new Helper();
        this.utilities = new Utilities();
        this.encDec = new ControllerEncDec(this.entityConfig.getEncIv(), this.entityConfig.getEncKey());
        this.controllerLog = new ControllerLog();
        cookieHelper = new CookieHelper();
    }

    public void cookieHandler() {
        //-------------------------------------cookies-------------------------------------
        Series<Header> headers = getRequest().getHeaders();
        System.out.println("headers = " + headers);
        this.cookieHelper = new CookieHelper();
        this.cookieClient = headers.getFirstValue("Cookie");
        this.cookieClient = (this.cookieClient != null) ? this.cookieClient : "nullCookieFromClient";
        boolean existCookie = cookieHelper.checkExistCookie(this.cookieClient);
        this.expireCookie = true;
        if (existCookie) {
            //check expire
            this.expireCookie = cookieHelper.checkExpireCookie(this.cookieClient);
            if (this.expireCookie) {
                //cookie expire, comment this if request can be paralel from client, if client request just one at the time, uncoment
                cookieHelper.removeCookie(this.cookieClient);
            } else {
                CookieSetting cS = cookieHelper.setCookie(this.cookieClient);//recreate/refresh new cookie
                this.getResponse().getCookieSettings().add(cS);
                this.cookieClient = cS.getValue(); // save new cookie to variable
            }
        } else { //generate cookies
            CookieSetting cS = cookieHelper.setCookie("");
            this.getResponse().getCookieSettings().add(cS);
            this.cookieClient = cS.getValue(); // save new cookie to variable
        }
        //----------------------------------end cookies-------------------------------------
    }

    @Options
    public void doOptions(Representation entity) {
        Form responseHeaders = (Form) getResponse().getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null) {
            responseHeaders = new Form();
            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
        }
        responseHeaders.add("Access-Control-Allow-Origin", "*");
        responseHeaders.add("Access-Control-Allow-Methods", "POST,OPTIONS");
        responseHeaders.add("Access-Control-Allow-Headers", "Content-Type");
        responseHeaders.add("Access-Control-Allow-Credentials", "false");
        responseHeaders.add("Access-Control-Max-Age", "60");
    }

    @Post
    public Representation postHandler(Representation entity) throws IOException, Exception {
        HttpServletRequest servletRequest;
        entity.getMediaType();
        String path = getRequest().getResourceRef().getPath();
        Series<Header> headers = getRequest().getHeaders();

        String sendKey = headers.getValues("send-key");


        String[] pathSplit = path.split("/");
        Representation result = null;
        Form form = new Form(entity);

        String address = getClientInfo().getAddress();
        int port = getClientInfo().getPort();

        String payloadRequest = form.getNames().toString();
        JSONArray arrayObj = null;
        JSONObject jSONObject = null;
        if (payloadRequest.length() == 2) {
            result = new StringRepresentation("API GATEWAY OTTOPAY 1.0.0 ["+entityConfig.getServer()+"]", MediaType.TEXT_HTML);
            setStatus(Status.SUCCESS_OK);
            return result;
        }
        try {
            JSONParser jsonParser = new JSONParser();
            arrayObj = (JSONArray) jsonParser.parse(payloadRequest);
            jSONObject = (JSONObject) arrayObj.get(0);
        } catch (ParseException ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
            Logger.getLogger(RESTResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        controllerLog.logStreamWriter("");
        controllerLog.logStreamWriter("[INTERNAL REQUEST] "+jSONObject);
        if (!entityConfig.getSendKey().equalsIgnoreCase(sendKey)){
            result = new StringRepresentation(Constant.Pesan.getInvalidSign.pesan().toString(), MediaType.TEXT_PLAIN);
            return result;
        }
        if ("status".equalsIgnoreCase(pathSplit[2].toLowerCase())) {
            result = new StringRepresentation("API GATEWAY OTTOPAY 1.0.0 ["+entityConfig.getServer()+"]", MediaType.TEXT_HTML);
            setStatus(Status.SUCCESS_OK);
        }
        else if ("inquiry".equalsIgnoreCase(pathSplit[2].toLowerCase())) {
            controllerLog.logStreamWriter("[INTERNAL INQUIRY]");

            ControllerOttopay controllerOttopay = new ControllerOttopay();
            String resp = controllerOttopay.doInquiry(jSONObject, cookieClient);
            result = new StringRepresentation(resp, MediaType.TEXT_PLAIN);
            setStatus(Status.SUCCESS_OK);
        }
        else if ("payment".equalsIgnoreCase(pathSplit[2].toLowerCase())) {
            controllerLog.logStreamWriter("[INTERNAL PAYMENT]");
            ControllerOttopay controllerOttopay = new ControllerOttopay();
            String resp = controllerOttopay.doPayment(jSONObject, cookieClient);
            result = new StringRepresentation(resp, MediaType.TEXT_PLAIN);
            setStatus(Status.SUCCESS_OK);
        }

        return result;
    }

    @Get
    public Representation getHandler(Representation entity) throws IOException {
        Representation result = null;
        String path = getRequest().getResourceRef().getPath();
        String[] pathSplit = path.split("/");
        Form form = new Form(entity);
        String payloadRequest = form.getNames().toString();
        System.out.println(payloadRequest.length());
        if (payloadRequest.length() == 2) {
            result = new StringRepresentation("<marquee behavior=\"alternate\" direction=\"left\">API GATEWAY OTTOPAY 1.0.0 ["+entityConfig.getServer()+"]</marquee>\n", MediaType.TEXT_HTML);
            setStatus(Status.SUCCESS_OK);
            return result;
        }
        return result;
    }
    @Delete
    public Representation deleteHandler(Representation entity) throws IOException, Exception {
        HttpServletRequest servletRequest;
        entity.getMediaType();
        String path = getRequest().getResourceRef().getPath();
        Series<Header> headers = getRequest().getHeaders();

        String[] pathSplit = path.split("/");
        Representation result = null;
        Form form = new Form(entity);

        String address = getClientInfo().getAddress();
        int port = getClientInfo().getPort();

        String payloadRequest = form.getNames().toString();
        JSONArray arrayObj = null;
        JSONObject jSONObject = null;
        if (payloadRequest.length() == 2) {
            result = new StringRepresentation("API GATEWAY OTTOPAY 1.0.0 ["+entityConfig.getServer()+"]", MediaType.TEXT_HTML);
            setStatus(Status.SUCCESS_OK);
            return result;
        }
        try {
            JSONParser jsonParser = new JSONParser();
            arrayObj = (JSONArray) jsonParser.parse(payloadRequest);
            jSONObject = (JSONObject) arrayObj.get(0);
        } catch (ParseException ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLog.logErrorWriter(s);
            Logger.getLogger(RESTResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        controllerLog.logStreamWriter("");
        controllerLog.logStreamWriter("[EXTERNAL REQUEST] "+jSONObject);

        String signatureEx = headers.getValues("Signature");
        String timestamp = headers.getValues("Timestamp");
        String authorization = headers.getValues("Authorization");
        String signatureIn = encDec.createSignature(jSONObject + ":"+timestamp, entityConfig.getOttopayKey());
        if (!signatureEx.equalsIgnoreCase(signatureIn)){
            result = new StringRepresentation(Constant.Pesan.getInvalidSign.pesan().toString(), MediaType.TEXT_PLAIN);
            return result;
        }
        if ("payment".equalsIgnoreCase(pathSplit[2].toLowerCase())) {
            controllerLog.logStreamWriter("[EXTERNAL DELETE PAYMENT]");
            ControllerOttopay controllerOttopay = new ControllerOttopay();
            String resp = controllerOttopay.doReverse(jSONObject, cookieClient);
            result = new StringRepresentation(resp, MediaType.TEXT_PLAIN);
            setStatus(Status.SUCCESS_OK);
        }
        else if ("refund".equalsIgnoreCase(pathSplit[2].toLowerCase())) {
            controllerLog.logStreamWriter("[EXTERNAL DELETE REFUND]");
            ControllerOttopay controllerOttopay = new ControllerOttopay();
            String resp = controllerOttopay.doRefund(jSONObject, cookieClient);
            result = new StringRepresentation(resp, MediaType.TEXT_PLAIN);
            setStatus(Status.SUCCESS_OK);
        }


        return result;
    }
}
