package com.fav.common;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fav.common.resp.ResultDto;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一异常捕获
 */
@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandle {
  
  /**
   * 处理未知异常
   *
   * @param ex the ex
   * @return the message dto
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(value = HttpStatus.OK)
  @ResponseBody
  public ResultDto<?> handleUnexpectedServerError(Exception ex, HttpServletRequest request) {
    log.error("访问异常", ex);
    Map<String,Object> p =new HashMap<>();
    p.put("msg", ex.getMessage());
    p.put("url", request.getRequestURL());
    return ResultDto.get(p, 500, "访问异常");
  }

}
