package by.tyv.account.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtils {
    static public String readResource(String name) throws Exception {
        var resource = TestUtils.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + name);
        }
        return Files.readString(Path.of(resource.toURI()), StandardCharsets.UTF_8);
    }
}
