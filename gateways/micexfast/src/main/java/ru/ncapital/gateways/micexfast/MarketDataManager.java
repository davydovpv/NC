package ru.ncapital.gateways.micexfast;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.openfast.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ncapital.gateways.micexfast.connection.messageprocessors.HeartbeatProcessor;
import ru.ncapital.gateways.micexfast.connection.messageprocessors.IncrementalProcessor;
import ru.ncapital.gateways.micexfast.connection.messageprocessors.SnapshotProcessor;
import ru.ncapital.gateways.micexfast.connection.messageprocessors.sequencevalidators.IMessageSequenceValidator;
import ru.ncapital.gateways.micexfast.connection.messageprocessors.sequencevalidators.MessageSequenceValidatorFactory;
import ru.ncapital.gateways.micexfast.domain.*;
import ru.ncapital.gateways.micexfast.messagehandlers.IMessageHandler;
import ru.ncapital.gateways.micexfast.messagehandlers.MessageHandlerFactory;
import ru.ncapital.gateways.micexfast.performance.IGatewayPerformanceLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by egore on 12/7/15.
 */

@Singleton
public class MarketDataManager {
    private Map<String, Subscription> subscriptions = new ConcurrentHashMap<String, Subscription>();

    private Map<String, BBO> bbos = new ConcurrentHashMap<String, BBO>();

    private Logger logger = LoggerFactory.getLogger("MarketDataManager");

    private OrderDepthEngine orderDepthEngine = new OrderDepthEngine();

    private IMarketDataHandler marketDataHandler;

    private SnapshotProcessor snapshotProcessorForOrderList;

    private SnapshotProcessor snapshotProcessorForStatistics;

    private IncrementalProcessor incrementalProcessorForOrderList;

    private IncrementalProcessor incrementalProcessorForStatistics;

    private IncrementalProcessor incrementalProcessorForPublicTrades;

    @Inject
    private MessageSequenceValidatorFactory messageSequenceValidatorFactory;

    @Inject
    private MessageHandlerFactory messageHandlerFactory;

    @Inject
    private HeartbeatProcessor heartbeatProcessor;

    private IGatewayPerformanceLogger performanceLogger;

    public MarketDataManager configure(IGatewayConfiguration configuration) {
        this.marketDataHandler = configuration.getMarketDataHandler();
        this.performanceLogger = configuration.getPerformanceLogger();

        IMessageHandler messageHandlerForOrderList = messageHandlerFactory.createOrderListMessageHandler(configuration.getAllowedTradingSessionIds(configuration.getMarketType()), configuration.getAllowedSymbols(configuration.getMarketType()));
        IMessageHandler messageHandlerForStatistics = messageHandlerFactory.createStatisticsMessageHandler(configuration.getAllowedTradingSessionIds(configuration.getMarketType()), configuration.getAllowedSymbols(configuration.getMarketType()));
        IMessageHandler messageHandlerForPublicTrades = messageHandlerFactory.createPublicTradesMessageHandler(configuration.getAllowedTradingSessionIds(configuration.getMarketType()), configuration.getAllowedSymbols(configuration.getMarketType()));

        IMessageSequenceValidator sequenceValidatorForOrderList = messageSequenceValidatorFactory.createMessageSequenceValidatorForOrderList();
        IMessageSequenceValidator sequenceValidatorForStatistics = messageSequenceValidatorFactory.createMessageSequenceValidatorForStatistics();
        IMessageSequenceValidator sequenceValidatorForPublicTrades = messageSequenceValidatorFactory.createMessageSequenceValidatorForPublicTrades();

        snapshotProcessorForOrderList = new SnapshotProcessor(messageHandlerForOrderList, sequenceValidatorForOrderList);
        snapshotProcessorForStatistics = new SnapshotProcessor(messageHandlerForStatistics, sequenceValidatorForStatistics);

        incrementalProcessorForOrderList = new IncrementalProcessor(messageHandlerForOrderList, sequenceValidatorForOrderList);
        incrementalProcessorForStatistics = new IncrementalProcessor(messageHandlerForStatistics, sequenceValidatorForStatistics);
        incrementalProcessorForPublicTrades = new IncrementalProcessor(messageHandlerForPublicTrades, sequenceValidatorForPublicTrades);

        return this;
    }

    public boolean subscribe(Subscription subscription) {
        long inTimeInTicks = Utils.currentTimeInTicks();
        if (logger.isTraceEnabled())
            logger.trace("onSubscribe " + subscription.getSubscriptionKey());

        if (!subscriptions.containsKey(subscription.getSubscriptionKey())) {
            if (logger.isDebugEnabled())
                logger.debug("Adding subscription for " + subscription.getSubscriptionKey());

            subscriptions.put(subscription.getSubscriptionKey(), subscription);
        }

        if (!bbos.containsKey(subscription.getSubscriptionKey()))
            bbos.put(subscription.getSubscriptionKey(), new BBO(subscription.getSubscriptionKey()));

        BBO bbo = bbos.get(subscription.getSubscriptionKey());
        synchronized (bbo) {
            List<DepthLevel> depthLevelsToSend = new ArrayList<DepthLevel>();
            depthLevelsToSend.add(new DepthLevel(subscription.getSubscriptionKey(), MdUpdateAction.SNAPSHOT));
            orderDepthEngine.getDepthLevels(subscription.getSubscriptionKey(), depthLevelsToSend);

            marketDataHandler.onBBO(bbo, inTimeInTicks);
            marketDataHandler.onDepthLevels(depthLevelsToSend.toArray(new DepthLevel[0]), inTimeInTicks);
            marketDataHandler.onStatistics(bbo, inTimeInTicks);
            marketDataHandler.onTradingStatus(bbo, inTimeInTicks);
        }
        return true;
    }

