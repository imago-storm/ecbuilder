package com.electriccloud.plugins.builder.dsl

import com.electriccloud.plugins.builder.PluginArchive
import spock.lang.Specification

class PluginArchiveTest extends Specification {
    def 'pack'() {
        when:
        PluginArchive archive = new PluginArchive(new File('/Users/imago/Documents/ecloud/plugins/EC-Nothing'))
        archive.pack(new File('/tmp/EC-Nothing.zip'))
        then:
        assert true
    }
}
