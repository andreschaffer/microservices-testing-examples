appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%-5p [%d{ISO8601,UTC}] %c: %m%n%rEx"
    }
}

root(INFO, ["CONSOLE"])
logger("kafka", WARN)
logger("org.apache.kafka", WARN)
logger("org.apache.zookeeper", WARN)