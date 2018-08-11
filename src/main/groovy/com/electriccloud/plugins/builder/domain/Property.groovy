package com.electriccloud.plugins.builder.domain

class Property extends EFEntity{
    String value

    Property(EFEntity parent) {
        this.parent = parent
    }

    def addAttribute(String name, value){
        if (name == 'propertyName') {
            this.name = value
        }
        else if (name == 'value') {
            this.value = value
            if (this.properties.size() > 1) {
                throw new RuntimeException("Property can be either a property or property sheet ${name}, ${value}")
            }
        }
        super.addAttribute(name, value)
    }

    def addChild(Property property) {
        if (this.value) {
            throw new RuntimeException("Property can either be a property or property sheet: ${property.name}")
        }
        super.addProperty(property)
    }
}
