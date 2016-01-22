/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */
package ch.icclab.cyclops.util;

import ch.icclab.cyclops.services.iaas.openstack.resource.impl.TelemetryResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;


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
