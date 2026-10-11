package ru.centraluniversity;

import java.nio.file.Path;
import java.util.Set;

public final class ChildFirstCryptoClassLoader extends CryptoClassLoader {
    private final Set<String> localNames;

    public ChildFirstCryptoClassLoader(ClassLoader parent, Path root, byte key,
                                      Set<String> localNames) {
        super(parent, root, key);
        this.localNames = Set.copyOf(localNames);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null && localNames.contains(name) && !name.startsWith("java.")) {
                try {
                    loaded = findClass(name);
                } catch (ClassNotFoundException e) {
                    loaded = super.loadClass(name, false);
                }
            }
            if (loaded == null) {
                loaded = super.loadClass(name, false);
            }
            if (resolve) {
                resolveClass(loaded);
            }
            return loaded;
        }
    }
}
