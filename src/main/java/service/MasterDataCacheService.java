package service;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author hasan
 */
public interface MasterDataCacheService {

    public ArrayList<HashMap<String, Object>> getMaster(String query);

}
