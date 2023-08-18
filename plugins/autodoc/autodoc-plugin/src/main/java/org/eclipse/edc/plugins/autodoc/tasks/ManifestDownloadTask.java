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

import org.eclipse.edc.plugins.autodoc.AutodocExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.tasks.TaskAction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class ManifestDownloadTask extends DefaultTask {

    public static final String NAME = "downloadManifests";
    private static final String EDC_GROUP = "org.eclipse.edc";
    private static final Duration MAX_MANIFEST_AGE = Duration.ofHours(24);
    private static final String MANIFEST_CLASSIFIER = "manifest";
    private static final String MANIFEST_TYPE = "json";
    private final HttpClient httpClient;
    private Path downloadDirectory;

    public ManifestDownloadTask() {
        httpClient = HttpClient.newHttpClient();
        downloadDirectory = getProject().getRootProject().getBuildDir().toPath().resolve("manifests");
    }

    @TaskAction
    public void downloadManifests() {
        var autodocExt = getProject().getExtensions().findByType(AutodocExtension.class);
        requireNonNull(autodocExt, "AutodocExtension cannot be null");

        if (autodocExt.getDownloadDirectory().isPresent()) {
            downloadDirectory = autodocExt.getDownloadDirectory().get().toPath();
        }

        getProject().getConfigurations()
                .stream().flatMap(config -> config.getDependencies().stream())
                .filter(dep -> EDC_GROUP.equals(dep.getGroup()))
                .filter(dep -> !getExclusions().contains(dep.getName()))
                .map(this::createDownloadRequest)
                .filter(Optional::isPresent)
                .forEach(dt -> downloadDependency(dt.get(), downloadDirectory));
    }

    private String createArtifactUrl(Dependency dep, MavenArtifactRepository repo) {
        return format("%s%s/%s/%s/%s-%s-%s.%s", repo.getUrl(), dep.getGroup().replace(".", "/"), dep.getName(), dep.getVersion(),
                dep.getName(), dep.getVersion(), MANIFEST_CLASSIFIER, MANIFEST_TYPE);
    }

    private void downloadDependency(DependencyDownload dt, Path outputDirectory) {

        var p = outputDirectory.resolve(dt.filename());
        var request = HttpRequest.newBuilder().uri(dt.uri()).GET().build();
        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                getLogger().warn("Could not download {}, HTTP response: {}", dt.dependency, response);
                return;
            }
            outputDirectory.toFile().mkdirs();
            getLogger().debug("Downloading {} into {}", dt, outputDirectory);
            try (var is = response.body(); var fos = new FileOutputStream(p.toFile())) {
                is.transferTo(fos);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a download request for a given dependency, classifier, and type. A download request is successfully created if:
     * <ul>
     *     <li>the output directory does not exists</li>
     *     <li>the file does not exist locally</li>
     *     <li>the file exists locally, but is too old (<24hrs) </li>
     *     <li>the file exists locally, but is not readable</li>
     *     <li>the file is found in at least one Maven repository. MavenLocal is ignored.</li>
     * </ul>
     *
     * @param dep the dependency to download
     * @return an optional DownloadRequest if the artifact should be downloaded, otherwise an empty optional
     */
    private Optional<DependencyDownload> createDownloadRequest(Dependency dep) {
        if (isLocalFileValid(dep)) {
            getLogger().debug("Local file {} was deemed to be viable, will not download", new DependencyDownload(dep, null, MANIFEST_CLASSIFIER, MANIFEST_TYPE).filename());
            return Optional.empty();
        }
        var repos = getProject().getRepositories().stream().toList();
        return repos.stream()
                .filter(repo -> repo instanceof MavenArtifactRepository)
                .map(repo -> (MavenArtifactRepository) repo)
                .map(repo -> {
                    var repoUrl = createArtifactUrl(dep, repo);
                    try {
                        // we use a HEAD request, because we only want to see whether that module has a `-manifest.json`
                        var uri = URI.create(repoUrl);
                        var headRequest = HttpRequest.newBuilder()
                                .uri(uri)
                                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                                .build();
                        var response = httpClient.send(headRequest, HttpResponse.BodyHandlers.discarding());
                        if (response.statusCode() == 200) {
                            return new DependencyDownload(dep, uri, MANIFEST_CLASSIFIER, MANIFEST_TYPE);
                        }
                        return null;
                    } catch (IOException | InterruptedException | IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst();
    }

    /**
     * Checks if the manifest for a dependency exists locally. A local file is considered valid if:
     * <ul>
     *  <li>The output directory exists</li>
     *  <li>The file exists locally and is readable</li>
     *  <li>The file is not older than 24 hours</li>
     * </ul>
     *
     * @param dep the dependency to check
     * @return true if the local file is valid, false otherwise
     */
    private boolean isLocalFileValid(Dependency dep) {
        if (!downloadDirectory.toFile().exists()) return false;
        var filePath = downloadDirectory.resolve(new DependencyDownload(dep, null, MANIFEST_CLASSIFIER, MANIFEST_TYPE).filename());
        var file = filePath.toFile();
        if (!file.exists() || !file.canRead()) return false;

        try {
            var date = Files.getLastModifiedTime(filePath).toInstant();
            return Duration.between(date, Instant.now()).compareTo(MAX_MANIFEST_AGE) <= 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> getExclusions() {
        return Set.of();
    }

    private record DependencyDownload(Dependency dependency, URI uri, String classifier, String type) {
        @Override
        public String toString() {
            return "{" +
                    "dependency=" + dependency +
                    ", uri=" + uri +
                    '}';
        }

        String filename() {
            return format("%s-%s-%s.%s", dependency.getName(), dependency.getVersion(), classifier, type);
        }
    }
}
