/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;

import apigatewayottopay.controller.ControllerEncDec;
import apigatewayottopay.entity.EntityConfig;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.json.simple.JSONObject;
import org.restlet.data.CookieSetting;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hasan
 */
public class CookieHelper {

    private JSONObject jSONObject;
    private Element elemCache;
    private final Helper helper;
    private final Cache cache;
    private final Cache cacheConfig;
    private final EntityConfig entityConfig;
    private final ControllerEncDec encDec;

    public CookieHelper() {
        //get config in cacheConfig
        CacheManager cacheManager = CacheManager.getInstance();
        this.cache = cacheManager.getCache("TrueMoneyAppCookies");
        this.cache.evictExpiredElements();
        this.cache.flush();

        this.cacheConfig = cacheManager.getCache("TrueMoneyApp");
        Element config = this.cacheConfig.get("config");
        this.entityConfig = (EntityConfig) config.getObjectValue();

        this.encDec = new ControllerEncDec(this.entityConfig.getEncIv(), this.entityConfig.getEncKey());
        this.helper = new Helper();
    }

    public boolean checkExistCookie(String cookie) {
        if (this.cache.isKeyInCache(cookie)) {
            this.elemCache = this.cache.get(cookie);
            if (this.elemCache != null) {
                if (this.cache.isExpired(this.elemCache)) {
                    //comment this if request can be paralel from client, if client request just one at the time, uncoment
                    removeCookie(cookie);
                    return false;
                } else {
                    this.jSONObject = (JSONObject) elemCache.getObjectValue();
                    return true;
                }
            } else {
                //comment this if request can be paralel from client, if client request just one at the time, uncoment
                removeCookie(cookie);
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean checkExpireCookie(String cookie) {
        this.elemCache = this.cache.get(cookie);
        this.jSONObject = (JSONObject) elemCache.getObjectValue();
        String cookieStore = (String) this.jSONObject.get("cookie");
        String expireDate = (String) this.jSONObject.get("expireDate");

        Date d1 = null;
        Date d2 = null;

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HHmmss");
        Date date = new Date();

        String dateStart = format.format(date);
        String dateStop = expireDate;
        long totalDiffSecond = 0;
        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateStop);

            //in milliseconds
            long diff = d2.getTime() - d1.getTime();

            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);

            long secondDay = diffDays * 1440 * 60;
            long secondHour = diffHours * 60 * 60;
            long secondMinute = diffMinutes * 60;
            long secondSec = diffSeconds;
            totalDiffSecond = secondDay + secondHour + secondMinute + secondSec;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (totalDiffSecond <= 0) {
            return true; //has expire
        } else {
            return false; //not expire
        }
    }

    public CookieSetting setCookie(String oldCookie) {
        //CookieSetting(version, name, value);
        CookieSetting cS = new CookieSetting(0, "TrueMoneyAppCookies", this.helper.createID());
        cS.setMaxAge(Integer.parseInt(this.entityConfig.getSessionTtl()));
        //save cookie to cache
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, cS.getMaxAge());
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HHmmss");
        String formatted = format.format(calendar.getTime());
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("cookie", cS.getValue());
        jSONObject.put("expireDate", formatted);
        //copy others data from old cookie
        if (!oldCookie.equals("")) {
            this.elemCache = this.cache.get(oldCookie);
            this.jSONObject = (JSONObject) this.elemCache.getObjectValue();
            for (Iterator iterator = this.jSONObject.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                if (!key.equals("cookie") && !key.equals("expireDate")) {
                    jSONObject.put(key, this.jSONObject.get(key));
                }
            }
            //remove old cookies
            if (this.cache.isKeyInCache(oldCookie)) {
                //comment this if request can be paralel from client, if client request just one at the time, uncoment
                this.removeCookie(oldCookie);
            }
        }
        this.cache.put(new Element(cS.getValue(), jSONObject));
        return cS;
    }

    public void addDataCookie(String cookie, String key, String value) {
        this.elemCache = this.cache.get(cookie);
        this.jSONObject = (JSONObject) this.elemCache.getObjectValue();
        this.jSONObject.put(key, value);
        this.cache.put(new Element(cookie, jSONObject));
    }

    public void removeCookie(final String cookie) {
        //avoid session expire when multithread http request trigger remove cookies same time, so we remove old cookie 2 minute after
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(Integer.parseInt(entityConfig.getTimeRemoveBeforeSessionDelete()));
                    cache.remove(cookie);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public String respExpire() {
        String resp = "";
        JSONObject jsonResp = new JSONObject();
        jsonResp.put("rc", "19");
        jsonResp.put("message", "Session expired");
        try {
            resp = new String(jsonResp.toString());
        } catch (Exception ex) {
            Logger.getLogger(CookieHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resp;
    }
}
