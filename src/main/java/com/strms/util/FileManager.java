package com.strms.util;

import com.strms.exceptions.FilePersistenceException;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class FileManager {

    public static void writeLines(String path, List<String> lines)
            throws FilePersistenceException {
        try {
            Path p = Paths.get(path);
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            Files.write(p, lines);
        } catch (IOException e) {
            throw new FilePersistenceException("Cannot write file: " + path, e);
        }
    }

    public static List<String> readLines(String path)
            throws FilePersistenceException {
        try {
            return Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            throw new FilePersistenceException("Cannot read file: " + path, e);
        }
    }

    public static void writeText(String path, String text)
            throws FilePersistenceException {
        try {
            Path p = Paths.get(path);
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            Files.write(p, text.getBytes());
        } catch (IOException e) {
            throw new FilePersistenceException("Cannot write file: " + path, e);
        }
    }
}
