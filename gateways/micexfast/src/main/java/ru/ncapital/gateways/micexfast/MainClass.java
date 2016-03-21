package ru.ncapital.gateways.micexfast;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ncapital.gateways.micexfast.connection.MarketType;
import ru.ncapital.gateways.micexfast.domain.*;
import ru.ncapital.gateways.micexfast.performance.IGatewayPerformanceLogger;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/**
 * Created by egore on 12/7/15.
 */
public class MainClass {

    private Instrument[] instruments;

    private CountDownLatch waiter = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        MainClass mc = new MainClass();

        mc.run(args);
    }

    MainClass() {

    }

    public void run(final String[] args) throws InterruptedException {
        GatewayManager.addConsoleAppender("%d{HH:mm:ss.SSS} %c{1} - %m%n", Level.INFO);
        GatewayManager.addFileAppender("log/log.out", "%d{HH:mm:ss.SSS} %c{1} - %m%n", Level.DEBUG);

        final IGatewayManager gwManager = GatewayManager.create(new NullGatewayConfiguration() {
            @Override
            public IMarketDataHandler getMarketDataHandler() {
                return new IMarketDataHandler() {
                    private Logger logger = LoggerFactory.getLogger("MarketDataHandler");

                    private long lastExchangeTime = 0;

                    @Override
                    public void onBBO(BBO bbo, long inTime) {
                    }

                    @Override
                    public void onDepthLevels(DepthLevel[] depthLevels, long inTimeInTicks) {
                    }

                    @Override
                    public void onPublicTrade(PublicTrade publicTrade, long inTime) {
                    }

                    @Override
                    public void onStatistics(BBO bbo, long inTime) {
                    }

                    @Override
                    public void onTradingStatus(BBO bbo, long inTime) {
                    }

                    @Override
                    public void onInstruments(Instrument[] _instruments) {
                        instruments = _instruments;
                        waiter.countDown();
                    }
                };
            }

            @Override
            public String getFastTemplatesFile() {
                return args[0];
            }

            @Override
            public String getNetworkInterface() {
                return args[1];
            }

            @Override
            public String getConnectionsFile() {
                return args[2];
            }

            @Override
            public MarketType getMarketType() { return MarketType.FOND; }

            @Override
            public TradingSessionId[] getAllowedTradingSessionIds(MarketType marketType) {
                // return new TradingSessionId[] {TradingSessionId.CETS};
                return new TradingSessionId[] {TradingSessionId.TQBR, TradingSessionId.TQBD, TradingSessionId.TQDE,
                                                TradingSessionId.TQIF, TradingSessionId.TQQI,
                                                TradingSessionId.TQTF, TradingSessionId.TQTD,
                                                TradingSessionId.TQOB, TradingSessionId.TQOD,TradingSessionId.TQDB,
                                                TradingSessionId.TQTC,
                            };
            }

            @Override
            public ProductType[] getAllowedProductTypes(MarketType marketType) {
                // return new ProductType[] {ProductType.CURRENCY};
                return new ProductType[] {ProductType.EQUITY, ProductType.INDEX
                };
            }
        });
        gwManager.start();

        waiter.await();
        Logger logger = LoggerFactory.getLogger("MainClass");
        logger.info("TOTAL " + instruments.length + " INSTRUMENTS");

        for (Instrument instrument : instruments) {
            logger.info("Instrument " + instrument.toString());
            //gwManager.subscribeForMarketData(instrument.getSymbol());
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        gwManager.stop();

    }
}
