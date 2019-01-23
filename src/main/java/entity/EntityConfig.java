
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

/**
 *
 * @author anto
 */
public class EntityConfig {

    private String server;

    private String workDir;
    private String httpPort;
    private String pathUri;

    private String dbPgsql;
    private String dbPgsqlIp;
    private String dbPgsqlPort;
    private String dbPgsqlUser;
    private String dbPgsqlPass;

    private String dbPgsqlLog;
    private String dbPgsqlLogIp;
    private String dbPgsqlLogPort;
    private String dbPgsqlLogUser;
    private String dbPgsqlLogPass;


    private String dbPgsqlAlfa;
    private String dbPgsqlIpAlfa;
    private String dbPgsqlPortAlfa;
    private String dbPgsqlUserAlfa;
    private String dbPgsqlPassAlfa;

    private String dbMysql;
    private String dbMysqlIp;
    private String dbMysqlPort;
    private String dbMysqlUser;
    private String dbMysqlPass;

    private String maxThreads;
    private String minThreads;
    private String lowThreads;
    private String maxQueued;
    private String maxTotalConnections;
    private String maxIoIdleTimeMs;
    private String sessionTtl;
    private String timeRemoveBeforeSessionDelete;

    private String encKey;
    private String encIv;
    private String encRequest;
    private String encResponse;
    private String encKeyDb;

    private String scaleImgSize;
    private String setConnectTimeout;
    private String setReadTimeout;
    private String logStream;
    private String masterFolder;
    private String ipService;

    private String ftpIp;
    private String ftpPort;
    private String ftpUser;
    private String ftpPass;
    private String ftpPath;

    private String altoTransfer;
    private String kunci;
    private String username;
    private String accountid;

    private String ottopayInitiator;
    private String ottopayKey;
    private String ottopayInqurl;
    private String ottopayPayurl;
    private String ottopayAdditionalData;

    private String baseApiUsername;
    private String baseApiKey;
    private String baseApiIdaccount;
    private String baseApiUrl;
    private String baseApiReverseUrl;

    private String sendKey;

    public String getSendKey() {
        return sendKey;
    }

    public String getBaseApiReverseUrl() {
        return baseApiReverseUrl;
    }

    public void setBaseApiReverseUrl(String baseApiReverseUrl) {
        this.baseApiReverseUrl = baseApiReverseUrl;
    }

    public void setSendKey(String sendKey) {
        this.sendKey = sendKey;
    }

    public String getDbPgsqlLog() {
        return dbPgsqlLog;
    }

    public void setDbPgsqlLog(String dbPgsqlLog) {
        this.dbPgsqlLog = dbPgsqlLog;
    }

    public String getDbPgsqlLogIp() {
        return dbPgsqlLogIp;
    }

    public void setDbPgsqlLogIp(String dbPgsqlLogIp) {
        this.dbPgsqlLogIp = dbPgsqlLogIp;
    }

    public String getDbPgsqlLogPort() {
        return dbPgsqlLogPort;
    }

    public void setDbPgsqlLogPort(String dbPgsqlLogPort) {
        this.dbPgsqlLogPort = dbPgsqlLogPort;
    }

    public String getDbPgsqlLogUser() {
        return dbPgsqlLogUser;
    }

    public void setDbPgsqlLogUser(String dbPgsqlLogUser) {
        this.dbPgsqlLogUser = dbPgsqlLogUser;
    }

    public String getDbPgsqlLogPass() {
        return dbPgsqlLogPass;
    }

