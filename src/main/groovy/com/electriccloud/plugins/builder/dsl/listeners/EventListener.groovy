package com.electriccloud.plugins.builder.dsl.listeners

interface EventListener {
    def startEvent(String name, entityName)

    def attribute(String name, value)

    def endEvent(String name, entityName)

    def done()
}