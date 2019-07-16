package com.fav.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.types.ObjectId;

/**
 * 
 * @ClassName StrUtils
 * @author Meteor
 * @date 2015年8月9日 上午11:15:52
 * @category 字符串处理工具类
 */
public class StringKit {

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:14:58
	 * @Title leftStr
	 * @param source	原始字符串
	 * @param maxByteLen截取的字节数
	 * @param flag		表示处理汉字的方式。1表示遇到半个汉字时补全，-1表示遇到半个汉字时舍
	 * @return String 返回类型
	 * @category 获得左边正确的汉字
	 */
	public static String leftStr(String source, int maxByteLen, int flag) {
		if (source == null || maxByteLen <= 0) {
			return "";
		}
		byte[] bStr = source.getBytes();
		if (maxByteLen >= bStr.length)
			return source;
		String cStr = new String(bStr, maxByteLen - 1, 2);
		if (cStr.length() == 1 && source.contains(cStr)) {
			maxByteLen += flag;
		}
		return new String(bStr, 0, maxByteLen);
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:17:19
	 * @Title isChinese
	 * @param s
	 * @return boolean 返回类型
	 * @category 是否全部是汉字
	 */
	public static boolean isChinese(String s) {
		Pattern p1 = Pattern.compile("^[\u4e00-\u9fa5]+$");
		Matcher m1 = p1.matcher(s);
		return m1.find();
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:17:28
	 * @Title isChinese
	 * @param c
	 * @return boolean 返回类型
	 * @category 是否汉字
	 */
	public static boolean isChinese(char c) {
		return (c + "").matches("[\u4E00-\u9FA5]");
	}

	
	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:19:27
	 * @Title toXmlFormat
	 * @param s
	 * @return String 返回类型
	 * @category 替换XML中不能有的特殊字符
	 */
	public static String toXmlFormat(String s) {
		// &lt; < 小于号
		// &gt; > 大于号
		// &amp; & 和,与
		// &apos; ' 单引号
		// &quot; " 双引号
		s = Replace(s, "&", "&amp;");
		s = Replace(s, "<", "&lt;");
		s = Replace(s, ">", "&gt;");
		s = Replace(s, "'", "&apos;");
		s = Replace(s, "\\\"", "&quot;");
		return s;
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:19:56
	 * @Title isDigit
	 * @param validString
	 * @return boolean 返回类型
	 * @category 判断字符串是否全数字
	 */
	public static boolean isDigit(String validString) {
		if (validString == null)
			return false;
		byte[] tempbyte = validString.getBytes();
		for (int i = 0; i < validString.length(); i++) {
			// by=tempbyte[i];
			if ((tempbyte[i] < 48) || (tempbyte[i] > 57)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:21:58
	 * @Title Replace
	 * @param source
	 * @param oldString
	 * @param newString
	 * @return String 返回类型
	 * @category 替换字符串
	 */
	public static String Replace(String source, String oldString, String newString) {
		if (source == null)
			return null;
		StringBuffer output = new StringBuffer();
		int lengOfsource = source.length();
		int lengOfold = oldString.length();
		int posStart = 0;
		int pos;
		while ((pos = source.indexOf(oldString, posStart)) >= 0) {
			output.append(source.substring(posStart, pos));
			output.append(newString);
			posStart = pos + lengOfold;
		}
		if (posStart < lengOfsource) {
			output.append(source.substring(posStart));
		}
		return output.toString();
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:22:27
	 * @Title getStringToSqlIn
	 * @param source
	 * @return String 返回类型
	 * @category 将传入的以','的字符串分解成SQL语句中的IN（'',''）能查询的语句；
	 */
	public static String getStringToSqlIn(String source) {
		String stmp = "";
		if (StringUtils.isBlank(source)) {
			return source;
		}
		try {
			source = source.replaceAll("，", ",");
			source = source.replaceAll(",", ",");
			//source = source.replaceAll("，", ",");
			//source = source.replaceAll("，", ",");
			stmp = source + ",";
			String[] str = stmp.split(",");
			for (int i = 0; i < str.length; i++) {
				if (i == 0) {
					stmp = "'" + str[i].trim() + "'";
				} else {
					stmp = stmp + ",'" + str[i].trim() + "'";
				}
			}
		} catch (Exception e) {
			return source;
		}
		return stmp;
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:23:44
	 * @Title toHtml
	 * @param s
	 * @return String 返回类型
	 * @category 替换HTML中不能有的特殊字符
	 */
	public static String toHtml(String s) {
		s = Replace(s, "&", "&amp;");
		s = Replace(s, "<", "&lt;");
		s = Replace(s, ">", "&gt;");
		s = Replace(s, "\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		s = Replace(s, "\r\n", "\n");
		// s = Replace(s,"\"","'");
		s = Replace(s, "\n", "<br>");
		s = Replace(s, "  ", "&nbsp;&nbsp;");
		// s = Replace(s,"","'");
		s = Replace(s, "'", "&#39;");
		s = Replace(s, "\\", "&#92;");
		return s;
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:24:09
	 * @Title unHtml
	 * @param s
	 * @return String 返回类型
	 * @category 将html转string
	 */
	public static String unHtml(String s) {
		s = Replace(s, "<br>", "\n");
		s = Replace(s, "<br/>", "\n");
		s = Replace(s, "<br />", "\n");
		s = Replace(s, "<br >", "\n");
		s = Replace(s, "</br>", "\n");
		s = Replace(s, "<BR>", "\n");
		s = Replace(s, "<BR/>", "\n");
		s = Replace(s, "&lt;", "<");
		s = Replace(s, "&nbsp;", " ");
		s = Replace(s, "&gt;", ">");
		return s;
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:25:42
	 * @Title clearHtml
	 * @param html
	 * @return String 返回类型
	 * @category 去掉html语法 <>中间的内容和括号
	 */
	public static String clearHtml(String html) {
		if (StringUtils.isBlank(html)) {
			return "";
		}

		return html.replaceAll("<[^>]+>", "");
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:27:50
	 * @Title unTitle
	 * @param str
	 * @return String 返回类型
	 * @category 去掉表头内容
	 */
	public static String unTitle(String str) {
		String s = str;
		s = StringUtils.replace(s, "</span>", "");
		int i = StringUtils.indexOf(s, ">");
		s = StringUtils.substring(s, i + 1);
		return s;
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:30:40
	 * @Title replaceHz
	 * @param source
	 * @param des
	 * @return String 返回类型
	 * @category 把中文字符转换为指定字符
	 */
	public static String replaceHz(String source, String des) {
		if (StringUtils.isNotBlank(source)) {
			String regEx = "[\\W]";
			Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
			Matcher mat = pat.matcher(source);
			String s = mat.replaceAll(des);
			return s;
		}
		return "";
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:31:19
	 * @Title Ip2num
	 * @param ip
	 * @return long 返回类型
	 * @category ip转换为数字
	 */
	public static long Ip2num(String ip) {
		long ip2l = 0;
		if (StringUtils.isNotBlank(ip)) {
			String[] ips = ip.split("\\.");
			if (ips.length == 4) {
				ip2l += NumberUtils.toLong(ips[0]) * Math.pow(256, 3);
				ip2l += NumberUtils.toLong(ips[1]) * Math.pow(256, 2);
				ip2l += NumberUtils.toLong(ips[2]) * Math.pow(256, 1);
				ip2l += NumberUtils.toLong(ips[3]);
			}
		}
		return ip2l;
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:35:35
	 * @Title clearBlank
	 * @param source
	 * @return String 返回类型
	 * @category 去掉字符串中所有空白字符，包括空格 回车 tab
	 */
	public static String clearBlank(String source) {
		source = StringUtils.trimToEmpty(source);
		return source.replaceAll("\\s", "");
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:35:55
	 * @Title str2List
	 * @param str
	 * @param prepend
	 * @return List<String> 返回类型
	 * @category 将以指定字符分隔的字符串转换为LIST
	 */
	public static List<String> str2List(String str, String prepend) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		if (StringUtils.isBlank(prepend)) {
			prepend = ",";
		}
		List<String> list = new ArrayList<String>();
		String[] arr = str.split(prepend);
		for (String one : arr) {
			list.add(one);
		}
		return list;
	}
	
	
	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:37:02
	 * @Title DoNumberCurrencyToChineseCurrency
	 * @param bigdMoneyNumber
	 * @return String 返回类型
	 * @category Description 将数字金额转换为中文金额 精确到千亿及小数点后2位
	 */
	public static String DoNumberCurrencyToChineseCurrency(BigDecimal bigdMoneyNumber) {
		// 中文金额单位数组
		String[] straChineseUnit = new String[] { "分", "角", "圆", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟"};
		// 中文数字字符数组
		String[] straChineseNumber = new String[] { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
		String strChineseCurrency = "";
		// 零数位标记
		boolean bZero = true;
		// 中文金额单位下标
		int ChineseUnitIndex = 0;
		try {
			if (bigdMoneyNumber.intValue() == 0)
				return "零圆整";
			// 处理小数部分，四舍五入 
			double doubMoneyNumber = Math.round(bigdMoneyNumber.doubleValue() * 100);
			// 是否负数
			boolean bNegative = doubMoneyNumber < 0;
			// 取绝对值
			doubMoneyNumber = Math.abs(doubMoneyNumber);
			// 循环处理转换操作
			while (doubMoneyNumber > 0) {
				// 整的处理(无小数位)
				if (ChineseUnitIndex == 2 && strChineseCurrency.length() == 0)
					strChineseCurrency = strChineseCurrency + "整";
				// 非零数位的处理
				if (doubMoneyNumber % 10 > 0) {
					double db=doubMoneyNumber % 10;
					int t=(int)db*10/10;				
					strChineseCurrency = straChineseNumber[t]
							+ straChineseUnit[ChineseUnitIndex] + strChineseCurrency;
					bZero = false;
				}
				// 零数位的处理
				else {
					// 元的处理(个位)
					if (ChineseUnitIndex == 2) {
						// 段中有数字
						if (doubMoneyNumber > 0) {
							strChineseCurrency = straChineseUnit[ChineseUnitIndex] + strChineseCurrency;
							bZero = true;
						}
					}
					// 万、亿数位的处理
					else if (ChineseUnitIndex == 6 || ChineseUnitIndex == 10) {
						// 段中有数字
						if (doubMoneyNumber % 1000 > 0)
							strChineseCurrency = straChineseUnit[ChineseUnitIndex] + strChineseCurrency;
					}
					// 前一数位非零的处理
					if (!bZero)
						strChineseCurrency = straChineseNumber[0] + strChineseCurrency;

					bZero = true;
				}
				doubMoneyNumber = Math.floor(doubMoneyNumber / 10);
				ChineseUnitIndex++;
			}
			// 负数的处理
			if (bNegative)
				strChineseCurrency = "负" + strChineseCurrency;
		} catch (Exception e) {
			e.printStackTrace();
			return "金额超出限制,请检查金额是否正确!";
		}
		return strChineseCurrency;
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午11:40:25
	 * @Title cpsTkno
	 * @param tkno
	 * @return String 返回类型
	 * @category 处理票号 9991234567890 处理成 999-1234567890
	 */
	public static String cpsTkno(String tkno){
		if (StringUtils.isNotBlank(tkno)){
			tkno = StringUtils.trimToEmpty(tkno);
			if(tkno.indexOf("-") == -1){
				tkno = StringUtils.substring(tkno, 0,3) +"-" + StringUtils.substring(tkno, 3);
			}
		}
		return tkno;
	}
	
	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 下午12:03:46
	 * @Title substrByte
	 * @param str
	 * @param len
	 * @return String 返回类型
	 * @category 截取字符串的字节数
	 */
	public static String substrByte(String str, int len) {
		if (str == null) {
			return "";
		}
		byte[] strByte = str.getBytes();
		int strLen = strByte.length;
		if (len >= strLen || len < 1) {
			return str;
		}
		int count = 0;
		for (int i = 0; i < len; i++) {
			int value = (int) strByte[i];
			if (value < 0) { // 通过尝试发现（gb2312，utf8）汉字确实会占用多于一个字节（2，3），而且都是负值
				count++;
			}
		}
		if (count % 2 != 0) { // 如果platform默认编码是utf8时会出错，某些置不能打印出汉字

			len = (len == 1) ? len + 1 : len - 1;
		}
		return new String(strByte, 0, len);
	}
	

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 下午12:03:56
	 * @Title getparam
	 * @param url
	 * @param p
	 * @return String 返回类型
	 * @category 获得url中的参数值,p参数不区分大小写
	 */
	public static String getparam(String url, String p) {
		if (url == null) {
			return "";
		}
		String tmp = url.endsWith("&") ? url : url + "&";
		p = p.toUpperCase();
		String search = "&" + p + "=";
		int pindex = tmp.toUpperCase().indexOf(search);
		if (pindex < 0) {
			search = "?" + p + "=";
			pindex = tmp.toUpperCase().indexOf(search);
		}
		if (pindex < 0) {
			search = p + "=";
			pindex = tmp.toUpperCase().indexOf(search);
		}
		if (pindex >= 0) {
			String pp = tmp.substring(pindex + search.length());
			pp = pp.substring(0, pp.indexOf("&"));
			return pp;
		} else {
			return "";
		}
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 下午12:04:10
	 * @Title getparamNull
	 * @param url
	 * @param p
	 * @return String 返回类型
	 * @category 不存在的参数返回null
	 */
	public static String getparamNull(String url, String p) {
		if (url == null) {
			return null;
		}
		String tmp = url.endsWith("&") ? url : url + "&";
		p = p.toUpperCase();
		String search = "&" + p + "=";
		int pindex = tmp.toUpperCase().indexOf(search);
		if (pindex < 0) {
			search = "?" + p + "=";
			pindex = tmp.toUpperCase().indexOf(search);
		}
		if (pindex < 0) {
			search = p + "=";
			pindex = tmp.toUpperCase().indexOf(search);
		}
		if (pindex >= 0) {
			String pp = tmp.substring(pindex + search.length());
			pp = pp.substring(0, pp.indexOf("&"));
			return pp;
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 下午12:04:28
	 * @Title getValue
	 * @param map
	 * @param name
	 * @return String 返回类型
	 * @category 根据名称获取Map VALUE 值
	 */
	public static String getValue(Map<?, ?> map, String name) {
		Object o = map.get(name);
		return o == null ? "" : StringUtils.trimToEmpty(String.valueOf(o));
	}
	
	public static String baseString(int num,int base) { 
		String str = "", digit = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"; 
		if(num == 0){ 
			return ""; 
		}else { 
			str = baseString(num / base,base); 
			return str + digit.charAt(num % base); 
		} 
	}

	public static String baseString(BigInteger num,int base) { 
		String str = "", digit = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"; 
		if(num.shortValue() == 0){ 
			return ""; 
		}else { 
			BigInteger valueOf = BigInteger.valueOf(base);
			str = baseString(num.divide(valueOf),base); 
			return str + digit.charAt(num.mod(valueOf).shortValue()); 
		} 
	}
	
	public static String toGB(long length){
		DecimalFormat df =null; 
		long l=length;
		Double ll=(double) 1024;
		Double outd=l/ll;
		if(outd<1){
			outd=outd*ll;
			df=new DecimalFormat("###.00B");
		}else if(outd>=1&&outd<1024){
			df=new DecimalFormat("###.00KB");
		}else if(outd>=1024&&outd<1048576){
			outd=outd/ll;
			df=new DecimalFormat("###.00MB");
		}else{
			outd=outd/ll/ll;
			df=new DecimalFormat("###.00GB");
		}
		String t=df.format(outd);
		return t;
	}

	public static String getMongoId(){
		ObjectId id= new ObjectId();
		return id.toString();
	}
}
