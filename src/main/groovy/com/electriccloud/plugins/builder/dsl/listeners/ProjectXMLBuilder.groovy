package com.electriccloud.plugins.builder.dsl.listeners

import groovy.xml.XmlUtil

class ProjectXMLBuilder implements EventListener {
    Closure callback

    ProjectXMLBuilder(Closure<String> callback) {
        this.callback = callback
    }

    Property currentProperty
    boolean ignoreThis

    Node parent = new Node(null, 'exportedData', [
        buildLabel  : 'build_3.5_30434_OPT_2010.01.13_07:32:22',
        buildVersion: '3.5.1.30434',
        version     : '39']
    )
    Node context = parent

    def startEvent(String name, entityName) {
        if (ignoreThis) {
            return
        }
        if (name == 'aclEntry') {
            ignoreThis = true
            return
        }
        if (name == 'property') {
            if (currentProperty) {
                Property property = new Property(currentProperty)
                currentProperty.addProperty(property)
                currentProperty = property
            } else {
                currentProperty = new Property()
            }
        } else {
            Node node = new Node(context, name)
            context = node
        }
    }

    def attribute(String name, value) {
        if (ignoreThis) {
            return
        }
        if (currentProperty) {
            if (name == 'propertyName')
                currentProperty.propertyName = value
            if (name == 'value')
                currentProperty.value = value
        } else {
            Node node = new Node(context, name, value)
        }
    }

    def endEvent(String name, entityName) {
        if (name == 'aclEntry') {
            ignoreThis = false
            return
        }
        if (name == 'property') {
            if (currentProperty) {
                if (currentProperty.parent) {
                    currentProperty = currentProperty.parent
                }
                else {
                    Node propertySheet = new Node(context, 'propertySheet')
                    if (currentProperty.properties.size() > 0) {
                        for(Property property in currentProperty.properties) {
                            Node propNode = new Node(propertySheet, 'property')
                            Node nameNode = new Node(propNode, 'propertyName', property.propertyName)
                            Node valueNode = new Node(propNode, 'value', property.value)
                        }
                    }
                    else {
                        Node propNode = new Node(propertySheet, 'property')
                        Node nameNode = new Node(propNode, 'propertyName', currentProperty.propertyName)
                        Node valueNode = new Node(propNode, 'value', currentProperty.value)
                    }
                    currentProperty = null
                }
            }
        } else {
            context = context.parent()
        }
    }

    def done() {
        String xml = XmlUtil.serialize(parent)
        this.callback.call(xml)
    }


    class Property {
        String propertyName
        List<Property> properties = []
        String value
        boolean expandable
        Property parent

        def addProperty(Property property) {
            properties.add(property)
        }

        Property() {
        }

        Property(Property parent) {
            this.parent = parent
        }

    }

}
