import org.junit.Assert;
import org.junit.Test;
import ru.ncapital.gateways.fortsfast.FortsConfigurationManager;
import ru.ncapital.gateways.fortsfast.FortsNullGatewayConfiguration;
import ru.ncapital.gateways.micexfast.MicexConfigurationManager;
import ru.ncapital.gateways.micexfast.MicexNullGatewayConfiguration;
import ru.ncapital.gateways.moexfast.ConfigurationManager;
import ru.ncapital.gateways.moexfast.connection.ConnectionId;

/**
 * Created by egore on 10.02.2016.
 */
public class ConfigurationManagerTest {
    @Test
    public void testMicexConfigurationManager() {
        ConfigurationManager cm = new MicexConfigurationManager().configure(new MicexNullGatewayConfiguration() {
            @Override
            public String getFastTemplatesFile() {
                return "src/main/resources/micex/fast_templates.xml";
            }

            @Override
            public String getNetworkInterface() {
                return "eth0";
            }

            @Override
            public String getConnectionsFile() {
                return "src/main/resources/micex/config_test_internet.xml";
            }
        });

        for (ConnectionId id : cm.getAllConnectionIds())
            assert cm.getConnection(id) != null;

        assert cm.getFastTemplatesFile() == "src/main/resources/micex/fast_templates.xml";

        Assert.assertEquals("eth0", cm.getPrimaryNetworkInterface(false));
        Assert.assertEquals("eth0", cm.getSecondaryNetworkInterface(false));
    }

    @Test
    public void testFortsConfigurationManager() {
        ConfigurationManager cm = new FortsConfigurationManager().configure(new FortsNullGatewayConfiguration() {
            @Override
            public String getFastTemplatesFile() {
                return "src/main/resources/forts/fast_templates.xml";
            }

            @Override
            public String getNetworkInterface() {
                return "eth0";
            }

            @Override
            public String getConnectionsFile() {
                return "src/main/resources/forts/config_test_internet.xml";
            }
        });

        for (ConnectionId id : cm.getAllConnectionIds())
            assert cm.getConnection(id) != null;

        assert cm.getFastTemplatesFile() == "src/main/resources/forts/fast_templates.xml";

        Assert.assertEquals("eth0", cm.getPrimaryNetworkInterface(false));
        Assert.assertEquals("eth0", cm.getSecondaryNetworkInterface(false));
    }

    @Test
    public void testMicexConfigurationManager2Interfaces() {
        ConfigurationManager cm = new MicexConfigurationManager().configure(new MicexNullGatewayConfiguration() {
            @Override
            public String getFastTemplatesFile() {
                return "src/main/resources/micex/fast_templates.xml";
            }

            @Override
            public String getNetworkInterface() {
                return "eth0;eth1";
            }

            @Override
            public String getConnectionsFile() {
                return "src/main/resources/micex/config_test_internet.xml";
            }
        });

        Assert.assertEquals("eth0", cm.getPrimaryNetworkInterface(false));
        Assert.assertEquals("eth1", cm.getSecondaryNetworkInterface(false));
    }

    @Test
    public void testFortsConfigurationManager2Interfaces() {
        ConfigurationManager cm = new FortsConfigurationManager().configure(new FortsNullGatewayConfiguration() {
            @Override
            public String getFastTemplatesFile() {
                return "src/main/resources/forts/fast_templates.xml";
            }

            @Override
            public String getNetworkInterface() {
                return "eth0;eth1";
            }

            @Override
            public String getConnectionsFile() {
                return "src/main/resources/forts/config_test_internet.xml";
            }
        });

        Assert.assertEquals("eth0", cm.getPrimaryNetworkInterface(false));
        Assert.assertEquals("eth1", cm.getSecondaryNetworkInterface(false));
    }

    @Test
    public void testFortsConfigurationManager2Interfaces2() {
        ConfigurationManager cm = new FortsConfigurationManager().configure(new FortsNullGatewayConfiguration() {
            @Override
            public String getFastTemplatesFile() {
                return "src/main/resources/forts/fast_templates.xml";
            }

            @Override
            public String getNetworkInterface() {
                return "eth0;eth1|eth2;eth3";
            }

            @Override
            public String getConnectionsFile() {
                return "src/main/resources/forts/config_test_internet.xml";
            }
        });

        Assert.assertEquals("eth0", cm.getPrimaryNetworkInterface(false));
        Assert.assertEquals("eth1", cm.getSecondaryNetworkInterface(false));
        Assert.assertEquals("eth2", cm.getPrimaryNetworkInterface(true));
        Assert.assertEquals("eth3", cm.getSecondaryNetworkInterface(true));
    }
}
