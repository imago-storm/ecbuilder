package com.electriccloud.plugins.builder.dsl

import com.electriccloud.plugins.builder.dsl.listeners.EventListener
import com.electriccloud.plugins.builder.exceptions.PromoteDSLDoesNotExistException
import org.codehaus.groovy.control.CompilerConfiguration

class DSLReader {

    String pluginKey
    String version
    String pluginFolder

    DSLReader(pluginFolder, pluginKey, version) {
        this.pluginFolder = pluginFolder
        this.pluginKey = pluginKey
        this.version = version
    }

//    TODO demote
    List<EventListener> process(List<EventListener> listeners) {
        File promoteFile = new File(pluginFolder, 'dsl/promote.groovy')
        if (!promoteFile.exists()) {
            throw new PromoteDSLDoesNotExistException()
        }
        String promoteDsl = promoteFile.text
        promoteDsl = promoteDsl
            .replaceAll('import com.electriccloud.commander.dsl.util.BasePlugin', '')
            .replaceAll('@BaseScript BasePlugin baseScript', '')
            .replaceAll(/getProperty/, 'getProperty_1')

        CompilerConfiguration cc = new CompilerConfiguration()
        cc.setScriptBaseClass(DelegatingScript.class.getName())

        Binding binding = new Binding([args: [
            pluginName     : "${pluginKey}-$version",
            upgradeAction  : 'upgrade',
            otherPluginName: "$pluginKey-$version-copy"
        ], pluginDir                       : pluginFolder])
        GroovyShell sh = new GroovyShell(this.class.classLoader, binding, cc)
        DelegatingScript script = (DelegatingScript) sh.parse(promoteDsl)
        DSLEvaluator evaluator = new DSLEvaluator(pluginFolder, pluginKey, version, listeners)
        script.setDelegate(evaluator)
        script.run()
        evaluator.done()
        return listeners
    }

}
