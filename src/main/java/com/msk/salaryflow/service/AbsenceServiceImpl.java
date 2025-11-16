package com.msk.salaryflow.service;

import com.msk.salaryflow.entity.Absence;
import com.msk.salaryflow.repository.AbsenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AbsenceServiceImpl implements AbsenceService {

    private final AbsenceRepository repository;
    private final CacheManager cacheManager;

    @Autowired
    public AbsenceServiceImpl(AbsenceRepository repository,   @Qualifier("mongoRedisCacheManager")CacheManager cacheManager) {
        this.repository = repository;
        this.cacheManager = cacheManager;
    }

    @Override
    // manual caching to avoid serializing PageImpl directly (jackson can't deserialize PageImpl)
    public Page<Absence> findAll(Pageable pageable) {
        String key = pageable.getPageNumber() + "-" + pageable.getPageSize();
        Cache cache = cacheManager.getCache("absence_pages");
        if (cache != null) {
            try {
                PageDto dto = cache.get(key, PageDto.class);
                if (dto != null) {
                    PageRequest pr = PageRequest.of(dto.number, dto.size);
                    return new PageImpl<>(dto.content, pr, dto.totalElements);
                }
            } catch (Exception ex) {
                // If deserialization fails (stale PageImpl or incompatible payload), evict the bad entry and continue.
                try { cache.evict(key); } catch (Exception ignore) {}
            }
        }

        Page<Absence> page = repository.findAll(pageable);

        if (cache != null) {
            PageDto dto = new PageDto(page.getContent(), page.getTotalElements(),
                    page.getNumber(), page.getSize(), page.getTotalPages(),
                    page.isFirst(), page.isLast());
            try { cache.put(key, dto); } catch (Exception ignore) {}
        }

        return page;
    }

    @Override
    @Cacheable(value = "absence", key = "#id", cacheManager = "mongoRedisCacheManager")
    public Optional<Absence> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    @CacheEvict(value = {"absence", "absence_pages"}, allEntries = true, cacheManager = "mongoRedisCacheManager")
    public Absence save(Absence absence) {
        if (absence.getId() == null) {
            absence.setId(UUID.randomUUID());
        }
        return repository.save(absence);
    }

    @Override
    @CacheEvict(value = {"absence", "absence_pages"}, allEntries = true, cacheManager = "mongoRedisCacheManager")
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    // Simple serializable DTO for caching page data
    static class PageDto implements Serializable {
        private static final long serialVersionUID = 1L;
        public List<Absence> content;
        public long totalElements;
        public int number;
        public int size;
        public int totalPages;
        public boolean first;
        public boolean last;

        // default ctor needed for jackson
        public PageDto() {}

        public PageDto(List<Absence> content, long totalElements, int number, int size,
                       int totalPages, boolean first, boolean last) {
            this.content = content;
            this.totalElements = totalElements;
            this.number = number;
            this.size = size;
            this.totalPages = totalPages;
            this.first = first;
            this.last = last;
        }
    }
}
