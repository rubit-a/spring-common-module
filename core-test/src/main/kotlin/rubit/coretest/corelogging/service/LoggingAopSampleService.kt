package rubit.coretest.corelogging.service

import org.springframework.stereotype.Service
import rubit.corelogging.aop.LogExecutionTime

@Service
class LoggingAopSampleService {

    @LogExecutionTime
    fun work(message: String): String {
        return "processed-$message"
    }
}
