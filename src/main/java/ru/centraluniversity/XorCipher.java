package ru.centraluniversity;

/**
 * Простейший побайтовый «шифратор» на XOR.
 *
 * <p>Операция XOR обратима сама к себе: {@code crypt(crypt(b, k), k) == b}. Поэтому один и тот же
 * метод и шифрует файл, и восстанавливает его обратно - отдельные методы encrypt/decrypt не нужны.
 * Надёжность не нужна: цель - получить байты, которые обычный загрузчик из classpath не сможет
 * интерпретировать как валидный class-файл (сломана сигнатура {@code CAFEBABE}).
 */
public final class XorCipher {

    private XorCipher() {
        // утилитный класс - без экземпляров
    }

    /**
     * Побайтово применяет XOR с заданным ключом.
     *
     * @param data  исходные байты
     * @param key   ключ
     * @return результат побайтового {@code data[i] ^ key}
     */
    public static byte[] crypt(byte[] data, byte key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key);
        }
        return result;
    }
}
