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

public class VersionsIndex {


    public static void prepare(final Sources sources) {


        try {
            final StringBuilder index = new StringBuilder();
            index.append(":jbake-type: page\n")
                    .append(":jbake-status: published\n")
                    .append(":jbake-title: Apache TomEE Documentation\n")
                    .append("\n")
            ;


            for (final Source source : sources.getSources()) {
                if ("master".equals(source.getName())) continue;
                if ("latest".equals(source.getName())) continue;

                index.append("*").append(source.getName());
                if (source.isLatest()) {
                    index.append(" (latest)");
                }

                index.append("*\n\n");

                final File docs = sources.getDestinationFor(source, "docs");
                final File examples = sources.getDestinationFor(source, "examples");

                if (docs.exists() && docs.listFiles().length > 0) {
                    index.append(" - link:").append(source.getName()).append("/docs[Documentation]\n");
                }
                if (examples.exists() && examples.listFiles().length > 0) {
                    index.append(" - link:").append(source.getName()).append("/examples[Examples]\n");
                }
                index.append("\n\n");
            }

            IO.copy(IO.read(index.toString()), new File(sources.getDestination(), "content/documentation.adoc"));
            IO.copy(IO.read(index.toString()), new File(sources.getDestination(), "content/docs.adoc"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
