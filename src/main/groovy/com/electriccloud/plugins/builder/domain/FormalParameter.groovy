package com.electriccloud.plugins.builder.domain

class FormalParameter extends EFEntity {
    Procedure parent
    FormalParameter(procedure) {
        parent = procedure
    }

    def addProperty(Property property) {
        throw new RuntimeException("Formal parameter cannot contain properties")
    }
}
