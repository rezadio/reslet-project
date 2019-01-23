/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package connection;

import entity.EntityConfig;

import javax.xml.parsers.ParserConfigurationException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author acang
 */
public class DatabaseUtilitiesPgsql {

    private Connection conn;

    public Connection getConnection(EntityConfig conf) throws ParserConfigurationException {
        if (conn == null) {
            try {
                //--------------------------PgSql------------------------------
                DriverManager.registerDriver(new org.postgresql.Driver());
                String ip = conf.getDbPgsqlIp();
                String port = conf.getDbPgsqlPort();
                String db = conf.getDbPgsql();
                String usr = conf.getDbPgsqlUser();
                String pwd = conf.getDbPgsqlPass();
                //conn=DriverManager.getConnection("jdbc:postgresql://10.1.15.120:5432/sijungjung","postgres","");
                conn = DriverManager.getConnection("jdbc:postgresql://" + ip + ":" + port + "/" + db, usr, pwd);
            } catch (SQLException ex) {
                //JOptionPane.showMessageDialog(null,"\nGagal Koneksi Database\n" +ex,"Kesalahan",JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(DatabaseUtilitiesPgsql.class.getName()).log(Level.SEVERE, null, ex);
//                System.exit(1);
            }
        }
        return conn;
    }
}
