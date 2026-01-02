package rubit.coretest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CoreTestApplication

fun main(args: Array<String>) {
    runApplication<CoreTestApplication>(*args)
}
