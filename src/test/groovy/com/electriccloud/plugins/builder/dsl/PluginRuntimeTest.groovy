package com.electriccloud.plugins.builder.dsl

import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.ActualParameter
import com.electriccloud.client.groovy.models.Credential
import com.electriccloud.plugins.builder.PluginMetadata
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.util.concurrent.PollingConditions

@Stepwise
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
        def params = [
            new ActualParameter('config', configName),
            new ActualParameter('param1', 'value')
        ]
        def result = ef.runProcedure(
            projectName: "/plugins/$pluginName/project",
            procedureName: 'Sample Procedure',
            actualParameters: params
        )
        def jobId = result?.jobId
        then:
        pollJob(jobId)
        def outcome = ef.getJobDetails(jobId: jobId)?.job?.outcome
        assert outcome == 'success'
        def configuration = ef.getProperty(jobId: jobId, propertyName: '/myJob/configuration')?.property?.value
        assert configuration =~ /passw0rd/
    }

    def pollJob(jobId) {
        def conditions = new PollingConditions(timeout: 20, initialDelay: 1.5, factor: 1.25)
        conditions.eventually {
            assert ef.getJobStatus(jobId: jobId)?.status == 'completed'
        }
        return true
    }

}
