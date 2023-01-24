/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.modulenames;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class CheckModuleNamesAction implements Action<Project> {

    private final Predicate<String> isSampleModule = displayName -> displayName.contains(":samples:");
    private final Predicate<String> isSystemTestModule = displayName -> displayName.contains(":system-tests:");
    private final Predicate<String> excludeSamplesAndSystemTests = isSampleModule.or(isSystemTestModule).negate();
    private final Function<String, String> projectName = it -> {
        var split = it.replace("project '", "").replace("'", "").split(":");
        return split[split.length - 1];
    };

    public CheckModuleNamesAction() {
    }

    @Override
    public void execute(Project project) {
        var subprojects = project.getSubprojects().stream()
                .filter(it -> it.getBuildFile().exists())
                .map(Project::getDisplayName)
                .filter(excludeSamplesAndSystemTests)
                .collect(groupingBy(projectName));

        var duplicatedSubprojects = subprojects.entrySet().stream()
                .filter(it -> it.getValue().size() > 1)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!duplicatedSubprojects.isEmpty()) {
            var message = duplicatedSubprojects.entrySet().stream()
                    .map(it -> it.getKey() + ":\n" + it.getValue().stream().collect(joining("\n\t", "\t", "")))
                    .collect(joining("\n"));

            throw new GradleException("Duplicate module names found: \n" + message);
        }
    }
}