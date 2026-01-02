package rubit.commonauth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CommonAuthApplication

fun main(args: Array<String>) {
    runApplication<CommonAuthApplication>(*args)
}
