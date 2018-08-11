package com.electriccloud.plugins.builder.domain

class Project extends EFEntity {
    List<Procedure> procedures = []

    def addAttribute(String name, value) {
        if (name == 'projectName') {
            this.name = value
        }
        else {
            super.addAttribute(name, value)
        }
    }
    def addChild(Procedure procedure) {
        procedures.add(procedure)
    }
}
