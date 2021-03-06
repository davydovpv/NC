package ru.ncapital.gateways.moexfast.domain.impl;

import ru.ncapital.gateways.moexfast.domain.intf.IInstrument;

/**
 * Created by Egor on 30-Sep-16.
 */
public abstract class Instrument<T> implements IInstrument {
    private String symbol;

    private String securityId;

    private T exchangeSecurityId;

    private String currency;

    private String description;

    private int lotSize;

    private double tickSize;

    private String tradingStatus;

    private double multiplier;

    private String underlying;

    private String maturityDate;

    public Instrument(String symbol, String securityId, T exchangeSecurityId) {
        this.symbol = symbol;
        this.securityId = securityId;
        this.exchangeSecurityId = exchangeSecurityId;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public String getSecurityId() {
        return securityId;
    }

    public T getExchangeSecurityId() {
        return exchangeSecurityId;
    }

    @Override
    public String getCurrency() {
        return currency;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getLotSize() {
        return lotSize;
    }

    @Override
    public double getTickSize() {
        return tickSize;
    }

    @Override
    public String getTradingStatus() {
        return tradingStatus;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public String getUnderlying() {
        return underlying;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getMaturityDate() { return maturityDate; }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLotSize(int lotSize) {
        this.lotSize = lotSize;
    }

    public void setTickSize(double tickSize) {
        this.tickSize = tickSize;
    }

    public void setTradingStatus(String tradingStatus) {
        this.tradingStatus = tradingStatus;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public void setUnderlying(String underlying) {
        this.underlying = underlying;
    }

    public void setMaturityDate(String maturityDate) {
        this.maturityDate = maturityDate;
    }

    @Override
    public String toString() {
        return getName() + "{" +
                "symbol='" + symbol + '\'' +
                ", securityId='" + securityId + '\'' +
                ", exchangeSecurityId='" + exchangeSecurityId + '\'' +
                ", currency='" + currency + '\'' +
                ", description='" + description + '\'' +
                ", lotSize=" + lotSize +
                ", tickSize=" + tickSize +
                ", tradingStatus='" + tradingStatus + '\'' +
                ", multiplier=" + multiplier +
                ", underlying='" + underlying + '\'' +
                '}';
    }

    public String getId() {
        return getName() + "{" +
                "symbol='" + symbol + '\'' +
                ", securityId='" + securityId + '\'' +
                ", exchangeSecurityId='" + exchangeSecurityId + '\'' +
                '}';
    }
}
