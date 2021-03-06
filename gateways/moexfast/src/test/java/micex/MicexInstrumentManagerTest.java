package micex;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openfast.*;
import org.openfast.codec.Coder;
import ru.ncapital.gateways.micexfast.IMicexGatewayConfiguration;
import ru.ncapital.gateways.micexfast.MicexGatewayManager;
import ru.ncapital.gateways.micexfast.MicexInstrumentManager;
import ru.ncapital.gateways.micexfast.MicexMarketDataManager;
import ru.ncapital.gateways.micexfast.domain.MicexInstrument;
import ru.ncapital.gateways.micexfast.domain.ProductType;
import ru.ncapital.gateways.micexfast.domain.TradingSessionId;
import ru.ncapital.gateways.moexfast.IMarketDataHandler;
import ru.ncapital.gateways.moexfast.connection.ConnectionManager;
import ru.ncapital.gateways.moexfast.domain.impl.BBO;
import ru.ncapital.gateways.moexfast.domain.intf.IInstrument;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;

/**
 * Created by egore on 4/26/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class MicexInstrumentManagerTest {
    @Mock
    private Context context;

    @Mock
    private Coder coder;

    private MicexInstrumentManager instrumentManager;

    @Mock
    private IMicexGatewayConfiguration configuration;

    @Mock
    private MicexMarketDataManager marketDataManager;

    @Mock
    private ConnectionManager connectionManager;

    @Mock
    private IMarketDataHandler marketDataHandler;

    @Mock
    private MicexGatewayManager gatewayManager;

    @Before
    public void setup() {
        when(configuration.getAllowedProductTypes()).thenReturn(new ProductType[] {ProductType.EQUITY});
        when(configuration.getAllowedTradingSessionIds()).thenReturn(new TradingSessionId[] {TradingSessionId.TQBR});
        when(configuration.getAllowedSecurityIds()).thenReturn(new String[] {"SBER;TQBR", "ROSN;TQBR"});
        when(configuration.getMarketDataHandler()).thenReturn(marketDataHandler);
        when(marketDataManager.createBBO(anyString())).thenCallRealMethod();

        instrumentManager = new MicexInstrumentManager();
        instrumentManager.setMarketDataManager(marketDataManager);
        instrumentManager.setGatewayManager(gatewayManager);

        instrumentManager.configure(configuration);
    }

    private void initTradingSessionIdAndTradingStatus(Message messageMock, String tradingSessionId) {
        SequenceValue marketSegmentSeq = mock(SequenceValue.class);
        GroupValue marketSegmentGrp = mock(GroupValue.class);

        SequenceValue tradingSessionRulesSeq = mock(SequenceValue.class);
        GroupValue tradingSessionRulesGrp = mock(GroupValue.class);

        when(messageMock.getSequence("MarketSegmentGrp")).thenReturn(marketSegmentSeq);
        when(marketSegmentSeq.getLength()).thenReturn(1);
        when(marketSegmentSeq.get(0)).thenReturn(marketSegmentGrp);

        when(marketSegmentGrp.getSequence("TradingSessionRulesGrp")).thenReturn(tradingSessionRulesSeq);
        when(tradingSessionRulesSeq.getLength()).thenReturn(1);
        when(tradingSessionRulesSeq.get(0)).thenReturn(tradingSessionRulesGrp);

        when(tradingSessionRulesGrp.getValue("TradingSessionID")).thenReturn(mock(FieldValue.class));
        when(tradingSessionRulesGrp.getString("TradingSessionID")).thenReturn(tradingSessionId);
        when(tradingSessionRulesGrp.getValue("TradingSessionSubID")).thenReturn(mock(FieldValue.class));
        when(tradingSessionRulesGrp.getString("TradingSessionSubID")).thenReturn("N");
        when(tradingSessionRulesGrp.getValue("SecurityTradingStatus")).thenReturn(mock(FieldValue.class));
        when(tradingSessionRulesGrp.getInt("SecurityTradingStatus")).thenReturn(17);
    }

    private void initProductType(Message messageMock, ProductType productType) {
        when(messageMock.getValue("Product")).thenReturn(mock(FieldValue.class));
        when(messageMock.getInt("Product")).thenReturn(productType.getProduct());
    }

    private Message getInstrumentMessageMock(int num) {
        Message message = Mockito.mock(Message.class);

        when(message.getString("MessageType")).thenReturn("d");
        when(message.getInt("MsgSeqNum")).thenReturn(num);
        when(message.getLong("SendingTime")).thenReturn(System.currentTimeMillis());
        when(message.getInt("TotNumReports")).thenReturn(5);

        switch (num) {
            // added
            case 1:
                when(message.getString("Symbol")).thenReturn("SBER");
                initTradingSessionIdAndTradingStatus(message, TradingSessionId.TQBR.getDescription());
                initProductType(message, ProductType.EQUITY);
                break;

            case 2:
                when(message.getString("Symbol")).thenReturn("ROSN");
                initTradingSessionIdAndTradingStatus(message, TradingSessionId.TQBR.getDescription());
                initProductType(message, ProductType.EQUITY);
                break;

            //ignored
            case 3:
                when(message.getString("Symbol")).thenReturn("LUKL");
                initTradingSessionIdAndTradingStatus(message, TradingSessionId.TQBR.getDescription());
                initProductType(message, ProductType.EQUITY);
                break;

            case 4:
                when(message.getString("Symbol")).thenReturn("LUKL");
                initTradingSessionIdAndTradingStatus(message, TradingSessionId.TQIF.getDescription());
                initProductType(message, ProductType.EQUITY);
                break;

            case 5:
                when(message.getString("Symbol")).thenReturn("VTBB");
                initTradingSessionIdAndTradingStatus(message, TradingSessionId.TQBR.getDescription());
                initProductType(message, ProductType.CORPORATE);
                break;
        }

        return message;
    }

    private Message getInstrumentStatusMessageMock(int num) {
        Message message = Mockito.mock(Message.class);

        when(message.getString("MessageType")).thenReturn("f");
        when(message.getInt("MsgSeqNum")).thenReturn(num);
        when(message.getLong("SendingTime")).thenReturn(System.currentTimeMillis());

        switch (num) {
            // added
            case 1:
                when(message.getString("Symbol")).thenReturn("SBER");
                when(message.getValue("TradingSessionID")).thenReturn(mock(FieldValue.class));
                when(message.getString("TradingSessionID")).thenReturn(TradingSessionId.TQBR.getDescription());
                when(message.getValue("TradingSessionSubID")).thenReturn(mock(FieldValue.class));
                when(message.getString("TradingSessionSubID")).thenReturn("N");
                when(message.getValue("SecurityTradingStatus")).thenReturn(mock(FieldValue.class));
                when(message.getInt("SecurityTradingStatus")).thenReturn(17);
                break;

            case 2:
                when(message.getString("Symbol")).thenReturn("ROSN");
                when(message.getValue("TradingSessionID")).thenReturn(mock(FieldValue.class));
                when(message.getString("TradingSessionID")).thenReturn(TradingSessionId.TQBR.getDescription());
                when(message.getValue("TradingSessionSubID")).thenReturn(mock(FieldValue.class));
                when(message.getString("TradingSessionSubID")).thenReturn("N");
                when(message.getValue("SecurityTradingStatus")).thenReturn(mock(FieldValue.class));
                when(message.getInt("SecurityTradingStatus")).thenReturn(17);
                break;

            //ignored
            case 3:
                when(message.getString("Symbol")).thenReturn("LUKL");
                when(message.getValue("TradingSessionID")).thenReturn(mock(FieldValue.class));
                when(message.getString("TradingSessionID")).thenReturn(TradingSessionId.TQBR.getDescription());
                when(message.getValue("TradingSessionSubID")).thenReturn(mock(FieldValue.class));
                when(message.getString("TradingSessionSubID")).thenReturn("N");
                when(message.getValue("SecurityTradingStatus")).thenReturn(mock(FieldValue.class));
                when(message.getInt("SecurityTradingStatus")).thenReturn(17);
                break;

            case 4:
                when(message.getString("Symbol")).thenReturn("LUKL");
                when(message.getValue("TradingSessionID")).thenReturn(mock(FieldValue.class));
                when(message.getString("TradingSessionID")).thenReturn(TradingSessionId.TQIF.getDescription());
                when(message.getValue("TradingSessionSubID")).thenReturn(mock(FieldValue.class));
                when(message.getString("TradingSessionSubID")).thenReturn("N");
                when(message.getValue("SecurityTradingStatus")).thenReturn(mock(FieldValue.class));
                when(message.getInt("SecurityTradingStatus")).thenReturn(17);
                break;

            case 5:
                when(message.getString("Symbol")).thenReturn("VTBB");
                when(message.getValue("TradingSessionID")).thenReturn(mock(FieldValue.class));
                when(message.getString("TradingSessionID")).thenReturn(TradingSessionId.TQBR.getDescription());
                when(message.getValue("TradingSessionSubID")).thenReturn(mock(FieldValue.class));
                when(message.getString("TradingSessionSubID")).thenReturn("N");
                when(message.getValue("SecurityTradingStatus")).thenReturn(mock(FieldValue.class));
                when(message.getInt("SecurityTradingStatus")).thenReturn(17);
                break;
        }

        return message;
    }

    @Test
    public void isAllowedInstrument() {
        testInstrumentAddAndFinish();

        assertTrue(instrumentManager.isAllowedInstrument(MicexInstrument.getSecurityId("SBER", "TQBR")));
        assertTrue(instrumentManager.isAllowedInstrument(MicexInstrument.getSecurityId("ROSN", "TQBR")));
        assertFalse(instrumentManager.isAllowedInstrument(MicexInstrument.getSecurityId("LUKL", "TQBR")));
        assertFalse(instrumentManager.isAllowedInstrument(MicexInstrument.getSecurityId("LUKL", "TQIF")));
        assertFalse(instrumentManager.isAllowedInstrument(MicexInstrument.getSecurityId("VTBB", "TQBR")));
    }


    @Test
    public void testInstrumentStatus() {
        testInstrumentAddAndFinish();

        Mockito.reset(marketDataManager);
        when(marketDataManager.createBBO(anyString())).thenCallRealMethod();

        for (int i : new int [] {1, 1, 2, 2, 3, 3, 4, 4, 5, 5})
            instrumentManager.handleMessage(getInstrumentStatusMessageMock(i), context, coder);

        ArgumentCaptor<BBO> bboCapture = ArgumentCaptor.forClass(BBO.class);
        verify(marketDataManager, times(2)).onBBO(bboCapture.capture());
        assertEquals("SBER;TQBR", bboCapture.getAllValues().get(0).getSecurityId());
        assertEquals("N-17", bboCapture.getAllValues().get(0).getTradingStatus());
        assertEquals("ROSN;TQBR", bboCapture.getAllValues().get(1).getSecurityId());
        assertEquals("N-17", bboCapture.getAllValues().get(1).getTradingStatus());
    }

    @Test
    public void testInstrumentAddAndFinish() {
        for (int i : new int [] {1, 1, 2, 2, 3, 3, 4, 4, 5, 5})
            instrumentManager.handleMessage(getInstrumentMessageMock(i), context, coder);

        ArgumentCaptor<BBO> bboCapture = ArgumentCaptor.forClass(BBO.class);
        verify(marketDataManager, times(2)).onBBO(bboCapture.capture());
        List<BBO> values = bboCapture.getAllValues();
        Collections.sort(values, new Comparator<BBO>() {
            @Override
            public int compare(BBO o1, BBO o2) {
                return o1.getSecurityId().compareTo(o2.getSecurityId());
            }
        });
        assertEquals("ROSN;TQBR", values.get(0).getSecurityId());
        assertEquals("SBER;TQBR", values.get(1).getSecurityId());
        assertEquals("N-17", values.get(0).getTradingStatus());
        assertEquals("N-17", values.get(1).getTradingStatus());

        assertTrue(instrumentManager.getSecurityId("ROSN;TQBR").equals("ROSN;TQBR"));
        assertTrue(instrumentManager.getSecurityId("SBER;TQBR").equals("SBER;TQBR"));
        assertTrue(instrumentManager.getExchangeSecurityId("ROSN;TQBR").equals("ROSN;TQBR"));
        assertTrue(instrumentManager.getExchangeSecurityId("SBER;TQBR").equals("SBER;TQBR"));

        verify(gatewayManager, times(1)).onInstrumentDownloadFinished();

        ArgumentCaptor<IInstrument[]> instrumentCapture = ArgumentCaptor.forClass(IInstrument[].class);
        verify(marketDataHandler, times(1)).onInstruments(instrumentCapture.capture());
        assertEquals(2, instrumentCapture.getValue().length);
        Arrays.sort(instrumentCapture.getValue(), new Comparator<IInstrument>() {
            @Override
            public int compare(IInstrument o1, IInstrument o2) {
                return o1.getSecurityId().compareTo(o2.getSecurityId());
            }
        });
        assertEquals("SBER;TQBR", instrumentCapture.getValue()[1].getSecurityId());
        assertEquals("ROSN;TQBR", instrumentCapture.getValue()[0].getSecurityId());
    }

    @Test
    public void testInstrumentAddAndNotFinished() {
        for (int i : new int [] {1, 1, 2, 2, 4, 4, 5, 5})
            instrumentManager.handleMessage(getInstrumentMessageMock(i), context, coder);

        verify(connectionManager, times(0)).stopInstrument();

        for (int i : new int [] {1, 1, 2, 2, 3, 3, 4, 4, 5, 5})
            instrumentManager.handleMessage(getInstrumentMessageMock(i), context, coder);

        verify(marketDataManager, times(2)).onBBO(any(BBO.class));
        verify(gatewayManager, times(1)).onInstrumentDownloadFinished();
        ArgumentCaptor<IInstrument[]> instrumentCapture = ArgumentCaptor.forClass(IInstrument[].class);
        verify(marketDataHandler, times(1)).onInstruments(instrumentCapture.capture());
        assertEquals(2, instrumentCapture.getValue().length);
    }

    @Test
    public void testInstrumentAddAndNotFinished2() {
        for (int i : new int [] {1, 1, 3, 3, 4, 4, 5, 5})
            instrumentManager.handleMessage(getInstrumentMessageMock(i), context, coder);

        verify(gatewayManager, times(0)).onInstrumentDownloadFinished();

        for (int i : new int [] {1, 1, 2, 2, 3, 3, 4, 4, 5, 5})
            instrumentManager.handleMessage(getInstrumentMessageMock(i), context, coder);

        verify(marketDataManager, times(2)).onBBO(any(BBO.class));
        verify(gatewayManager, times(1)).onInstrumentDownloadFinished();
        ArgumentCaptor<IInstrument[]> instrumentCapture = ArgumentCaptor.forClass(IInstrument[].class);
        verify(marketDataHandler, times(1)).onInstruments(instrumentCapture.capture());
        assertEquals(2, instrumentCapture.getValue().length);
    }

    @Test
    public void testInstrumentAddAndNotFinished3() {
        for (int i : new int [] {1, 1, 3, 3, 4, 4, 5, 5})
            instrumentManager.handleMessage(getInstrumentMessageMock(i), context, coder);

        verify(gatewayManager, times(0)).onInstrumentDownloadFinished();

        for (int i : new int [] {1, 1, 2, 2, 4, 4, 5, 5})
            instrumentManager.handleMessage(getInstrumentMessageMock(i), context, coder);

        verify(marketDataManager, times(2)).onBBO(any(BBO.class));
        verify(gatewayManager, times(1)).onInstrumentDownloadFinished();
        ArgumentCaptor<IInstrument[]> instrumentCapture = ArgumentCaptor.forClass(IInstrument[].class);
        verify(marketDataHandler, times(1)).onInstruments(instrumentCapture.capture());
        assertEquals(2, instrumentCapture.getValue().length);
    }

}
