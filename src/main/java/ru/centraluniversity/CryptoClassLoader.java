package ru.centraluniversity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CryptoClassLoader extends ClassLoader {
    private final Path root;
    private final byte key;

    public CryptoClassLoader(ClassLoader parent, Path root, byte key) {
        super(parent);
        this.root = root;
        this.key = key;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Path file = root.resolve(name.replace('.', '/') + ".class");
        byte[] encrypted;
        try {
            encrypted = Files.readAllBytes(file);
        } catch (IOException | SecurityException e) {
            throw new ClassNotFoundException(name, e);
        }
        byte[] definition = XorCipher.crypt(encrypted, key);
        return defineClass(name, definition, 0, definition.length);
    }
}
