package com.github.shawven.calf.oplog.server.datasource.etcd;

import com.alibaba.fastjson.JSON;
import com.github.shawven.calf.oplog.server.core.ServiceSwitcher;
import com.github.shawven.calf.oplog.server.mode.Command;
import com.github.shawven.calf.oplog.server.mode.CommandType;
import com.github.shawven.calf.oplog.base.Consts;
import com.github.shawven.calf.oplog.server.datasource.NodeConfigDataSource;
import com.github.shawven.calf.oplog.server.datasource.DataSourceException;
import com.github.shawven.calf.oplog.server.datasource.NodeConfig;
import com.github.shawven.calf.oplog.server.KeyPrefixUtil;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author wanglaomo
 * @since 2019/6/4
 **/
public class EtcdNodeConfigDataSource implements NodeConfigDataSource {

    private static final Logger logger = LoggerFactory.getLogger(EtcdNodeConfigDataSource.class);

    private final Client etcdClient;

    private final KeyPrefixUtil keyPrefixUtil;

    public EtcdNodeConfigDataSource(Client etcdClient, KeyPrefixUtil keyPrefixUtil) {
        this.etcdClient = etcdClient;
        this.keyPrefixUtil = keyPrefixUtil;
    }

    @Override
    public List<NodeConfig> init(String dataSourceType) {
        List<NodeConfig> nodeConfigs = getAll();

        if(nodeConfigs.isEmpty()) {
            logger.warn("There is no available binlog config!");
            return nodeConfigs;
        }
        Set<String> namespaces = new HashSet<>();
        List<NodeConfig> filterDataSource = new ArrayList<>();
        nodeConfigs.forEach(config -> {
            if(StringUtils.isEmpty(config.getNamespace())) {
                throw new IllegalArgumentException("You need to config namespace!");
            }
            if(!namespaces.add(config.getNamespace())) {
                throw new IllegalArgumentException("Duplicated namespace!");
            }
            if(config.getDataSourceType().equals(dataSourceType)){
                filterDataSource.add(config);
            }
        });
       return filterDataSource;
    }

    @Override
    public List<NodeConfig> getAll() {
        try {
            return asyncGetAll().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new DataSourceException("Failed to connect to etcd server.", e);
        }
    }

    @Override
    public CompletableFuture<List<NodeConfig>> asyncGetAll() {
        return etcdClient.getKVClient()
                .get(ByteSequence.from(keyPrefixUtil.withPrefix(Consts.DEFAULT_BINLOG_CONFIG_KEY), StandardCharsets.UTF_8))
                .thenApply(configRes -> {
                    if(configRes == null || configRes.getCount() == 0) {
                        return new ArrayList<>();
                    }
                    // not range query
                    String configListStr = configRes.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
                    return JSON.parseArray(configListStr, NodeConfig.class);
                });
    }

    @Override
    public boolean create(NodeConfig newConfig) {
        List<NodeConfig> nodeConfigs = getAll();

        boolean exist = nodeConfigs.stream().anyMatch(c -> c.getNamespace().equals(newConfig.getNamespace()));

        if(exist) {
            return false;
        }

        nodeConfigs.add(newConfig);
        persistConfig(nodeConfigs);

        return true;
    }

    @Override
    public void update(NodeConfig newConfig) {
        if(Thread.currentThread().isInterrupted()) {
            return;
        }
        asyncGetAll().thenAccept(configList -> {
            AtomicBoolean modifyFlag = new AtomicBoolean(false);
            configList = configList.stream().map((c) -> {
                if(newConfig.getNamespace().equalsIgnoreCase(c.getNamespace())) {

                    // 版本号小于集群中版本号则忽略
                    if(newConfig.getVersion() < c.getVersion()) {
                        logger.warn("Ignore BinLogConfig[{}] Modify case local version [{}] < current version [{}]", newConfig.getNamespace(), newConfig.getVersion(), c.getVersion());
                        return c;
                    } else {
                        modifyFlag.set(true);

                        return newConfig;
                    }
                }
                return c;
            }).collect(Collectors.toList());

            if(modifyFlag.get()) {
                persistConfig(configList);
            }
        });
    }

    @Override
    public NodeConfig remove(String namespace) {
        if(StringUtils.isEmpty(namespace)) {
            return null;
        }

        NodeConfig removedConfig = null;

        List<NodeConfig> NodeConfigs = getAll();
        Iterator<NodeConfig> iterator = NodeConfigs.iterator();
        while (iterator.hasNext()){
            NodeConfig config = iterator.next();
            if(config.getNamespace().equals(namespace)) {
                removedConfig = config;
                iterator.remove();
                break;
            }
        }

        persistConfig(NodeConfigs);

        return removedConfig;
    }

    @Override
    public NodeConfig getByNamespace(String namespace) {
        List<NodeConfig> nodeConfigs = getAll();
        Optional<NodeConfig> optional = nodeConfigs.stream().filter(config -> namespace.equals(config.getNamespace())).findAny();

        if (optional.isPresent()) {
            return optional.get();
        }

        return null;
    }
    @Override
    public List<String> getNamespaceList() {
        return getAll().stream()
                .map(NodeConfig::getNamespace)
                .collect(Collectors.toList());
    }

    @Override
    public void registerWatcher(ServiceSwitcher serviceSwitcher) {
        Watch watchClient = etcdClient.getWatchClient();
        watchClient.watch(
                ByteSequence.from(keyPrefixUtil.withPrefix(Consts.DEFAULT_BINLOG_CONFIG_COMMAND_KEY), StandardCharsets.UTF_8),
                WatchOption.newBuilder().withPrevKV(true).withNoDelete(true).build(),
                new Watch.Listener() {

                    @Override
                    public void onNext(WatchResponse response) {

                        List<WatchEvent> eventList = response.getEvents();
                        for(WatchEvent event: eventList) {

                            if (WatchEvent.EventType.PUT.equals(event.getEventType())) {
                                Command command = JSON.parseObject(event.getKeyValue().getValue().toString(StandardCharsets.UTF_8), Command.class);

                                // 根据不同的命令类型（START/STOP）执行不同的逻辑
                                if(CommandType.START_DATASOURCE.equals(command.getType())) {
                                    serviceSwitcher.start(command);
                                } else if (CommandType.STOP_DATASOURCE.equals(command.getType())) {
                                    serviceSwitcher.stop(command);
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logger.error("Watch binlog config command error.", throwable);
                        new Thread(() -> registerWatcher(serviceSwitcher)).start();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("Watch binlog config command completed.");
                        new Thread(() -> registerWatcher(serviceSwitcher)).start();
                    }
                }
        );
    }


    private void persistConfig(List<NodeConfig> NodeConfigs) {
        etcdClient.getKVClient().put(
                ByteSequence.from(keyPrefixUtil.withPrefix(Consts.DEFAULT_BINLOG_CONFIG_KEY), StandardCharsets.UTF_8),
                ByteSequence.from(JSON.toJSONString(NodeConfigs), StandardCharsets.UTF_8)
        );
    }
}
