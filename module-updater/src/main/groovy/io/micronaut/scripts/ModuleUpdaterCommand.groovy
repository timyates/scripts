package io.micronaut.scripts

import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.core.util.StringUtils
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option

import java.util.function.Consumer
import java.util.function.Function

import static groovy.io.FileType.FILES

@Command(name = 'module-updater', description = 'Performs changes to a module such as updating dependencies',
        mixinStandardHelpOptions = true)
class ModuleUpdaterCommand implements Runnable {

    @Option(names = ['-v', '--verbose'], description = 'whether you want verbose output')
    boolean verbose

    @Option(names = ['-m', '--module'], description = 'location of the module being upgraded', required = true)
    String module

    @Option(names = ['-p', '--platform'], description = 'location of the platform being upgraded', required = true)
    String platform

    @Option(names = ['-g', '--gradle'], description = 'Micronaut Gradle Plugin Version', defaultValue = "4.0.0-M4", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    String gradlePluginVersion

    @Option(names = ['--micronaut-build-plugin'], description = 'Micronaut Build Plugin Version', defaultValue = "6.5.0", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    String micronautBuildPlugin

    @Option(names = ['--remove-snapshot'], defaultValue = StringUtils.TRUE, description = 'remove snapshots repositories from module')
    boolean removeSnapshot = true

    static void main(String[] args) throws Exception {
        PicocliRunner.run(ModuleUpdaterCommand.class, args)
    }

    void run() {
        File platformVersion = new File("$platform/gradle/libs.versions.toml")
        File moduleVersion = new File("$module/gradle/libs.versions.toml")
        File module = new File(module)
        Map<String, String> versions = micronautVersions(platformVersion)
        updateModuleVersions(versions, moduleVersion)
        updateModuleVersions(['micronaut-gradle-plugin': gradlePluginVersion], moduleVersion)
        if (removeSnapshot) {
            processGradleFiles(module, gradleFile -> removeSnapshotRepository(gradleFile))
        }
        removeCoreBranch(module)

        if (!moduleVersion.text.contains("micronaut-core = { module = 'io.micronaut:micronaut-core-bom', version.ref = 'micronaut' }")) {
            replaceInLines(moduleVersion, "[libraries]", "micronaut-core = { module = 'io.micronaut:micronaut-core-bom', version.ref = 'micronaut' }\n[libraries]\n")
        }

        processGradleFiles(module, gradleFile -> {
            removeMicronautRuntime(gradleFile)
            removeMicronautInjectJavaImplementation(gradleFile)
            removeMicronautInjectGroovyImplementation(gradleFile)
        })

        addMicronautLogging(module, moduleVersion, versions)

        processGradleSettingsFiles(module, settingsFile -> {
            replaceInLines(settingsFile, "    io.micronaut.build.shared.settings", "    id(\"io.micronaut.build.shared.settings\") version \"${micronautBuildPlugin}\"", true)}
        )
    }

    private static void addMicronautLogging(File module, File moduleVersion, Map<String, String> versions) {
        processGradleFiles(module, gradleFile -> {
            boolean logging = replaceInLines(gradleFile, "mn.logback.classic", "mnLogging.logback.classic")
            if (logging) {
                if (!moduleVersion.text.contains('micronaut-logging')) {
                    replaceInLines(moduleVersion, "[libraries]", "micronaut-logging = \"${versions['micronaut-logging']}\"\n[libraries]\nmicronaut-logging = { module = \"io.micronaut.logging:micronaut-logging-bom\", version.ref = \"micronaut-logging\" }")
                }
                processGradleSettingsFiles(module, settingsFile -> {
                    if (!settingsFile.text.contains('micronaut-logging')) {
                        replaceInLines(settingsFile, "micronautBuild {", "micronautBuild {\n    importMicronautCatalog(\"micronaut-logging\")\n")
                    }
                })
            }
        })
    }

    private static void removeCoreBranch(File module) {
        module.eachFileRecurse(FILES) { File gradleProperties ->
            if(gradleProperties.name == 'gradle.properties') {
                boolean modified = false
                List<String> modifiedLines = new ArrayList<>()
                gradleProperties.eachLine {line ->
                    if (line.contains('# Micronaut core branch for BOM pull requests') || line.contains('githubCoreBranch=') || line.contains('bomProperty=') || line.contains("bomProperties=")) {
                        modified = true
                    } else {
                        modifiedLines << line
                    }
                }
                if (modified) {
                    gradleProperties.text = String.join("\n", modifiedLines) + '\n'
                }
            }
        }
    }

    private static void processGradleFiles(File module, Consumer<File> gradleFileProcessor) {
        module.eachFileRecurse(FILES) { File gradleFile ->
            if(gradleFile.name.endsWith('.gradle') || gradleFile.name.endsWith('.gradle.kts')) {
                gradleFileProcessor.accept(gradleFile)
            }
        }
    }

    private static void processGradleSettingsFiles(File module, Consumer<File> settingsFileProcessor) {
        module.eachFileRecurse(FILES) { File gradleFile ->
            if(gradleFile.name == 'settings.gradle' || gradleFile.name == 'settings.gradle.kts') {
                settingsFileProcessor.accept(gradleFile)
            }
        }
    }

    private static void removeSnapshotRepository(File gradleFile) {
        removeLines(gradleFile, line -> line.contains('https://s01.oss.sonatype.org/content/repositories/snapshots/') || line.contains('addSnapshotRepository()'))
    }

    private static void removeLines(File f, Function<String, Boolean> shouldRemoveLine) {
        boolean modified = false
        List<String> modifiedLines = new ArrayList<>()
        f.eachLine {line ->
            if (!shouldRemoveLine(line)) {
                modifiedLines << line
            } else {
                modified = true
            }
        }
        if (modified) {
            f.text = String.join("\n", modifiedLines) + '\n'
        }
    }

    private static boolean replaceInLines(File f, String lookingFor, String replacement, boolean replaceWholeLine = false) {
        boolean modified = false
        List<String> modifiedLines = new ArrayList<>()
        f.eachLine {line ->
            if (line.contains(lookingFor)) {
                String[] arr = replacement.split("\n")
                if (arr.length == 1) {
                    if (replaceWholeLine) {
                        modifiedLines << replacement
                    } else {
                        modifiedLines << line.replace(lookingFor, replacement)
                    }

                } else {
                    for (String newLine : arr) {
                        modifiedLines << newLine
                    }
                }
                modified = true
            } else {
                modifiedLines << line
            }
        }
        if (modified) {
            f.text = String.join("\n", modifiedLines) + '\n'
        }
        return modified
    }

    private static void updateModuleVersions(Map<String, String> versions , File moduleVersionFile) {
        List<String> modifiedLines = new ArrayList<>()
        moduleVersionFile.eachLine {l ->
            boolean modified = false
            for (String moduleName : versions.keySet()) {
                String prefix = "$moduleName = \""
                if (l.startsWith(prefix)) {
                    modifiedLines << ("$moduleName = \"${versions[moduleName]}\"" as String)
                    modified = true
                    break
                }
            }
            if (!modified) {
                modifiedLines << l
            }
        }
        moduleVersionFile.text = String.join("\n", modifiedLines) + '\n'
    }

    private static Map<String, String> micronautVersions(File platformVersion) {
        Map<String, String> micronautVersions = new HashMap<>()
        platformVersion.eachLine { l ->
            if (l.startsWith('managed-micronaut-core')) {
                micronautVersions.put('micronaut', l.substring('managed-micronaut-core = '.length()).replaceAll('"', ''))
            } else if (l.startsWith('managed-micronaut')) {
                micronautVersions.put(l.substring('managed-'.length(), l.indexOf(' = ')),
                        l.substring(l.indexOf(' = ') + ' = '.length()).replaceAll('"', ''))
            }
        }
        if (micronautVersions.containsKey("micronaut-serialization")) {
            micronautVersions.put("micronaut-serde", micronautVersions.get("micronaut-serialization"))
        }
        micronautVersions
    }

    private static void removeMicronautRuntime(File gradleFile) {
        removeLines(gradleFile, line -> line.contains('(mn.micronaut.runtime)'))
    }

    private static void removeMicronautInjectJavaImplementation(File gradleFile) {
        removeLines(gradleFile, line -> line.contains('implementation(mn.micronaut.inject.java)'))
    }

    private static void removeMicronautInjectGroovyImplementation(File gradleFile) {
        removeLines(gradleFile, line -> line.contains('implementation(mn.micronaut.inject.groovy)'))
    }
}
