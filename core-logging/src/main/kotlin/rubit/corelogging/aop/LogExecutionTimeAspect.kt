package rubit.corelogging.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import rubit.corelogging.config.AopLoggingProperties
import java.util.concurrent.TimeUnit

@Aspect
class LogExecutionTimeAspect(
    private val properties: AopLoggingProperties
) {

    @Around("@annotation(rubit.corelogging.aop.LogExecutionTime) || @within(rubit.corelogging.aop.LogExecutionTime)")
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val startTime = System.nanoTime()
        try {
            val result = joinPoint.proceed()
            val elapsedMs = elapsedMillis(startTime)
            logSuccess(joinPoint, elapsedMs)
            if (properties.logResult && shouldLog(elapsedMs)) {
                logWithArgs(joinPoint, elapsedMs, "result={}", result)
            }
            return result
        } catch (ex: Throwable) {
            val elapsedMs = elapsedMillis(startTime)
            logFailure(joinPoint, elapsedMs, ex)
            throw ex
        }
    }

    private fun logSuccess(joinPoint: ProceedingJoinPoint, elapsedMs: Long) {
        if (!shouldLog(elapsedMs)) return
        if (properties.logArgs) {
            val args = joinPoint.args.joinToString(prefix = "[", postfix = "]") { it.safeToString() }
            logWithArgs(joinPoint, elapsedMs, "args={}", args)
            return
        }
        logBasic(joinPoint, elapsedMs)
    }

    private fun logFailure(joinPoint: ProceedingJoinPoint, elapsedMs: Long, ex: Throwable) {
        val logger = LoggerFactory.getLogger(joinPoint.signature.declaringType)
        val methodName = methodName(joinPoint)
        if (properties.logArgs) {
            val args = joinPoint.args.joinToString(prefix = "[", postfix = "]") { it.safeToString() }
            logger.error("aop failed elapsedMs={} method={} args={}", elapsedMs, methodName, args, ex)
        } else {
            logger.error("aop failed elapsedMs={} method={}", elapsedMs, methodName, ex)
        }
    }

    private fun logBasic(joinPoint: ProceedingJoinPoint, elapsedMs: Long) {
        val logger = LoggerFactory.getLogger(joinPoint.signature.declaringType)
        val methodName = methodName(joinPoint)
        if (properties.slowThresholdMs > 0 && elapsedMs >= properties.slowThresholdMs) {
            logger.warn("aop slow elapsedMs={} method={}", elapsedMs, methodName)
            return
        }
        logger.info("aop elapsedMs={} method={}", elapsedMs, methodName)
    }

    private fun logWithArgs(joinPoint: ProceedingJoinPoint, elapsedMs: Long, extraFormat: String, extraValue: Any?) {
        val logger = LoggerFactory.getLogger(joinPoint.signature.declaringType)
        val methodName = methodName(joinPoint)
        val format = if (properties.slowThresholdMs > 0 && elapsedMs >= properties.slowThresholdMs) {
            "aop slow elapsedMs={} method={} $extraFormat"
        } else {
            "aop elapsedMs={} method={} $extraFormat"
        }
        if (properties.slowThresholdMs > 0 && elapsedMs >= properties.slowThresholdMs) {
            logger.warn(format, elapsedMs, methodName, extraValue)
        } else {
            logger.info(format, elapsedMs, methodName, extraValue)
        }
    }

    private fun shouldLog(elapsedMs: Long): Boolean {
        return properties.slowThresholdMs <= 0 || elapsedMs >= properties.slowThresholdMs
    }

    private fun methodName(joinPoint: ProceedingJoinPoint): String {
        val declaring = joinPoint.signature.declaringType.simpleName
        return "$declaring.${joinPoint.signature.name}"
    }

    private fun elapsedMillis(startTime: Long): Long {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
    }

    private fun Any?.safeToString(): String = this?.toString() ?: "null"
}
