#Auto-generated part begins
######################################
#Do not touch anything below this line

use strict;
use warnings;
use ElectricCommander;
use JSON qw(decode_json);
use subs qw(debug);

my $category = $commander->getProperty('ec_pluginCategory')->findvalue('//value') || 'Utilities';
my $stepsWithCredentials = getStepsWithCredentials();

#TODO demote logic

my @logs = ();
sub debug($) {
    my ($message) = @_;
    push @logs, $message;
}


for my $procedure ($commander->getProcedures({ projectName => '@PLUGIN_NAME@' })->findnodes('//procedure')) {
    my $procedureName = $procedure->findvalue('procedureName')->string_value;
    my $description = $procedure->findvalue('description')->string_value;
    if (shouldAddStepPicker($procedureName)) {
        debug "Adding step picker to $procedureName";
        addStepPicker($procedureName, $description, $category);
    }
}

if ($upgradeAction eq 'upgrade') {
    migrateConfigurations();
    #    migrateProperties();
#    TODO
}

my $nowString = localtime;
$commander->setProperty("/plugins/$pluginName/project/logs/$nowString", {value => join("\n", @logs)});


sub migrateConfigurations {
#    TODO take from somewhere
    my $configName = 'ec_plugin_cfgs';

    $commander->clone({
        path => "/plugins/$otherPluginName/project/$configName",
        cloneName => "/plugins/$pluginName/project/$configName"
    });

    my $xpath = $commander->getCredentials("/plugins/$otherPluginName/project");
    for my $credential ($xpath->findnodes('//credential')) {
        my $credName = $credential->findvalue('credentialName')->string_value;
        debug "Migrating credential $credName";
        $commander->clone({
            path      => "/plugins/$otherPluginName/project/credentials/$credName",
            cloneName => "/plugins/$pluginName/project/credentials/$credName"
        });
        $commander->deleteAclEntry({
            principalName  => "project: $otherPluginName",
            projectName    => $pluginName,
            credentialName => $credName,
        });
        $commander->deleteAclEntry({
            principalType  => 'user',
            principalName  => "project: $pluginName",
            credentialName => $credName,
        });

        $commander->createAclEntry({
            principalType             => 'user',
            principalName             => "project: $pluginName",
            projectName               => $pluginName,
            credentialName            => $credName,
            objectType                => 'credential',
            readPrivilege             => 'allow',
            modifyPrivilege           => 'allow',
            executePrivilege          => 'allow',
            changePermissionPrivilege => 'allow'
        });

        for my $step (@$stepsWithCredentials) {
            $commander->attachCredential({
                projectName    => $pluginName,
                procedureName  => $step->{procedureName},
                stepName       => $step->{stepName},
                credentialName => $credName,
            });
            debug "Attached credential to $step->{stepName}";
        }
    }

}


sub getStepsWithCredentials {
    my $retval = [];
    eval {
        my $pluginName = '@PLUGIN_NAME@';
        my $stepsJson = $commander->getProperty("/projects/$pluginName/procedures/CreateConfiguration/ec_stepsWithAttachedCredentials");
        $retval = decode_json($stepsJson);
    };
    return $retval;
}


#-#    commander => $commander,
#    -#    pluginName => $pluginName,
#        -#    otherPluginName => $otherPluginName,
#            -#    upgradeAction => $upgradeAction,
#                -#    promoteAction => $promoteAction,


#TODO demote
sub addStepPicker {
    my ($procedureName, $description, $category) = @_;

    my $pickerDescription = descriptionForStepPicker($procedureName);
    $pickerDescription ||= $description;
    $pickerDescription ||= $procedureName;

    my $label = '@PLUGIN_KEY@ - ' . $procedureName;
    $batch->deleteProperty("/server/ec_customEditors/pickerStep/$label");
    push @::createStepPickerSteps, {
        label       => $label,
        procedure   => $procedureName,
        description => $pickerDescription,
        category    => $category,
    };
    debug "Added step picker $label";
}


sub descriptionForStepPicker {
    my ($procedureName) = @_;
    my $pluginName = '@PLUGIN_NAME@';
    my $description = '';
    eval {
        $description = $commander->getProperty("/projects/$pluginName/procedures/$procedureName/stepPickerDescription")->findvalue('//value')->string_value;
    };
    return $description;
}

sub shouldAddStepPicker {
    my ($procedureName) = @_;
    if ($procedureName =~ /CreateConfiguration|DeleteConfiguration|EditConfiguration/) {
        return 0;
    }
    my $shouldAdd = 1;
    eval {
        my $value = $commander->getProperty("/projects/@PLUGIN_NAME@/procedures/$procedureName/standardStepPicker")->findvalue('//value')->string_value;
        if ($value eq 'false' || $value eq '1') {
            $shouldAdd = 0;
        }
    };
    return $shouldAdd;
}


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
    });
}



# Auto-generated part ends
####################################