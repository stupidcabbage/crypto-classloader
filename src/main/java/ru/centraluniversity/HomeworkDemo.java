package ru.centraluniversity;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public final class HomeworkDemo {
    private static final String SECRET = "ru.centraluniversity.SecretGreeting";
    private static final String COUNTING = "ru.centraluniversity.CountingGreeting";
    private static final String HELPER = "ru.centraluniversity.GreetingText";
    private static final byte KEY = 42;
    private static final Path LABS = Path.of("build/classes/java/labs");
    private static final Path ENCRYPTED = Path.of("build/encrypted");
    private static final ClassLoader PARENT = HomeworkDemo.class.getClassLoader();

    private HomeworkDemo() {
    }

    public static void main(String[] args) throws Exception {
        CryptoDemo.main(args);
        encrypt(LABS, COUNTING, ENCRYPTED);
        encrypt(LABS, HELPER, ENCRYPTED);
        ownPayload();
        delegation();
        errors();
        System.out.println("Все дополнительные проверки пройдены");
    }

    private static void ownPayload() throws Exception {
        CryptoClassLoader first = new CryptoClassLoader(PARENT, ENCRYPTED, KEY);
        CryptoClassLoader second = new CryptoClassLoader(PARENT, ENCRYPTED, KEY);
        Class<?> c1 = first.loadClass(COUNTING);
        Class<?> c2 = second.loadClass(COUNTING);
        check(c1 != c2, "Разные загрузчики должны давать разные типы");
        check(second.loadClass(COUNTING) == c2, "Повторная загрузка должна вернуть тот же Class");
        System.out.println("CountingGreeting загружен, до создания объектов static init не было");
        GreetingProvider a = (GreetingProvider) c1.getConstructor().newInstance();
        GreetingProvider b = (GreetingProvider) c2.getConstructor().newInstance();
        GreetingProvider another = (GreetingProvider) c2.getConstructor().newInstance();
        String a1 = a.greeting();
        String b1 = b.greeting();
        String b2 = another.greeting();
        String a2 = a.greeting();
        check(a1.endsWith("№1") && b1.endsWith("№1")
                && b2.endsWith("№2") && a2.endsWith("№2"), "Счётчики должны быть изолированы");
        check(second.loadClass(HELPER).getClassLoader() == second, "Зависимость загрузил другой загрузчик");
        check(second.loadClass(GreetingProvider.class.getName()) == GreetingProvider.class,
                "Интерфейс должен быть общим");
        System.out.println("первый загрузчик: " + a1 + "; " + a2);
        System.out.println("второй загрузчик, два объекта: " + b1 + "; " + b2);
        System.out.println("GreetingText определил тот же загрузчик: " + second);
    }

    private static void delegation() throws Exception {
        URL[] urls = {LABS.toUri().toURL()};
        try (URLClassLoader parentWithPayload = new URLClassLoader(urls, PARENT)) {
            Class<?> parentClass = parentWithPayload.loadClass(SECRET);
            CryptoClassLoader normal = new CryptoClassLoader(parentWithPayload, ENCRYPTED, KEY);
            check(normal.loadClass(SECRET) == parentClass, "Ожидалось делегирование родителю");
            ChildFirstCryptoClassLoader local = new ChildFirstCryptoClassLoader(
                    parentWithPayload, ENCRYPTED, KEY, Set.of(SECRET));
            Class<?> localClass = Class.forName(SECRET, false, local);
            check(localClass != parentClass && localClass.getClassLoader() == local,
                    "Child-first должен определить собственный класс");
            check(local.loadClass(SECRET) == localClass, "Child-first потерял кэш");
            check(local.loadClass("java.lang.String") == String.class, "Нарушена загрузка JDK");
            check(local.loadClass(GreetingProvider.class.getName()) == GreetingProvider.class,
                    "Child-first должен делегировать общий интерфейс");
            GreetingProvider greeting = (GreetingProvider) localClass.getConstructor().newInstance();
            System.out.println("parent-first вернул класс родителя: true");
            System.out.println("child-first определил отдельный класс: true, " + greeting.greeting());
            ChildFirstCryptoClassLoader fallback = new ChildFirstCryptoClassLoader(
                    parentWithPayload, Path.of("build/empty"), KEY, Set.of(SECRET));
            check(fallback.loadClass(SECRET) == parentClass, "Не сработал fallback к родителю");
            System.out.println("child-first без локального файла вернулся к родителю: true");
        }

        Path splitApi = Path.of("build/split-api");
        encrypt(LABS, SECRET, splitApi);
        encrypt(Path.of("build/classes/java/main"), GreetingProvider.class.getName(), splitApi);
        ChildFirstCryptoClassLoader broken = new ChildFirstCryptoClassLoader(
                PARENT, splitApi, KEY, Set.of(SECRET, GreetingProvider.class.getName()));
        Object instance = broken.loadClass(SECRET).getConstructor().newInstance();
        check(!GreetingProvider.class.isInstance(instance), "Копия интерфейса неожиданно совместима");
        try {
            GreetingProvider.class.cast(instance);
            throw new AssertionError("Ожидался ClassCastException");
        } catch (ClassCastException expected) {
            System.out.println("собственная копия GreetingProvider: ClassCastException");
        }
    }

    private static void errors() throws Exception {
        CryptoClassLoader loader = new CryptoClassLoader(PARENT, ENCRYPTED, KEY);
        try {
            loader.loadClass("ru.centraluniversity.AbsentGreeting");
            throw new AssertionError("Ожидался ClassNotFoundException");
        } catch (ClassNotFoundException expected) {
            check(expected.getCause() instanceof IOException, "Потеряна причина ошибки чтения");
            System.out.println("запрос отсутствующего класса: ClassNotFoundException");
        }
        Path unreadable = Path.of("build/unreadable");
        Files.createDirectories(unreadable.resolve("ru/centraluniversity/SecretGreeting.class"));
        try {
            new CryptoClassLoader(PARENT, unreadable, KEY).loadClass(SECRET);
            throw new AssertionError("Ожидалась ошибка чтения каталога как файла");
        } catch (ClassNotFoundException expected) {
            check(expected.getCause() instanceof IOException, "Потеряна причина ошибки чтения");
            System.out.println("каталог вместо class-файла: ClassNotFoundException");
        }
        try {
            new CryptoClassLoader(PARENT, ENCRYPTED, (byte) (KEY + 1)).loadClass(SECRET);
            throw new AssertionError("Ожидался ClassFormatError");
        } catch (ClassFormatError expected) {
            System.out.println("неверный ключ: ClassFormatError");
        }
        Path missingDependency = Path.of("build/missing-dependency");
        encrypt(LABS, COUNTING, missingDependency);
        CryptoClassLoader incomplete = new CryptoClassLoader(PARENT, missingDependency, KEY);
        GreetingProvider greeting = (GreetingProvider) incomplete.loadClass(COUNTING)
                .getConstructor().newInstance();
        try {
            greeting.greeting();
            throw new AssertionError("Ожидался NoClassDefFoundError");
        } catch (NoClassDefFoundError expected) {
            check(expected.getCause() instanceof ClassNotFoundException,
                    "Ожидалась ошибка поиска зависимости");
            System.out.println("вызов метода без GreetingText: NoClassDefFoundError, причина ClassNotFoundException");
        }
    }

    private static void encrypt(Path source, String name, Path destination) throws IOException {
        String relative = name.replace('.', '/') + ".class";
        Path target = destination.resolve(relative);
        Files.createDirectories(target.getParent());
        Files.write(target, XorCipher.crypt(Files.readAllBytes(source.resolve(relative)), KEY));
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
