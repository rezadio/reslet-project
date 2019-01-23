package service;


import entity.EntityConfig;

/**
 * @author Reza Dio Nugraha
 */
public interface ConfigService {
    public EntityConfig getData(EntityConfig conf, String workDir);
}
