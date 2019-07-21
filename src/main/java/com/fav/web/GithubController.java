package com.fav.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fav.common.ControllerPlus;

import cn.hutool.http.HttpRequest;

@RestController
@RequestMapping
public class GithubController {

  @ControllerPlus
  @RequestMapping(value = "/gtb")
  public String gtb(@RequestParam(required=false) String tk, @RequestParam(required=false)  String url) throws Exception {
    return getGithub(tk, url);
  }

  private String getGithub(String tk, String url) throws UnsupportedEncodingException {
    url = URLDecoder.decode(url, "UTF-8");
    return HttpRequest.get(url)
        .header("Authorization", "token " + tk)
        .header("access-control-allow-origin", "*")
        .timeout(10000).execute().body();
  }
}
