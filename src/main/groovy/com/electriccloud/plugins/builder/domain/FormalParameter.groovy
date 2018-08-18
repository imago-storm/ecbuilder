package com.electriccloud.plugins.builder.domain

import com.electriccloud.plugins.builder.exceptions.InvalidHierarchyException

class FormalParameter extends EFEntity {
    Procedure parent
    FormalParameter(procedure) {
        parent = procedure
    }

    def addAttribute(String name, value) {
        if (name == 'formalParameterName') {
            this.name = value
        }
        else {
            super.addAttribute(name, value)
        }
    }

    def addProperty(Property property) {
        throw new InvalidHierarchyException("Formal parameter cannot contain properties: formalParameterName is ${this.name}, property name is ${property.name}")
    }
}
