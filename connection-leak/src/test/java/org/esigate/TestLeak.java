package org.esigate;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.esigate.server.EsigateServer;
import org.junit.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Due to unproper esigate stop method, tests must be run one by one
 */
public class TestLeak {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void setUp() throws Exception {
        stubFor(get(urlEqualTo("/redirect"))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Location", "/include")
                        .withBody("<response>Some content</response>")));

        stubFor(get(urlEqualTo("/include"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("content")));

        stubFor(get(urlEqualTo("/page"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type","text/html")
                        .withBody("<html><body><esi:include src='$(PROVIDER{backend})/redirect' /></body></html>")));



        Thread t = new Thread(() -> {
            try {
                Properties properties = new Properties();
                properties.setProperty("config", "connection-leak/src/test/resources/esigate.properties");
                EsigateServer.init(properties);
                EsigateServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
        while (!EsigateServer.isStarted()) {
            System.out.println("Waiting for esigate to be started");
            Thread.sleep(1000);
        }

    }

    @After
    public void tearDown() throws Exception {
        System.out.println("esigate server stop");
        EsigateServer.stop();

    }

    @Test(timeout = 5000)
    public void testSingleConnection() throws Exception {
        Assert.assertEquals("<html><body>content</body></html>",doGet("http://localhost:8080/page"));
    }

    @Test(timeout = 5000)
    public void testConnectionUnderPoolSize() throws Exception {
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals("<html><body>content</body></html>",doGet("http://localhost:8080/page"));
        }
    }

    @Test(timeout = 5000)
    public void testConnectionOverPoolSize() throws Exception {
        for (int i = 0; i < 50; i++) {
            Assert.assertEquals("<html><body>content</body></html>",doGet("http://localhost:8080/page"));
        }
    }

    @Test(timeout = 5000)
    public void test50redirect() throws Exception {
        for (int i = 0; i < 50; i++) {
            Assert.assertEquals("content",doGet("http://localhost:8080/redirect"));
        }
    }

    private String doGet(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        InputStream response = connection.getInputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response))) {
            return reader.readLine();
        }
    }

}
