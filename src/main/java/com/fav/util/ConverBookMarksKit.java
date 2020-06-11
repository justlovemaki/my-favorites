package com.fav.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;

public class ConverBookMarksKit {
	private static final String HOME = "/home/maki/桌面/marks/";

	public static void main(String[] args) throws Exception {
//		toMarkDown(getBookMarks(HOME + "test-mark-g.html"));
//		toMarkDownAtavi(getBookMarks(HOME + "test-mark.html"));
//		toBookMarksXml(HOME + "xxx.md");
	}

	private static String readTmpl(String source) throws Exception {
		Resource resource = new ClassPathResource(source);
		File sourceFile = resource.getFile();
		return FileUtil.readString(sourceFile, "UTF-8");
	}

	public static void toBookMarksXml(String path) throws Exception {
		int flagLevel = 0;
		StringBuilder sb = new StringBuilder();
		sb.append(readTmpl("bookmarks/head.html")).append("\r\n");
		List<String> list = FileUtil.readLines(new File(path), "UTF-8");
		for (int i = 0; i < list.size(); i++) {
			String str = list.get(i);
			int flag = getLevel(str);
			if (flag < flagLevel) {
				for (int j = 0; j < flagLevel - flag; j++) {
					sb.append(readTmpl("bookmarks/end.html")).append("\r\n");
				}
			}
			flagLevel = flag;
			getTmplChild(str, sb);
		}
		sb.append(readTmpl("bookmarks/end.html")).append("\r\n");
		sb.append(readTmpl("bookmarks/end.html")).append("\r\n");
//		 System.out.println(sb.toString());
		FileUtil.writeString(sb.toString(), new File(HOME + "dr.html"), "UTF-8");
	}

	private static void getTmplChild(String str, StringBuilder sb) throws Exception {
		String text = null;
		String herf = null;
		int bgText = str.indexOf("* [");
		int edText = str.indexOf("](");
		long time = System.currentTimeMillis() / 1000;
		if (str.endsWith("(dir)")) {
//			 System.out.println(str.substring(bgText+3,edText));
			String tmpl = readTmpl("bookmarks/dir.html");
			sb.append(tmpl.replace("{{time}}", time + "").replace("{{text}}", str.substring(bgText + 3, edText)))
					.append("\r\n");
			sb.append(readTmpl("bookmarks/bg.html")).append("\r\n");
		} else {
//			 System.out.println(str.substring(bgText+3,edText));
//			 System.out.println(str.substring(edText+2,str.length()-1));
			String tmpl = readTmpl("bookmarks/item.html");
			sb.append(
					tmpl.replace("{{time}}", time + "").replace("{{href}}", str.substring(edText + 2, str.length() - 1))
							.replace("{{text}}", str.substring(bgText + 3, edText)))
					.append("\r\n");
		}
	}

	private static int getLevel(String str) {
		StringBuilder xin = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			xin.append("* ");
		}
		for (int i = xin.length(); i >= 0; i = i - 2) {
			if (str.indexOf(xin.toString()) == 0) {
				return xin.length() / 2;
			}
			xin.delete(0, 2);
		}
		return 1;
	}

	private static InputStream getBookMarks(String path) {
		return IoUtil.toStream(new File(path));
	}

	public static void toMarkDownAtavi(InputStream in) {
		try {
			Document doc = Jsoup.parse(in, "UTF-8", "");
			Elements list = doc.select("DT");
			List<Element> nonameList = new ArrayList<Element>();
			for (int i = 0; i < list.size(); i++) {
				Element element = list.get(i);
				if (StringUtils.isNotBlank(element.html())) {
					nonameList.add(element);
				}
			}
			List<List<Element>> noname = new ArrayList<List<Element>>();
			noname.add(nonameList);
			appendMdContent("Atavi", noname, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void toMarkDown(InputStream in) {
		try {
			Document doc = Jsoup.parse(in, "UTF-8", "");
			Elements focus = doc.select("H3[PERSONAL_TOOLBAR_FOLDER]");
			Element bgEle = focus.get(0).parent().child(1);
			Elements list = bgEle.children();
			List<Element> nonameList = new ArrayList<Element>();
			for (int i = 0; i < list.size(); i++) {
				Element element = list.get(i);
				if (StringUtils.isNotBlank(element.html())) {
					if (element.childNodeSize() == 2) {
						nonameList.add(element);
					} else {
						Element el = (Element) element.child(0);
						appendMdContent(el.text(), element.child(1).children(), i);
					}
				}
			}
			List<List<Element>> noname = new ArrayList<List<Element>>();
			noname.add(nonameList);
			appendMdContent("未定义", noname, 99);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void appendMdContent(String fileName, List<?> list, int index) {
		String mdFileName = null;
		String rindex = "" + index;
		if (index < 10) {
			rindex = "0" + index;
		}
		mdFileName = rindex + "-" + fileName;
		System.out.println(mdFileName + ".md");

		Iterator it = null;
		if (list instanceof Elements) {
			Elements els = (Elements) list;
			it = els.iterator();
		} else {
			List<Element> els = (List<Element>) list.get(0);
			it = els.iterator();
		}
		StringBuilder sb = new StringBuilder();
		StringBuilder level = new StringBuilder();
		level.append("* ");
		foreachIterator(it, sb, level);
//		 System.out.println(sb.toString());
		FileUtil.writeString(sb.toString(), new File(HOME + mdFileName + ".md"), "utf-8");
	}

	private static void foreachIterator(Iterator<Element> it, StringBuilder sb, StringBuilder level) {
		StringBuilder leveltmp = new StringBuilder(level);
		while (it.hasNext()) {
			Element el = it.next();
			if (el.childNodeSize() < 2) {
				continue;
			} else if (el.childNodeSize() < 4) {// mark
				Element e = el.child(0);
				level = new StringBuilder(leveltmp);
//        sb.append(level.toString()).append("[" + e.text() + "]").append("(" + e.attr("href") + ")").append("![图片alt](" + e.attr("icon") + ")").append("\r\n");
				sb.append(level.toString()).append("[" + e.text() + "]").append("(" + e.attr("href") + ")")
						.append("\r\n");
			} else {// dir
				Element e = el.child(0);
				level = new StringBuilder(leveltmp);
				sb.append(level.toString()).append("[" + e.text() + "]").append("(dir)").append("\r\n");
				level.append("* ");
				foreachIterator(el.child(1).children().iterator(), sb, level);
			}
		}
	}
}
