package com.fav.common.msg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PushService implements IPushService{
  @Value("${pushjiang.sendurl}")
  private String url;
  @Value("${pushjiang.sendkey}")
  private String key;

  
  @Override
  public void pushMsgWithOutMsg(String title) {
    pushMsg(title, "");
  }
  
  @Override
  public void pushMsg(String title,String msg) {
    String pushurl = url.replace("{$1}", key).replace("{$2}", title).replace("{$3}", msg);
    String resp = HttpUtil.get(pushurl);
    log.info(resp);
  }
}
