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

package org.eclipse.edc.plugins.openapimerger.tasks;

import com.rameshkp.openapi.merger.gradle.task.OpenApiMergerTask;
import org.gradle.api.tasks.options.Option;

import java.io.File;

/**
 * Customization of the {@link OpenApiMergerTask}, which allows to pass in the input and output directories via command line.
 */
public class MergeApiSpecByPathTask extends OpenApiMergerTask {

    public static final String NAME = "mergeApiSpec";

    @Option(option = "output", description = "Output directory where the merged spec file is stores. Optional.")
    public void setOutputDir(String outputDirectory) {
        // inject the command line arg into the merger task's config
        getOutputFileProperty().set(new File(outputDirectory));
    }

    @Option(option = "input", description = "Input directory where to look for partial specs. Required")
    public void setInputDir(String inputDir) {
        // inject the command line arg into the merger task's config
        getInputDirectory().set(new File(inputDir));
    }
}

