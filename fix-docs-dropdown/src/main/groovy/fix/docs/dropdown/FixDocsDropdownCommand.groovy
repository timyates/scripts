package fix.docs.dropdown

import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.core.util.StringUtils
import picocli.CommandLine.Command
import picocli.CommandLine.Option

import java.util.stream.Stream

@Command(name = 'fix-docs-dropdown',
        description = 'CLI utility to clone and fix the docs dropdown',
        mixinStandardHelpOptions = true)
class FixDocsDropdownCommand implements Runnable {

    private static final List<List<String>> REPOSITORIES = [
            ['micronaut-aot'],
            ['micronaut-acme'],
            ['micronaut-aws'],
            ['micronaut-azure'],
            ['micronaut-cache'],
            ['micronaut-cassandra'],
            ['micronaut-chatbots'],
            ['micronaut-control-panel'],
            ['micronaut-crac'],
            ['micronaut-data'],
            ['micronaut-discovery-client'],
            ['micronaut-elasticsearch'],
            ['micronaut-email'],
            ['micronaut-flyway'],
            ['micronaut-gcp'],
            ['micronaut-graphql'],
            ['micronaut-grpc'],
            ['micronaut-groovy'],
            ['micronaut-hibernate-validator'],
            ['micronaut-jackson-xml'],
            ['micronaut-jaxrs'],
            ['micronaut-jmx'],
            ['micronaut-jms'],
            ['micronaut-kafka'],
            ['micronaut-kubernetes'],
            ['micronaut-kotlin'],
            ['micronaut-liquibase'],
            ['micronaut-logging'],
            ['micronaut-micrometer'],
            ['micronaut-microstream'],
            ['micronaut-multitenancy'],
            ['micronaut-mqtt'],
            ['micronaut-mongodb'],
            ['micronaut-neo4j'],
            ['micronaut-nats'],
            ['micronaut-object-storage'],
            ['micronaut-openapi'],
            ['micronaut-oracle-cloud'],
            ['micronaut-picocli'],
            ['micronaut-problem-json'],
            ['micronaut-pulsar'],
            ['micronaut-rabbitmq'],
            ['micronaut-r2dbc'],
            ['micronaut-redis'],
            ['micronaut-reactor'],
            ['micronaut-rxjava2'],
            ['micronaut-rxjava3'],
            ['micronaut-rss'],
            ['micronaut-serialization'],
            ['micronaut-servlet'],
            ['micronaut-spring'],
            ['micronaut-security'],
            ['micronaut-session'],
            ['micronaut-sql'],
            ['micronaut-test'],
            ['micronaut-test-resources'],
            ['micronaut-tracing'],
            ['micronaut-toml'],
            ['micronaut-validation'],
            ['micronaut-views'],
            ['micronaut-docs', 'micronaut-docs-mn1', 'micronaut-docs-mn2', 'micronaut-docs-mn3']
    ]
    @Option(names = ['-f', '--folder'], description = 'Folder to clone the repositories', required = true)
    String folder

    static void main(String[] args) throws Exception {
        PicocliRunner.run(FixDocsDropdownCommand.class, args)
    }

    void run() {
        for (List<String> slugs : REPOSITORIES) {
            for (String slug : slugs) {
                String path = String.join("/", folder, slug)
                File clonedRepoFolder = new File(path)
                if (!clonedRepoFolder.exists()) {
                    cloneRepo(folder, slug)
                }
            }
        }
        for (List<String> slugs : REPOSITORIES) {
            for (String slug : slugs) {
                String path = String.join("/", folder, slug)
                List<SoftwareVersion> softwareVersions = []
                for (String s : slugs) {
                    new File(String.join("/", folder, s)).eachFile { f ->
                        if (!(f.name.startsWith(".") || f.name.contains("x") || f.name.contains("RC") || f.name.contains("M") || f.name.count(".") != 2)) {
                            softwareVersions << SoftwareVersion.build(f.name)
                        }
                    }
                }
                softwareVersions = softwareVersions.unique()

                softwareVersions.sort()
                softwareVersions = softwareVersions.reverse()
                if (softwareVersions) {
                    for (SoftwareVersion softwareVersion : softwareVersions) {
                        String version = softwareVersion.versionText
                        updateIndex(folder, slug, softwareVersions, version)
                    }
                    updateIndex(folder, slug, softwareVersions, "latest")
                    updateIndex(folder, slug, softwareVersions, "snapshot")
                }
                openTower(path)
            }
        }
    }

