package service;

import connection.DatabaseUtilitiesPgsql;
import controller.ControllerEncDec;
import entity.EntityConfig;
import helper.Helper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.xml.parsers.ParserConfigurationException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Reza Dio Nugraha
 */
public class MasterDataCacheServiceImpl implements MasterDataCacheService {

    EntityConfig entityConfig;
    Helper hlp;
    JSONParser parser = new JSONParser();
    JSONObject jsonObject;
    private final ControllerEncDec encDec;

    public MasterDataCacheServiceImpl(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
        this.encDec = new ControllerEncDec(this.entityConfig.getEncIv(), this.entityConfig.getEncKey());
        this.hlp = new Helper();
    }

    public void testerDataMaster(ArrayList<HashMap<String, Object>> row) {
        for (HashMap<String, Object> map : row) {
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String key = pair.getKey().toString();
                String value = pair.getValue().toString();
            }
        }
    }

    @Override
    public ArrayList<HashMap<String, Object>> getMaster(String query) {
        //query to mysql ms_user_fundsource
        DatabaseUtilitiesPgsql databaseUtilitiesPgsql = new DatabaseUtilitiesPgsql();
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        ArrayList<HashMap<String, Object>> row = new ArrayList<>();
        try {
            conn = databaseUtilitiesPgsql.getConnection(this.entityConfig);
            st = conn.prepareStatement(query);
            rs = st.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int colCount = metaData.getColumnCount();
            row = new ArrayList<>();
            //convert resultset to arraylist
            while (rs.next()) {
                HashMap<String, Object> columns = new HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    if (rs.getObject(i) == null) {
                        columns.put(metaData.getColumnLabel(i), "");
                    } else{
                        columns.put(metaData.getColumnLabel(i), rs.getObject(i));
                    }
                }
                row.add(columns);
            }
            //testerDataMaster(row);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(MasterDataCacheServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MasterDataCacheServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(MasterDataCacheServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return row;
    }

}
