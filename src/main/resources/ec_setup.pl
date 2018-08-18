use strict;
use warnings;
use ElectricCommander;

#TODO picker
#TODO steps with credentials
#TODO acls


#    commander => $commander,
#    pluginName => $pluginName,
#    otherPluginName => $otherPluginName,
#    upgradeAction => $upgradeAction,
#    promoteAction => $promoteAction,

my @objectTypes = qw(resources workspaces projects);
for my $objectType (@objectTypes) {
    $commander->createAclEntry({
        principalType             => 'user',
        principalName             => "project: $pluginName",
        systemObjectName          => $objectType,
        objectType                => 'systemObject',
        readPrivilege             => 'allow',
        modifyPrivileg            => 'allow',
        executePrivilege          => 'allow',
        changePermissionPrivilege => 'allow'
    })
}




#dsl/procedures/createConfiguration/steps/createAndAttachCredential.pl:my $stepsJSON = $ec->getPropertyValue("/projects/$projName/procedures/CreateConfiguration/ec_stepsWithAttachedCredentials");
#