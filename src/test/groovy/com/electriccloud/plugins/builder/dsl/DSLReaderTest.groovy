package com.electriccloud.plugins.builder.dsl

import com.electriccloud.plugins.builder.ProjectToXML
import com.electriccloud.plugins.builder.domain.Project
import com.electriccloud.plugins.builder.dsl.listeners.ProjectBuilder
import com.electriccloud.plugins.builder.dsl.listeners.ProjectXMLBuilder
import com.electriccloud.plugins.builder.dsl.listeners.SampleListener
import com.electriccloud.plugins.builder.dsl.listeners.EventListener
import spock.lang.Specification

class DSLReaderTest extends Specification {
    def "read plugin"() {
        when:
        def pluginFolder = '/Users/imago/Documents/ecloud/plugins/containers/EC-OpenShift'
        DSLReader reader = new DSLReader(pluginFolder, "EC-OpenShift", "1.4.0")
        EventListener xml = new ProjectBuilder({ Project project ->
            String xml = new ProjectToXML(project).generateXml()
            new File('/tmp/project.xml').write(xml)
        })
        List listeners = []
        listeners.add(xml)
        reader.process(listeners)
        then:
        assert xml
    }

    def 'translate to xml'() {
        when:
        def pluginFolder = '/Users/imago/Documents/ecloud/plugins/containers/EC-OpenShift'
        DSLReader reader = new DSLReader(pluginFolder, "EC-OpenShift", "1.4.0")
        EventListener xml = new ProjectXMLBuilder({ xml ->
            new File('/tmp/project.xml').write(xml)
        })
        List listeners = []
        listeners.add(xml)
        reader.process(listeners)
        then:
        assert xml

    }
}
