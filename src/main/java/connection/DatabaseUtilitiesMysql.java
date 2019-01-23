/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connection;

import apigatewayottopay.entity.EntityConfig;

import javax.xml.parsers.ParserConfigurationException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author DPPH
 */
public class DatabaseUtilitiesMysql {

    private Connection conn;
//    private ControllerLog ctrlLog;

    public Connection getConnection(EntityConfig conf) throws ParserConfigurationException {
        if (conn == null) {
            try {
                //--------------------------MySql------------------------------
                DriverManager.registerDriver(new com.mysql.jdbc.Driver());
                String ip = conf.getDbMysqlIp();
                String port = conf.getDbMysqlPort();
                String db = conf.getDbMysql();
                String usr = conf.getDbMysqlUser();
                String pwd = conf.getDbMysqlPass();
                conn = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + db, usr, pwd);
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseUtilitiesMysql.class.getName()).log(Level.SEVERE, null, ex);
//                this.ctrlLog.logErrorWriter(ex.toString());
//                JOptionPane.showMessageDialog(null, "\nGagal Koneksi Database\n" + ex, "Kesalahan", JOptionPane.ERROR_MESSAGE);
//                System.exit(1);
            }
        }
        return conn;
    }
}
