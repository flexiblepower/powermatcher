package net.powermatcher.scenarios.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import net.powermatcher.scenarios.data.Scenario;
import net.powermatcher.scenarios.data.ScenarioConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import aQute.bnd.annotation.component.Component;

@Component
public class ScenarioTest {
    private final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

    private Scenario sample;

    @Before
    public void setUp() {
        sample = new Scenario(Arrays.asList(
                                    new ScenarioConfiguration("net.powermatcher.core",
                                                              "net.powermatcher.core.auctioneer.Auctioneer",
                                                              new HashMap<String, String>() {
                                                                  private static final long serialVersionUID = 1L;
                                                                  {
                                                                      put("agentId", "auctioneer");
                                                                      put("clusterId", "DefaultCluster");
                                                                  }
                                                              })
                                    ));
    }

    @Test
    public void testSavingToJsonString() {
        // Act
        String json = sample.save();

        // Assert
        assertEquals(json,
                     "{\"configurations\":[{\"bundleId\":\"net.powermatcher.core\",\"factoryId\":\"net.powermatcher.core.auctioneer.Auctioneer\",\"properties\":{\"agentId\":\"auctioneer\",\"clusterId\":\"DefaultCluster\"}}]}");
    }

    @Test
    public void testLoadingFromJsonString() {
        // Arrange
        String json = "{\"configurations\":[{\"bundleId\":\"net.powermatcher.core\",\"factoryId\":\"net.powermatcher.core.auctioneer.Auctioneer\",\"properties\":{\"agentId\":\"auctioneer\",\"clusterId\":\"DefaultCluster\"}}]}";

        // Act
        Scenario scenario = Scenario.load(json);

        // Assert
        assertEquals(scenario.configurations.size(), 1);
        assertEquals(scenario.configurations.get(0).bundleId, "net.powermatcher.core");
        assertEquals(scenario.configurations.get(0).factoryId, "net.powermatcher.core.auctioneer.Auctioneer");
        assertEquals(scenario.configurations.get(0).properties.size(), 2);
        assertEquals(scenario.configurations.get(0).properties.get("agentId"), "auctioneer");
    }

    @Test
    public void testLoadingFromJsonFile() throws IOException {
        // Arrange
        URL sample = context.getBundle().getEntry("samples/sample1.json");

        // Act
        Scenario scenario = Scenario.load(sample);

        // Assert
        assertEquals(scenario.configurations.size(), 1);
    }
}
