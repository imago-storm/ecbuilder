#Auto-generated part begins
######################################
#Do not touch anything below this line

use strict;
use ElectricCommander;
use JSON qw(decode_json);
use subs qw(debug);

my $pluginKey = '@PLUGIN_KEY@';
my $category = eval { $commander->getProperty("/plugins/$pluginKey/project/ec_pluginCategory")->findvalue('//value')->string_value } || 'Utilities';
my $stepsWithCredentials = getStepsWithCredentials();
debug "Category: $category";
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

print "Upgrade Action: $upgradeAction\n";
cleanAllStepPickers();

if ($upgradeAction eq 'upgrade') {
    migrateConfigurations();
    migrateProperties();
}

my $nowString = localtime;
$commander->setProperty("/plugins/$pluginName/project/logs/$nowString", {value => join("\n", @logs)});


sub migrateConfigurations {
    my $configName = eval {
        $commander->getProperty("/plugins/$pluginKey/project/ec_configPropertySheet")->findvalue('//value')->string_value
    } || 'ec_plugin_cfgs';

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


sub migrateProperties {
    my $clonedPropertySheets = eval {
        decode_json($commander->getProperty("/plugins/$pluginKey/project/ec_clonedProperties")->findvalue('//value')->string_value);
    };
    unless ($clonedPropertySheets) {
        debug "No properties to migrate";
        return;
    }

    for my $prop (@$clonedPropertySheets) {
        $commander->clone({
            path => "/plugins/$otherPluginName/project/$prop",
            cloneName => "/plugins/$pluginName/project/$prop"
        });
        debug "Cloned $prop"
    }
}


sub getStepsWithCredentials {
    my $retval = [];
    eval {
        my $pluginName = '@PLUGIN_NAME@';
        my $stepsJson = $commander->getProperty("/projects/$pluginName/procedures/CreateConfiguration/ec_stepsWithAttachedCredentials")->findvalue('//value')->string_value;
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
    push @::createStepPickerSteps, {
        label       => $label,
        procedure   => $procedureName,
        description => $pickerDescription,
        category    => $category,
    };
    debug "Added step picker $label";
}

sub cleanAllStepPickers {
    my $xpath = $commander->getProperties({path => '/server/ec_customEditors/pickerStep'});
    for my $step ($xpath->findnodes('//property')) {
        my $name = $step->finvalue('propertyName')->string_value;
        if ($name =~ m/^$pluginKey\s-\s/) {
            debug("Deleting step picker $name");
            $batch->deleteProperty("/server/ec_customEditors/pickerStep/$name");
        }
    }
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