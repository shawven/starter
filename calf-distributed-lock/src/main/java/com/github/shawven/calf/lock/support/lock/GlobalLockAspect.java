package com.github.shawven.calf.lock.support.lock;

import com.github.shawven.calf.lock.support.constant.CacheName;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GlobalLockAspect {

    private final Logger logger = LoggerFactory.getLogger(GlobalLockAspect.class);

    private final static String PROCESSING_IN = "正常处理请稍后...";

    @Autowired
    private RedissonClient redissonClient;

    @Pointcut("@annotation(com.github.shawven.calf.lock.support.lock.GlobalLock) " +
            "|| @annotation(com.github.shawven.calf.lock.support.lock.GlobalTryLock)")
    private void pointcut() {}


    @Around("pointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        GlobalLock annotation = extractAnnotation(method);

        String lockKey = getLockKey(pjp);
        RLock lock = redissonClient.getLock(lockKey);

        long waitTime = annotation.waitTime();
        long leaseTime = annotation.leaseTime();
        boolean release = annotation.release();
        String fallback = annotation.fallback();

        if (annotation.tryLock()) {
            boolean locked = false;
            try {
                locked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("try acquire globalLock {} interrupted!", lockKey);
                    return invokeFallback(pjp, fallback);
                }
            }
            try {
                if (locked) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("try acquire globalLock {} success!", lockKey);
                    }

                    return pjp.proceed();
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("try acquire globalLock {} failure!", lockKey);
                    }

                    return invokeFallback(pjp, fallback);
                }
            } finally {
                if (release && locked) {
                    release(lock, lockKey);
                }
            }
        } else {
            try {
                lock.lockInterruptibly(leaseTime, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("acquire globalLock {} interrupted!", lockKey);
                    return invokeFallback(pjp, fallback);
                }
            }
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("acquire globalLock {} success!", lockKey);
                }

                return pjp.proceed();
            } finally {
                if (release) {
                    release(lock, lockKey);
                }
            }
        }
    }

    /**
     * 调用降级方法
     *
     * @param joinPoint
     * @param fallback
     * @return
     */
    private Object invokeFallback(JoinPoint joinPoint, String fallback) {
        if (StringUtils.isBlank(fallback)) {
            throw new IllegalArgumentException("fallback should not be empty");
        }

        Object target = joinPoint.getTarget();
        try {
            Method method = target.getClass().getDeclaredMethod(fallback);
            return method.invoke(joinPoint.getThis(), joinPoint.getArgs());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private void release(RLock lock, String lockKey) {
        try {
            lock.unlock();
            logger.debug("release globalLock {} success!", lockKey);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 获取锁key
     *
     * @param joinPoint
     * @return
     */
    private String getLockKey(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        GlobalLock annotation = extractAnnotation(method);
        String key = annotation.value();
        if (StringUtils.isBlank(key)) {
            key = ClassUtils.getAbbreviatedName(joinPoint.getTarget().getClass(), 1)
                    + ":" + method.getName()
                    + "@" + Integer.toHexString(resolveMethodName(method).hashCode());
        }
        String[] keys = annotation.key();
        if (ArrayUtils.isNotEmpty(keys)) {
            String suffix = SpELUtils.parse(joinPoint, keys);
            if (StringUtils.isNotBlank(suffix)) {
                key = key + ":" + suffix;
            }
        }
        return CacheName.globalLock(key);
    }

    public String resolveMethodName(Method method) {
        StringBuilder sb = new StringBuilder();

        String className = method.getDeclaringClass().getName();
        String name = method.getName();
        Class<?>[] params = method.getParameterTypes();
        sb.append(className).append(":").append(name);
        sb.append("(");

        int paramPos = 0;
        for (Class<?> clazz : params) {
            sb.append(clazz.getCanonicalName());
            if (++paramPos < params.length) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private GlobalLock extractAnnotation(Method method) {
        GlobalLock mergedAnnotation = AnnotatedElementUtils.getMergedAnnotation(method, GlobalLock.class);
        if (mergedAnnotation == null) {
            throw new IllegalStateException("the merged annotation GlobalLock was not found");
        }
        return mergedAnnotation;
    }

}
