package com.electriccloud.plugins.builder.dsl

import com.electriccloud.plugins.builder.PluginMetadata
import com.electriccloud.plugins.builder.ProjectXMLGenerator
import com.electriccloud.plugins.builder.domain.Project
import com.electriccloud.plugins.builder.dsl.listeners.ProjectBuilder
import com.electriccloud.plugins.builder.dsl.listeners.ProjectXMLBuilder

import com.electriccloud.plugins.builder.dsl.listeners.EventListener
import com.electriccloud.plugins.builder.exceptions.UnsupportedDSLException
import spock.lang.Specification
import spock.lang.Unroll

class DSLReaderTest extends Specification {
//    def "generate project for #pluginName"() {
//        when:
////        def pluginFolder = '/Users/imago/Documents/ecloud/plugins/EC-Nothing'
//        def pluginFolder = '/Users/imago/Documents/ecloud/plugins/containers/EC-Kubernetes'
//        DSLReader reader = new DSLReader(pluginFolder, "EC-Nothing", "1.4.0")
//        EventListener xml = new ProjectBuilder({ Project project ->
//            String xml = new ProjectXMLGenerator(project).generateXml()
//            new File('/tmp/project.xml').write(xml)
//        })
//        List listeners = []
//        listeners.add(xml)
//        reader.process(listeners)
//        then:
//        assert xml
//    }


    @Unroll
    def 'generate project for #plugin'() {
        given:
        def pluginName = new File(plugin).name
        when:
        PluginMetadata metadata = new PluginMetadata(new File(plugin, 'META-INF/plugin.xml'))
        assert metadata.version
        assert metadata.key
        DSLReader reader = new DSLReader(new File(plugin), metadata.key , metadata.version)
        Project project
        EventListener listener = new ProjectBuilder({ Project proj ->
            project = proj
        })
        List listeners = []
        listeners.add(listener)
        boolean unsupportedDsl = false
        try {
            reader.process(listeners)
        } catch (UnsupportedDSLException e) {
            unsupportedDsl = true
        }
        then:
        if (!unsupportedDsl) {
            checkProject(project)
        }
        else {
            assert true
        }
        where:
        plugin << TestHelper.getPlugins()
    }

    def checkProject(Project project) {
//        No doubles
        assert project.name
        project.procedures.each { procedure ->
            def doubles = project.procedures.findAll { it.name == procedure.name}
            assert doubles.size() == 1 : "procedure ${procedure.name} has doubles"
            assert procedure.name : "procedure does not have name"
            assert procedure.steps.size() >= 1 : "procedure ${procedure.name} does not have steps"

            procedure.steps.each { step ->
                assert step.name : "step does not have a name"
            }

            procedure.formalParameters.each { formalParameter ->
                assert formalParameter.name : "formal parameter does not have a name"
            }
        }
//        TODO properties
    }
}
