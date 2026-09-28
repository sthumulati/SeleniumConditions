package io.seleniumstudio.lab.fixture;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Serves the HTML fixtures from the test classpath over loopback using the JDK's built-in HTTP
 * server. No external dependency and no network egress, so the suite is hermetic and the URL-based
 * conditions ({@code urlToBe}, {@code urlContains}, {@code urlMatches}) have a real origin to
 * assert against.
 */
public final class TestSite {

    private final HttpServer server;
    private final String baseUrl;

    private TestSite(HttpServer server, String baseUrl) {
        this.server = server;
        this.baseUrl = baseUrl;
    }

    public static TestSite start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", TestSite::serve);
        server.setExecutor(null);
        server.start();
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        return new TestSite(server, base);
    }

    public String url(String page) { return baseUrl + "/" + page; }

    public String baseUrl() { return baseUrl; }

    public void stop() { server.stop(0); }

    private static void serve(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path)) path = "/index.html";

        try (InputStream in = TestSite.class.getResourceAsStream("/site" + path)) {
            if (in == null) {
                byte[] body = ("not found: " + path).getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, body.length);
                try (OutputStream out = exchange.getResponseBody()) { out.write(body); }
                return;
            }
            byte[] body = in.readAllBytes();
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.getResponseHeaders().add("Cache-Control", "no-store");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) { out.write(body); }
        }
    }
}
