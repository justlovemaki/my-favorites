# my-favorites

基于github的网络收藏夹

## 简介
* 基于SpringBoot2后端+Vue前端
* 通过Github Api获取书签数据
* js解析root目录下md文件，生成书签目录
* 使用ConverBookMarksKit，可转换chrome,360导出的书签为md
* 支持多级目录嵌套，默认5级
* sessionStorage作为本地缓存
* 支持隐藏显示书签目录
* 支持私有github仓库
* 配套可增删改的chrome插件 src/chrome-ext
* 独立前端代码，配合nginx重写路径 src/html

## 使用
* fork项目到你的github仓库
* 修改root目录下的md文件
* 修改"* ",星号越多层级越多
* hide.md结尾的文件，默认隐藏
* 搜索框输入89757显示隐藏,可修改showkey文件来改变显示的key
* Demo访问路径 https://book.justlikemaki.vip/favorites/{你的仓库根路径}
* 可通过设置token访问私有仓库内容
* [试用一下吧](https://book.justlikemaki.vip/favorites/)

## Issue
* 欢迎您的更多反馈

## 鸣谢
* 创意借鉴自[UUID](https://github.com/netnr/uuid)
