package com.example.shop.common;

import com.example.shop.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageRequests {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_FIELD = "id";

    private PageRequests() {
    }

    /**
     * Builds a {@link Pageable} from {@code page}, {@code size} and {@code sort=field,asc|desc}.
     * Only whitelisted fields are sortable so clients cannot sort on internal columns.
     */
    public static Pageable of(Integer page, Integer size, String sort, Set<String> sortableFields) {
        int pageNumber = page == null ? DEFAULT_PAGE : page;
        int pageSize = size == null ? DEFAULT_SIZE : size;
        return PageRequest.of(pageNumber, pageSize, parseSort(sort, sortableFields));
    }

    private static Sort parseSort(String sort, Set<String> sortableFields) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(DEFAULT_SORT_FIELD).ascending();
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        if (!sortableFields.contains(field)) {
            throw new BadRequestException("INVALID_SORT_FIELD",
                    "Không thể sắp xếp theo trường '%s'".formatted(field),
                    "sort", "Các trường hợp lệ: " + String.join(", ", sortableFields.stream().sorted().toList()));
        }
        Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }
}
