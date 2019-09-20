package com.synopsys.integration.detectable.detectables.bazel.functional.bazel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.detectable.detectable.executable.ExecutableOutput;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunner;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunnerException;
import com.synopsys.integration.detectable.detectables.bazel.model.BazelExternalId;
import com.synopsys.integration.detectable.detectables.bazel.model.BazelExternalIdExtractionFullRule;
import com.synopsys.integration.detectable.detectables.bazel.model.BazelExternalIdExtractionSimpleRule;
import com.synopsys.integration.detectable.detectables.bazel.parse.BazelExternalIdGenerator;
import com.synopsys.integration.detectable.detectables.bazel.parse.BazelQueryXmlOutputParser;
import com.synopsys.integration.detectable.detectables.bazel.parse.BazelVariableSubstitutor;
import com.synopsys.integration.detectable.detectables.bazel.parse.RuleConverter;
import com.synopsys.integration.detectable.detectables.bazel.parse.XPathParser;
import com.synopsys.integration.detectable.detectables.bazel.parse.detail.ArtifactStringsExtractor;
import com.synopsys.integration.detectable.detectables.bazel.parse.detail.ArtifactStringsExtractorXml;

public class BazelExternalIdGeneratorTest {
    private static final String commonsIoXml = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"no\"?> "
                                          +"<query version=\"2\"> "
                                          +"    <rule class=\"maven_jar\" location=\"/root/home/steve/examples/java-tutorial/WORKSPACE:6:1\" name=\"//external:org_apache_commons_commons_io\"> "
                                          +"        <string name=\"name\" value=\"org_apache_commons_commons_io\"/> "
                                          +"        <string name=\"artifact\" value=\"org.apache.commons:commons-io:1.3.2\"/> "
                                          +"    </rule> "
                                          +"</query>";
    private static final String guavaXml = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"no\"?> "
                                               + "<query version=\"2\"> "
                                               + "    <rule class=\"maven_jar\" location=\"/root/home/steve/examples/java-tutorial/WORKSPACE:1:1\" name=\"//external:com_google_guava_guava\"> "
                                               + "        <string name=\"name\" value=\"com_google_guava_guava\"/> "
                                               + "        <string name=\"artifact\" value=\"com.google.guava:guava:18.0\"/> "
                                               + "    </rule> "
                                               + "</query>";

    @Test
    public void test() throws ExecutableRunnerException {

        final ExecutableRunner executableRunner = Mockito.mock(ExecutableRunner.class);
        final String bazelExe = "notUsed";
        final XPathParser xPathParser = new XPathParser();
        final BazelQueryXmlOutputParser parser = new BazelQueryXmlOutputParser(xPathParser);
        final File workspaceDir = new File("notUsed");
        final String bazelTarget = "//testproject:ProjectRunner";

        final ArtifactStringsExtractor artifactStringsExtractor = Mockito.mock(ArtifactStringsExtractor.class);
        Optional<List<String>> bazelArtifactStringsCommonsIO = Optional.of(Arrays.asList("org.apache.commons:commons-io:1.3.2"));
        Optional<List<String>> bazelArtifactStringsGuava = Optional.of(Arrays.asList("com.google.guava:guava:18.0"));
        Mockito.when(artifactStringsExtractor.extractArtifactStrings(Mockito.any(BazelExternalIdExtractionFullRule.class), Mockito.eq("//external:org_apache_commons_commons_io"), Mockito.anyMap()))
            .thenReturn(bazelArtifactStringsCommonsIO);
        Mockito.when(artifactStringsExtractor.extractArtifactStrings(Mockito.any(BazelExternalIdExtractionFullRule.class), Mockito.eq("//external:com_google_guava_guava"), Mockito.anyMap()))
            .thenReturn(bazelArtifactStringsGuava);
        BazelExternalIdGenerator generator = new BazelExternalIdGenerator(executableRunner, bazelExe, artifactStringsExtractor, artifactStringsExtractor, workspaceDir, bazelTarget);

        BazelExternalIdExtractionSimpleRule simpleRule = new BazelExternalIdExtractionSimpleRule("@.*:jar", "maven_jar",
            "artifact", ":");
        BazelExternalIdExtractionFullRule xPathRule = RuleConverter.simpleToFull(simpleRule);

        // executableRunner.executeQuietly(workspaceDir, bazelExe, targetOnlyVariableSubstitutor.substitute(xPathRule.getTargetDependenciesQueryBazelCmdArguments()));
        final BazelVariableSubstitutor targetOnlyVariableSubstitutor = new BazelVariableSubstitutor(bazelTarget);
        ExecutableOutput executableOutputQueryForDependencies = new ExecutableOutput(0, "@org_apache_commons_commons_io//jar:jar\n@com_google_guava_guava//jar:jar", "");
        Mockito.when(executableRunner.execute(workspaceDir, bazelExe, targetOnlyVariableSubstitutor.substitute(xPathRule.getTargetDependenciesQueryBazelCmdArguments()))).thenReturn(executableOutputQueryForDependencies);

        // executableRunner.executeQuietly(workspaceDir, bazelExe, dependencyVariableSubstitutor.substitute(xPathRule.getDependencyDetailsXmlQueryBazelCmdArguments()));
        final BazelVariableSubstitutor dependencyVariableSubstitutorCommonsIo = new BazelVariableSubstitutor(bazelTarget, "//external:org_apache_commons_commons_io");
        final BazelVariableSubstitutor dependencyVariableSubstitutorGuava = new BazelVariableSubstitutor(bazelTarget, "//external:com_google_guava_guava");
        ExecutableOutput executableOutputQueryCommonsIo = new ExecutableOutput(0, commonsIoXml, "");
        ExecutableOutput executableOutputQueryGuava = new ExecutableOutput(0, guavaXml, "");
        Mockito.when(executableRunner.execute(workspaceDir, bazelExe, dependencyVariableSubstitutorCommonsIo.substitute(xPathRule.getDependencyDetailsXmlQueryBazelCmdArguments()))).thenReturn(executableOutputQueryCommonsIo);
        Mockito.when(executableRunner.execute(workspaceDir, bazelExe, dependencyVariableSubstitutorGuava.substitute(xPathRule.getDependencyDetailsXmlQueryBazelCmdArguments()))).thenReturn(executableOutputQueryGuava);

        List<BazelExternalId> bazelExternalIds = generator.generate(xPathRule);
        assertEquals(2, bazelExternalIds.size());
        assertEquals("org.apache.commons", bazelExternalIds.get(0).getGroup());
        assertEquals("commons-io", bazelExternalIds.get(0).getArtifact());
        assertEquals("1.3.2", bazelExternalIds.get(0).getVersion());

        assertEquals("com.google.guava", bazelExternalIds.get(1).getGroup());
        assertEquals("guava", bazelExternalIds.get(1).getArtifact());
        assertEquals("18.0", bazelExternalIds.get(1).getVersion());
    }
}
