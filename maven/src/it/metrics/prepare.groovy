import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

System.out.println(">>> [Invoker Pre-Build] Starting Prometheus Pushgateway via Testcontainers...")

// 1. Initialize and start the container
def pushgateway = new GenericContainer("prom/pushgateway:v1")
        .withExposedPorts(9091)
        .waitingFor(Wait.forHttp("/-/ready").forPort(9091))

pushgateway.start()

// 2. Save a reference to the container instance so we can stop it later in the post-build hook
context.put("pushgatewayContainer", pushgateway)

// 3. Inject the dynamic port and host into the child Maven user properties
def pushgatewayAddress = pushgateway.getHost() + ":" + pushgateway.getMappedPort(9091)

System.out.println(">>> [Invoker Pre-Build] Pushgateway is up at: " + pushgatewayAddress)

def userProperties = context.get('userProperties')
userProperties.put("jqassistant.metrics.prometheus.pushgateway.address", pushgatewayAddress)
