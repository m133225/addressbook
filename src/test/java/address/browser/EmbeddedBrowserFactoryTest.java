package address.browser;

import commons.PlatformExecUtil;
import address.testutil.TestUtil;
import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.BrowserType;
import hubturbo.embeddedbrowser.EmbeddedBrowserFactory;
import hubturbo.embeddedbrowser.fxbrowser.FxBrowserAdapter;
import hubturbo.embeddedbrowser.jxbrowser.JxBrowserAdapter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * To test the EmbeddedBrowserFactory
 */
public class EmbeddedBrowserFactoryTest {

    @BeforeClass
    public static void setup() throws TimeoutException {
        TestUtil.initRuntime();
        TestUtil.initBrowserInStatic();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        TestUtil.tearDownRuntime();
    }

    @Test
    public void testCreateBrowser_fullFeatureBrowser_success() {
        EmbeddedBrowser browser = EmbeddedBrowserFactory.createBrowser(BrowserType.FULL_FEATURE_BROWSER);
        assertNotNull(browser);
        assertTrue(browser instanceof JxBrowserAdapter);

        browser.dispose();
    }

    @Test
    public void testCreateBrowser_limitedFeatureBrowser_success() {
        final AtomicReference<EmbeddedBrowser> browser = new AtomicReference<>();
        PlatformExecUtil.runLaterAndWait(() ->
                browser.set(EmbeddedBrowserFactory.createBrowser(BrowserType.LIMITED_FEATURE_BROWSER)));
        assertNotNull(browser.get());
        assertTrue(browser.get() instanceof FxBrowserAdapter);

        browser.get().dispose();
    }

    @Test
    public void testCreateBrowser_invalidChoice_fail() {
        EmbeddedBrowser browser = null;
        try {
            browser = EmbeddedBrowserFactory.createBrowser(BrowserType.valueOf("SUPER_FEATURE_BROWSER"));
        } catch (IllegalArgumentException e) {
        }
        assertNull(browser);
    }

}
