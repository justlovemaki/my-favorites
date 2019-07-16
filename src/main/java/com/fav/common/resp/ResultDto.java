package com.fav.common.resp;

public class ResultDto<T> {
  private static final int SUCC = 1;
  private static final int FAIL = 0;
  private ResDto<T> res;

  public static <E> ResultDto<E> getSuccess(E data) {
    return get(data, SUCC, "操作成功");
  }
  
  public static <E> ResultDto<E> getFail(String desc) {
    return get(null, FAIL, desc);
  }

  public static <E> ResultDto<E> get(E data, Integer code, String desc) {
    ResultDto<E> msg = new ResultDto<>();
    ResDto<E> res = new ResDto<>();
    msg.setRes(res);

    HdDto hdDto = new HdDto();
    hdDto.setCode(code);
    hdDto.setDesc(desc);
    res.setHd(hdDto);

    BdDto<E> bdDto = new BdDto<>();
    bdDto.setData(data);
    res.setBd(bdDto);
    return msg;
  }

  public ResDto<T> getRes() {
    return res;
  }

  public void setRes(ResDto<T> res) {
    this.res = res;
  }
}
