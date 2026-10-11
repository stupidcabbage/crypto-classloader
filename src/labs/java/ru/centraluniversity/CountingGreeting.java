package ru.centraluniversity;

public class CountingGreeting implements GreetingProvider {
    private static int calls;

    static {
        System.out.println("[CountingGreeting] static init, определил: "
                + CountingGreeting.class.getClassLoader());
    }

    @Override
    public String greeting() {
        return GreetingText.message(++calls);
    }
}
