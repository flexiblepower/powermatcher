package net.powermatcher.scenarios.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import net.powermatcher.scenarios.Scenario;
import net.powermatcher.scenarios.ScenarioConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;

@Component
public class ScenarioTest {
    private Scenario sample;
    private BundleContext context;

    @Activate
    public void activate(BundleContext context) {
        this.context = context;
    }

    @SuppressWarnings("serial")
    @Before
    public void setUp() {
        sample = new Scenario(Arrays.asList(
                                    new ScenarioConfiguration("net.powermatcher.core",
                                                              "net.powermatcher.core.auctioneer.Auctioneer",
                                                              new HashMap<String, String>() {
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
        // // Arrange
        // File file = new File(context.getBundle().getResource("sample/sample1.json").getFile());
        //
        // // Act
        // Scenario scenario = Scenario.load(file);
        //
        // // Assert
        // assertEquals(scenario.configurations.size(), 1);
    }
}
