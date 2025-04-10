package security.service;

import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

public class ZapScanner {

    private static final String ZAP_PROXY_ADDRESS = "localhost";
    private static final int ZAP_PROXY_PORT = 8080;
    private static final String ZAP_PROXY_API = ""; // ZAP --> Tools --> Options --> API --> API key
    private ClientApi clientApi;

    public void securityScan(String targetURL)
    {
        clientApi = new ClientApi(ZAP_PROXY_ADDRESS, ZAP_PROXY_PORT, ZAP_PROXY_API);
        System.out.println("✅ Security test executed.");
        try {
            clientApi.core.accessUrl(targetURL, "true");
            System.out.println("✅ Target URL added to ZAP scan tree: " + targetURL);
            try {
                Thread.sleep(10000); // Give ZAP time to capture the request
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Passive Scan (waits for previous scans)
            clientApi.pscan.enableAllScanners();

            // Start the spider scan to find all pages
            clientApi.spider.scan(targetURL, null, null, null, null);

            // Wait for the Spider scan to complete
            while (true) {
                Thread.sleep(5000);  // Wait 5 seconds
                String spiderStatus = String.valueOf(clientApi.spider.status("0"));  // Check spider status
                System.out.println("🔄 Spider Scan Progress: " + spiderStatus + "%");
                if ("100".equals(spiderStatus)) break;  // Exit loop when spidering is complete
            }
            System.out.println("✅ Spider Scan Completed!");
            String scanId = clientApi.ascan.scan(targetURL, "True", "False", null, null, null).toString();
            System.out.println("✅ Active Scan Started, Scan ID: " + scanId);
            while (true) {
                Thread.sleep(5000);  // Wait 5 seconds before checking status
                String status = clientApi.ascan.status(scanId).toString();
                System.out.println("🔄 Active Scan Progress: " + status + "%");
                if ("100".equals(status)) {
                    System.out.println("✅ Active Scan Completed.");
                    break;
                }
            }
            ApiResponse sites = clientApi.core.sites();
            System.out.println("✅ Sites in ZAP Tree: " + sites.toString());
        } catch (ClientApiException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
