/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import entity.EntityConfig;
import service.ConfigService;
import service.ConfigServiceImpl;

/**
 *
 * @author DPPH
 */
public class ControllerConfig {
    ConfigService service;
    EntityConfig conf;
    String workDir;
    public ControllerConfig(EntityConfig conf, String workDir){
        this.service =  (ConfigService) new ConfigServiceImpl();
        this.conf = conf;
        this.workDir = workDir;
    }
    public EntityConfig getData() throws ClassNotFoundException{
        this.conf = this.service.getData(this.conf,this.workDir);
        return this.conf;
    }
}