    public void setDbPgsqlLogPass(String dbPgsqlLogPass) {
        this.dbPgsqlLogPass = dbPgsqlLogPass;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getOttopayPayurl() {
        return ottopayPayurl;
    }

    public void setOttopayPayurl(String ottopayPayurl) {
        this.ottopayPayurl = ottopayPayurl;
    }

    public String getOttopayAdditionalData() {
        return ottopayAdditionalData;
    }

    public void setOttopayAdditionalData(String ottopayAdditionalData) {
        this.ottopayAdditionalData = ottopayAdditionalData;
    }

    public String getOttopayInitiator() {
        return ottopayInitiator;
    }

    public void setOttopayInitiator(String ottopayInitiator) {
        this.ottopayInitiator = ottopayInitiator;
    }

    public String getOttopayKey() {
        return ottopayKey;
    }

    public void setOttopayKey(String ottopayKey) {
        this.ottopayKey = ottopayKey;
    }

    public String getOttopayInqurl() {
        return ottopayInqurl;
    }

    public void setOttopayInqurl(String ottopayInqurl) {
        this.ottopayInqurl = ottopayInqurl;
    }

    public String getBaseApiUrl() {
        return baseApiUrl;
    }

    public void setBaseApiUrl(String baseApiUrl) {
        this.baseApiUrl = baseApiUrl;
    }



    public String getBaseApiUsername() {
        return baseApiUsername;
    }

    public void setBaseApiUsername(String baseApiUsername) {
        this.baseApiUsername = baseApiUsername;
    }

    public String getBaseApiKey() {
        return baseApiKey;
    }

    public void setBaseApiKey(String baseApiKey) {
        this.baseApiKey = baseApiKey;
    }

    public String getBaseApiIdaccount() {
        return baseApiIdaccount;
    }

    public void setBaseApiIdaccount(String baseApiIdaccount) {
        this.baseApiIdaccount = baseApiIdaccount;
    }

    public void setLogStream(String logStream) {
        this.logStream = logStream;
    }

    public String getAltoTransfer() {
        return altoTransfer;
    }

    public void setAltoTransfer(String altoTransfer) {
        this.altoTransfer = altoTransfer;
    }

    public String getKunci() {
        return kunci;
    }

    public void setKunci(String kunci) {
        this.kunci = kunci;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccountid() {
        return accountid;
    }

    public void setAccountid(String accountid) {
        this.accountid = accountid;
    }



    public EntityConfig() {
    }

    public String getWorkDir() {
        return this.workDir;
    }

    public String getHttpPort() {
        return this.httpPort;
    }

    //---------------------db postgres---------------
    public String getDbPgsql() {
        return this.dbPgsql;
    }

    public String getDbPgsqlIp() {
        return this.dbPgsqlIp;
    }

    public String getDbPgsqlPort() {
        return this.dbPgsqlPort;
    }

    public String getDbPgsqlUser() {
        return this.dbPgsqlUser;
    }

    public String getDbPgsqlPass() {
        return this.dbPgsqlPass;
    }

    //---------------------end db postgres---------------
    //---------------------db mysql---------------
    public String getDbMysql() {
        return this.dbMysql;
    }

    public String getDbMysqlIp() {
        return this.dbMysqlIp;
    }

    public String getDbMysqlPort() {
        return this.dbMysqlPort;
    }

    public String getDbMysqlUser() {
        return this.dbMysqlUser;
    }

    public String getDbMysqlPass() {
        return this.dbMysqlPass;
    }
    //---------------------end db mysql---------------

    //---------------------get db postgres Afla---------------
    public String getDbPgsqlAlfa() {
        return this.dbPgsqlAlfa;
    }

    public String getDbPgsqlIpAlfa() {
        return this.dbPgsqlIpAlfa;
    }

    public String getDbPgsqlPortAlfa() {
        return this.dbPgsqlPortAlfa;
    }

    public String getDbPgsqlUserAlfa() {
        return this.dbPgsqlUserAlfa;
    }

    public String getDbPgsqlPassAlfa() {
        return this.dbPgsqlPassAlfa;
    }
    //---------------------end db postgres---------------

    public String getMaxThreads() {
        return this.maxThreads;
    }

    public String getMinThreads() {
        return this.minThreads;
    }

    public String getLowThreads() {
        return this.lowThreads;
    }

    public String getMaxQueued() {
        return this.maxQueued;
    }

    public String getMaxTotalConnections() {
        return this.maxTotalConnections;
    }

    public String getMaxIoIdleTimeMs() {
        return this.maxIoIdleTimeMs;
    }

    public String getPathUri() {
        return this.pathUri;
    }

    public String getSessionTtl() {
        return this.sessionTtl;
    }

    public String getTimeRemoveBeforeSessionDelete() {
        return this.timeRemoveBeforeSessionDelete;
    }

    public String getEncKeyDb() {
        return this.encKeyDb;
    }

    public String getEncKey() {
        return this.encKey;
    }

    public String getEncIv() {
        return this.encIv;
    }

    public String getEncRequest() {
        return this.encRequest;
    }

    public String getEncResponse() {
        return this.encResponse;
    }

    public String getScaleImgSize() {
        return this.scaleImgSize;
    }

    public String getSetConnectTimeout() {
        return this.setConnectTimeout;
    }

    public String getSetReadTimeout() {
        return this.setReadTimeout;
    }

    public String getLogStream() {
        return this.logStream;
    }

    public String getMasterFolder() {
        return this.masterFolder;
    }

    public String getIpService() {
        return this.ipService;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    //------------------------db postgres-------------------
    public void setDbPgsql(String database) {
        this.dbPgsql = database;
    }

    public void setDbPgsqlIp(String dbIp) {
        this.dbPgsqlIp = dbIp;
    }

    public void setDbPgsqlPort(String dbPort) {
        this.dbPgsqlPort = dbPort;
    }

    public void setDbPgsqlUser(String dbUser) {
        this.dbPgsqlUser = dbUser;
    }

    public void setDbPgsqlPass(String dbPass) {
        this.dbPgsqlPass = dbPass;
    }

    //------------------------end db postgres-------------------
    //------------------------db postgres Alfamart -------------------
    public void setDbPgsqlAlfa(String database) {
        this.dbPgsqlAlfa = database;
    }

    public void setDbPgsqlIpAlfa(String dbIp) {
        this.dbPgsqlIpAlfa = dbIp;
    }

    public void setDbPgsqlPortAlfa(String dbPort) {
        this.dbPgsqlPortAlfa = dbPort;
    }

    public void setDbPgsqlUserAlfa(String dbUser) {
        this.dbPgsqlUserAlfa = dbUser;
    }

    public void setDbPgsqlPassAlfa(String dbPass) {
        this.dbPgsqlPassAlfa = dbPass;
    }

    //------------------------end db postgres-------------------
    //------------------------db mysql-------------------
    public void setDbMysql(String database) {
        this.dbMysql = database;
    }

    public void setDbMysqlIp(String dbIp) {
        this.dbMysqlIp = dbIp;
    }

    public void setDbMysqlPort(String dbPort) {
        this.dbMysqlPort = dbPort;
    }

    public void setDbMysqlUser(String dbUser) {
        this.dbMysqlUser = dbUser;
    }

    public void setDbMysqlPass(String dbPass) {
        this.dbMysqlPass = dbPass;
    }
    //------------------------end db mysql-------------------

    public void setMaxThreads(String maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void setMinThreads(String minThreads) {
        this.minThreads = minThreads;
    }

    public void setLowThreads(String lowThreads) {
        this.lowThreads = lowThreads;
    }

    public void setMaxQueued(String maxQueued) {
        this.maxQueued = maxQueued;
    }

    public void setMaxTotalConnections(String maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public void setMaxIoIdleTimeMs(String maxIoIdleTimeMs) {
        this.maxIoIdleTimeMs = maxIoIdleTimeMs;
    }

    public void setPathUri(String pathUri) {
        this.pathUri = pathUri;
    }

    public void setSessionTtl(String sessionTtl) {
        this.sessionTtl = sessionTtl;
    }

    public void setTimeRemoveBeforeSessionDelete(String timeRemoveBeforeSessionDelete) {
        this.timeRemoveBeforeSessionDelete = timeRemoveBeforeSessionDelete;
    }

    public void setEncKeyDb(String encKey) {
        this.encKeyDb = encKey;
    }

    public void setEncKey(String encKey) {
        this.encKey = encKey;
    }

    public void setEncIv(String encIv) {
        this.encIv = encIv;
    }

    public void setEncRequest(String encRequest) {
        this.encRequest = encRequest;
    }

    public void setEncResponse(String encResponse) {
        this.encResponse = encResponse;
    }

    public void setScaleImgSize(String scaleImgSize) {
        this.scaleImgSize = scaleImgSize;
    }

    public void setSetConnectTimeout(String setConnectTimeout) {
        this.setConnectTimeout = setConnectTimeout;
    }

    public void setSetReadTimeout(String setReadTimeout) {
        this.setReadTimeout = setReadTimeout;
    }

    public void setlogStream(String logStream) {
        this.logStream = logStream;
    }

    public void setMasterFolder(String masterFolder) {
        this.masterFolder = masterFolder;
    }

    public void setIpService(String ipService) {
        this.ipService = ipService;
    }

    //---------------------ftp---------------
    public void setFtpIp(String ftpIp) {
        this.ftpIp = ftpIp;
    }

    public String getFtpIp() {
        return this.ftpIp;
    }

    public void setFtpPort(String ftpPort) {
        this.ftpPort = ftpPort;
    }

    public String getFtpPort() {
        return this.ftpPort;
    }

    public void setFtpUser(String ftpUser) {
        this.ftpUser = ftpUser;
    }

    public String getFtpUser() {
        return this.ftpUser;
    }

    public void setFtpPass(String ftpPass) {
        this.ftpPass = ftpPass;
    }

    public String getFtpPass() {
        return this.ftpPass;
    }

    public void setFtpPath(String ftpPath) {
        this.ftpPath = ftpPath;
    }

    public String getFtpPath() {
        return this.ftpPath;
    }
    //---------------------end ftp---------------

    //---------------------alfamartEmoney---------------
    public void setaltoTransfer(String altoTransfer) {
        this.altoTransfer = altoTransfer;
    }

    public String getaltoTransfer() {
        return this.altoTransfer;
    }

    public void setkunci(String kunci) {
        this.kunci = kunci;
    }

    public String getkunci() {
        return this.kunci;
    }

    public void setusername(String username) {
        this.username = username;
    }

    public String getusername() {
        return this.username;
    }

    public void setaccountId(String accountId) {
        this.accountid = accountId;
    }

    public String getaccountId() {
        return this.accountid;
    }

       //---------------------end alfamartEmoney---------------
}
