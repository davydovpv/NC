package ru.ncapital.gateways.micexfast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ncapital.gateways.micexfast.domain.BBO;
import ru.ncapital.gateways.micexfast.domain.DepthLevel;
import ru.ncapital.gateways.micexfast.domain.Instrument;
import ru.ncapital.gateways.micexfast.domain.PublicTrade;

/**
 * Created by egore on 12/7/15.
 */
public class DefaultMarketDataHandler implements IMarketDataHandler {

    private Logger logger = LoggerFactory.getLogger("DefaultMarketDataHandler");

    @Override
    public void onBBO(BBO bbo, long inTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("onBBO: ");
        sb.append(bbo.getSymbol()).append(" ");
        sb.append(bbo.getBidSize()).append("@").append(bbo.getBidPx()).append(" - ");
        sb.append(bbo.getOfferSize()).append("@").append(bbo.getOfferPx());

        logger.info(sb.toString());
    }

    @Override
    public void onTradingStatus(BBO bbo, long inTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("onTradingStatus: ");
        sb.append(bbo.getSymbol()).append(" ");
        sb.append(bbo.getTradingStatus()).append(" ");

        logger.info(sb.toString());
    }

    @Override
    public void onInstruments(Instrument[] instruments) {

    }

    @Override
    public void onStatistics(BBO bbo, long inTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("onStatistics: ");
        sb.append(bbo.getSymbol());
        sb.append(" La:").append(bbo.getLastSize()).append("@").append(bbo.getLastPx());
        sb.append(" H:").append(bbo.getHighPx());
        sb.append(" L:").append(bbo.getLowPx());
        sb.append(" O:").append(bbo.getOpenPx());
        sb.append(" C:").append(bbo.getClosePx());

        logger.info(sb.toString());
    }

    @Override
    public void onDepthLevels(DepthLevel[] depthLevels, long inTime) {
        for (DepthLevel depthLevel : depthLevels) {
            StringBuilder sb = new StringBuilder();
            sb.append("onDepthLevel: ");
            switch (depthLevel.getMdUpdateAction()) {
                case INSERT:
                    sb.append("Insert ");
                    break;
                case UPDATE:
                    sb.append("Update ");
                    break;
                case DELETE:
                    sb.append("Delete ");
                    break;
                case SNAPSHOT:
                    sb.append("Snapshot ");
                    break;
            }
            sb.append(depthLevel.getSymbol()).append(" ");
            sb.append(depthLevel.isBid() ? "B" : "S").append(depthLevel.getMdEntryId()).append(" ");
            sb.append(depthLevel.getMdEntrySize()).append("@").append(depthLevel.getMdEntryPx());

            logger.info(sb.toString());
        }
    }

    @Override
    public void onPublicTrade(PublicTrade publicTrade, long inTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("onPublicTrade: ");
        sb.append(publicTrade.getSymbol()).append(" ");
        sb.append(publicTrade.isBid() ? "B" : "S");
        sb.append(publicTrade.getLastSize()).append("@").append(publicTrade.getLastPx());
    }
}
