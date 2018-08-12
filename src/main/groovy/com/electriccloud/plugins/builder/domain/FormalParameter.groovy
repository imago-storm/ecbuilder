package com.electriccloud.plugins.builder.domain

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
        throw new RuntimeException("Formal parameter cannot contain properties")
    }
}
