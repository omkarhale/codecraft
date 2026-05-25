package com.codecraft.model;

public enum Language {
    PYTHON(71, "Python 3.11", "python"),
    JAVASCRIPT(93, "Node.js 18", "javascript"),
    JAVA(62, "Java 17", "java"),
    CPP(54, "C++ 17", "cpp"),
    C(50, "C (GCC)", "c"),
    GO(95, "Go 1.21", "go"),
    RUST(73, "Rust", "rust"),
    PHP(68, "PHP 8", "php"),
    RUBY(72, "Ruby 3", "ruby"),
    KOTLIN(78, "Kotlin", "kotlin"),
    TYPESCRIPT(74, "TypeScript", "typescript"),
    SWIFT(83, "Swift", "swift"),
    R(80, "R", "r"),
    BASH(46, "Bash", "bash");

    public final int judge0Id;
    public final String displayName;
    public final String monacoId;

    Language(int judge0Id, String displayName, String monacoId) {
        this.judge0Id = judge0Id;
        this.displayName = displayName;
        this.monacoId = monacoId;
    }
}
