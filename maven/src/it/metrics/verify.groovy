import org.testcontainers.containers.GenericContainer

try {
    // 1. Verify pushed metrics
    def pushgatewayAddress = context.get("userProperties").get("jqassistant.metrics.prometheus.pushgateway.address")
    String metricsUrl = "http://$pushgatewayAddress/metrics"
    System.out.println(">>> [Invoker Post-Build] Fetching metrics from: ${metricsUrl}")
    URL url = new URI(metricsUrl).toURL()
    HttpURLConnection connection = (HttpURLConnection) url.openConnection()
    connection.requestMethod = "GET"
    connection.connectTimeout = 5000
    connection.readTimeout = 5000
    assert connection.responseCode == 200
    String responseBody = connection.inputStream.text
    assert responseBody.contains("jqassistant_distribution")
} finally {
    // 2. Cleanup pushgateway container
    System.out.println(">>> [Invoker Post-Build] Cleaning up container resources...")

    def pushgateway = (GenericContainer) context.get("pushgatewayContainer")

    if (pushgateway != null && pushgateway.isRunning()) {
        pushgateway.stop()
        System.out.println(">>> [Invoker Post-Build] Prometheus Pushgateway container stopped successfully.")
    } else {
        System.out.println(">>> [Invoker Post-Build] Warning: Container reference was null or already stopped.")
    }
}
