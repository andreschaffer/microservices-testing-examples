appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%-5p [%d{ISO8601,UTC}] %c: %m%n%rEx"
    }
}

root(INFO, ["CONSOLE"])