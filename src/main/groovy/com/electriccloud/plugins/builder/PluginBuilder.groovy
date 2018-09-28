package com.electriccloud.plugins.builder

import com.electriccloud.plugins.builder.domain.Project
import com.electriccloud.plugins.builder.domain.Property
import com.electriccloud.plugins.builder.dsl.DSLReader
import com.electriccloud.plugins.builder.dsl.listeners.EventListener
import com.electriccloud.plugins.builder.dsl.listeners.ProjectBuilder
import groovy.util.logging.Slf4j

@Slf4j
class PluginBuilder {
    static final String METAFILE_PATH = 'META-INF/plugin.xml'
    static final String PROJECT_XML_PATH = 'META-INF/project.xml'
    static final String BUILD_FOLDER = "build"
    static final String EC_SETUP = 'ec_setup.pl'
    static final String EC_SETUP_APPEND = 'ec_setup_append.pl'

    File pluginFolder
    String buildNumber
    String version
    String pluginKey
    boolean dependenciesIntoProperties

    @Lazy(soft = true)
    List<File> folders = {
        return pluginFolder.listFiles().findAll {
            it.isDirectory() && !(it.name in ['META-INF', 'dsl', 'build', 'specs'])
        }
    }()

    PluginBuilder(File pluginFolder) {
        this.pluginFolder = pluginFolder
    }

    String generateProjectXml(metadata) {
        // project xml
        DSLReader reader = new DSLReader(pluginFolder, metadata.key, metadata.version)
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
            Property ecSetupProperty = new Property(project)
            ecSetupProperty.addAttribute('propertyName', 'ec_setup')
            ecSetupProperty.addAttribute('value', ecSetup)
            project.addProperty(ecSetupProperty)

            if (this.dependenciesIntoProperties) {
                project = packDependenciesIntoProperties(project)
            }
            projectXml = new ProjectXMLGenerator(project).generateXml()
        }
        EventListener projectBuilder = new ProjectBuilder(callback)
        reader.process([projectBuilder])
        assert projectXml: "Project xml is empty"
        return projectXml
    }


    def readPluginMetadata() {
        File metadataFile = new File(pluginFolder, METAFILE_PATH)
        assert metadataFile.exists(): "no metadata file is found at ${metadataFile.absolutePath}"
        PluginMetadata metadata = new PluginMetadata(metadataFile)
        return metadata
    }

    def build() {
        PluginMetadata metadata = readPluginMetadata()
        if (!this.version) {
            this.version = metadata.version
        }
        this.pluginKey = metadata.key
        log.info("Plugin Key: $pluginKey")
        log.info("Plugin Version: $version")

        File buildFolder = new File(pluginFolder, BUILD_FOLDER)
        if (buildFolder.exists()) {
            buildFolder.delete()
            log.info("Cleaned build folder $buildFolder")
        }
        buildFolder.mkdir()
        File output = new File(pluginFolder, BUILD_FOLDER + "/${pluginKey}.zip")

        String projectXml = generateProjectXml(metadata)
        projectXml = insertPlaceholders(projectXml, metadata)

        ArchiveBuilder builder = new ArchiveBuilder()
        builder.addItem(PROJECT_XML_PATH, projectXml)

//        plugin.xml
        File metadataFile = new File(pluginFolder, METAFILE_PATH)
        String pluginXml = metadataFile.text
        pluginXml = pluginXml.replaceAll(/<version>.+?<\/version>/, "<version>${this.version}</version>")
        builder.addItem(METAFILE_PATH, pluginXml)

        folders.each { File folder ->
            builder.addFolder(folder.name, folder)
        }

        builder.pack(output)
        log.info "Archive ${output.absolutePath} has been created"
        return output
    }

    def insertPlaceholders(projectXml, metadata) {
        String pluginName = metadata.key + '-' + metadata.version
        String retval = projectXml
            .replaceAll(/@PLUGIN_NAME@/, pluginName)
            .replaceAll(/@PLUGIN_KEY@/, metadata.key)
            .replaceAll(/@PLUGIN_VERSION@/, this.version)
        return retval
    }

    def generateECSetup(metadata) {
        String ecSetupCommon = new File(getClass().getResource("/" + EC_SETUP).toURI()).text
        ecSetupCommon = insertPlaceholders(ecSetupCommon, metadata)
        def append = new File(pluginFolder, EC_SETUP_APPEND)
        if (append.exists()) {
            ecSetupCommon += "\n" + append.text
        }
        return ecSetupCommon
    }

    def packDependenciesIntoProperties(Project project) {
        return project
    }

}
