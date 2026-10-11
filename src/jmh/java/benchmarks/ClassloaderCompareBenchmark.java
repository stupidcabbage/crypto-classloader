package benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import ru.centraluniversity.CryptoClassLoader;
import ru.centraluniversity.GreetingProvider;
import ru.centraluniversity.XorCipher;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 2, jvmArgsAppend = {"-Xms256m", "-Xmx256m"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class ClassloaderCompareBenchmark {
    private static final String NAME = "ru.centraluniversity.SecretGreeting";
    private static final byte KEY = 42;
    private URL[] urls;
    private Path encrypted;
    private ClassLoader parent;

    @Setup
    public void setup() throws IOException, ClassNotFoundException {
        Path labs = Path.of("build/classes/java/labs");
        String relative = NAME.replace('.', '/') + ".class";
        encrypted = Path.of("build/encrypted");
        Path target = encrypted.resolve(relative);
        Files.createDirectories(target.getParent());
        Files.write(target, XorCipher.crypt(Files.readAllBytes(labs.resolve(relative)), KEY));
        urls = new URL[]{labs.toUri().toURL()};
        parent = GreetingProvider.class.getClassLoader();
        try {
            parent.loadClass(NAME);
            throw new IllegalStateException("Пейлоад попал на classpath бенчмарка");
        } catch (ClassNotFoundException expected) {
            try (URLClassLoader loader = new URLClassLoader(urls, parent)) {
                if (loader.loadClass(NAME).getClassLoader() != loader) {
                    throw new IllegalStateException("Класс загрузил родитель");
                }
            }
        }
    }

    @Benchmark
    public Class<?> jdkLoader() throws IOException, ClassNotFoundException {
        try (URLClassLoader loader = new URLClassLoader(urls, parent)) {
            return loader.loadClass(NAME);
        }
    }

    @Benchmark
    public Class<?> customLoader() throws ClassNotFoundException {
        return new CryptoClassLoader(parent, encrypted, KEY).loadClass(NAME);
    }
}
