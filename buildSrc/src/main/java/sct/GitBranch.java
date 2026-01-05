package sct;

import org.gradle.api.Project;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public abstract class GitBranch {

    @Inject
    protected ExecOperations getExecOperations() {
        return null;
    }

    public String getGitBranchName(Project project) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        getExecOperations().exec(execSpec -> {
            execSpec.setWorkingDir(project.getRootDir());
            execSpec.commandLine("git", "rev-parse", "--abbrev-ref", "HEAD");
            execSpec.setStandardOutput(baos);
            execSpec.setIgnoreExitValue(true);
        });

        return baos.toString(StandardCharsets.UTF_8).trim();
    }
}
