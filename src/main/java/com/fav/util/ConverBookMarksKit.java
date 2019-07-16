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

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;

public class ConverBookMarksKit {
  public static void main(String[] args) {
    toMarkDown(getBookMarks("/home/maki/桌面/marks/test-mark.html"));
  }

  private static InputStream getBookMarks(String path) {
    return IoUtil.toStream(new File(path));
  }

  public static void toMarkDown(InputStream in) {
    try {
      Document doc = Jsoup.parse(in, "UTF-8", "");
      Elements focus = doc.select("H3[PERSONAL_TOOLBAR_FOLDER]");
      Element bgEle = focus.get(0).parent().child(1);
      Elements list = bgEle.children();
      List<Element> nonameList = new ArrayList<Element>();
      for (int i = 0; i < list.size(); i++) {
        // for (int i = 2; i < 3; i++) {
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
    // System.out.println(sb.toString());
    FileUtil.writeString(sb.toString(), new File("/home/maki/桌面/marks/" + mdFileName + ".md"), "utf-8");
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
        sb.append(level.toString()).append("[" + e.text() + "]").append("(" + e.attr("href") + ")").append("![图片alt](" + e.attr("icon") + ")").append("\r\n");
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
