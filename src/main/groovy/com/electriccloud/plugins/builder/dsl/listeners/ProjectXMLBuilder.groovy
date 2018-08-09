package com.electriccloud.plugins.builder.dsl.listeners

import groovy.xml.XmlUtil

class ProjectXMLBuilder implements EventListener {
    Closure callback
    ProjectXMLBuilder(Closure callback) {
        this.callback = callback
    }

    String current

    Node parent = new Node(null, 'exportedData', [
        buildLabel  : 'build_3.5_30434_OPT_2010.01.13_07:32:22',
        buildVersion: '3.5.1.30434',
        version     : '39']
    )
    Node context = parent

    def startEvent(String name) {
        Node node = new Node(context, name)
        context = node
    }

    def attribute(String name, value) {
        if (value instanceof String) {
            if (context.find {it.name() == name && it.value() == value} ) {
                println "Found duplicate node"
            }
           new Node(context, name, value)
        } else {
            println "Attribute $name, $value"
        }
    }

    def endEvent(String name) {
        context = context.parent()
    }

    def done() {
        String xml = XmlUtil.serialize(parent)
        this.callback.call(xml)
    }

}
