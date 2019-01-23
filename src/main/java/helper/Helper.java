/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;


import apigatewayottopay.connection.DatabaseUtilitiesMysql;
import apigatewayottopay.connection.DatabaseUtilitiesPgsql;
import apigatewayottopay.controller.ControllerEncDec;
import apigatewayottopay.controller.ControllerLog;
import apigatewayottopay.entity.EntityConfig;
import com.google.common.base.Throwables;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

//import bright.express.controller.ControllerLog;

/**
 *
 * @author DPPH
 */
public class Helper {

//    ControllerLog ctrlLog;
    JSONParser parser = new JSONParser();
    JSONObject jsonObject;
    private final Cache cacheConfig;
    private final EntityConfig entityConfig;
    private final ControllerEncDec encDec;
    private Object controllerLog;
    private ControllerLog controllerLogg;

    public Helper() {
        CacheManager cacheManager = CacheManager.getInstance();
        this.cacheConfig = cacheManager.getCache("TrueMoneyApp");
        Element config = this.cacheConfig.get("config");
        this.entityConfig = (EntityConfig) config.getObjectValue();
        this.encDec = new ControllerEncDec(this.entityConfig.getEncIv(), this.entityConfig.getEncKey());
    }

    public synchronized String createID() {
        String Id = null;
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            messageDigest.update(Long.toString(System.nanoTime()).getBytes(Charset.forName("UTF8")));
            final byte[] resultByte = messageDigest.digest();
            Id = new String(Hex.encodeHex(resultByte));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
//            this.ctrlLog.logErrorWriter(ex.toString());
        }
        return Id;
    }

    public BufferedImage resizeImage(BufferedImage originalImage, int type, int IMG_WIDTH, int IMG_HEIGHT) {
        BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
        g.dispose();

        return resizedImage;
    }

    public BufferedImage resizeImageWithHint(BufferedImage originalImage, int type, int IMG_WIDTH, int IMG_HEIGHT) {

        BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        return resizedImage;
    }

    public String encodeToString(BufferedImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);

            bos.close();
        } catch (IOException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
//            this.ctrlLog.logErrorWriter(e.toString());
        }
        return imageString;
    }

    public String encodeImage(byte[] imageByteArray) {
        return Base64.encodeBase64String(imageByteArray);
    }

    public ArrayList<HashMap<String, Object>> executeQuery(EntityConfig conf, String query, JSONObject jObj) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        ArrayList<HashMap<String, Object>> row = new ArrayList<>();
        try {
            DatabaseUtilitiesMysql databaseUtilitiesMysql = new DatabaseUtilitiesMysql();
            try {
                conn = databaseUtilitiesMysql.getConnection(conf);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
            }
            st = conn.prepareStatement(query);

            Object json = this.parser.parse(jObj.toString());
            this.jsonObject = (JSONObject) json;
            for (int i = 1; i <= this.jsonObject.size(); i++) {
                JSONObject jsonobj = (JSONObject) this.jsonObject.get(String.valueOf(i));
                String getType = (String) jsonobj.get("type");
                String getVal = (String) jsonobj.get("val");
                if (getType.equals("string")) {
                    st.setString(i, getVal);
                } else if (getType.equals("timestamp")) {
                    st.setTimestamp(i, Timestamp.valueOf(getVal));
                } else if (getType.equals("int")) {
                    st.setInt(i, Integer.parseInt(getVal));
                }
            }
            rs = st.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int colCount = metaData.getColumnCount();
            row = new ArrayList<>();
            //convert resultset to arraylist
            while (rs.next()) {
                HashMap<String, Object> columns = new HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    columns.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                row.add(columns);
            }
        } catch (SQLException | ParseException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
//            this.ctrlLog.logErrorWriter(ex.toString());
        } finally {
            try {
                rs.close();
            } catch (Exception e) {
                /* ignored */ }
            try {
                st.close();
            } catch (Exception e) {
                /* ignored */ }
            try {
                conn.close();
            } catch (Exception e) {
                /* ignored */ }
        }
        return row;
    }

    public String executeUpdate(EntityConfig conf, String queryUpd, JSONObject jObjUpd) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            DatabaseUtilitiesMysql databaseUtilitiesMysql = new DatabaseUtilitiesMysql();
            try {
                conn = databaseUtilitiesMysql.getConnection(conf);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
            }
            st = conn.prepareStatement(queryUpd);

            Object json = this.parser.parse(jObjUpd.toString());
            this.jsonObject = (JSONObject) json;
            for (int i = 1; i <= this.jsonObject.size(); i++) {
                JSONObject jsonobj = (JSONObject) this.jsonObject.get(String.valueOf(i));
                String getType = (String) jsonobj.get("type");
                String getVal = (String) jsonobj.get("val");
                if (getType.equals("string")) {
                    st.setString(i, getVal);
                } else if (getType.equals("timestamp")) {
                    st.setTimestamp(i, Timestamp.valueOf(getVal));
                } else if (getType.equals("int")) {
                    st.setInt(i, Integer.parseInt(getVal));
                }
            }
            st.executeUpdate();
        } catch (SQLException | ParseException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
//            this.ctrlLog.logErrorWriter(ex.toString());
        } finally {
            try {
                rs.close();
            } catch (Exception e) {
                /* ignored */ }
            try {
                st.close();
            } catch (Exception e) {
                /* ignored */ }
            try {
                conn.close();
            } catch (Exception e) {
                /* ignored */ }
        }
        return null;
    }

    public JSONObject extractMultipart(Representation entity, EntityConfig entityConfig) {
        JSONObject json = new JSONObject();
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(1000240);
            RestletFileUpload upload = new RestletFileUpload(factory);
            FileItemIterator fileIterator = upload.getItemIterator(entity);
            String extFile = null;
            String nameFile = null;
            StringBuilder jsonMessageEncripted = null;
            while (fileIterator.hasNext()) {
                FileItemStream fi = fileIterator.next();
                if (fi.getFieldName().equals("action")) {
                    jsonMessageEncripted = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(fi.openStream()));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        jsonMessageEncripted.append(line);
                    }

                    byte[] decodedBytes = Base64.decodeBase64(jsonMessageEncripted.toString());
                    //----------------------parsing the json message-------------------------------
                    String queryJson = this.encDec.getDecrypt(decodedBytes);
                    JSONObject jsonReq = (JSONObject) parser.parse(queryJson);
                    json.put("action", jsonReq);
                } else if (fi.getFieldName().equals("fileUpload1")) {
                    extFile = FilenameUtils.getExtension(fi.getName());
                    String fileBefore = this.createID();
                    nameFile = fileBefore + "." + extFile;

                    InputStream attachmentStream = fi.openStream();
                    byte[] attachmentBytes;

                    //cara 1
//                    try {
//                        attachmentBytes = ByteStreams.toByteArray(attachmentStream);
//                    } finally {
//                        attachmentStream.close();
//                    }
                    //cara 2
                    ByteArrayOutputStream out = null;
                    try {
                        out = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024]; // you can configure the buffer size
                        int length;
                        while ((length = attachmentStream.read(buffer)) != -1) {
                            out.write(buffer, 0, length); //copy streams
                        }
                        attachmentBytes = out.toByteArray();
                    } finally {
                        attachmentStream.close();
                        out.close();
                    }

                    FileOutputStream fos = new FileOutputStream(entityConfig.getWorkDir() + "tmp/" + nameFile);
                    try {
                        fos.write(attachmentBytes);
                    } finally {
                        fos.close();
                    }
                    json.put("fileUploadTmp1", nameFile);
                } else if (fi.getFieldName().equals("fileUpload2")) {
                    extFile = FilenameUtils.getExtension(fi.getName());
                    String fileBefore = this.createID();
                    nameFile = fileBefore + "." + extFile;

                    InputStream attachmentStream = fi.openStream();
                    byte[] attachmentBytes;

                    ByteArrayOutputStream out = null;
                    try {
                        out = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024]; // you can configure the buffer size
                        int length;
                        while ((length = attachmentStream.read(buffer)) != -1) {
                            out.write(buffer, 0, length); //copy streams
                        }
                        attachmentBytes = out.toByteArray();
                    } finally {
                        attachmentStream.close();
                        out.close();
                    }

                    FileOutputStream fos = new FileOutputStream(entityConfig.getWorkDir() + "tmp/" + nameFile);
                    try {
                        fos.write(attachmentBytes);
                    } finally {
                        fos.close();
                    }
                    json.put("fileUploadTmp2", nameFile);
                } else if (fi.getFieldName().equals("fileUpload3")) {
                    extFile = FilenameUtils.getExtension(fi.getName());
                    String fileBefore = this.createID();
                    nameFile = fileBefore + "." + extFile;

                    InputStream attachmentStream = fi.openStream();
                    byte[] attachmentBytes;

                    ByteArrayOutputStream out = null;
                    try {
                        out = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024]; // you can configure the buffer size
                        int length;
                        while ((length = attachmentStream.read(buffer)) != -1) {
                            out.write(buffer, 0, length); //copy streams
                        }
                        attachmentBytes = out.toByteArray();
                    } finally {
                        attachmentStream.close();
                        out.close();
                    }

                    FileOutputStream fos = new FileOutputStream(entityConfig.getWorkDir() + "tmp/" + nameFile);
                    try {
                        fos.write(attachmentBytes);
                    } finally {
                        fos.close();
                    }
                    json.put("fileUploadTmp3", nameFile);
                } else if (fi.getFieldName().equals("fileUpload4")) {
                    extFile = FilenameUtils.getExtension(fi.getName());
                    String fileBefore = this.createID();
                    nameFile = fileBefore + "." + extFile;

                    InputStream attachmentStream = fi.openStream();
                    byte[] attachmentBytes;

                    ByteArrayOutputStream out = null;
                    try {
                        out = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024]; // you can configure the buffer size
                        int length;
                        while ((length = attachmentStream.read(buffer)) != -1) {
                            out.write(buffer, 0, length); //copy streams
                        }
                        attachmentBytes = out.toByteArray();
                    } finally {
                        attachmentStream.close();
                        out.close();
                    }

                    FileOutputStream fos = new FileOutputStream(entityConfig.getWorkDir() + "tmp/" + nameFile);
                    try {
                        fos.write(attachmentBytes);
                    } finally {
                        fos.close();
                    }
                    json.put("fileUploadTmp4", nameFile);
                } else if (fi.getFieldName().equals("fileUpload5")) {
                    extFile = FilenameUtils.getExtension(fi.getName());
                    String fileBefore = this.createID();
                    nameFile = fileBefore + "." + extFile;

                    InputStream attachmentStream = fi.openStream();
                    byte[] attachmentBytes;

                    ByteArrayOutputStream out = null;
                    try {
                        out = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024]; // you can configure the buffer size
                        int length;
                        while ((length = attachmentStream.read(buffer)) != -1) {
                            out.write(buffer, 0, length); //copy streams
                        }
                        attachmentBytes = out.toByteArray();
                    } finally {
                        attachmentStream.close();
                        out.close();
                    }

                    FileOutputStream fos = new FileOutputStream(entityConfig.getWorkDir() + "tmp/" + nameFile);
                    try {
                        fos.write(attachmentBytes);
                    } finally {
                        fos.close();
                    }
                    json.put("fileUploadTmp5", nameFile);
                }
            }
        } catch (FileUploadException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json;
    }

    public String getParamMsParameter(String paramName) {
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        String url = "";
        try {
            //get msparam
            connPgsql = databaseUtilitiesPgsql.getConnection(entityConfig);
            String query = "SELECT * FROM ms_parameter "
                    + "WHERE parameter_name = ?";
            stPgsql = connPgsql.prepareStatement(query);
            stPgsql.setString(1, paramName);
            rsPgsql = stPgsql.executeQuery();
            while (rsPgsql.next()) {
                url = rsPgsql.getString("parameter_value");
            }
        } catch (Exception ex) {
            String s = Throwables.getStackTraceAsString(ex);
            controllerLogg.logErrorWriter(s);
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
                controllerLogg.logErrorWriter(s);
            }
        }
        return url;
    }

    public String requestToGwKomunitas(JSONObject payloadRequest) {
        String paramValue = this.getParamMsParameter("komunitas_gw");
        String[] param = paramValue.split(":");
        InetSocketAddress socketAddress = new InetSocketAddress(param[0], Integer.parseInt(param[1]));
        final Socket socket = new Socket();
        final int tTimeout = 1000 * 60;
        final byte cEndMessageByte = -0x01;

        ControllerLog ctrlLog = new ControllerLog();
        ctrlLog.logStreamWriter("Upline -> Req : " + socketAddress.getAddress() + ":" + socketAddress.getPort()
                + " " + payloadRequest.toString());

        String tResponse = "";
        try {
            socket.setSoTimeout(tTimeout);
            socket.connect(socketAddress, tTimeout);

            ByteArrayOutputStream tRequestByteStream = new ByteArrayOutputStream();
            tRequestByteStream.write(payloadRequest.toString().getBytes());
            tRequestByteStream.write(cEndMessageByte);
            socket.getOutputStream().write(tRequestByteStream.toByteArray());

            byte tMessageByte = cEndMessageByte;
            StringBuffer sb = new StringBuffer();
            while ((tMessageByte = (byte) socket.getInputStream().read()) != cEndMessageByte) {
                sb.append((char) tMessageByte);
            }
            tResponse = sb.toString();
            ctrlLog.logStreamWriter("Upline -> Resp : " + tResponse);
        } catch (IOException iOException) {
        }
        return tResponse;
    }

    public String encrypt(String text, String Kunci) throws Exception {
        // SecretKeySpec spec = getKeySpec();
        Key key = generateKey(Kunci);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        BASE64Encoder enc = new BASE64Encoder();
        return enc.encode(cipher.doFinal(text.getBytes())).toString();
    }

    public String decrypt(String text, String Kunci) throws Exception {
        // SecretKeySpec spec = getKeySpec();
        String result = null;
        try {
            Key key = generateKey(Kunci);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            BASE64Decoder dec = new BASE64Decoder();
            result = new String(cipher.doFinal(dec.decodeBuffer(text)));
        } catch (Exception e) {
            controllerLogg.logErrorWriter("Error while decrypting: " + e.toString());
        } finally {
            // optional, use this block if necessary
        }

        return result;
    }

    private Key generateKey(String kunciEnkripsi) throws Exception {
        Key key = new SecretKeySpec(konversiKeByte(kunciEnkripsi), "AES");
        return key;
    }

    private byte[] konversiKeByte(String Kunci) {
        byte[] array_byte = new byte[32];
        int i = 0;
        while (i < Kunci.length()) {
            array_byte[i] = (byte) Kunci.charAt(i);
            i++;
        }
        if (i < 32) {
            while (i < 32) {
                array_byte[i] = (byte) i;
                i++;
            }
        }
        return array_byte;
    }

    public String parsingDateNow() {
        String parseDate1 = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        parseDate1 = sdf.format(new Date());

        return parseDate1;
    }
    
    public Timestamp getDate() throws java.text.ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = format.format(new Date()).toString();

        Date date1 = format.parse(date);
        Timestamp timestamp = new Timestamp(date1.getTime());
        return timestamp;
    }
    public String getUnixTimestamp(){
        String result = "";
        try {
            Date currentDate = new Date();
            System.out.println(currentDate);
            long times = currentDate.getTime() / 1000;
            result = times+"";
            Instant.now().toEpochMilli();
        }catch (Exception e){
            controllerLogg.logErrorWriter("Error while getUnixTimestamp: " + e.getMessage());
        }
        return Instant.now().toEpochMilli()+"";
    }



}
