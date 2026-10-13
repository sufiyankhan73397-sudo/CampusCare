package com.campuscare;

/**
 * Entry point used by the fat JAR / EXE.
 * It must NOT extend Application, otherwise "JavaFX runtime components are missing" appears
 * when JavaFX is packed on the classpath.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
