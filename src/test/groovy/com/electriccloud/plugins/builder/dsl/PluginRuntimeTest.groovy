package com.electriccloud.plugins.builder.dsl

import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.ActualParameter
import com.electriccloud.client.groovy.models.Credential
import com.electriccloud.plugins.builder.PluginMetadata
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.util.concurrent.PollingConditions

@Stepwise
@Requires({TestHelper.environmentVariables()})
class PluginRuntimeTest extends Specification {

    @Shared
    def pluginName = 'SamplePlugin'
    @Shared
    ElectricFlow ef
    @Shared
    def configName
    @Shared
    TestHelper helper = new TestHelper()

    def setupSpec() {
        ef = helper.buildEF()
        def archivePath = helper.buildPlugin(pluginName)
        helper.login()
        helper.uninstallPlugin(pluginName)
        helper.installPlugin(archivePath)
        PluginMetadata meta = helper.getPluginMetadata(pluginName)
        helper.promotePlugin(pluginName + '-' + meta.version)
        configName = 'Test Config'
    }

    def 'run configuration procedure'() {
        when:
        def params = [
            new ActualParameter('config', configName),
            new ActualParameter('credential', 'credential')
        ]
        def credentails = [
            new Credential(userName: 'username', password: 'passw0rd', credentialName: 'credential')
        ]
        def result = ef.runProcedure(
            projectName: "/plugins/$pluginName/project",
            procedureName: 'CreateConfiguration', actualParameters: params,
            credentials: credentails)
        def jobId = result?.jobId
        then:
        pollJob(jobId)
        def outcome = ef.getJobDetails(jobId: jobId)?.job?.outcome
        assert outcome == 'success'
    }

    def 'run sample procedure'() {
        when:
        def details = runProcedure('Sample Procedure', [config: configName, param1: 'value'])
        then:
        def outcome = details?.outcome
        assert outcome == 'success'
        def configuration = ef.getProperty(jobId: details.jobId, propertyName: '/myJob/configuration')?.property?.value
        assert configuration =~ /passw0rd/
    }


    @Ignore("Misconfigured STOMP")
    def 'retrieve dependencies'() {
        when:
        def details = runProcedure('Setup', [:])
        then:
        def outcome = details?.outcome
        assert outcome == 'success'
    }


    def 'upgrade plugin'() {
        when: 'the plugin is reinstalled'
        def archivePath = helper.buildPlugin(pluginName)
        helper.login()
        helper.installPlugin(archivePath)
        PluginMetadata meta = helper.getPluginMetadata(pluginName)
        helper.promotePlugin(pluginName + '-' + meta.version)
        then: 'the previously created configuration must stay in place'
        def oldConfiguration = ef.getProperties(
            path: "/plugins/$pluginName/project/ec_plugin_cfgs/$configName")?.propertySheet
        assert oldConfiguration

        def step = ef.getSteps(
            projectName: "/plugins/$pluginName/project",
            procedureName: 'Sample Procedure',
            stepName: 'sample step'
        )?.step
        println step
        def attachedCredentials = step.attachedCredentials
        assert attachedCredentials.size() == 1
    }

    def pollJob(jobId) {
        def conditions = new PollingConditions(timeout: 20, initialDelay: 1.5, factor: 1.25)
        conditions.eventually {
            assert ef.getJobStatus(jobId: jobId)?.status == 'completed'
        }
        return true
    }


    def runProcedure(procedureName, p) {
        def params = []
        p.each {k, v ->
            params << new ActualParameter(k, v)
        }
        def result = ef.runProcedure(
            projectName: "/plugins/$pluginName/project",
            procedureName: procedureName,
            actualParameters: params
        )
        def jobId = result?.jobId
        pollJob(jobId)
        return ef.getJobDetails(jobId: jobId)?.job
    }
}
