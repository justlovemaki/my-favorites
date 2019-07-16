package com.fav.common.msg;

public interface IPushService {

  void pushMsg(String title, String msg);

  void pushMsgWithOutMsg(String title);

}
