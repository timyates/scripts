package io.micronaut.scripts.wip;

import io.micronaut.configuration.picocli.PicocliRunner;

import io.micronaut.http.util.HttpHeadersUtil;
import io.micronaut.scripts.github.GithubApiClient;
import io.micronaut.scripts.github.GithubConfiguration;
import io.micronaut.scripts.github.GithubIssue;
import io.micronaut.scripts.github.GithubLink;
import jakarta.inject.Inject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import io.micronaut.http.HttpResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Command(name = "wip", description = "...",
        mixinStandardHelpOptions = true)
public class WipCommand implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(WipCommand.class);

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    @Inject
    GithubApiClient githubApiClient;

    @Inject
    GithubConfiguration configuration;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(WipCommand.class, args);
    }

    public void run() {

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Issues");
        int row = 0;
        Row header = sheet.createRow(row);

        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Repository");

        headerCell = header.createCell(1);
        headerCell.setCellValue("# Issues");

        List<String> repositories = Arrays.asList(
                "micronaut-aot",
                "micronaut-acme",
                "micronaut-aws",
                "micronaut-azure",
                "micronaut-cache",
                "micronaut-cassandra",
                "micronaut-chatbots",
                "micronaut-control-panel",
                "micronaut-core",
                "micronaut-crac",
                "micronaut-data",
                "micronaut-discovery-client",
                "micronaut-elasticsearch",
                "micronaut-email",
                "micronaut-flyway",
                "micronaut-gcp",
                "micronaut-graphql",
                "micronaut-grpc",
                "micronaut-groovy",
                "micronaut-hibernate-validator",
                "micronaut-jackson-xml",
                "micronaut-jaxrs",
                "micronaut-jmx",
                "micronaut-jms",
                "micronaut-kafka",
                "micronaut-kubernetes",
                "micronaut-kotlin",
                "micronaut-liquibase",
                "micronaut-logging",
                "micronaut-micrometer",
                  "micronaut-microstream",
                "micronaut-multitenancy",
                "micronaut-mqtt",
                "micronaut-mongodb",
                "micronaut-neo4j",
                "micronaut-nats",
                "micronaut-object-storage",
                "micronaut-openapi",
                "micronaut-oracle-cloud",
                "micronaut-picocli",
                "micronaut-problem-json",
                  "micronaut-pulsar",
                "micronaut-rabbitmq",
                "micronaut-r2dbc",
                "micronaut-redis",
                  "micronaut-reactor",
                "micronaut-rxjava2",
                "micronaut-rxjava3",
                "micronaut-rss",
                "micronaut-serialization",
                "micronaut-servlet",
                "micronaut-spring",
                  "micronaut-security",
                  "micronaut-session",
                "micronaut-sql",
                "micronaut-test",
                "micronaut-test-resources",
                "micronaut-tracing",
                "micronaut-toml",
                "micronaut-validation",
                "micronaut-views");


        for (String slug : repositories) {
            row++;
            int count = 0;
            List<GithubLink> links = new ArrayList<>();
            int page = 1;
            int perPage = 50;
            do {
                HttpResponse<List<GithubIssue>> issuesResponse = githubApiClient.issues(configuration.getOrganization(), slug, page, perPage);
                //HttpHeadersUtil.trace(LOG, issuesResponse.getHeaders());
                String link = issuesResponse.getHeaders().get("link");
                if (link != null) {
                    links = links(link);

                }
                List<GithubIssue> issues = issuesResponse.body();
                int issuesWithoutRenovate = issues.stream()
                        .filter(issue -> !issue.user().login().equals("renovate[bot]"))
                        .toList().size();

                count += issuesWithoutRenovate;

                page++;
            } while (links.stream().anyMatch(link -> link.rel().equals("next")));

            //System.out.println(slug + " issues and pull requests without renovate: " + count);

            Row excelRow = sheet.createRow(row);

            System.out.println( slug + " " +  count);
            Cell repoCell = excelRow.createCell(0);
            repoCell.setCellValue(slug);

            Cell countCell = excelRow.createCell(1);
            countCell.setCellValue(count);
        }



        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "micronaut-project-issues.xlsx";

        try {
            FileOutputStream outputStream = new FileOutputStream(fileLocation);
            workbook.write(outputStream);
            workbook.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    List<GithubLink> links(String link) {
        String[] arr = link.split(",");
        List<GithubLink> links = new ArrayList<>();
        for (String str : arr) {
            if (str.contains("<") && str.contains(">") && str.contains("rel=")) {
                links.add(new GithubLink(str.substring(str.indexOf("<") + "<".length(), str.indexOf(">")), str.substring(str.indexOf("rel=") + "rel=".length()).replaceAll("\"", "")));
            }
        }
        return links;
    }
}
