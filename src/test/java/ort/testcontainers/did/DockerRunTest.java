package ort.testcontainers.did;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.core.command.LogContainerCmdImpl;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.dockerclient.DockerClientProviderStrategy;
import org.testcontainers.dockerclient.LogToStringContainerCallback;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.testcontainers.DockerClientFactory.DEFAULT_LABELS;

class DockerRunTest {

    @Test
    @DisplayName("Test run and exit docker container")
    @Timeout(value = 30, unit = SECONDS)
    void testRunDockerInDocker() {
        final Logger logger = LoggerFactory.getLogger(this.getClass());

        final List<DockerClientProviderStrategy> configurationStrategies = new ArrayList<>();
        final ServiceLoader<DockerClientProviderStrategy> load = ServiceLoader.load(DockerClientProviderStrategy.class);
        load.forEach(configurationStrategies::add);
        final DockerClientProviderStrategy strategy = DockerClientProviderStrategy.getFirstValidStrategy(configurationStrategies);
        final DockerClient strategyDockerClient = strategy.getClient();

        final String TINY_IMAGE = TestcontainersConfiguration.getInstance().getTinyImage();
        logger.info("Pulling {} image", TINY_IMAGE);
        logger.info("Image {} pulled successfully", TINY_IMAGE);
        strategyDockerClient.pullImageCmd(TINY_IMAGE);
        final CreateContainerCmd createContainerCmd = strategyDockerClient
                .createContainerCmd(TINY_IMAGE)
                .withLabels(DEFAULT_LABELS)
                .withCmd("sh", "-c", "ip route|awk '/default/ { print $3 }' ; exit");

        final String createContainerCmdId = createContainerCmd.exec().getId();
        logger.info("createContainerCmdId={}", createContainerCmdId);

        try {
            strategyDockerClient.startContainerCmd(createContainerCmdId).exec();
            final LogToStringContainerCallback loggingCallback = new LogToStringContainerCallback();
            final LogContainerCmdImpl logContainerCmd2 = (LogContainerCmdImpl) strategyDockerClient
                    .logContainerCmd(createContainerCmdId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true);
            logger.info("logContainerCmd2={}", logContainerCmd2);
            final LogToStringContainerCallback exec = logContainerCmd2.exec(loggingCallback);

            try {
                logger.info("exec={}", exec);
                final LogContainerResultCallback logContainerResultCallback = exec.awaitStarted();
                logger.info("logContainerResultCallback={}", logContainerResultCallback);
                final boolean awaitCompletion = loggingCallback.awaitCompletion(3L, SECONDS);
                logger.info("awaitCompletion={}", awaitCompletion);
                final String loggingCallbackResult = loggingCallback.toString().trim();
                logger.info("loggingCallbackResult={}", loggingCallbackResult);

                assertTrue(loggingCallbackResult.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$"));
                return;
            } catch (Exception ex) {
                logger.error("Can't parse the default gateway IP", ex);
            }

        } finally {
            try {
                logger.info("Try to remove container {}", createContainerCmdId);
                strategyDockerClient.removeContainerCmd(createContainerCmdId)
                        .withRemoveVolumes(true)
                        .withForce(true)
                        .exec();
                logger.info("Removed container {}", createContainerCmdId);
            } catch (final InternalServerErrorException | NotFoundException ex) {
                logger.error("", ex);
            }
        }

        throw new AssertionError("The container is not finished correctly. See the logs.");
    }
}
