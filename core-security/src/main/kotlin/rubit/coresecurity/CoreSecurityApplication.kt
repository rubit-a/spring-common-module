package rubit.coresecurity

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CoreSecurityApplication

fun main(args: Array<String>) {
    runApplication<CoreSecurityApplication>(*args)
}
