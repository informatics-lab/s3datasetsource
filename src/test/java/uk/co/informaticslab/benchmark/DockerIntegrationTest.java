package uk.co.informaticslab.benchmark;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.configuration.DockerComposeFiles;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class DockerIntegrationTest {

    private static final String THREDDS = "thredds";
    private static final String TELEGRAF = "telegraf";

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .files(DockerComposeFiles.from("docker-compose.yml", "src/test/resources/docker-compose.yml"))
            .waitingForService(THREDDS, HealthChecks.toRespond2xxOverHttp(8080,
                    (port) -> port.inFormat("http://$HOST:$EXTERNAL_PORT/thredds/catalog.html")))
            .waitingForService(TELEGRAF, HealthChecks.toHaveAllPortsOpen())
            .build();

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    public static String endpoint;
    public static List<String> requests;

    @BeforeClass
    public static void initialize() {
        requests = new ArrayList<>();
        requests.add("/thredds/catalog.html?dataset=prods_op_mogreps-uk_20160309_15_10_009.nc");
        requests.add("/thredds/dodsC/s3/mogreps-uk/prods_op_mogreps-uk_20160309_15_10_009.nc.html");

        DockerPort thredds = docker.containers().container(THREDDS).port(8080);
        endpoint = String.format("http://%s:%s", thredds.getIp(), thredds.getExternalPort());
    }

    @Test
    public void test() throws IOException {
        for (String request : requests) {
            File response = makeHttpRequest(endpoint + request);
            assertTrue(response.length() > 0);
            System.out.println("done " + endpoint + request);
        }
    }

    public File makeHttpRequest(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        InputStream is = url.openStream();
        try {
            File targetFile = folder.newFile();
            Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return targetFile;
        } finally {
            is.close();
        }
    }
}
