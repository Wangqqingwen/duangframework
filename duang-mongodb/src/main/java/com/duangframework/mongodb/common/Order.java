package com.duangframework.mongodb.common;

import com.duangframework.core.kit.ToolsKit;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 排序对象
 * @author laotang
 *
 */
public class Order {

	private LinkedHashMap<String, String> orderLinkedMap = null;
	public final static String ASC = "asc";
	public final static String DESC = "desc";
	
	
	public Order() {
		orderLinkedMap = new LinkedHashMap<String,String>();
	}
	
	/**
	 * 添加排序
	 * @param fieldName		排序的字段名
	 * @param order			排序方向
	 * @return
	 */
	public Order add(String fieldName, String order) {
		orderLinkedMap.put(fieldName, order);
		return this;
	}
	
	public Map<String,String> getOrder() {
		return orderLinkedMap;
	}
	
	public DBObject getDBOrder() {
		DBObject orderObj = new BasicDBObject();
		if(ToolsKit.isNotEmpty(orderLinkedMap)){
			for(Iterator<Entry<String,String>> it = orderLinkedMap.entrySet().iterator(); it.hasNext();){
				Entry<String,String> entry = it.next();
//				orderObj.putAll( MongoKit.builderOrder(entry.getKey(), entry.getValue()) );
			}
		}
		return orderObj;
	}
}
