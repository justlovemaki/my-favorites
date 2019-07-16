package com.fav.common.resp;

public class ResDto<T> {
  private HdDto hd;
  private BdDto<T> bd;

  public HdDto getHd() {
    return hd;
  }

  public void setHd(HdDto hd) {
    this.hd = hd;
  }

  public BdDto<T> getBd() {
    return bd;
  }

  public void setBd(BdDto<T> bd) {
    this.bd = bd;
  }
}
