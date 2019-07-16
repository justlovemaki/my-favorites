package com.fav.common.resp;

import java.util.List;

import org.springframework.data.domain.Page;

public class PageDto {
  private int totalPages;
  private long totalElements;
  private List<?> list;
  private int size;
  private int pageNumber;

  public PageDto(Page<?> page) {
    /// 获取总页数
    this.totalPages = page.getTotalPages();
    /// 获取总元素个数
    this.totalElements = page.getTotalElements();
    /// 获取该分页的列表
    this.list = page.getContent();
    this.size = page.getSize();
    this.pageNumber = page.getNumber();
  }

  public int getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(int totalPages) {
    this.totalPages = totalPages;
  }

  public long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(long totalElements) {
    this.totalElements = totalElements;
  }

  public List<?> getList() {
    return list;
  }

  public void setList(List<?> list) {
    this.list = list;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

}
