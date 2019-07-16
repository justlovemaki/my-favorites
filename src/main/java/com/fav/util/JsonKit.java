package com.fav.util;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;


/**
 * JsonUtil
 */
public class JsonKit {
//	private static Gson gson;
//	static{
//		GsonBuilder builder = new GsonBuilder();
//		//builder.setPrettyPrinting();//数据格式美化
//		builder.excludeFieldsWithModifiers(Modifier.PROTECTED);//不转换保护类型的属性
//		builder.disableHtmlEscaping();//不允许html转意字符串
//		gson = builder.create();
//	}
	
//	public static String toJsonPretty(Object bean){
//		GsonBuilder builder = new GsonBuilder();
//		builder.setPrettyPrinting();//数据格式美化
//		builder.excludeFieldsWithModifiers(Modifier.PROTECTED);//不转换保护类型的属性
//		builder.disableHtmlEscaping();//不允许html转意字符串
//		gson = builder.create();
//		return gson.toJson(bean);
//	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午10:23:45
	 * @Title bean2JSON
	 * @param bean
	 * @return String 返回类型
	 * @category 对象转换为JSON并用gzip压缩
	 */
	public static String bean2JSON(Object bean) {
		return JSONObject.toJSONString(bean);
	}
	
	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午10:24:21
	 * @Title json2Bean
	 * @param json
	 * @param clazz
	 * @return T 返回类型
	 * @category JSON转换为对象
	 */
	public static <T> T json2Bean(String json, Class<T> clazz) {
	  return JSONObject.parseObject(json, clazz);
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午10:24:48
	 * @Title json2Map
	 * @param json
	 * @return Map<String,String> 返回类型
	 * @category JSON转换为Map
	 */
	@SuppressWarnings("unchecked")
  public static Map<String, String> json2Map(String json){
	  return JSONObject.parseObject(json, Map.class);
	}
	
	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午10:25:07
	 * @Title map2JSON
	 * @param map
	 * @return String 返回类型
	 * @category Map转换为JSON
	 */
	public static String map2JSON(Map<String, String> map) {
		return JSONObject.toJSONString(map);
	}
	
	public static List<String> json2List(String jsonlist) {
		return JSONObject.parseArray(jsonlist, String.class);
	}
}
