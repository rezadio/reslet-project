/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;


import apigatewayottopay.connection.DatabaseUtilitiesPgsql;
import apigatewayottopay.entity.EntityConfig;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author defran1812
 */
public class SendEmailAMSMemberReg {

//    static final Logger logger = Logger.getLogger(SendEmailAMSMemberReg.class);
    public void sent(Map<String, String> map) {
        try {
            SendEmailAMDThread gcm = new SendEmailAMDThread(map);
            Thread t = new Thread(gcm);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(SendEmailAMSMemberReg.class.getName()).log(Level.SEVERE, null, e);
            Logger.getLogger(SendEmailAMSMemberReg.class.getName()).log(Level.SEVERE, null, e.getMessage());
        }
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("Tipe", "sendRegisterMemberAndroid");
        map.put("idMember", "48325685");
        map.put("idMemberAccount", "48325685");
        map.put("VarNama", "JUMINTEN");
        map.put("VarHP", "48325685");
        map.put("VarEmail", "TESTING@TESTING.COM");
        map.put("username", "USERNAME");
        map.put("password", "PASSWORD");
        map.put("VarKTP", "KTP");
        map.put("VarPaspor", "PASSPORT");
        map.put("PIN_Member", "PIN");

        new SendEmailAMSMemberReg().sent(map);
    }
}

class SendEmailAMDThread implements Runnable {

//    static final Logger logger = Logger.getLogger(SendEmailAMDThread.class);
    static SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");

    static ResourceBundle rb = ResourceBundle.getBundle("config.config");

    private Map<String, String> map;

    EntityConfig entityConfig;

    public SendEmailAMDThread(Map<String, String> map, EntityConfig entityConfig) {
        this.map = map;
        this.entityConfig = entityConfig;
    }

    SendEmailAMDThread(Map<String, String> map) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        Connection connPosgre = null;
        String emailAMS = null;
        String senderuser = null;
        String senderpassword = null;

        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection connPgsql = null;
        PreparedStatement stPgsql = null;
        ResultSet rsPgsql = null;
        try {
            String port = null;
            String host = null;
            connPgsql = databaseUtilitiesPgsql.getConnection(entityConfig);

            String sql = "SELECT\n" + "	*\n" + "FROM\n" + "	tb_mailserver\n" + "WHERE\n" + "	keyword = 'NOREPLY'\n"
                    + "AND flagactive = TRUE\n" + "ORDER BY\n" + "	\"timestamp\" DESC\n" + "LIMIT 1";

            stPgsql = connPgsql.prepareStatement(sql);
            rsPgsql = stPgsql.executeQuery();
            while (rsPgsql.next()) {
                senderuser = rsPgsql.getString("username");
                senderpassword = rsPgsql.getString("password");
                port = rsPgsql.getString("port");
                host = rsPgsql.getString("host");
            }

            final String username = senderuser;
            final String password = senderpassword;

            sql = "SELECT\n" + "	mail_address\n" + "FROM\n" + "	tb_mailgroup\n" + "WHERE\n" + "	keyword = 'AMS'\n"
                    + "AND flagactive = TRUE\n" + "AND \"upper\"(program_name) = 'MEMBER_REGISTRATION'\n" + "ORDER BY\n"
                    + "	\"timestamp\" DESC\n" + "LIMIT 1";

            stPgsql = connPgsql.prepareStatement(sql);
            rsPgsql = stPgsql.executeQuery();
            while (rsPgsql.next()) {
                emailAMS = rsPgsql.getString("mail_address");
            }

            if (emailAMS != null && !emailAMS.isEmpty()) {
                Properties props = new Properties();

                props.put("mail.smtp.port", port);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.host", host);

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

                // Create a default MimeMessage object.
                Message message = new MimeMessage(session);

                try {
                    message.setFrom(new InternetAddress(senderuser, "Info TrueMoney"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // Set To: header field of the header.
                InternetAddress[] toArray = InternetAddress.parse(emailAMS);
                message.setRecipients(Message.RecipientType.TO, toArray);

                String verificationID = null;
                if (map.get("VarKTP").toString().equalsIgnoreCase("")) {
                    verificationID = map.get("VarPaspor").toString();
                }

                if (map.get("VarPaspor").toString().equalsIgnoreCase("")) {
                    verificationID = map.get("VarKTP").toString();
                }

                String pesan = "Dear AMS Team, <br><br>"
                        + "There is a New TrueMoney Member that needs your concern to verify.<br>"
                        + "To verify the new member, kindly use \"Member Registration Monitoring\" menu, "
                        + "search by registration date, then search by Member ID (3x24 Hours).<br><br>"
                        + "New Member Detail:<br><br>" + "- ID Member: " + map.get("idMember") + "<br>"
                        + "- Registration Date: " + format.format(new Date()) + "<br>"
                        + "- Registration From: Android Application<br>" + "- Name: " + map.get("VarNama") + "<br>"
                        + "- Verification Status: " + map.get("statusVerifikasi") + " <br>"
                        + "- Identification ID: " + verificationID + "<br>"
                        + "- Handphone Number: " + map.get("VarHP")
                        //						+ "<br>" + "- Email: " + map.get("VarEmail")
                        + "<br><br>" + "Thank you for your concern.<br>";

                // Set Subject: header field
                message.setSubject("[New TM Member Registration Notification] ID Member: " + map.get("idMember"));

                // Create the message part
                BodyPart messageBodyPart = new MimeBodyPart();

                // Create a multipar message
                Multipart multipart = new MimeMultipart();

                // Set text message part
                multipart.addBodyPart(messageBodyPart);

                // Initialize velocity
                VelocityEngine ve = new VelocityEngine();
                ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
                ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
                ve.init();

                /* next, get the Template */
                Template t = ve.getTemplate("config/mail-template.vm");

                /* create a context and add data */
                VelocityContext context1 = new VelocityContext();
                context1.put("headerTemplate", "New TM Member Registration Notification");
                context1.put("bodyTemplate", pesan);

                /* now render the template into a StringWriter */
                StringWriter out = new StringWriter();
                t.merge(context1, out);

                messageBodyPart.setContent(out.toString(), "text/html");

                // Part two is attachment
                messageBodyPart = new MimeBodyPart();

                // Send the complete message parts
                message.setContent(multipart);

                // Send message
                Transport.send(message);
            }

        } catch (MessagingException e) {
            e.printStackTrace();
            Logger.getLogger(SendEmailAMSMemberReg.class.getName()).log(Level.SEVERE, null, e);
            Logger.getLogger(SendEmailAMSMemberReg.class.getName()).log(Level.SEVERE, null, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(SendEmailAMSMemberReg.class.getName()).log(Level.SEVERE, null, e);
            Logger.getLogger(SendEmailAMSMemberReg.class.getName()).log(Level.SEVERE, null, e.getMessage());
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

}
