package com.github.shawven.calf.oplog.server.core;

import com.github.shawven.calf.oplog.base.DatabaseEvent;
import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.server.datasource.ClientInfo;
import com.github.shawven.calf.oplog.server.mode.WriteRowsDTO;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: kl @kailing.pub
 * @date: 2020/1/7
 */
public class OpLogWriteEventHandler extends OpLogEventHandler {
    private static final Logger log = LoggerFactory.getLogger(OpLogWriteEventHandler.class);

    public OpLogWriteEventHandler(OpLogEventContext context) {
        super(context);
    }

    @Override
    protected EventBaseDTO formatData(Document event) {
        WriteRowsDTO writeRowsDTO = new WriteRowsDTO();
        writeRowsDTO.setEventType(DatabaseEvent.WRITE_ROWS);
        //添加表信息
        writeRowsDTO.setDatabase(super.getDataBase(event));
        writeRowsDTO.setTable(super.getTable(event));
        writeRowsDTO.setNamespace(context.getNodeConfig().getNamespace());
        //添加列映射
        Document context = (Document) event.get(OpLogClientFactory.CONTEXT_KEY);
        List<Map<String, Object>> urs = new ArrayList<>();
        urs.add(context);
        writeRowsDTO.setRowMaps(urs);
        return writeRowsDTO;
    }

    @Override
    protected Set<ClientInfo> filter(Document event) {
        String database = super.getDataBase(event);
        String table = super.getTable(event);
        String tableKey = database.concat("/").concat(table);
        return clientInfoMap.get(tableKey);
    }
}