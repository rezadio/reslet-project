package service;

import entity.EntityConfig;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Reza Dio Nugraha
 */
public class ConfigServiceImpl implements ConfigService {
    @Override
    public EntityConfig getData(EntityConfig conf, String workDir) {
        try {
            File file = new File(workDir + "config/config.xml");

            SAXBuilder saxBuilder = new SAXBuilder();
            org.jdom2.Document doc = saxBuilder.build(file);
            Element rootNode = doc.getRootElement();

            conf.setWorkDir(workDir);

            //populate commonServer config
            List list = rootNode.getChildren("commonServer");
            for (Object objectCommonServer : list) {
                Element node = (Element) objectCommonServer;
                conf.setServer(node.getChildText("server"));
                conf.setHttpPort(node.getChildText("portAdapter"));
                conf.setMaxThreads(node.getChildText("maxThreads"));
                conf.setMinThreads(node.getChildText("minThreads"));
                conf.setLowThreads(node.getChildText("lowThreads"));
                conf.setMaxQueued(node.getChildText("maxQueued"));
                conf.setMaxTotalConnections(node.getChildText("maxTotalConnections"));
                conf.setMaxIoIdleTimeMs(node.getChildText("maxIoIdleTimeMs"));
                conf.setPathUri(node.getChildText("pathUri"));
                conf.setSessionTtl(node.getChildText("sessionTtl"));
                conf.setTimeRemoveBeforeSessionDelete(node.getChildText("timeRemoveBeforeSessionDelete"));
            }
            //populate database pgsql config
            list = rootNode.getChildren("databasePgsql");
            for (Object objectDatabase : list) {
                Element node = (Element) objectDatabase;
                conf.setDbPgsql(node.getChildText("database"));
                conf.setDbPgsqlIp(node.getChildText("ip"));
                conf.setDbPgsqlPort(node.getChildText("port"));
                conf.setDbPgsqlUser(node.getChildText("user"));
                conf.setDbPgsqlPass(node.getChildText("password"));
            }

            list = rootNode.getChildren("databasePgsqlLog");
            for (Object objectDatabase : list) {
                Element node = (Element) objectDatabase;
                conf.setDbPgsqlLog(node.getChildText("database"));
                conf.setDbPgsqlLogIp(node.getChildText("ip"));
                conf.setDbPgsqlLogPort(node.getChildText("port"));
                conf.setDbPgsqlLogUser(node.getChildText("user"));
                conf.setDbPgsqlLogPass(node.getChildText("password"));
            }
            //populate database mysql config
            list = rootNode.getChildren("databaseMysql");
            for (Object objectDatabase : list) {
                Element node = (Element) objectDatabase;
                conf.setDbMysql(node.getChildText("database"));
                conf.setDbMysqlIp(node.getChildText("ip"));
                conf.setDbMysqlPort(node.getChildText("port"));
                conf.setDbMysqlUser(node.getChildText("user"));
                conf.setDbMysqlPass(node.getChildText("password"));
            }
            //populate encryption config
            list = rootNode.getChildren("encryption");
            for (Object objectXMPP : list) {
                Element node = (Element) objectXMPP;
                conf.setEncKeyDb(node.getChildText("keyDb"));
                conf.setEncKey(node.getChildText("key"));
                conf.setEncIv(node.getChildText("iv"));
                conf.setEncRequest(node.getChildText("encRequest"));
                conf.setEncResponse(node.getChildText("encResponse"));
            }
            //populate misc config
            list = rootNode.getChildren("misc");
            for (Object objectXMPP : list) {
                Element node = (Element) objectXMPP;
                conf.setScaleImgSize(node.getChildText("scaleImgSize"));
                conf.setSetConnectTimeout(node.getChildText("setConnectTimeout"));
                conf.setSetReadTimeout(node.getChildText("setReadTimeout"));
                conf.setlogStream(node.getChildText("logStream"));
                conf.setMasterFolder(node.getChildText("masterFolder"));
                conf.setIpService(node.getChildText("ipService"));
            }
            //populate ftpServerInventory config
            list = rootNode.getChildren("ftpServerInventory");
            for (Object objectXMPP : list) {
                Element node = (Element) objectXMPP;
                conf.setFtpIp(node.getChildText("ip"));
                conf.setFtpPort(node.getChildText("port"));
                conf.setFtpUser(node.getChildText("user"));
                conf.setFtpPass(node.getChildText("password"));
                conf.setFtpPath(node.getChildText("path"));
            }

            //populate config port cashin alfa
            list = rootNode.getChildren("altoGatewayRemittance");
            for (Object objectXMPP : list) {
                Element node = (Element) objectXMPP;
                conf.setaltoTransfer(node.getChildText("alto"));
                conf.setkunci(node.getChildText("kunci"));
                conf.setusername(node.getChildText("username"));
                conf.setaccountId(node.getChildText("accountid"));
            }
            list = rootNode.getChildren("ottoPay");
            for (Object objectXMPP : list) {
                Element node = (Element) objectXMPP;
                conf.setOttopayInitiator(node.getChildText("initiator"));
                conf.setOttopayKey(node.getChildText("key"));
                conf.setOttopayInqurl(node.getChildText("inqurl"));
                conf.setOttopayPayurl(node.getChildText("payurl"));
                conf.setOttopayAdditionalData(node.getChildText("additionalData"));
            }
            list = rootNode.getChildren("baseApi");
            for (Object objectXMPP : list) {
                Element node = (Element) objectXMPP;
                conf.setBaseApiUsername(node.getChildText("username"));
                conf.setBaseApiKey(node.getChildText("key"));
                conf.setBaseApiIdaccount(node.getChildText("idaccount"));
                conf.setBaseApiUrl(node.getChildText("url"));
                conf.setBaseApiReverseUrl(node.getChildText("reverseUrl"));
            }
            list = rootNode.getChildren("validation");
            for (Object objectXMPP : list) {
                Element node = (Element) objectXMPP;
                conf.setSendKey(node.getChildText("sendkey"));
            }
        } catch (JDOMException | IOException e) {
        }
        return conf;
    }
}
