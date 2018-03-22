package com.duangframework.mysql.plugin;

import com.duangframework.core.annotation.db.Entity;
import com.duangframework.core.annotation.db.Index;
import com.duangframework.core.exceptions.MvcStartUpException;
import com.duangframework.core.exceptions.MysqlException;
import com.duangframework.core.interfaces.IPlugin;
import com.duangframework.core.kit.ToolsKit;
import com.duangframework.core.utils.BeanUtils;
import com.duangframework.core.utils.ClassUtils;
import com.duangframework.mysql.common.IMySql;
import com.duangframework.mysql.common.MySqlConnect;
import com.duangframework.mysql.utils.MysqlUtils;
import io.netty.util.internal.ObjectUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Mysql 插件
 * @author Created by laotang
 * @date on 2017/11/20.
 */
public class MysqlPlugin implements IPlugin {

    private List<MySqlConnect> connectList = new ArrayList<>();

    /**
     * 单数据库时使用
     * @param host
     * @param port
     * @param userName
     * @param passWord
     * @param dataBase
     */
    public MysqlPlugin(String host, int port, String userName, String passWord,String dataBase) {
        MySqlConnect connect = new MySqlConnect(host, port, userName, passWord, dataBase);
        connectList.add(connect);
    }

    /**
     * 多数据库时使用
     * @param connectList
     */
    public MysqlPlugin(List<MySqlConnect> connectList) {
        this.connectList.addAll(connectList);
    }

    /**
     * 多数据库时使用
     * @param model
     */
    public MysqlPlugin(IMySql model) {
        try {
            this.connectList.addAll(model.builderConnects());
        } catch (Exception e) {
            throw new MvcStartUpException("start MysqlPlugin is fail: " + e.getMessage(), e);
        }
    }

    @Override
    public void init() throws Exception {
        // 可以初始一些值，框架先执行init方法后再执行start
    }

    @Override
    public void start() throws Exception {
        try {
            MysqlUtils.initDataSource(connectList);
            // 创建表与索引
            Map<Class<?>, Object> entityMap = BeanUtils.getAllBeanMaps().get(Entity.class.getSimpleName());
            if(ToolsKit.isEmpty(entityMap)) {
                return;
            }
            for(Iterator<Class<?>> iterator = entityMap.keySet().iterator() ; iterator.hasNext();) {
                Class<?> entityClass = iterator.next();
                Field[] fields = ClassUtils.getFields(entityClass);
                if (ToolsKit.isEmpty(fields)) {
                    return;
                }
                for (int i = 0; i < fields.length; i++) {
                    Index index = fields[i].getAnnotation(Index.class);
                    if (ToolsKit.isNotEmpty(index)) {
                        String name = ToolsKit.isEmpty(index.name()) ? "_" + fields[i].getName() + "_" : index.name();
                        // TODO 创建索引
                    }
                }
            }
        } catch (Exception e) {
            throw new MysqlException("connection is fail: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() throws Exception {

    }
}