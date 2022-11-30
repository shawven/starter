package com.github.shawven.calf.oplog.server.datasource;

import com.github.shawven.calf.oplog.server.datasource.leaderselector.LeaderSelector;
import com.github.shawven.calf.oplog.server.datasource.leaderselector.OplogLeaderSelectorListener;

/**
 * @author xw
 * @date 2022/11/30
 */
public interface LeaderSelectorFactory {

    LeaderSelector getLeaderSelector(String namespace, long timeToLive,
                                     String identification, String identificationPath,
                                     OplogLeaderSelectorListener listener);
}
