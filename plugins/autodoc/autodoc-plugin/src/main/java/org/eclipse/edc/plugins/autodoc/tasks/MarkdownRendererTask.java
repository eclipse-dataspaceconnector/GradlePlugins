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

package org.eclipse.edc.plugins.autodoc.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.plugins.autodoc.html.HtmlManifestRenderer;
import org.eclipse.edc.plugins.autodoc.json.JsonManifestReader;
import org.eclipse.edc.plugins.autodoc.markdown.MarkdownManifestRenderer;
import org.eclipse.edc.plugins.autodoc.spi.ManifestRenderer;
import org.eclipse.edc.plugins.autodoc.spi.ManifestWriter;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Function;

import static java.lang.String.format;

public class MarkdownRendererTask extends DefaultTask {

    private final JsonManifestReader reader = new JsonManifestReader(new ObjectMapper());
    private final Function<OutputStream, ManifestRenderer> manifestRendererSupplier;
    private final String fileExtension;

    public MarkdownRendererTask(Function<OutputStream, ManifestRenderer> manifestRendererSupplier, String fileExtension) {
        this.manifestRendererSupplier = manifestRendererSupplier;
        this.fileExtension = fileExtension;
    }

    @TaskAction
    public void renderMarkdown() {
        var buildDir = getProject().getLayout().getBuildDirectory().getAsFile().get();

        File manifest;
        if (getProject().getRootProject().equals(getProject())) {
            manifest = Path.of(buildDir.getAbsolutePath(), "manifest.json").toFile();
        } else {
            manifest = Path.of(buildDir.getAbsolutePath(), "edc.json").toFile();
        }

        if (manifest.exists()) {
            var outputFile = new File(buildDir, getProject().getName() + "." + fileExtension);
            try (
                    var fos = new FileOutputStream(outputFile);
                    var fis = new FileInputStream(manifest)
            ) {
                var writer = new ManifestWriter(manifestRendererSupplier.apply(fos));
                getLogger().lifecycle(format("Rendering %s for input %s", outputFile, manifest));
                try (var os = writer.convert(reader.read(fis))) {
                    os.flush();
                }
            } catch (IOException e) {
                throw new GradleException("Error rendering Markdown", e);
            }
        }
    }

    public static class ToHtml extends MarkdownRendererTask {
        public static final String NAME = "doc2html";

        public ToHtml() {
            super(HtmlManifestRenderer::new, "html");
        }
    }

    public static class ToMarkdown extends MarkdownRendererTask {
        public static final String NAME = "doc2md";

        public ToMarkdown() {
            super(MarkdownManifestRenderer::new, "md");
        }
    }
}
