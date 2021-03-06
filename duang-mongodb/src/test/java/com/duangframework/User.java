package com.duangframework;

import com.alibaba.fastjson.annotation.JSONField;
import com.duangframework.core.annotation.db.Entity;
import com.duangframework.core.annotation.db.VoColl;
import com.duangframework.core.common.IdEntity;
import com.duangframework.vo.UserVo;

import java.util.List;
import java.util.Map;

/**
 * @author Created by laotang
 * @date on 2017/11/22.
 */
@Entity(name = "testUser")
public class User extends IdEntity {

    @JSONField(name = "user_age")
    private int age;
    private String name;
    private long white;
    private Map<String, String> addressMap ;
    @VoColl
    private List<UserVo> typeList;
    @VoColl
    private Map<String, UserVo> userVoMap ;



    public long getWhite() {
        return white;
    }

    public void setWhite(long white) {
        this.white = white;
    }

    public double getHight() {
        return hight;
    }

    public void setHight(double hight) {
        this.hight = hight;
    }

    private double hight;

    public Map<String, String> getAddressMap() {
        return addressMap;
    }

    public void setAddressMap(Map<String, String> addressMap) {
        this.addressMap = addressMap;
    }

    public List<UserVo> getTypeList() {
        return typeList;
    }

    public void setTypeList(List<UserVo> typeList) {
        this.typeList = typeList;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, UserVo> getUserVoMap() {
        return userVoMap;
    }

    public void setUserVoMap(Map<String, UserVo> userVoMap) {
        this.userVoMap = userVoMap;
    }
}
