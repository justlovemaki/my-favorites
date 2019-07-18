const deep = 5;

async function getMd(name, url, arrRoot, initObj) {
	arrRoot.push(initObj);
	var res = await axios.get(url);
	initObj.list = parseMdContent(res.data);
	initObj.roothtml = buildRootHtml(initObj.list);
	initObj.deep = 1;
	initObj.path = "/";
	if(name.indexOf("hide.md") >= 0){
		initObj.display='none';
	}
	
	sessionStorage.setItem(name, JSON.stringify(initObj));
}

function serachMarks(query, arrRoot){
	var arrResult = new Array();
	arrResult.push({title:"无结果",url:"###"});
	query = query.replace(/\s*/g,"");
	if(query.length == 0){
		return arrResult;
	}
	arrRoot.forEach(function(obj) {
		obj.list.forEach(function(one) {
			if(one.name.toUpperCase().indexOf(query.toUpperCase()) >= 0){
				arrResult.push({title:one.name,url:one.href});
			}
		});
	});
	if(arrResult.length>1){
		arrResult.splice(0,1);
	}
	return arrResult;
}

function getTmplMark(mark){
	if(mark.isdir==0){
		return "<div data-dir='"+mark.isdir+"' data-type='mark' class='my-mark-link my-mark-color'><a href='"+mark.href+"' target='_blank' rel='noreferrer'>"+mark.name+"</a></div>";
	}else{
		return "<div data-dir='"+mark.isdir+"' data-type='mark' class='my-dir-color '>" + mark.name + "</div>";
	}
}

function pathAndMark(path,mark){
	return "<div class='my-border-bottom my-path'>"+path+"</div>"+"<div class='my-mark'>"+mark+"</div>";
}

function buildMarkPath(path){
	var list = path.split('/');
	list = list.filter(item => !!item);
	var arrLine = new Array();	
	arrLine.push("<span data-index='0' data-type='path'>..</span>");
	arrLine.push("/");
	var index = 1;
	list.forEach(function(t) {
		arrLine.push("<span data-index='"+index+"' data-type='path'>"+t+"</span>");
		arrLine.push("/");
		index++;
	});
	return arrLine.join("");
}

// root dir mark
function buildRootHtml(listmark) {
	var arrLine = new Array();
	listmark.forEach(function(mark) {
		if (mark.pathdeep == 1) {
			arrLine.push(getTmplMark(mark));
		}
	});
	
	return pathAndMark(buildMarkPath("/"),arrLine.join(""));
}

function buildDirHtml(event) {
	var type = event.target.getAttribute('data-type');
	var path,deep = null;
	if(type=='mark'){
		var dir = event.target.getAttribute('data-dir');
		if (!dir) {
			return;
		}
		if (dir == 0) {
			return;
		}
	}else if(type=='path'){
		var index = event.target.getAttribute('data-index');
		var pathText = event.target.parentElement.innerText;
		var list = pathText.split('/');
		list = list.filter(item => !!item);
		var arrLine = new Array();
		for (var i = 1; i <= index; i++) {
			arrLine.push("/"+list[i]);
		}
		arrLine.push("/");
		path = arrLine.join("");
		deep = +index + 1;
	}
	buildDirHtmlByPath(event, path, deep);
}

function buildDirHtmlByPath(event,path,deep){
	var index = event.target.parentElement.parentElement.getAttribute('data-index');
	if(!index){
		return;
	}
	var that = vm.arrRoot[index];
	if(!!path && path==that.path){
		return;
	}
	var name = event.target.textContent;	
	that.path = !!path ? path : that.path + name + "/";
	that.deep = !!deep ? deep : that.deep + 1;
	var arrLine = new Array();
	that.list.forEach(function(mark) {
		if (mark.pathdeep == that.deep && mark.path.startsWith(that.path)) {
			arrLine.push(getTmplMark(mark));
		}
	});

	that.roothtml = pathAndMark(buildMarkPath(that.path),arrLine.join(""));
	// console.log(that.roothtml);
}

function parseMdContent(data) {
// console.log(data);
	var rootone = {
		isdir : 0,
		path : "/",
		pathdeep : 0,
		name : "noname",
		href : "",
		icon : ""
	};
	var pathtmp = "";
	var list = data.split('\n');
	var arrLine = new Array();
	// 遍历每一行，解析成A标签
	list.forEach(function(line) {
		if (!!line && "---" != line) {
			var one = JSON.parse(JSON.stringify(rootone));
			one.pathdeep = buildDeep(line);

			if (/\(dir\)/.test(line)) {
				if (one.pathdeep == deep) {
					return false;
				}
				one.isdir = 1;
				line.replace(/\[.*?\]/, function(x) {
					one.name = x.substring(1, x.length - 1).trim();
				});
			} else if (/\[.*?\]\(http.*?\)/.test(line)) {
				line.replace(/\(http.*?\)/, function(x) {
					one.href = x.substring(1, x.length - 1).trim();
				});
				line.replace(/\[.*?\]/, function(x) {
					one.name = x.substring(1, x.length - 1).trim();
				});
				line.replace(/\(data:image.*?\)/, function(x) {
					one.icon = x.substring(1, x.length - 1).trim();
				});
			}

			pathtmp = buildDirPath(line, pathtmp, one.name);
			one.path = pathtmp;
			arrLine.push(one);
		}
	});
	return arrLine;
}

function buildDirPath(line, pathtmp, name) {
	var arr = new Array();
	var count = buildDeep(line);
	var list = pathtmp.split('/');
	if (list.length > count) {
		for (var i = 1; i <= count - 1; i++) {
			arr.push("/");
			arr.push(list[i]);
		}
		arr.push("/" + name);
		pathtmp = arr.join("");
	} else {
		arr.push("/" + name);
		pathtmp = pathtmp + arr.join("");
	}
	return pathtmp;
}

function buildDeep(line) {
	var count = line.match(/\* /g).length;
	if (count > deep) {
		count = deep;
	}
	return count;
}
