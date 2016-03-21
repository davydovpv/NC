package ru.ncapital.gateways.micexfast.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by egore on 16.02.2016.
 */
public enum TradingSessionId {
    // CURR
    CETS("CETS"),
    CNDG("CNDG"),

    // FOND
    TQBR("TQBR"), // SHARES
    TQBD("TQBD"), // SHARES
    TQDE("TQDE"), // SHARES
    TQBD("TQIF"), // MUTUAL FUNDS
    TQDE("TQQI"), // MUTUAL FUNDS
    TQTF("TQTF"), // ETF
    TQTD("TQTD"), // ETF
    TQOB("TQOB"), // BONDS
    TQOD("TQOD"), // BONDS
    TQDB("TQDB"), // BONDS
    TQTC("TQTC"); // ETC

    private String description;

    private static Map<String, TradingSessionId> typeMap = new HashMap<String, TradingSessionId>();

    TradingSessionId(String description) {
        this.description = description;
    }

    static {
        {
            for (TradingSessionId type : TradingSessionId.values()) {
                typeMap.put(type.getDescription(), type);
            }
        }
    }

    public String getDescription() {
        return this.description;
    }

    public static TradingSessionId convert(String description) { return typeMap.get(description); }
}
