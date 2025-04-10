package security;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.*;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;
import security.service.ZapScanner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ZAPSecurityTest extends ZAPSecurityBase{

    private static final String ZAP_PROXY_ADDRESS = "localhost";
    private static final int ZAP_PROXY_PORT = 8080;
    private static final String ZAP_PROXY_API = ""; // ZAP --> Tools --> Options --> API --> API key

    private WebDriver driver;
    private ClientApi clientApi;

    //private String targetURL ="https://juice-shop.herokuapp.com/#/";
    private String targetURL ="https://owasp.org/www-project-webgoat/";

    //protected WebDriver driver;
    protected ZapScanner zapScanner;
    protected ZAPSecurityBase zapSecurityBase;

    @BeforeClass
    public void setUp() {
        zapSecurityBase = new ZAPSecurityBase();
        try {
            //zapSecurityBase.startZap();
            zapSecurityBase.startZAPDaemon();
            zapSecurityBase.waitForZapStartup(180);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        String proxyServerURL = ZAP_PROXY_ADDRESS + ":" + ZAP_PROXY_PORT;
        Proxy proxy = new Proxy();
        proxy.setHttpProxy(proxyServerURL);
        proxy.setSslProxy(proxyServerURL);
        ChromeOptions options = new ChromeOptions();
        options.setProxy(proxy);
        options.setAcceptInsecureCerts(true);
        options.addArguments("--proxy-server=http://localhost:8080");
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        clientApi = new ClientApi(ZAP_PROXY_ADDRESS, ZAP_PROXY_PORT, ZAP_PROXY_API);
    }

    @Test
    public void securityTest()
    {
        driver.get(targetURL);
        driver.navigate().refresh();
        zapScanner = new ZapScanner();
        zapScanner.securityScan(targetURL);
    }

    @AfterClass
    public void tearDown()
    {
        if (clientApi != null)
        {
             String title = "OWASP ZAP Security Test Report";
             String template = "traditional-html";
             String theme = ""; // Leave empty if not using a theme
             String description = "This is OWASP ZAP Security Test Report.";
             String contexts = ""; // Provide the correct context if applicable
             String sites = ""; // Provide target sites if needed
             String sections = ""; // Leave empty or set to specific report sections
             String includedConfidences = ""; // False Positive | Low | Medium |High
             String includeRisks = "";     //Informational,Low,Medium,High
             String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
             String reportfilename = "OWASP-ZAP-Report_" + timestamp + ".html";
             String reportFileNamePattern = "traditional-html"; // Ensure it's a valid format (e.g., "HTML", "PDF", "XML")
             String reportdir = "C:\\Report"; // generated report save at this location
             String  display = "false";

            try {
                System.out.println("Client API core version: "+clientApi.core.version());
                ApiResponse templates = clientApi.reports.templateDetails(template);
                System.out.println("Available Templates: " + templates.toString());
                ApiResponse alertsSummary = clientApi.core.numberOfAlerts(targetURL);
                System.out.println("⚠️ Alert Summary: " + alertsSummary.toString());
                ApiResponse apiResponse = clientApi.reports.generate(title, template, theme, description, contexts, sites, sections, includedConfidences, includeRisks, reportfilename, reportFileNamePattern, reportdir, display);
                System.out.println("ZAP report generated at : "+ apiResponse.toString());
            } catch (ClientApiException e) {
                throw new RuntimeException(e);
            }
        }
        zapSecurityBase.stopZAP();
        driver.quit();
    }
}