    static void updateIndex(String folder, String slug, List<SoftwareVersion> softwareVersions, String version) {
        SoftwareVersion latestVersion = softwareVersions.get(0)
        final String anchor = "this.options[this.selectedIndex].value;'>"
        final String bodyAnchor = '<body class="body" id="docs" onload="addJsClass();" onhashchange="highlightMenu()">'
        String optionsHtml = ""
        for (SoftwareVersion sv : softwareVersions) {
            optionsHtml += docsUrl(slug, sv.versionText, version == sv.versionText)
        }
        String latestOptions = docsUrl(slug, "latest", version == 'latest') +  docsUrl(slug, "snapshot", version == 'snapshot')
        String indexPath = String.join("/", folder, slug, version, "guide", "index.html")
        File indexHtml = new File(indexPath)
        if (indexHtml.exists()) {
            String html = indexHtml.text
            if (html.contains(anchor)) {
                int index = html.indexOf(anchor) + anchor.length()
                String before = html.substring(0, index)
                String after = html.substring(index)
                after = after.substring(after.indexOf("</select>"))
                indexHtml.text = before + latestOptions + optionsHtml + after
            }
            html = indexHtml.text
            SoftwareVersion softwareVersion = SoftwareVersion.build(version)
            if (softwareVersion && html.contains(bodyAnchor) && softwareVersion <=> latestVersion) {
                int index = html.indexOf(bodyAnchor) + bodyAnchor.length()
                String before = html.substring(0, index)
                String after = html.substring(index)
                if (html.contains("background-color: color(display-p3 1 0.784 0.2)")) {
                    after = after.substring(after.indexOf("</a></div>") + "</a></div>".length())
                }
                indexHtml.text = before + oldDocsBanner(slug) + after
            }
        }
    }

    static String oldDocsBanner(String slug) {
        String[] arr = slug.split("-")
        String name = slug.startsWith("micronaut-docs") ? "Micronaut Core" : String.join(" ", Stream.of(arr).map(StringUtils::capitalize).toList())
        """\
<div style="background-color: color(display-p3 1 0.784 0.2);border-top: 1px solid #000;z-index: 999;font-weight: bold;position:fixed;bottom: 0;left: 0;width: 100%;height: 42px;font-family: -apple-system, 'Helvetica Neue', Helvetica, sans-serif;font-size: 25px;text-align:center;padding-top: 15px;">The documentation you are viewing is not the latest documentation of <a style="color: #fff !important;" href="${docsUrl(slug.startsWith("micronaut-docs") ? "micronaut-docs" : slug, "latest")}">${name}</a></div>
"""
    }

    static String docsUrl(String slug, String version, boolean selected) {
        option(docsUrl(slug, version), version, selected)
    }

    static String docsUrl(String slug, String version) {
        if (slug.startsWith('micronaut-docs')) {
            if (version.startsWith("1.")) {
                return "https://micronaut-projects.github.io/micronaut-docs-mn1/${version}/guide/index.html"
            } else if (version.startsWith("2.")) {
                return "https://micronaut-projects.github.io/micronaut-docs-mn2/${version}/guide/index.html"
            } else if (version.startsWith("3.")) {
                return "https://micronaut-projects.github.io/micronaut-docs-mn3/${version}/guide/index.html"
            }
            return "https://docs.micronaut.io/${version}/guide/index.html"
        }
        "https://micronaut-projects.github.io/${slug}/${version}/guide/index.html"
    }

    static String option(String value, String text, boolean selected) {
        "<option ${selected ?  "selected " : ""}value='${value}'>${text.toUpperCase()}</option>"
    }

    static void cloneRepo(String organization = "micronaut-projects", String folder, String slug) {
        execute("git clone git@github.com:${organization}/${slug}.git ${String.join("/", folder, slug)} --branch gh-pages")
    }

    static void openTower(String path) {
        execute("open -n -a Tower ${path}")
    }

    static void execute(String command) {
        StringBuilder sout = new StringBuilder()
        StringBuilder serr = new StringBuilder()
        Process proc = command.execute()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForProcessOutput(sout, serr)
        proc.waitForOrKill(1000)
        println "out> $sout\nerr> $serr"
    }
}
