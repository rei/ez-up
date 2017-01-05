package com.rei.ezup.util;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class ZipUtils {
    public static FileSystem createZipFileSystem(Path zipFile, boolean create) throws IOException {
        // convert the filename to a URI
        final URI uri = URI.create("jar:file:" + zipFile.toUri().getPath());

        final Map<String, String> env = new HashMap<>();
        if (create) {
            env.put("create", "true");
        }

        return FileSystems.newFileSystem(uri, env);
    }

    /**
     * Creates a zip file from the directory
     * 
     * @param dir directory to zip
     * @param dest destination zip file
     * @throws IOException
     */
    public static void create(Path basedir, Path destZip) throws IOException {
        try (FileSystem zipFs = createZipFileSystem(destZip, true)) {
            final Path zipRoot = zipFs.getPath("/");
            // for directories, walk the file tree
            Files.walkFileTree(basedir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final Path dest = zipFs.getPath(zipRoot.toString(), basedir.relativize(file).toString());
                    Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException { 
                    final Path dirToCreate = zipFs.getPath(zipRoot.toString(), basedir.relativize(dir).toString());
                    if (Files.notExists(dirToCreate)) {
                        Files.createDirectories(dirToCreate);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
