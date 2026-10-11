package ru.centraluniversity;

/**
 * Пейлоад для {@link CryptoClassLoader}: обычный класс, реализующий общий интерфейс
 * {@link GreetingProvider}. Собирается в отдельный source set {@code labs}, поэтому его
 * байты не лежат на classpath приложения - приложение знает только интерфейс, а сам класс
 * загружается из зашифрованного файла.
 *
 * <p>Статический блок печатает загрузчик, чтобы было видно: инициализация ({@code <clinit>})
 * выполняется один раз для каждого определения класса, то есть для каждого определившего
 * загрузчика - даже если байты одни и те же.
 */
public class SecretGreeting implements GreetingProvider {

    private static final String MESSAGE = "Привет от зашифрованного класса";

    static {
        System.out.println("[SecretGreeting] static init, определил: "
                + SecretGreeting.class.getClassLoader());
    }

    @Override
    public String greeting() {
        return MESSAGE;
    }
}
