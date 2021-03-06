/**
 * detectable
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.detectable.detectables.lerna;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.detectable.detectable.executable.ExecutableOutput;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunner;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunnerException;
import com.synopsys.integration.detectable.detectables.lerna.model.LernaPackage;

public class LernaPackageDiscoverer {
    private final ExecutableRunner executableRunner;
    private final Gson gson;

    public LernaPackageDiscoverer(ExecutableRunner executableRunner, Gson gson) {
        this.executableRunner = executableRunner;
        this.gson = gson;
    }

    public List<LernaPackage> discoverLernaPackages(File workingDirectory, File lernaExecutable) throws ExecutableRunnerException {
        ExecutableOutput lernaLsExecutableOutput = executableRunner.execute(workingDirectory, lernaExecutable, "ls", "--all", "--json");
        String lernaLsOutput = lernaLsExecutableOutput.getStandardOutput();

        Type lernaPackageListType = new TypeToken<ArrayList<LernaPackage>>() {}.getType();
        List<LernaPackage> lernaPackages = gson.fromJson(lernaLsOutput, lernaPackageListType);

        return lernaPackages.stream()
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }
}
