package com.electriccloud.plugins.builder.dsl.listeners

class SampleListener implements EventListener {
    int indent = 0

    def startEvent(String name) {
        indent++
        println "${' '.multiply(indent)}Event started: $name"
    }

    def attribute(String name, value) {
        println "${' '.multiply(indent + 1)}Attribute $name, $value"
    }

    def endEvent(String name) {
        println "${' '.multiply(indent)}End event $name"
        indent--
    }

    def done() {
        println "Done"
    }
}