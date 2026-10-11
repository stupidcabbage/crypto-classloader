package ru.centraluniversity;

import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Запускаемая демонстрация: шифрует пейлоад, загружает его отдельными загрузчиками
 * и показывает ключевые свойства собственного загрузчика. Запуск - {@code ./gradlew run}.
 *
 * <p>Порядок {@code main}:
 * <ol>
 *   <li>берёт скомпилированный {@code SecretGreeting.class} из {@code build/classes/java/labs}
 *       (пейлоад собран в отдельный source set {@code labs}, на classpath приложения его нет);</li>
 *   <li>шифрует его {@link XorCipher}'ом и кладёт в {@code build/encrypted} - обычный загрузчик
 *       из classpath такой файл как класс прочитать не сможет;</li>
 *   <li>через {@link CryptoClassLoader} загружает класс из зашифрованного каталога и приводит
 *       экземпляр к {@link GreetingProvider} (интерфейс - из общего загрузчика приложения);</li>
 *   <li>вторым загрузчиком из тех же байтов создаёт ещё один класс и показывает, что типы
 *       не совпадают (идентичность класса включает определивший загрузчик).</li>
 * </ol>
 */
public final class CryptoDemo {

    /** Имя пейлоада - строка, а не ссылка на класс: типа SecretGreeting на classpath приложения нет. */
    private static final String PAYLOAD_NAME = "ru.centraluniversity.SecretGreeting";

    private static final byte KEY = 42;

    private CryptoDemo() {
        // утилитный класс - без экземпляров
    }

    public static void main(String[] args) throws Exception {
        Path labsClasses = Path.of("build", "classes", "java", "labs");
        Path payload = labsClasses.resolve("ru/centraluniversity/SecretGreeting.class");
        if (!Files.exists(payload)) {
            throw new IllegalStateException("Не найден пейлоад: " + payload.toAbsolutePath()
                    + ". Соберите проект командой ./gradlew run (labsClasses).");
        }

        byte[] plain = Files.readAllBytes(payload);

        // Шифруем байты и кладём в build/encrypted. Такой файл уже не выглядит как
        // class-файл (начала 0xCAFEBABE там нет), и обычный загрузчик его не возьмёт.
        Path encryptedDir = Path.of("build", "encrypted");
        byte[] secret = XorCipher.crypt(plain, KEY);
        Path target = encryptedDir.resolve("ru/centraluniversity/SecretGreeting.class");
        Files.createDirectories(target.getParent());
        Files.write(target, secret);

        ClassLoader parent = CryptoDemo.class.getClassLoader();

        // 1. Мало того что пейлоада нет на classpath - даже по имени его не найти.
        try {
            Class.forName(PAYLOAD_NAME);
            System.out.println("родитель нашёл класс по имени - такого быть не должно");
        } catch (ClassNotFoundException e) {
            System.out.println("родитель класс по имени НЕ находит (его нет на classpath)");
        }

        // 2. Грузит только собственный загрузчик из зашифрованного каталога.
        CryptoClassLoader loader1 = new CryptoClassLoader(parent, encryptedDir, KEY);
        Class<?> c1 = loader1.loadClass(PAYLOAD_NAME);
        Constructor<?> ctor1 = c1.getConstructor();
        GreetingProvider g1 = (GreetingProvider) ctor1.newInstance();
        System.out.println("greeting = " + g1.greeting());
        System.out.println("определял загрузчик c1: " + c1.getClassLoader());

        // 3. Идентичность типа = «бинарное имя + определивший загрузчик».
        //    Второй загрузчик из тех же байтов даёт другой тип, хотя файл один и тот же.
        CryptoClassLoader loader2 = new CryptoClassLoader(parent, encryptedDir, KEY);
        Class<?> c2 = loader2.loadClass(PAYLOAD_NAME);
        Object instance2 = c2.getConstructor().newInstance();

        System.out.println("c1 == c2? " + (c1 == c2));
        System.out.println("c1.isInstance(экземпляр c2)? " + c1.isInstance(instance2));
        System.out.println("загрузчики: " + c1.getClassLoader() + " vs " + c2.getClassLoader());

        // 4. Оба экземпляра совместимы с интерфейсом из общего загрузчика приложения.
        GreetingProvider g2 = (GreetingProvider) c2.getConstructor().newInstance();
        System.out.println("greeting из c2 = " + g2.greeting());
    }
}
