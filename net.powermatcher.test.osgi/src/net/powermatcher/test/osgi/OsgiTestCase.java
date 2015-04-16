package net.powermatcher.test.osgi;

import java.util.List;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base case for OSGI testcases
 *
 * @author FAN
 * @version 2.0
 */
public abstract class OsgiTestCase
    extends TestCase {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

    protected ClusterHelper clusterHelper;

    public static <T> T getLast(List<T> list) {
        assertFalse(list.isEmpty());
        return list.get(list.size() - 1);
    }

    public static <T> void assertEmpty(List<T> list) {
        if (!list.isEmpty()) {
            throw new AssertionError("list is not empty");
        }
    }

    public static <T> void assertNotEmpty(List<T> list) {
        if (list.isEmpty()) {
            throw new AssertionError("list is empty");
        }
    }

    /**
     * Setup tests, which cleans existing OSGI servers and gets reference to configuration admin.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        assertNotNull("These unit tests should be executed in an OSGi environment", context);
        clusterHelper = new ClusterHelper(context);
    }

    @Override
    protected void tearDown() throws Exception {
        clusterHelper.close();
        super.tearDown();
    }
}
