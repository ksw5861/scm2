package com.yedam.scm.dto;

import lombok.Data;

@Data
public class PageDTO {

  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
  private boolean hasNext;
  private boolean hasPrevious;

  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_SIZE = 10;

  public PageDTO() {
    this.page = DEFAULT_PAGE;
    this.size = DEFAULT_SIZE;
  }

  public PageDTO(int page, int size) {
    this.page = (page > 0) ? page : DEFAULT_PAGE;
    this.size = (size > 0) ? size : DEFAULT_SIZE;
  }

  public void updatePageInfo(long totalElements) {
    this.page = (page > 0) ? page : DEFAULT_PAGE;
    this.size = (size > 0) ? size : DEFAULT_SIZE;

    this.totalElements = totalElements;
    this.totalPages = (int) Math.ceil((double) totalElements / size);
    this.hasNext = page < totalPages;
    this.hasPrevious = page > 1;
  }

  public int getStartRow() {
    return (page - 1) * size + 1;
  }

  public int getEndRow() {
    return page * size;
  }

  public int getOffset() {
    return (page - 1) * size;
  }

}
