package ru.centraluniversity;

/**
 * Интерфейс обмена между приложением и классами, которые грузит {@link CryptoClassLoader}.
 *
 * <p>Интерфейс - чтобы подгружаемый класс ({@code SecretGreeting}
 * из source set {@code labs}) можно было привести обратно к знакомому приложению типу.
 * Интерфейс живёт в загрузчике приложения (общем для всех), поэтому экземпляр, созданный
 * из байтов, загруженных {@link CryptoClassLoader}'ом, спокойно приводится к
 * {@code GreetingProvider} и методы вызываются как обычно.
 */
public interface GreetingProvider {

    /**
     * Возвращает строку-приветствие.
     */
    String greeting();
}
