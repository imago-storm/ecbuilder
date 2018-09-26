package com.electriccloud.plugins.builder

import com.electriccloud.plugins.builder.domain.Project
import com.electriccloud.plugins.builder.dsl.DSLReader
import com.electriccloud.plugins.builder.dsl.listeners.EventListener
import com.electriccloud.plugins.builder.dsl.listeners.ProjectBuilder

import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ArchiveBuilder {

    List<Item> items = []

    def addItem(String itemName, String item) {
        Item it = Item.newInstance(this, itemName, item, null)
        items.add(it)
    }

    def addItem(String itemName, File item) {
        Item it = Item.newInstance(this, itemName, null, item)
        items.add(it)
    }

    def pack(File destination) {
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(destination.absolutePath))

        for(Item item in items) {
            zip.putNextEntry(new ZipEntry(item.name))
            item.writeInto(zip)
            zip.closeEntry()
        }
        zip.close()
    }

    def __pack(File destination) {
        File pluginXml = new File(pluginFolder, 'META-INF/plugin.xml')
        DSLReader reader = new DSLReader(pluginFolder, metadata.key, metadata.version)
        String projectXml
        def callback = { Project project ->
            projectXml = new ProjectXMLGenerator(project).generateXml()
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

    class Item {
        String name
        String content
        File file

        Item(name, content = null, file = null) {
            this.name = name
            this.content = content
            this.file = file
        }

        def writeInto(OutputStream out) {
            if (file) {
                def buffer = new byte[file.size()]
                file.withInputStream {
                    out.write(buffer, 0, it.read(buffer))
                }
                return
            }
            if (content) {
                byte[] bytes = content.getBytes(Charset.forName('UTF-8'))
                out.write(bytes, 0, bytes.length)
                return
            }
            throw new RuntimeException("Empty item: neither contnet nor file is provided")
        }

    }

}
