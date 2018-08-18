package com.electriccloud.plugins.builder.dsl

import com.electriccloud.plugins.builder.PluginBuilder
import groovy.util.logging.Slf4j
import spock.lang.Specification
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
//        TODO assert installation
        where:
        plugin << TestHelper.getPlugins()
    }



}
