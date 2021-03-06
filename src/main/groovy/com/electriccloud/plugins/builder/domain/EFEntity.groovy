package com.electriccloud.plugins.builder.domain

import sun.reflect.generics.reflectiveObjects.NotImplementedException

class EFEntity {
    String name
    String description
    Map attributes = [:]
    List<Property> properties = []
    EFEntity parent

    def addAttribute(String name, value) {
        if (name == 'description') {
            description = value
        } else {
            this.attributes[name] = value
        }
    }

    def addProperty(Property property) {
        if (this.properties.find { it.name == property.name }) {
            println "Property already found in list"
        } else {
            this.properties.add(property)
        }
    }

    def listProperties() {
        return this.properties
    }

    def addChild(Property property) {
        addProperty(property)
    }

    def findChild(Class className, String name) {
        if (Property == className) {
            return this.properties.find { it.name == name }
        }
        throw new NotImplementedException()
    }


    def addChild(EFEntity child) {
        throw new RuntimeException("Cannot add ${child.class} to ${this.class}")
    }
}
