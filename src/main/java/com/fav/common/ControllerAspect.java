package com.fav.common;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.fav.common.resp.ResultDto;

import lombok.extern.slf4j.Slf4j;

/**
 * 日志记录aop
 */
@Aspect
@Component
@Slf4j
public class ControllerAspect {
  private static final String REQ_INFO_PATTERN = "%s->%s | %s.%s | req:%s";
  private static final String RESP_INFO_PATTERN = "%s->%s | %s.%s | resp:%s";

  private String getIpAddress(HttpServletRequest request) {
    String ip = request.getHeader("x-forwarded-for");
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_CLIENT_IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }

  @Pointcut("@annotation(com.fav.common.ControllerPlus)")
  public void controllerLogAspect() {}

  /**
   * 环绕通知
   */
  @Around(value = "controllerLogAspect()")
  public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    MethodSignature ms = (MethodSignature) joinPoint.getSignature();
    Object[] args = joinPoint.getArgs();
    HashMap<String, String> map = new HashMap<>();
    for (int i = 0; i < args.length; i++) {
      if (!(args[i] instanceof BindingResult)) {
        map.put("arg" + i, JSON.toJSONString(args[i]));
      }
    }
    String ip = getIpAddress(request);
    log.info(String.format(REQ_INFO_PATTERN, ip, request.getServerName(), ms.getDeclaringType().getSimpleName(), ms.getName(), JSON.toJSONString(map)));

    try {
//      ControllerPlus controllerPlus = ms.getMethod().getAnnotation(ControllerPlus.class);
      return joinPoint.proceed(args);
    } catch (Exception ex) {
      return handleUnexpectedServerError(ex, request, ms);
    }
  }

  /**
   * 在controller执行之后
   *
   * @param joinPoint the join point
   * @param result    the result
   */
  @AfterReturning(pointcut = "controllerLogAspect()", returning = "result")
  public void doAfterLog(JoinPoint joinPoint, Object result) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    MethodSignature ms = (MethodSignature) joinPoint.getSignature();
    ControllerPlus controllerPlus = ms.getMethod().getAnnotation(ControllerPlus.class);
    if (controllerPlus.printLog()) {
      log.info(String.format(RESP_INFO_PATTERN, getIpAddress(request), request.getServerName(), ms.getDeclaringType().getSimpleName(), ms.getName(), JSON.toJSONString(result)));
    } else {
      log.info(String.format(RESP_INFO_PATTERN, getIpAddress(request), request.getServerName(), ms.getDeclaringType().getSimpleName(), ms.getName(), JSON.toJSONString(result).length()));
    }
  }

  private Object handleUnexpectedServerError(Exception ex, HttpServletRequest request, MethodSignature ms) {
    log.error("出现系统异常,ip" + getIpAddress(request) + ",异常信息:" + ex.getMessage(), ex);
    return  ResultDto.get(null, 500, ex.getMessage());
  }
  
}
