package com.electriccloud.plugins.builder.domain

class Project extends EFEntity {
    List<Procedure> procedures = []

    def addChild(Procedure procedure) {
        procedures.add(procedure)
    }
}
