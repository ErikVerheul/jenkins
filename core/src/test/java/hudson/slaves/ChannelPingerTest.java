package hudson.slaves;

import com.google.common.testing.EqualsTester;
import hudson.remoting.Channel;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ChannelPinger.class })
public class ChannelPingerTest {

    @Mock private Channel mockChannel;

    private final Map<String, String> savedSystemProperties = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockStatic(ChannelPinger.class);
    }

    @Before
    public void preserveSystemProperties() throws Exception {
        preserveSystemProperty("hudson.slaves.ChannelPinger.pingInterval");
        preserveSystemProperty("hudson.slaves.ChannelPinger.pingIntervalSeconds");
        preserveSystemProperty("hudson.slaves.ChannelPinger.pingTimeoutSeconds");
    }

    @After
    public void restoreSystemProperties() throws Exception {
        for (Map.Entry<String, String> entry : savedSystemProperties.entrySet()) {
            if (entry.getValue() != null) {
                System.setProperty(entry.getKey(), entry.getValue());
            } else {
                System.clearProperty(entry.getKey());
            }
        }
    }

    private void preserveSystemProperty(String propertyName) {
        savedSystemProperties.put(propertyName, System.getProperty(propertyName));
        System.clearProperty(propertyName);
    }

    @Test
    public void testDefaults() throws Exception {
        ChannelPinger channelPinger = new ChannelPinger();
        channelPinger.install(mockChannel, null);

        verify(mockChannel).call(eq(new ChannelPinger.SetUpRemotePing(ChannelPinger.PING_TIMEOUT_SECONDS_DEFAULT,
                                                                      ChannelPinger.PING_INTERVAL_SECONDS_DEFAULT)));
        verifyStatic();
        ChannelPinger.setUpPingForChannel(mockChannel, null, ChannelPinger.PING_TIMEOUT_SECONDS_DEFAULT,
                                          ChannelPinger.PING_INTERVAL_SECONDS_DEFAULT, true);
    }

    @Test
    public void testFromSystemProperties() throws Exception {
        System.setProperty("hudson.slaves.ChannelPinger.pingTimeoutSeconds", "42");
        System.setProperty("hudson.slaves.ChannelPinger.pingIntervalSeconds", "73");

        ChannelPinger channelPinger = new ChannelPinger();
        channelPinger.install(mockChannel, null);

        verify(mockChannel).call(new ChannelPinger.SetUpRemotePing(42, 73));
        verifyStatic();
        ChannelPinger.setUpPingForChannel(mockChannel, null, 42, 73, true);
    }

    @Test
    public void testFromOldSystemProperty() throws Exception {
        System.setProperty("hudson.slaves.ChannelPinger.pingInterval", "7");

        ChannelPinger channelPinger = new ChannelPinger();
        channelPinger.install(mockChannel, null);

        verify(mockChannel).call(eq(new ChannelPinger.SetUpRemotePing(ChannelPinger.PING_TIMEOUT_SECONDS_DEFAULT, 420)));
        verifyStatic();
        ChannelPinger.setUpPingForChannel(mockChannel, null, ChannelPinger.PING_TIMEOUT_SECONDS_DEFAULT, 420, true);
    }

    @Test
    public void testNewSystemPropertyTrumpsOld() throws Exception {
        System.setProperty("hudson.slaves.ChannelPinger.pingIntervalSeconds", "73");
        System.setProperty("hudson.slaves.ChannelPinger.pingInterval", "7");

        ChannelPinger channelPinger = new ChannelPinger();
        channelPinger.install(mockChannel, null);

        verify(mockChannel).call(eq(new ChannelPinger.SetUpRemotePing(ChannelPinger.PING_TIMEOUT_SECONDS_DEFAULT, 73)));
        verifyStatic();
        ChannelPinger.setUpPingForChannel(mockChannel, null, ChannelPinger.PING_TIMEOUT_SECONDS_DEFAULT, 73, true);
    }

    @Test
    public void testSetUpRemotePingEquality() {
         new EqualsTester()
             .addEqualityGroup(new ChannelPinger.SetUpRemotePing(1, 2), new ChannelPinger.SetUpRemotePing(1, 2))
             .addEqualityGroup(new ChannelPinger.SetUpRemotePing(2, 3), new ChannelPinger.SetUpRemotePing(2, 3))
             .testEquals();
    }
}
