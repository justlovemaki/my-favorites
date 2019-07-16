package com.fav;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.catalina.filters.RemoteIpFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.fav.common.ControllerInterceptor;

@Configuration
public class WebConfiguration extends WebMvcConfigurationSupport  {

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    // 注册Spring data jpa pageable的参数分解器
    argumentResolvers.add(new PageableHandlerMethodArgumentResolver());
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    // 移除jackson转换器
    Iterator<HttpMessageConverter<?>> iterator = converters.iterator();
    while (iterator.hasNext()) {
      HttpMessageConverter<?> h = iterator.next();
      if (h instanceof MappingJackson2HttpMessageConverter) {
        iterator.remove();
      }
      if (h instanceof StringHttpMessageConverter) {
        StringHttpMessageConverter s = (StringHttpMessageConverter) h;
        s.setDefaultCharset(Charset.forName("UTF8"));
      }
      if (h instanceof Jaxb2RootElementHttpMessageConverter) {
        Jaxb2RootElementHttpMessageConverter j = (Jaxb2RootElementHttpMessageConverter) h;
        j.setDefaultCharset(Charset.forName("UTF8"));
      }
    }
    // 1.需要定义一个Convert转换消息的对象
    FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
    // 2.添加fastjson的配置信息，比如是否要格式化返回的json数据
    FastJsonConfig fastJsonConfig = new FastJsonConfig();
    fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
    // fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat,
    // SerializerFeature.BrowserSecure, SerializerFeature.WriteBigDecimalAsPlain,
    // SerializerFeature.WriteEnumUsingToString, SerializerFeature.WriteDateUseDateFormat);
    // 配置处理类型
    List<MediaType> fastMediaTypes = new ArrayList<MediaType>();
    fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
    fastMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
    fastConverter.setSupportedMediaTypes(fastMediaTypes);
    // 3.在convert中添加配置信息
    fastConverter.setFastJsonConfig(fastJsonConfig);
    converters.add(fastConverter);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    /**
     * 多个拦截器组成一个拦截器链
     * addPathPatterns 用于添加拦截规则
     * excludePathPatterns 用于排除拦截
     */
    registry.addInterceptor(new ControllerInterceptor()).addPathPatterns("/**");
    super.addInterceptors(registry);
  }
  
  @Override
  protected void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**").addResourceLocations("classpath:/META-INF/resources/").addResourceLocations("classpath:/resources/")
    .addResourceLocations("classpath:/static/").addResourceLocations("classpath:/public/");
    super.addResourceHandlers(registry);
  }

  
  @Bean
  public RemoteIpFilter remoteIpFilter() {
    return new RemoteIpFilter();
  }

  
  // 跨域访问时启用,注解支持跨域　@CrossOrigin(value = "*")
  // @Bean
  public FilterRegistrationBean<?> corsFilter() {
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      CorsConfiguration config = new CorsConfiguration();
      config.setAllowCredentials(true);
      config.addAllowedOrigin("*");
      source.registerCorsConfiguration("/**", config);
      FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean(new CorsFilter(source));
      bean.setOrder(0);
      return bean;
  }
}
