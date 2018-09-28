package com.electriccloud.plugins.builder.dsl

import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.plugins.builder.PluginBuilder
import groovy.util.logging.Slf4j
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

@Slf4j
@Stepwise
@Requires({TestHelper.environmentVariables()})
class PluginBuilderTest extends Specification {

    @Shared
    def pluginName = 'SamplePlugin'
    @Shared
    def destinationArchive
    @Shared
    def pluginKey
    @Shared
    def pluginVersion
    @Shared
    ElectricFlow ef
    @Shared
    TestHelper helper = new TestHelper()

    def setupSpec() {
        ef = new ElectricFlow()
        ef.login(helper.commanderServer, 'admin', 'changeme')
        helper.login()
    }

    @Unroll
    def 'building sample plugin'() {
        given:
        File plugin = new File(this.class.getResource("/$pluginName").toURI())
        destinationArchive = new File(plugin, "build/${pluginName}.zip")
        when:
        PluginBuilder builder = new PluginBuilder(plugin)
        builder.build()
        pluginKey = builder.pluginKey
        pluginVersion = builder.version
        then:
        assert destinationArchive.exists(): "Plugin archive ${pluginName} does not exist"
    }

    def 'installing plugin'() {
        when: 'installation runs'

        helper.installPlugin(destinationArchive)
        then: 'the plugin project should be on server'
        def plugin = ef.getProject(projectName: "$pluginKey-$pluginVersion")?.project
        assert plugin
    }

    def 'promoting plugin'() {
        when: 'promotion runs'
        helper.promotePlugin("$pluginKey-$pluginVersion")
        then:
        def plugin = ef.getPlugin(pluginName: pluginKey)?.plugin
        assert plugin.promoted
        validatePlugin("$pluginKey-$pluginVersion")
    }

//    TODO check picker steps
//    TODO check dependencies load


    def validatePlugin(pluginName) {
        def procedures = ef.getProcedures(projectName: "/projects/$pluginName")?.procedure
        def sampleProcedure = procedures.find { it.procedureName == 'Sample Procedure' }

        assert sampleProcedure: 'sample procedure is not found'
        assert !(procedures.find { it.procedureName == 'groovyProcedureTemplate' }): 'found ignored procedure'

        def procedureName = sampleProcedure.procedureName

        assert sampleProcedure.description
        def steps = ef.getSteps(projectName: pluginName, procedureName: procedureName)?.step
        assert steps
        assert steps.size() == 1
        assert steps[0].command
        assert steps[0].shell

        String ecSetup = ef.getProperty(projectName: pluginName, propertyName: 'ec_setup')
        println ecSetup
        assert ecSetup =~ /Auto-generated part begins/
        assert ecSetup =~ /Some additional logic/

        def parameters = ef.getFormalParameters(projectName: pluginName, procedureName: procedureName)?.formalParameter
        assert parameters.find { it.formalParameterName == 'config' }
        assert parameters.find { it.formalParameterName == 'param1' }

        def parameterForm = ef.getProperty(projectName: pluginName, procedureName: procedureName, propertyName: 'ec_parameterForm')?.property
        assert parameterForm.value

        def customEditorData = ef.getProperties(
            projectName: pluginName,
            procedureName: procedureName,
            path: 'ec_customEditorData'
        )?.propertySheet
        assert customEditorData

        return true
    }


}
