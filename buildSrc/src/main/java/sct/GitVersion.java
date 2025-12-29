package sct;

import org.gradle.api.Project;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class GitVersion {
    private GitVersion() {}

    public static String setVersionfromGit(Project project) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ExecResult result = project.exec((ExecSpec execSpec) -> {
            execSpec.commandLine("git", "describe", "--tags", "--abbrev=0");
            execSpec.setStandardOutput(baos);
            execSpec.setIgnoreExitValue(true);
        });

        String out = baos.toString(StandardCharsets.UTF_8).trim();
        if (out.startsWith("v")) {
            out = out.substring(1);
        }
        return out;
    }
}
