# Дополнительный материал

- [ClassLoader (JDK docs)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/ClassLoader.html) - официальная справка по методам `loadClass`, `findClass`, `defineClass`
- [Механизмы загрузки классов в Java (Baeldung)](https://www.baeldung.com/java-classloaders) - обзор встроенной системы загрузчиков и делегирования, в том числе про `Class.forName`
- [Class.forName (JDK docs)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Class.html#forName(java.lang.String,boolean,java.lang.ClassLoader)) - сигнатура с явным загрузчиком и флагом инициализации
- [The Anatomy of a Class File (Oracle)](https://www.oracle.com/technical-resources/articles/java/javaclassfile.html) - разбор формата class-файла, в том числе сигнатуры `CAFEBABE`
- [Java Classloader internals (Jenkov)](https://tutorials.jenkov.com/java/classloaders.html) - как устроено делегирование родителю и изоляция
