package com.electriccloud.plugins.builder

import com.electriccloud.plugins.builder.domain.Project
import com.electriccloud.plugins.builder.dsl.DSLReader
import com.electriccloud.plugins.builder.dsl.listeners.EventListener
import com.electriccloud.plugins.builder.dsl.listeners.ProjectBuilder

import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PluginArchive {
    File pluginFolder

    PluginArchive(File pluginFolder) {
        this.pluginFolder = pluginFolder
    }

    def pack(File destination) {
        File pluginXml = new File(pluginFolder, 'META-INF/plugin.xml')
        def metaData = new XmlSlurper().parse(pluginXml)
        DSLReader reader = new DSLReader(pluginFolder, metaData.key, metaData.version)
        String projectXml
        def callback = { Project project ->
            projectXml = new ProjectToXML(project).generateXml()
        }
        EventListener projectBuilder = new ProjectBuilder(callback)
        reader.process([projectBuilder])


        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(destination.absolutePath))
        zip.putNextEntry(new ZipEntry("META-INF/project.xml"))
        byte[] bytes = projectXml.getBytes(Charset.forName('UTF-8'))
        zip.write(bytes, 0, bytes.length)
        zip.closeEntry()

        zip.putNextEntry(new ZipEntry("META-INF/plugin.xml"))
        def buffer = new byte[pluginXml.size()]
        pluginXml.withInputStream {
            zip.write(buffer, 0, it.read(buffer))
        }
        zip.closeEntry()

        zip.close()
    }

}
