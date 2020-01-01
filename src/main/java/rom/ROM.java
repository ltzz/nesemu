package rom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ROM {
    public static byte[] loadFile(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
}
