package security;


import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ZAPSecurityBase {

    private static final String ZAP_PATH = "C:\\Program Files\\ZAP\\Zed Attack Proxy\\zap.bat"; // zap.bat file location
    private static final String ZAP_API_KEY = ""; // Replace with your actual API key
    private static final String ZAP_HOST = "localhost";
    private static final int ZAP_PORT = 8080;
    private Process zapProcess;

    public void startZAPDaemon() throws IOException {

        ProcessBuilder processBuilder = new ProcessBuilder(
                "cmd.exe", "/c", "start", "\"ZAP\"", ZAP_PATH,
                "-daemon",
                "-port", String.valueOf(ZAP_PORT),
                "-host", ZAP_HOST,
                "-config", "api.key="+ ZAP_API_KEY,
                "-config", "sessionManagement.autoPersistence=false",
                "-config", "connection.dnsTtlSuccessfulQueries=-1",
                "-config", "connection.dnsTtlFailedQueries=0",
                "-config", "ui.view.default=ConsoleView", // Avoid GUI views
                "-config", "database.newsession=true",
                "-config", "session.trim=true",
                "-config", "view.mode=attack"
        );
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(new File("C:\\Program Files\\ZAP\\Zed Attack Proxy")); // Set working dir
        zapProcess = processBuilder.start();
        System.out.println("✅ ZAP daemon starting...");
        try {
            waitForZapStartup(180);
        } catch (InterruptedException e) {
            System.err.println("❌ Failed to start ZAP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopZAP() {
        System.out.println("\n🛑 Stopping ZAP...");
        try {
            String zapShutdownUrl = "http://" + ZAP_HOST + ":" + ZAP_PORT +
                    "/JSON/core/action/shutdown/?apikey=" + ZAP_API_KEY;
            HttpURLConnection connection = (HttpURLConnection) new URL(zapShutdownUrl).openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                System.out.println("✅ ZAP shutdown successfully.");
            } else {
                System.err.println("❌ Failed to shut down ZAP. Response code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("❌ Exception during ZAP shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void waitForZapStartup(int timeoutInSeconds) throws InterruptedException {
        int waited = 0;
        while (waited < timeoutInSeconds) {
            try {
                URL url = new URL("http://localhost:9090");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(2000);
                conn.connect();
                if (conn.getResponseCode() == 200) {
                    System.out.println("✅ ZAP is ready.");
                    return;
                }
            } catch (IOException ignored) { }

            Thread.sleep(1000);
            waited++;
            System.out.print(".");
        }
        throw new RuntimeException("❌ ZAP did not start within expected time.");
    }
}
