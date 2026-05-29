package com.star.es.domain.dto;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class UserSearchDTO {

    private String name;

    private Integer age;

    private Integer minAge;

    private Integer maxAge;

    private String email;

    private int page = 0;

    private int size = 10;

    private String sortField = "id";

    private String sortOrder = "desc";

    public Pageable toPageable() {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }
}
