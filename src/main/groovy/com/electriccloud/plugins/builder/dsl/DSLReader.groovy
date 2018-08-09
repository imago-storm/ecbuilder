package com.electriccloud.plugins.builder.dsl

import com.electriccloud.plugins.builder.dsl.listeners.EventListener
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

    List<EventListener> process(List<EventListener> listeners) {
        String promoteDsl = new File(pluginFolder, "dsl/promote.groovy").text
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
