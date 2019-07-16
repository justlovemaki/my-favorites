package com.fav.common.resp;

public class BdDto<T> {
  private T data;

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
