/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import controller.ControllerConfig;
import controller.ControllerMasterDataCache;
import entity.EntityConfig;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.application.Encoder;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.service.CorsService;
import org.restlet.service.EncoderService;
import org.restlet.util.Series;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Reza Nugraha
 */
public class Main {

    private static EntityConfig conf;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
            String workDir = args[0];
            //----------------get the config data and pass to config entity--------------------
            Main.conf = new EntityConfig();
            ControllerConfig konf = new ControllerConfig(Main.conf, workDir);
            Main.conf = konf.getData();
            //--------------------------ehcache init--------------------------------------  
            DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
            diskStoreConfiguration.setPath(workDir + "cache");
            // Already created a configuration object ...
            Configuration cacheConfiguration = new Configuration();
            cacheConfiguration.addDiskStore(diskStoreConfiguration);
            //Create a CacheManager using custom configuration
            CacheManager cacheManager = CacheManager.create(cacheConfiguration);
            //--------------------------ehcache save the config entity------------------------
            Cache memoryOnlyCache = new Cache("TrueMoneyApp", 1, false, true, 0, 0);
            cacheManager.addCache(memoryOnlyCache);
            //--------------------------get cache and put new element-------------------------------------
            Cache thisCache = cacheManager.getCache("TrueMoneyApp");
            thisCache.put(new Element("config", Main.conf));
            //-------------------------ehcache for cookies--------------------------
            //new Cache(workDir, maxElementsInMemory, overflow to disk, eternal, timeToLiveSeconds, timeToIdleSeconds)
            Cache memoryPlusDiskCache = new Cache("TrueMoneyAppCookies", 10000, true, false, 900, 0);
            cacheManager.addCache(memoryPlusDiskCache);
            
            //-------------------------ehcache for dataMaster--------------------------
            //new Cache(workDir, maxElementsInMemory, overflow to disk, eternal, timeToLiveSeconds, timeToIdleSeconds)
            memoryPlusDiskCache = new Cache("TrueMoneyAppMaster", 1, true, true, 0, 0);
            cacheManager.addCache(memoryPlusDiskCache);
            //get all master data to save to cache
            ControllerMasterDataCache controllerMasterDataCache = new ControllerMasterDataCache(Main.conf);
            ArrayList<HashMap<String, Object>> rowMsWording = controllerMasterDataCache.getMsWording();
            //--------------------------get cache of TrueMoneyAppMaster and put new element-------------------------
            thisCache = cacheManager.getCache("TrueMoneyAppMaster");
            thisCache.put(new Element("msWording", rowMsWording));
            
            //-----------------------------start web server----------------------------------
            final Component component = new Component();
            // Disable damn log
            component.setLogService(new org.restlet.service.LogService(false));

            //HTTP
            component.getServers().add(Protocol.HTTP, Integer.parseInt(Main.conf.getHttpPort()));
            component.getContext().getParameters().add("maxThreads", Main.conf.getMaxThreads());
            component.getContext().getParameters().add("minThreads", Main.conf.getMinThreads());
            component.getContext().getParameters().add("lowThreads", Main.conf.getLowThreads());
            component.getContext().getParameters().add("maxQueued", Main.conf.getMaxQueued());
            component.getContext().getParameters().add("maxTotalConnections", Main.conf.getMaxTotalConnections());
            component.getContext().getParameters().add("maxIoIdleTimeMs", Main.conf.getMaxIoIdleTimeMs());

//            HTTPS
            int httpsPort = Integer.parseInt(Main.conf.getHttpPort()) + 1;
            Server server = component.getServers().add(Protocol.HTTPS, httpsPort);
            Series<Parameter> parameters = server.getContext().getParameters();
            parameters.add("sslContextFactory", "org.restlet.engine.ssl.DefaultSslContextFactory");
            parameters.add("sslContextFactory", "org.eclipse.jetty.http.ssl.SslContextFactory");
            parameters.add("keystorePath", workDir + "config/TMMobileMember.jks");
            parameters.add("keystorePassword", "smartcity123");
            parameters.add("keyPassword", "smartcity123");
            parameters.add("keystoreType", "JKS");
            parameters.add("maxThreads", Main.conf.getMaxThreads());
            parameters.add("minThreads", Main.conf.getMinThreads());
            parameters.add("lowThreads", Main.conf.getLowThreads());
            parameters.add("maxQueued", Main.conf.getMaxQueued()); //never reject
            parameters.add("maxTotalConnections", Main.conf.getMaxTotalConnections());
            parameters.add("maxIoIdleTimeMs", Main.conf.getMaxIoIdleTimeMs());

            //allow cross domain
            CorsService corsService = new CorsService();
            corsService.setAllowedOrigins(new HashSet(Arrays.asList("*")));
            corsService.setAllowedCredentials(true);
            component.getServices().add(corsService);

            // encode the response
            Encoder encoder = new Encoder(component.getContext().createChildContext(), false, true, new EncoderService(true));
            encoder.setNext(RESTResource.class);
            component.getDefaultHost().attach(Main.conf.getPathUri(), encoder);

            //view all register server
            List<ConnectorHelper<Server>> servers = Engine.getInstance().getRegisteredServers();
            for (ConnectorHelper<Server> connectorHelper : servers) {
            }

            System.out.println("Server Ready At - " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            component.start();



        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