    public void onBBO(BBO newBBO, long inTimeInTicks) {
        if (logger.isTraceEnabled())
            logger.trace("onBBO " + newBBO.getSymbol());

        if (!bbos.containsKey(newBBO.getSymbol()))
            bbos.put(newBBO.getSymbol(), new BBO(newBBO.getSymbol()));

        BBO currentBBO = bbos.get(newBBO.getSymbol());
        synchronized (currentBBO) {
            boolean[] changed = orderDepthEngine.updateBBO(currentBBO, newBBO);

            if (subscriptions.containsKey(newBBO.getSymbol())) {
                if (changed[0])
                    marketDataHandler.onBBO(currentBBO, inTimeInTicks);
                if (changed[1])
                    marketDataHandler.onStatistics(currentBBO, inTimeInTicks);
                if (changed[2])
                    marketDataHandler.onTradingStatus(currentBBO, inTimeInTicks);
            }
        }
    }

    public void onDepthLevels(DepthLevel[] depthLevels, long inTimeInTicks) {
        if (logger.isTraceEnabled())
            logger.trace("onDepthLevel " + depthLevels[0].getSymbol());

        if (!bbos.containsKey(depthLevels[0].getSymbol()))
            bbos.put(depthLevels[0].getSymbol(), new BBO(depthLevels[0].getSymbol()));

        BBO bbo = bbos.get(depthLevels[0].getSymbol());
        synchronized (bbo) {
            List<DepthLevel> depthLevelsToSend = new ArrayList<DepthLevel>();
            orderDepthEngine.onDepthLevels(depthLevels, depthLevelsToSend);

            if (subscriptions.containsKey(depthLevels[0].getSymbol()))
                marketDataHandler.onDepthLevels(depthLevelsToSend.toArray(new DepthLevel[0]), inTimeInTicks);
        }

        if (depthLevels[0].getMdUpdateAction() != MdUpdateAction.SNAPSHOT && performanceLogger != null) {
            for (DepthLevel depthLevel : depthLevels) {
                if (depthLevel.getMdEntryTime() > 0 && inTimeInTicks > 0)
                    performanceLogger.notify(depthLevel.getMdEntryTime(), inTimeInTicks, "external");
            }
        }
    }

    public void onPublicTrade(PublicTrade publicTrade, long inTimeInTicks) {
        if (logger.isTraceEnabled())
            logger.trace("onPublicTrade " + publicTrade.getSymbol());

        if (!bbos.containsKey(publicTrade.getSymbol()))
            bbos.put(publicTrade.getSymbol(), new BBO(publicTrade.getSymbol()));

        BBO bbo = bbos.get(publicTrade.getSymbol());
        synchronized (bbo) {
            orderDepthEngine.onPublicTrade(publicTrade);

            if (subscriptions.containsKey(publicTrade.getSymbol()))
                marketDataHandler.onPublicTrade(publicTrade, inTimeInTicks);
        }
    }

    public MessageHandler getSnapshotProcessorForOrderList() {
        return snapshotProcessorForOrderList;
    }

    public MessageHandler getSnapshotProcessorForStatistics() {
        return snapshotProcessorForStatistics;
    }

    public MessageHandler getIncrementalProcessorForOrderList() {
        return incrementalProcessorForOrderList;
    }

    public MessageHandler getIncrementalProcessorForStatistics() {
        return incrementalProcessorForStatistics;
    }

    public MessageHandler getIncrementalProcessorForPublicTrades() {
        return incrementalProcessorForPublicTrades;
    }

    public MessageHandler getHeartbeatProcessor() {
        return heartbeatProcessor;
    }

    public ThreadLocal<Long> getIncrementalProcessorForOrderListInTimestamp() {
        return incrementalProcessorForOrderList.getInTimestampHolder();
    }

    public ThreadLocal<Long> getIncrementalProcessorForStatisticsInTimestamp() {
        return incrementalProcessorForStatistics.getInTimestampHolder();
    }

    public ThreadLocal<Long> getIncrementalProcessorForPublicTradesInTimestamp() {
        return incrementalProcessorForPublicTrades.getInTimestampHolder();
    }

    public ThreadLocal<Long> getSnapshotProcessorForOrderListInTimestamp() {
        return snapshotProcessorForOrderList.getInTimestampHolder();
    }

    public ThreadLocal<Long> getSnapshotProcessorForStatisticsInTimestamp() {
        return snapshotProcessorForStatistics.getInTimestampHolder();
    }

    public void setIncrementalProcessorForOrderListIsPrimary(boolean isPrimary) {
        this.incrementalProcessorForOrderList.setIsPrimary(isPrimary);
    }

    public void setIncrementalProcessorForStatisticsIsPrimary(boolean isPrimary) {
        this.incrementalProcessorForStatistics.setIsPrimary(isPrimary);
    }

    public void setIncrementalProcessorForPublicTradesIsPrimary(boolean isPrimary) {
        this.incrementalProcessorForPublicTrades.setIsPrimary(isPrimary);
    }
}