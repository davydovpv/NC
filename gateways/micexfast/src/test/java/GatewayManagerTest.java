import com.google.inject.Guice;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import ru.ncapital.gateways.micexfast.*;
import ru.ncapital.gateways.micexfast.domain.ProductType;
import ru.ncapital.gateways.micexfast.domain.TradingSessionId;
import ru.ncapital.gateways.moexfast.DefaultMarketDataHandler;
import ru.ncapital.gateways.moexfast.IMarketDataHandler;

/**
 * Created by egore on 2/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class GatewayManagerTest {

    @Before
    public void setup() {
        GatewayManager.create(new NullGatewayConfiguration() {
            @Override
            public IMarketDataHandler getMarketDataHandler() {
                return new DefaultMarketDataHandler();
            }

            @Override
            public String getFastTemplatesFile() {
                return "src/main/resources/fast_templates.xml";
            }

            @Override
            public String getNetworkInterface() {
                return "lo";
            }

            @Override
            public String getConnectionsFile() {
                return "src/main/resources/config_test_internet.xml";
            }

            @Override
            public TradingSessionId[] getAllowedTradingSessionIds() {
                return new TradingSessionId[] {TradingSessionId.CETS};
            }

            @Override
            public ProductType[] getAllowedProductTypes() {
                return new ProductType[]{ProductType.CURRENCY};
            }
        });
    }

    @Test
    public void testCreate() {
        MarketDataManager md = Guice.createInjector(new GatewayModule()).getInstance(MarketDataManager.class);

        assert md.getHeartbeatProcessor() != null;
    }
}
