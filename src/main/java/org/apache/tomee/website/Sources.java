/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.website;

import org.apache.openejb.loader.IO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The goal of this class is to combine several git repositories and branches into
 * one canonical base that can be used with jbake.
 *
 * Repositories are cloned outside of target into a '/repos/' directory so that
 * repeated running can be supported.  Subsequent runs will do a git pull to check
 * for new content.
 *
 * The prepare step will copy relevant asciidoc and markdown sources into the
 * target/jbake/content/<name> directory.
 *
 */
public class Sources {

    private final File destination;
    private final File repos;
    private final File mainSource;
    private final List<Source> sources = new ArrayList<>();

    public Sources(final File destination, final File repos, final File mainSource, final Source... sources) {
        this.destination = destination;
        this.repos = repos;
        this.mainSource = mainSource;

        destination.mkdirs();
        repos.mkdirs();

        Collections.addAll(this.sources, sources);
        for (final Source source : sources) {
            if (source.isLatest()) {
                this.sources.add(new Source(source.getScmUrl(), source.getBranch(), "latest"));
                break;
            }
        }
    }

    public File getDestination() {
        return destination;
    }

    public List<Source> getSources() {
        return sources;
    }

    /**
     * This is the heart of the code to merge several documentation
     * sources into one tree.
     */
    public void prepare() {
        final Docs docs = new Docs(this);
        final Examples2 examples = new Examples2(this);
        final VersionIndex versionIndex = new VersionIndex(this);

        try {
            IO.copyDirectory(mainSource, destination);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sources.stream()
                .peek(source -> source.setDir(new File(repos, source.getName())))
                .peek(Repos::download)
                .peek(docs::prepare)
                .peek(examples::prepare)
                .peek(versionIndex::prepare)
                .forEach(Sources::done);
        ;

        VersionsIndex.prepare(this);
    }

    public File getDestinationFor(final Source source, final String... parts) {
        final File content = new File(destination, "content");
        File dir = new File(content, source.getName());

        for (final String part : parts) {
            dir = new File(dir, part);
        }

        if (!dir.exists()) {
            if (!dir.mkdirs()) throw new RuntimeException("Could not make directory: " + dir.getAbsolutePath());
        }
        return dir;
    }

    private static void done(final Source source) {
        System.out.println("Done " + source);
    }
}
