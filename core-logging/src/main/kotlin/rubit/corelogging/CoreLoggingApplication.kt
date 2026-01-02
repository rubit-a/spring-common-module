package rubit.corelogging

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CoreLoggingApplication

fun main(args: Array<String>) {
    runApplication<CoreLoggingApplication>(*args)
}
