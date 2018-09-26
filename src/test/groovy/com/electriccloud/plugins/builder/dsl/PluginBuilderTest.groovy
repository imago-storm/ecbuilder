package com.electriccloud.plugins.builder.dsl

import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.plugins.builder.PluginBuilder
import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

@Slf4j
class PluginBuilderTest extends Specification {

    @Unroll
    def 'building plugin #plugin'() {
        given:
        def pluginName = new File(plugin).name
        when:
        PluginBuilder builder = new PluginBuilder(new File(plugin))
        builder.build()
        then:
        File destinationArchive = new File(plugin, "build/${pluginName}.zip")
        assert destinationArchive.exists() : "Plugin archive ${pluginName} does not exist"
        installPlugin(destinationArchive, builder.pluginKey, builder.version)
        validatePlugin("${builder.pluginKey}-${builder.version}")
        where:
        plugin << TestHelper.getPlugins()
    }

    def installPlugin(File archive, pluginKey, pluginVersion) {
        String username = System.getenv('COMMANDER_USERNAME') ?: 'admin'
        String password = System.getenv('COMMANDER_PASSWORD') ?: 'changeme'
        runCommand("ectool --server $commanderServer login  $username $password")
        runCommand("ectool --server $commanderServer installPlugin ${archive.absolutePath}")
//        runCommand("ectool --server $commanderServer promotePlugin  ${pluginKey}-${pluginVersion} ")
    }


    def runCommand(command) {
        def stdout = new StringBuilder()
        def stderr = new StringBuilder()
        def process = command.execute()
        process.consumeProcessOutput(stdout, stderr)
        process.waitForOrKill(20 * 1000)
        println "STDOUT: $stdout"
        println "STDERR: $stderr"
        println "Exit Code: ${process.exitValue()}"
        def text = "$stdout\n$stderr"
        assert process.exitValue() == 0
        text
    }


    def validatePlugin(pluginName) {
        ElectricFlow ef = new ElectricFlow()
        ef.login(commanderServer, 'admin', 'changeme')
        def procedures = ef.getProcedures(projectName: "/projects/$pluginName")?.procedure
        def found = false
        for (def procedure : procedures) {
            assert procedure.procedureName
            assert procedure.description
            found = true
            def properties = ef.getProperties(projectName: pluginName, procedureName: procedure.procedureName,  expand: false)?.propertySheet?.property
            println properties
            assert properties.any { it.propertyName == 'ec_customEditorData' }
            assert properties.any { it.propertyName == 'ec_parameterForm' }

            def formalParameters = ef.getFormalParameters(projectName:  pluginName, procedureName: procedure.procedureName)?.formalParameter
            for (def param : formalParameters) {
                println "Formal parameter ${param.formalParameterName}"
                assert param.formalParameterName
                assert param.description
                assert param.type
            }
            def steps = ef.getSteps(projectName: pluginName, procedureName: procedure.procedureName)?.step
            for(def step : steps) {
                println "Step ${step.stepName}"
                assert step.command || step.subprocedure
            }
        }
        assert found : "no procedures were found in $pluginName"
        return true
    }

    def getCommanderServer() {
        String commanderServer = System.getenv('COMMANDER_SERVER')
        assert commanderServer : "COMMANDER_SERVER environment variable is not provided"
        return commanderServer
    }


}
