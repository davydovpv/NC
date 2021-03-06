package ru.ncapital.gateways.fortsfast;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ncapital.gateways.moexfast.DefaultMarketDataHandler;
import ru.ncapital.gateways.moexfast.IGatewayManager;
import ru.ncapital.gateways.moexfast.IMarketDataHandler;
import ru.ncapital.gateways.moexfast.Utils;
import ru.ncapital.gateways.moexfast.connection.MarketType;
import ru.ncapital.gateways.moexfast.domain.intf.IBBO;
import ru.ncapital.gateways.moexfast.domain.intf.IDepthLevel;
import ru.ncapital.gateways.moexfast.domain.intf.IInstrument;
import ru.ncapital.gateways.moexfast.domain.intf.IPublicTrade;

import java.util.concurrent.CountDownLatch;

/**
 * Created by egore on 12/7/15.
 */
public class MainClass {

    private IInstrument[] instruments;

    private CountDownLatch waiter = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        MainClass mc = new MainClass();

        mc.run(args);
    }

    public void run(final String[] args) throws InterruptedException {
        FortsGatewayManager.addConsoleAppender("%d{HH:mm:ss.SSS} %c{1} - %m%n", Level.INFO);
        FortsGatewayManager.addFileAppender("log/log.out", "%d{HH:mm:ss.SSS} %c{1} - %m%n", Level.DEBUG);
        Logger logger = LoggerFactory.getLogger("MainClass");
        if (args.length < 3) {
            System.err.println("Usage MainClass <fast_templates> <interface[s]> <connections_file>");
            return;
        }

        final IGatewayManager gwManager = FortsGatewayManager.create(new FortsNullGatewayConfiguration() {
            @Override
            public IMarketDataHandler getMarketDataHandler() {
                return new DefaultMarketDataHandler() {

                    @Override
                    public void onBBO(IBBO bbo) {
                    }

                    @Override
                    public void onDepthLevels(IDepthLevel[] depthLevels) {
                    }

                    @Override
                    public void onStatistics(IBBO bbo) {
                    }

                    @Override
                    public void onPublicTrade(IPublicTrade publicTrade) {
                    }

                    @Override
                    public void onInstruments(IInstrument[] _instruments) {
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
            public MarketType getMarketType() { return MarketType.FUT; }

            @Override
            public String[] getAllowedUnderlyings() {
               return new String[] {"*"};
            }

            @Override
            public boolean isAsynchChannelReader() {
                return true;
            }

            @Override
            public boolean isListenSnapshotChannelOnlyIfNeeded() {
                return true;
            }
        });

        gwManager.start();

        waiter.await();
        logger.info("TOTAL " + instruments.length + " INSTRUMENTS");

        for (IInstrument instrument : instruments) {
            logger.info(instrument.toString());
            // gwManager.subscribeForMarketData(instrument.getSecurityId());
        }

        gwManager.subscribeForMarketData("GZZ6");

        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            Utils.printStackTrace(e, logger, "InterruptedException occurred..");
        }

        gwManager.stop();
    }
}
