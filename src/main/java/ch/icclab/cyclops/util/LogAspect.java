package ch.icclab.cyclops.util;

import ch.icclab.cyclops.services.iaas.openstack.resource.impl.TelemetryResource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Created by Konstantin on 22.10.2015.
 */
@Aspect
public class LogAspect {
    final static Logger logger = LogManager.getLogger(TelemetryResource.class.getName());

    /*long start = System.currentTimeMillis();
        Object result = point.proceed();
        Logger.info(
                "#%s(%s): %s in %[msec]s",
                MethodSignature.class.cast(point.getSignature()).getMethod().getName(),
                point.getArgs(),
                result,
                System.currentTimeMillis() - start
        );*/

    @Around("execution(* *(..)) && @annotation(Loggable)")
    public void around(ProceedingJoinPoint point) {
        System.out.println(MethodSignature.class.cast(point.getSignature()).getMethod().getName());
        logger.trace("BEGIN ASPECT " + MethodSignature.class.cast(point.getSignature()).getMethod().getName());
        logger.trace("END ASPECT " + MethodSignature.class.cast(point.getSignature()).getMethod().getName());
    }
}
