package com.electriccloud.plugins.builder.dsl.listeners

interface EventListener {
    def startEvent(String name)

    def attribute(String name, value)

    def endEvent(String name)

    def done()
}