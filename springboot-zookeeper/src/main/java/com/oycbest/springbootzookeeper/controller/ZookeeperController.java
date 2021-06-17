package com.oycbest.springbootzookeeper.controller;

import com.oycbest.springbootzookeeper.util.WatcherApi;
import com.oycbest.springbootzookeeper.util.ZkApi;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description:
 * @Author oyc
 * @Date 2020/10/29 9:25 下午
 */
@RestController
@RequestMapping("zk")

public class ZookeeperController {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperController.class);
    @Autowired
    private ZkApi zkApi;

    @GetMapping
    public Object zk() throws KeeperException, InterruptedException {
        //zkApi.exists("/t1",null);
        //zkApi.getData("/orchestration/orchestration_ds/config/schema/sharding_db", new WatcherApi());

        Stat isExists  =zkApi.exists("/node1",false);


     //   zkApi.createNode("/node1","abc");
       // zkApi.createNode("/node1","123");
        List<String> list  = (List<String>)zkApi.getChildren("/node1");

        logger.info(String.valueOf(list.size()));

        return zkApi.getData("/node1", new WatcherApi());
    }
}
