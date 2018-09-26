package com.electriccloud.plugins.builder

import com.electriccloud.plugins.builder.domain.Project
import com.electriccloud.plugins.builder.domain.Property
import com.electriccloud.plugins.builder.dsl.DSLReader
import com.electriccloud.plugins.builder.dsl.listeners.EventListener
import com.electriccloud.plugins.builder.dsl.listeners.ProjectBuilder
import groovy.util.logging.Slf4j

@Slf4j
class PluginBuilder {
    File pluginFolder
    String buildNumber
    static final String METAFILE_PATH = 'META-INF/plugin.xml'
    static final String PROJECT_XML_PATH = 'META-INF/project.xml'
    String version
    String pluginKey

    PluginBuilder(File pluginFolder) {
        this.pluginFolder = pluginFolder
    }

    def build() {
        File metadataFile = new File(pluginFolder, METAFILE_PATH)
        PluginMetadata metadata = new PluginMetadata(metadataFile)
        String pluginKey = metadata.key
        String version = metadata.version
        File output = new File(pluginFolder, "build/${pluginKey}.zip")

//        project xml
        DSLReader reader = new DSLReader(pluginFolder, metadata.key, metadata.version)
        this.version = version
        this.pluginKey = pluginKey
        String projectXml
        String category = metadata.category
        def callback = { Project project ->
//            Will be used later in ec_setup
            Property categoryProperty = new Property(project)
            categoryProperty.name = 'ec_pluginCategory'
            categoryProperty.value = category
            project.addProperty(categoryProperty)

//            ec_setup
            String ecSetup = generateECSetup(metadata)
            project.properties.find { it.name == 'ec_setup' }.value = ecSetup;

            projectXml = new ProjectXMLGenerator(project).generateXml()
        }
        EventListener projectBuilder = new ProjectBuilder(callback)
        reader.process([projectBuilder])
        assert projectXml: "Project xml is empty"

        projectXml = insertPlaceholders(projectXml, metadata)
        new File('/tmp/project.xml').write(projectXml)

        ArchiveBuilder builder = new ArchiveBuilder()
        builder.addItem(PROJECT_XML_PATH, projectXml)

//        plugin.xml
        String pluginXml = metadataFile.text
        pluginXml = pluginXml.replaceAll(/<version>.+?<\/version>/, "<version>${metadata.version}</version>")
        builder.addItem(METAFILE_PATH, pluginXml)

//        TODO other files
//        TODO binaries

        builder.pack(output)
        log.info "Archive ${output.absolutePath} has been created"
    }

    def insertPlaceholders(projectXml, metadata) {
        String pluginName = metadata.key + '-' + metadata.version
        String retval = projectXml
            .replaceAll(/@PLUGIN_NAME@/, pluginName)
            .replaceAll(/@PLUGIN_KEY@/, metadata.key)
            .replaceAll(/@PLUGIN_VERSION@/, metadata.version)
        return retval
    }

    def generateECSetup(metadata) {
//        TODO concat
        String ecSetupCommon = new File(getClass().getResource("/ec_setup.pl").toURI()).text
        ecSetupCommon = insertPlaceholders(ecSetupCommon, metadata)
        return ecSetupCommon
    }


}
