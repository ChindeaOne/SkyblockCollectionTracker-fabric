package sct;

import org.gradle.api.Project;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public abstract class GitVersion {

    @Inject
    protected ExecOperations getExecOperations() {
        return null;
    }

    public String setVersionfromGit(Project project) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        assert getExecOperations() != null;
        getExecOperations().exec(execSpec -> {
            execSpec.setWorkingDir(project.getRootDir());
            execSpec.commandLine("git", "describe", "--tags", "--abbrev=0");
            execSpec.setStandardOutput(baos);
            execSpec.setIgnoreExitValue(true);
        });

        String out = baos.toString(StandardCharsets.UTF_8).trim();
        if (out.startsWith("v")) out = out.substring(1);

        int plusIndex = out.indexOf("+");
        if (plusIndex != -1) out = out.substring(0, plusIndex);

        return out;
    }
}
