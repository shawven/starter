package com.github.shawven.calf.oplog.server.core;


import com.github.shawven.calf.oplog.base.DatabaseEvent;
import com.github.shawven.calf.oplog.base.EventBaseDTO;
import com.github.shawven.calf.oplog.server.datasource.ClientInfo;
import com.github.shawven.calf.oplog.server.mode.UpdateRow;
import com.github.shawven.calf.oplog.server.mode.UpdateRowsDTO;
import com.github.shawven.calf.oplog.server.publisher.DataPublisherManager;
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
public class OpLogUpdateEventHandler extends AbstractOpLogEventHandler {
    private static final Logger log = LoggerFactory.getLogger(OpLogUpdateEventHandler.class);

    public OpLogUpdateEventHandler(String namespace, DataPublisherManager dataPublisherManager, Map<String, Set<ClientInfo>> clientInfoMap) {
        super(namespace, dataPublisherManager, clientInfoMap);
    }

    @Override
    protected EventBaseDTO formatData(Document event) {
        UpdateRowsDTO updateRowsDTO = new UpdateRowsDTO();
        updateRowsDTO.setEventType(DatabaseEvent.UPDATE_ROWS);
        //添加表信息
        updateRowsDTO.setDatabase(super.getDataBase(event));
        updateRowsDTO.setTable(super.getTable(event));
        updateRowsDTO.setNamespace(getNamespace());
        //添加列映射

        List<UpdateRow> urs = new ArrayList<>();
        Document updateWhere = (Document)event.get(OpLogClientFactory.UPDATE_WHERE_KEY);
        Document context = (Document) event.get(OpLogClientFactory.CONTEXT_KEY);
        context = (Document) context.get(OpLogClientFactory.UPDATE_CONTEXT_KEY);
        urs.add(new UpdateRow(updateWhere,context));
        updateRowsDTO.setRows(urs);
        return updateRowsDTO;
    }
}
