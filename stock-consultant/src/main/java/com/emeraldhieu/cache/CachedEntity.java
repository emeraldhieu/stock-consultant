package com.emeraldhieu.cache;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CachedEntity<T> {
    private T entity;
}
