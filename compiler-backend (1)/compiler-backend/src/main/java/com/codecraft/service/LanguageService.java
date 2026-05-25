package com.codecraft.service;

import com.codecraft.model.Language;
import com.codecraft.model.LanguageInfo;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Returns language metadata + starter boilerplate for the editor.
 * When user selects a language, the editor shows this code by default.
 */
@Service
public class LanguageService {

    private static final Map<Language, String> STARTER_CODE = Map.ofEntries(
        Map.entry(Language.PYTHON, """
            # Python 3.11
            def greet(name):
                return f"Hello, {name}!"

            print(greet("World"))
            """),
        Map.entry(Language.JAVASCRIPT, """
            // Node.js 18
            const greet = (name) => `Hello, ${name}!`;

            console.log(greet("World"));
            """),
        Map.entry(Language.JAVA, """
            // Java 17
            public class Main {
                public static void main(String[] args) {
                    System.out.println(greet("World"));
                }

                static String greet(String name) {
                    return "Hello, " + name + "!";
                }
            }
            """),
        Map.entry(Language.CPP, """
            // C++ 17
            #include <iostream>
            #include <string>

            std::string greet(const std::string& name) {
                return "Hello, " + name + "!";
            }

            int main() {
                std::cout << greet("World") << std::endl;
                return 0;
            }
            """),
        Map.entry(Language.GO, """
            // Go 1.21
            package main

            import "fmt"

            func greet(name string) string {
                return fmt.Sprintf("Hello, %s!", name)
            }

            func main() {
                fmt.Println(greet("World"))
            }
            """),
        Map.entry(Language.RUST, """
            // Rust
            fn greet(name: &str) -> String {
                format!("Hello, {}!", name)
            }

            fn main() {
                println!("{}", greet("World"));
            }
            """),
        Map.entry(Language.TYPESCRIPT, """
            // TypeScript
            const greet = (name: string): string => {
                return `Hello, ${name}!`;
            };

            console.log(greet("World"));
            """),
        Map.entry(Language.KOTLIN, """
            // Kotlin
            fun greet(name: String) = "Hello, $name!"

            fun main() {
                println(greet("World"))
            }
            """),
        Map.entry(Language.RUBY, """
            # Ruby 3
            def greet(name)
              "Hello, #{name}!"
            end

            puts greet("World")
            """),
        Map.entry(Language.PHP, """
            <?php
            // PHP 8
            function greet(string $name): string {
                return "Hello, $name!";
            }

            echo greet("World") . PHP_EOL;
            """),
        Map.entry(Language.SWIFT, """
            // Swift
            func greet(_ name: String) -> String {
                return "Hello, \\(name)!"
            }

            print(greet("World"))
            """),
        Map.entry(Language.C, """
            // C (GCC)
            #include <stdio.h>

            void greet(const char* name) {
                printf("Hello, %s!\\n", name);
            }

            int main() {
                greet("World");
                return 0;
            }
            """),
        Map.entry(Language.R, """
            # R
            greet <- function(name) {
              paste0("Hello, ", name, "!")
            }

            cat(greet("World"), "\\n")
            """),
        Map.entry(Language.BASH, """
            #!/bin/bash
            greet() {
              echo "Hello, $1!"
            }

            greet "World"
            """)
    );

    public List<LanguageInfo> getAllLanguages() {
        return Arrays.stream(Language.values())
                .map(lang -> LanguageInfo.builder()
                        .id(lang.name())
                        .displayName(lang.displayName)
                        .monacoId(lang.monacoId)
                        .starterCode(STARTER_CODE.getOrDefault(lang, "// Start coding..."))
                        .build())
                .toList();
    }
}
