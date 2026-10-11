package ru.centraluniversity;

public final class GreetingText {
    private GreetingText() {
    }

    public static String message(int number) {
        return "Привет из своего пейлоада, вызов №" + number;
    }
}
