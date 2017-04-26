# Kotlin сериализация для JS и Native

Тестовое задание для летней практики JetBrains 2017.

## Предварительные знания и инструменты

1. Язык **Kotlin** https://kotlinlang.org <br>
   Очень рекомендуется https://kotlinlang.org/docs/tutorials/koans.html
   
2. Язык **JavaScript** https://developer.mozilla.org/en-US/docs/Web/JavaScript

3. **JVM bytecode** https://docs.oracle.com/javase/specs/jvms/se8/html/index.html <br>
   Можно начать отсюда http://www.javaworld.com/article/2077233/core-java/bytecode-basics.html <br>
   И посмотреть сюда https://www.slideshare.net/CharlesNutter/javaone-2011-jvm-bytecode-for-dummies

4. Любить и использовать **IntelliJ IDEA** (Community Edition) https://www.jetbrains.com/idea/ <br>
   Котлин поддерживается "из коробки"

5. Работать с проектом на **Gradle** https://gradle.org <br>
   Работа с Котлином описана здесь https://kotlinlang.org/docs/reference/using-gradle.html

6. Поставить себе Kotlin Plugin с поддержкой сериализации согласно инструкции 
   [здесь](https://github.com/elizarov/KotlinSerializationPrototypePlayground)

## Задание

В этом репозитории содержится проект-затравка (открывать в IntelliJ IDEA).
[Файл Main.kt](src/main/kotlin/exercise/Main.kt) содержит минимальный демо-код для прототипа Kotlin сериализации.

Сборка и запуск приложения

```sh
gradlew run
```

При этом запускается код тестового приложения, которое выводит на консоль:

```text
{"message":"Hello, world!"}
Zoo test passes: true
```

Тестовое приложение весьма примитивно -- оно сериализует простой класс `Test` в формате JSON и выводит его 
на экран, а также проверяет что сералиализаци достаточно сложного объекта с зоопарктом разных
типов (поэтому он называется `zoo`) позволяет его восстановить после десериализации.

* Реализацию формата JSON можно посмотреть [здесь](https://github.com/JetBrains/kotlin/blob/rr/elizarov/kotlin-serialization/plugins/kotlin-serialization/kotlin-serialization-runtime/src/kotlin/serialization/JSON.kt)
* Другие примеры можно посмотреть [здесь](https://github.com/elizarov/KotlinSerializationPrototypePlayground)

Задача: Реализовать формат записи и чтения формата [CBOR](http://cbor.io) так, чтобы он успешно записывал и 
восстанавливал объект `zoo`.


 