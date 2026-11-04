package com.github.farzam_raoufi.template.service;

import com.github.farzam_raoufi.template.dto.config.SystemConfigDTO;
import com.github.farzam_raoufi.template.model.ConfigKey;

import java.util.List;

public interface SystemConfigService {

    // ---- typed read API (used by other services) ----
    String getString(ConfigKey key);

    int getInt(ConfigKey key);

    long getLong(ConfigKey key);

    boolean getBoolean(ConfigKey key);

    // ---- admin API ----
    List<SystemConfigDTO> getAll();

    SystemConfigDTO getByKey(String key);

    SystemConfigDTO updateValue(String key, String value);

    SystemConfigDTO resetToDefault(String key);
}