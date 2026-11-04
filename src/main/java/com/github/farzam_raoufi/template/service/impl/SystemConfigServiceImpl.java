package com.github.farzam_raoufi.template.service.impl;

import com.github.farzam_raoufi.template.dto.config.SystemConfigDTO;
import com.github.farzam_raoufi.template.exception.BusinessException;
import com.github.farzam_raoufi.template.exception.ErrorMessage;
import com.github.farzam_raoufi.template.model.ConfigKey;
import com.github.farzam_raoufi.template.model.ConfigValueType;
import com.github.farzam_raoufi.template.model.SystemConfig;
import com.github.farzam_raoufi.template.repository.SystemConfigRepository;
import com.github.farzam_raoufi.template.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final long CACHE_TTL_MILLIS = 30_000;
    private static final String MASK = "********";

    private final SystemConfigRepository repository;

    /** Cached copy of all admin overrides (the table is tiny). */
    private volatile Snapshot snapshot = Snapshot.EMPTY;

    private record Snapshot(Map<String, String> values, long loadedAtMillis) {
        static final Snapshot EMPTY = new Snapshot(Map.of(), 0L);

        boolean expired() {
            return System.currentTimeMillis() - loadedAtMillis >= CACHE_TTL_MILLIS;
        }
    }

    // ------------------------------------------------------------------
    // Typed read API
    // ------------------------------------------------------------------

    @Override
    public String getString(ConfigKey key) {
        requireType(key, ConfigValueType.STRING);
        return resolve(key);
    }

    @Override
    public int getInt(ConfigKey key) {
        requireType(key, ConfigValueType.INTEGER);
        try {
            return Integer.parseInt(resolve(key));
        } catch (NumberFormatException e) {
            log.warn("Config '{}' holds an invalid integer, using default", key.getKey());
            return Integer.parseInt(key.getDefaultValue());
        }
    }

    @Override
    public long getLong(ConfigKey key) {
        requireType(key, ConfigValueType.LONG);
        try {
            return Long.parseLong(resolve(key));
        } catch (NumberFormatException e) {
            log.warn("Config '{}' holds an invalid long, using default", key.getKey());
            return Long.parseLong(key.getDefaultValue());
        }
    }

    @Override
    public boolean getBoolean(ConfigKey key) {
        requireType(key, ConfigValueType.BOOLEAN);
        return Boolean.parseBoolean(resolve(key));
    }

    // ------------------------------------------------------------------
    // Admin API
    // ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<SystemConfigDTO> getAll() {
        Map<String, SystemConfig> overrides = repository.findAll().stream()
                .collect(Collectors.toMap(SystemConfig::getConfigKey, Function.identity()));
        return Arrays.stream(ConfigKey.values())
                .map(k -> toDTO(k, overrides.get(k.getKey())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SystemConfigDTO getByKey(String key) {
        ConfigKey configKey = findKey(key);
        return toDTO(configKey, repository.findByConfigKey(configKey.getKey()).orElse(null));
    }

    // Intentionally NOT @Transactional: save() commits on its own, so the cache
    // is invalidated only after the new value is really in the database.
    @Override
    public SystemConfigDTO updateValue(String key, String value) {
        ConfigKey configKey = findKey(key);

        // The panel sent the masked placeholder back: keep the stored secret.
        if (configKey.isSensitive() && MASK.equals(value)) {
            return getByKey(key);
        }

        String normalized = validateAndNormalize(configKey, value);

        SystemConfig entity = repository.findByConfigKey(configKey.getKey())
                .orElseGet(() -> SystemConfig.builder().configKey(configKey.getKey()).build());
        entity.setConfigValue(normalized);
        entity.setUpdatedBy(currentUsername());

        SystemConfig saved = repository.save(entity);
        invalidateCache();

        log.info("Config '{}' updated by '{}'", configKey.getKey(), saved.getUpdatedBy()); // never log the value
        return toDTO(configKey, saved);
    }

    @Override
    public SystemConfigDTO resetToDefault(String key) {
        ConfigKey configKey = findKey(key);
        repository.findByConfigKey(configKey.getKey()).ifPresent(repository::delete);
        invalidateCache();
        log.info("Config '{}' reset to default by '{}'", configKey.getKey(), currentUsername());
        return toDTO(configKey, null);
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    private String resolve(ConfigKey key) {
        Snapshot current = snapshot;
        if (current.expired()) {
            current = refresh();
        }
        return current.values().getOrDefault(key.getKey(), key.getDefaultValue());
    }

    private synchronized Snapshot refresh() {
        Snapshot current = snapshot;
        if (!current.expired()) { // another thread already refreshed
            return current;
        }
        try {
            Map<String, String> values = new HashMap<>();
            for (SystemConfig c : repository.findAll()) {
                values.put(c.getConfigKey(), c.getConfigValue() == null ? "" : c.getConfigValue());
            }
            snapshot = new Snapshot(Map.copyOf(values), System.currentTimeMillis());
        } catch (RuntimeException e) {
            // DB hiccup: keep serving the previous values instead of failing the caller
            log.warn("Could not reload system configs, keeping previous values: {}", e.getMessage());
            snapshot = new Snapshot(current.values(), System.currentTimeMillis());
        }
        return snapshot;
    }

    private void invalidateCache() {
        snapshot = Snapshot.EMPTY;
    }

    private ConfigKey findKey(String key) {
        return ConfigKey.fromKey(key)
                .orElseThrow(() -> new BusinessException(ErrorMessage.CONFIG_NOT_FOUND, key));
    }

    private void requireType(ConfigKey key, ConfigValueType expected) {
        if (key.getType() != expected) {
            throw new IllegalArgumentException(
                    "Config " + key.getKey() + " is " + key.getType() + ", not " + expected);
        }
    }

    private String validateAndNormalize(ConfigKey key, String raw) {
        String value = raw == null ? "" : raw.strip();
        try {
            return switch (key.getType()) {
                case INTEGER -> String.valueOf(Integer.parseInt(value));
                case LONG -> String.valueOf(Long.parseLong(value));
                case BOOLEAN -> normalizeBoolean(value);
                case STRING -> value;
            };
        } catch (IllegalArgumentException e) { // NumberFormatException is a subclass
            throw new BusinessException(ErrorMessage.CONFIG_VALUE_INVALID, key.getKey(), key.getType());
        }
    }

    private String normalizeBoolean(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return value.toLowerCase();
        }
        throw new IllegalArgumentException("not a boolean");
    }

    private SystemConfigDTO toDTO(ConfigKey key, SystemConfig override) {
        boolean overridden = override != null;
        String value = overridden && override.getConfigValue() != null
                ? override.getConfigValue()
                : key.getDefaultValue();
        return SystemConfigDTO.builder()
                .key(key.getKey())
                .value(mask(key, value))
                .defaultValue(mask(key, key.getDefaultValue()))
                .type(key.getType())
                .sensitive(key.isSensitive())
                .overridden(overridden)
                .description(key.getDescription())
                .updatedAt(overridden ? override.getUpdatedAt() : null)
                .updatedBy(overridden ? override.getUpdatedBy() : null)
                .build();
    }

    private String mask(ConfigKey key, String value) {
        return key.isSensitive() && value != null && !value.isEmpty() ? MASK : value;
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}