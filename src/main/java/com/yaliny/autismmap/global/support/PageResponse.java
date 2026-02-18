package com.yaliny.autismmap.global.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

// TODO 추후에 페이징 처리로 진행할 때 해당 공통 response로 수정하기
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber() + 1,
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext()
        );
    }

    public <U> PageResponse<U> map(Function<? super T, U> converter) {
        return new PageResponse<>(
            this.content.stream().map(converter).toList(),
            this.page,
            this.size,
            this.totalElements,
            this.totalPages,
            this.hasNext
        );
    }
}

