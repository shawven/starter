package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.extension.NodeConfig;
import com.github.shawven.calf.extension.ClientDataSource;
import com.github.shawven.calf.extension.NodeConfigDataSource;
import com.github.shawven.calf.oplog.server.DataPublisher;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.bson.BsonTimestamp;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
@Component
public class OpLogClientFactory {

    private static final Logger log = LoggerFactory.getLogger(OpLogClientFactory.class);

    protected final RedissonClient redissonClient;

    private final DataPublisher dataPublisher;

    private final NodeConfigDataSource nodeConfigDataSource;
    private final ClientDataSource clientDataSource;


    public volatile AtomicLong eventCount = new AtomicLong(0);

    /**
     * 事件类型的key，值可为i(插入)、u(更新)、d(删除)
     */
    public static final String EVENTTYPE_KEY = "op";
    /**
     * 数据库、集合名称
     */
    public static final String DATABASE_KEY = "ns";
    /**
     * 消费时间戳
     */
    public static final String TIMESTAMP_KEY = "ts";
    /**
     * 内容
     */
    public static final String CONTEXT_KEY = "o";
    /**
     * 更新事件更新的条件
     */
    public static final String UPDATE_WHERE_KEY = "o2";
    /**
     * 更新事件，更新的内容
     */
    public static final String UPDATE_CONTEXT_KEY = "$set";


    private long lastEventCount = 0;

    public OpLogClientFactory(RedissonClient redissonClient,
                              @Qualifier("opLogDataPublisher") DataPublisher dataPublisher,
                              NodeConfigDataSource nodeConfigDataSource,
                              ClientDataSource clientDataSource) {
        this.redissonClient = redissonClient;
        this.dataPublisher = dataPublisher;
        this.nodeConfigDataSource = nodeConfigDataSource;
        this.clientDataSource = clientDataSource;
    }

    public OplogClient initClient(NodeConfig nodeConfig) {

        String namespace = nodeConfig.getNamespace();

        MongoClient mongoClient = this.getMongoClient(nodeConfig);
        OpLogEventContext context = new OpLogEventContext(mongoClient, nodeConfig, dataPublisher);

        OpLogEventHandlerFactory opLogEventHandlerFactory = new OpLogEventHandlerFactory(context);

        OplogClient client = new OplogClient(mongoClient, opLogEventHandlerFactory);
        // 配置当前位置
        configOpLogStatus(client, nodeConfig);
        // 启动Client列表数据监听
        registerMetaDataWatcher(nodeConfig, opLogEventHandlerFactory);
        return client;
    }

    /**
     * 配置当前binlog位置
     *
     * @param oplogClient
     * @param nodeConfig
     */
    private void configOpLogStatus(OplogClient oplogClient, NodeConfig nodeConfig) {
        Map<String, String> binLogStatus = clientDataSource.getBinaryLogStatus(nodeConfig);
        if (binLogStatus != null) {
            int seconds = Integer.parseInt(binLogStatus.get("binlogFilename"));
            int inc = Integer.parseInt(binLogStatus.get("binlogPosition"));
            oplogClient.setTs(new BsonTimestamp(seconds, inc));
        }
    }

    private MongoClient getMongoClient(NodeConfig nodeConfig) {
        return new MongoClient(new MongoClientURI(nodeConfig.getDataSourceUrl()));
    }

    private void registerMetaDataWatcher(NodeConfig nodeConfig, OpLogEventHandlerFactory opLogEventHandlerFactory) {
    }



    public boolean closeClient(OplogClient oplogClient, String namespace) {
        try {
            // remove active namespace
            oplogClient.close();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        NodeConfig nodeConfig = nodeConfigDataSource.getByNamespace(namespace);
        try {
            while (nodeConfig.isActive()) {
                TimeUnit.SECONDS.sleep(1);
                nodeConfig = nodeConfigDataSource.getByNamespace(namespace);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            return false;
        }

        return true;
    }


    public long getEventCount() {
        return eventCount.get();
    }

    public long eventCountSinceLastTime() {

        long total = eventCount.get();
        long res = total - lastEventCount;

        lastEventCount = total;

        return res;
    }
}
