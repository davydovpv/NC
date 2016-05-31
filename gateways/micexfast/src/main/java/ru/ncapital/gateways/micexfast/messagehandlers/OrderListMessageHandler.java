package ru.ncapital.gateways.micexfast.messagehandlers;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.openfast.GroupValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ncapital.gateways.micexfast.IGatewayConfiguration;
import ru.ncapital.gateways.micexfast.MarketDataManager;
import ru.ncapital.gateways.micexfast.Utils;
import ru.ncapital.gateways.micexfast.domain.*;
import ru.ncapital.gateways.micexfast.performance.PerformanceData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by egore on 12/7/15.
 */
public class OrderListMessageHandler extends AMessageHandler {

    private Map<String, List<DepthLevel>> depthLevelMap = new HashMap<String, List<DepthLevel>>();

    protected String lastDealNumber;

    @AssistedInject
    public OrderListMessageHandler(MarketDataManager marketDataManager,
                                   @Assisted IGatewayConfiguration configuration) {

        super(marketDataManager, configuration);
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger("OrderListMessageHandler");
    }

    @Override
    protected void onSnapshotMdEntry(String securityId, GroupValue mdEntry) {
        MdEntryType mdEntryType = MdEntryType.convert(mdEntry.getString("MDEntryType").charAt(0));

        if (mdEntryType == null)
            return;

        DepthLevel depthLevel = null;
        switch (mdEntryType) {
            case BID:
                depthLevel =
                        new DepthLevel(securityId,
                                MdUpdateAction.INSERT,
                                mdEntry.getString("MDEntryID"),
                                mdEntry.getDouble("MDEntryPx"),
                                mdEntry.getDouble("MDEntrySize"),
                                null,
                                true);

                depthLevel.getPerformanceData().setExchangeEntryTime(Utils.getEntryTimeInTicks(mdEntry));
                break;
            case OFFER:
                depthLevel =
                        new DepthLevel(securityId,
                                MdUpdateAction.INSERT,
                                mdEntry.getString("MDEntryID"),
                                mdEntry.getDouble("MDEntryPx"),
                                mdEntry.getDouble("MDEntrySize"),
                                null,
                                false);

                depthLevel.getPerformanceData().setExchangeEntryTime(Utils.getEntryTimeInTicks(mdEntry));
                break;
            case EMPTY:
                break;
            default:
                logger.warn("Unhandled snapshot mdEntry " + mdEntry.toString());
                break;
        }

        if (depthLevel != null) {
            List<DepthLevel> depthLevelList = depthLevelMap.get(securityId);
            if (depthLevelList == null) {
                depthLevelList = new ArrayList<>();
                depthLevelMap.put(securityId, depthLevelList);
            }
            depthLevelList.add(depthLevel);
        }
    }

    @Override
    public void onIncrementalMdEntry(String securityId, GroupValue mdEntry, PerformanceData perfData) {
        MdEntryType mdEntryType = MdEntryType.convert(mdEntry.getString("MDEntryType").charAt(0));
        MdUpdateAction mdUpdateAction = MdUpdateAction.convert(mdEntry.getString("MDUpdateAction").charAt(0));

        if (mdEntryType == null)
            return;

        DepthLevel depthLevel = null;
        switch (mdEntryType) {
            case BID:
                depthLevel =
                        new DepthLevel(securityId,
                                mdUpdateAction,
                                mdEntry.getString("MDEntryID"),
                                mdEntry.getDouble("MDEntryPx"),
                                mdUpdateAction == MdUpdateAction.DELETE ? 0.0 : mdEntry.getDouble("MDEntrySize"),
                                mdEntry.getString("DealNumber"),
                                true);

                depthLevel.getPerformanceData().updateFrom(perfData).setExchangeEntryTime(Utils.getEntryTimeInTicks(mdEntry));
                break;
            case OFFER:
                depthLevel =
                        new DepthLevel(securityId,
                                mdUpdateAction,
                                mdEntry.getString("MDEntryID"),
                                mdEntry.getDouble("MDEntryPx"),
                                mdUpdateAction == MdUpdateAction.DELETE ? 0.0 : mdEntry.getDouble("MDEntrySize"),
                                mdEntry.getString("DealNumber"),
                                false);

                depthLevel.getPerformanceData().updateFrom(perfData).setExchangeEntryTime(Utils.getEntryTimeInTicks(mdEntry));
                break;
            case EMPTY:
                depthLevel = new DepthLevel(securityId, MdUpdateAction.SNAPSHOT);
                break;
            default:
                logger.warn("Unhandled incremental mdEntry " + mdEntry.toString());
                break;
        }

        if (depthLevel != null) {
            List<DepthLevel> depthLevelList = depthLevelMap.get(securityId);
            if (depthLevelList == null) {
                depthLevelList = new ArrayList<>();
                depthLevelMap.put(securityId, depthLevelList);
            }
            depthLevelList.add(depthLevel);
        }
    }

    @Override
    protected void onBeforeSnapshot(String securityId) {
        List<DepthLevel> depthLevelList = new ArrayList<>();
        depthLevelMap.put(securityId, depthLevelList);
        depthLevelList.add(new DepthLevel(securityId, MdUpdateAction.SNAPSHOT));
    }

    @Override
    protected void onAfterSnapshot(String securityId) {
        for (List<DepthLevel> depthLevelList : depthLevelMap.values()) {
            marketDataManager.onDepthLevels(depthLevelList.toArray(new DepthLevel[0]));
        }
        depthLevelMap.clear();
    }

    @Override
    public void onBeforeIncremental(GroupValue mdEntry) {
        String dealNumber = mdEntry.getString("DealNumber");
        if (dealNumber != null && !dealNumber.equals(lastDealNumber)) {
            lastDealNumber = dealNumber;
        } else {
            mdEntry.setString("DealNumber", null);
        }
    }

    @Override
    public void flushIncrementals() {
        for (List<DepthLevel> depthLevelList : depthLevelMap.values()) {
            marketDataManager.onDepthLevels(depthLevelList.toArray(new DepthLevel[0]));
        }
        depthLevelMap.clear();
    }

    @Override
    public MessageHandlerType getType() {
        return MessageHandlerType.ORDER_LIST;
    }
}