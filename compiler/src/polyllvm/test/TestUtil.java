package polyllvm.test;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

final class TestUtil {

    private TestUtil() {
    }

    public static String fileToString(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, Charset.defaultCharset());
    }

    public static List<File> collectFiles(String ext, Path... dirs)
            throws IOException {
        List<File> files = new ArrayList<>();

        for (Path dir : dirs) {
            files.addAll(Files.walk(dir, FileVisitOption.FOLLOW_LINKS)
                              .map(Path::toFile)
                              .filter(f -> f.getName().endsWith("." + ext))
                              .collect(Collectors.toList()));
        }

        return files;
    }

    public static void make(File dir, int timeout, String... args)
            throws IOException, InterruptedException {
        String description = Stream.of(args).reduce((s1, s2) -> s1 + " " + s2).orElse("[empty]");
        String[] cmd = Stream.concat(Stream.of("make"), Stream.of(args)).toArray(String[]::new);
        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.directory(dir);
        builder.redirectOutput(Redirect.INHERIT);
        builder.redirectError(Redirect.INHERIT);
        Process proc = builder.start();
        if (!proc.waitFor(timeout, TimeUnit.SECONDS) || proc.exitValue() != 0) {
            if (proc.isAlive()) {
                proc.destroyForcibly();
                fail("Timeout occurred while making: " + description);
            }
            else {
                fail("Nonzero exit status while making: " + description);
            }
        }
    }

    public static File changeExtension(File file, String oldExt,
            String newExt) {
        assert file.toString().endsWith(oldExt);
        int baseLength = file.toString().length() - oldExt.length();
        String base = file.toString().substring(0, baseLength);
        return new File(base.concat(newExt));
    }
}
