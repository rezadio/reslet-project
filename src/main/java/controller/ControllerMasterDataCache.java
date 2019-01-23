package controller;




import entity.EntityConfig;
import service.MasterDataCacheService;
import service.MasterDataCacheServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Anto
 */
public class ControllerMasterDataCache {

    EntityConfig entityConfig;
    MasterDataCacheService service;

    public ControllerMasterDataCache(EntityConfig conf) {
        this.entityConfig = conf;
        this.service = new MasterDataCacheServiceImpl(this.entityConfig);
    }

    public ArrayList<HashMap<String, Object>> getMsWording() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<>();
        try {
            String query = "SELECT wording_content, wording_content_name FROM \"MsWordingContent\"";
            data = this.service.getMaster(query);
        } catch (Exception ex) {
            Logger.getLogger(ControllerMasterDataCache.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
}
