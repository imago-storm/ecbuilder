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
        }
        super.addAttribute(name, value)
    }

    def addChild(Property property) {
        super.addProperty(property)
    }
}
