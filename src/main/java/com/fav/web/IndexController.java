package com.fav.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fav.common.ControllerPlus;

@Controller
@RequestMapping
public class IndexController {

  @ControllerPlus
  @RequestMapping(value = "/")
  public String indexRoot() {
    return "index";
  }
  
  @ControllerPlus
  @RequestMapping(value = "/{name}")
  public String indexName(@PathVariable String name) {
    return "index";
  }

}
