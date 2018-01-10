package com.duangframework.config.kit;

import com.duangframework.config.core.ConfigFactory;
import com.duangframework.core.common.enums.IConfigKeyEnums;
import com.duangframework.core.interfaces.IConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 用作整个系统的配置
 * @author laotang
 * @date 2017/11/16 0016
 */
public class ConfigKit {

    private static Logger logger = LoggerFactory.getLogger(ConfigKit.class);

    private static ConfigKit _configKit;
    private static Lock _configKitLock = new ReentrantLock();
    private static IConfig _configuration;
    private static String _key;
    private static Object _defaultValue;

    public static ConfigKit duang() {
        if(null == _configKit) {
            try {
                _configKitLock.lock();
                _configKit = new ConfigKit();
                _configuration = ConfigFactory.getClient();
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            } finally {
                _configKitLock.unlock();
            }
        }
        clear();
        return _configKit;
    }

    private static void clear() {
        _key = "";
        _defaultValue = null;
    }

    private ConfigKit() {
    }

    public ConfigKit key(String key) {
        _key = key;
        return this;
    }

    public ConfigKit key(IConfigKeyEnums keyEnums) {
        _key = keyEnums.getValue();
        return this;
    }



    public ConfigKit defaultValue(Object defaultValue) {
        _defaultValue = defaultValue;
        return this;
    }

    public String asString() {
        try {
            return _configuration.getString(_key, _defaultValue+"");
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return null;
        }
    }

    public int asInt() {
        try {
            return _configuration.getInt(_key, Integer.parseInt(_defaultValue + ""));
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return -1;
        }
    }

    public long asLong() {
        try {
            return _configuration.getLong(_key, Long.parseLong(_defaultValue + ""));
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return -1L;
        }
    }

    public double asDouble() {
        try {
            return _configuration.getDouble(_key, Double.parseDouble(_defaultValue + ""));
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return -1d;
        }
    }

    public String[] asArray() {
        try {
            return _configuration.getStringArray(_key);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return null;
        }
    }

    public List<String> asList() {
        try {
            return  _configuration.getList(_key, new ArrayList());
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return null;
        }
    }



}