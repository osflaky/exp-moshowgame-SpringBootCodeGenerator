
$.inputArea = undefined;
$.outputArea = undefined;

$(function(){
	//powered by zhengkai.blog.csdn.net

	//init input code area
	$.inputArea = CodeMirror.fromTextArea(document.getElementById("inputArea"), {
		mode: "text/x-sql", // SQL
		theme: "idea",  // IDEA主题
		lineNumbers: true,   //显示行号
		smartIndent: true, // 自动缩进
		autoCloseBrackets: true// 自动补全括号
	});
	$.inputArea.setSize('auto','auto');

	// init output code area
	$.outputArea = CodeMirror.fromTextArea(document.getElementById("outputArea"), {
		mode: "text/x-java", // JAV
		theme: "idea",   // IDEA主题
		lineNumbers: true,   //显示行号
		smartIndent: true, // 自动缩进
		autoCloseBrackets: true// 自动补全括号
	});
	$.outputArea.setSize('auto','auto');

});


const vm = new Vue({
	el: '#rrapp',
	data: {
		formData: {
			tableSql: "CREATE TABLE 'sys_user_info' (\n" +
				"  'user_id' int(11) NOT NULL AUTO_INCREMENT COMMENT '用户编号',\n" +
				"  'user_name' varchar(255) NOT NULL COMMENT '用户名',\n" +
				"  'status' tinyint(1) NOT NULL COMMENT '状态',\n" +
				"  'create_time' datetime NOT NULL COMMENT '创建时间',\n" +
				//下面可以留着方便开发调试时打开
				// "  `updateTime` datetime NOT NULL COMMENT '更新时间',\n" +
				// "  ABc_under_Line-Hypen-CamelCase varchar comment '乱七八糟的命名风格',\n" +
				"  PRIMARY KEY ('user_id')\n" +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户信息'",
			options: {
				dataType: "sql",

				authorName: "${(value.author)!!}",
				packageName: "${(value.packageName)!!}",
				returnUtilSuccess: "${(value.returnUtilSuccess)!!}",
				returnUtilFailure: "${(value.returnUtilFailure)!!}",

				isPackageType: true,
				isSwagger: false,
				isAutoImport: false,
				isWithPackage: false,
				isComment: true,
				isLombok: true,

				ignorePrefix:"sys_",
				tinyintTransType: "int",
				nameCaseType: "CamelCase",
				timeTransType: "Date"
			}
		},
		templates:[{}],
		historicalData:[],
		currentSelect:'plusentity',
		outputStr: "${(value.outputStr)!!}",
		outputJson: {}
	},
	methods: {
		//set the template for output 选择页面输出的模板类型
		setOutputModel: function (event) {
			const targetModel = event.target.innerText.trim();
			console.log(targetModel);
			vm.currentSelect = targetModel ;
			if(vm.outputStr.length>30){
				vm.outputStr=vm.outputJson[targetModel];
				$.outputArea.setValue(vm.outputStr.trim());
				//console.log(vm.outputStr);
				$.outputArea.setSize('auto', 'auto');
			}
		},
		//switch HistoricalData
		switchHistoricalData: function (event) {
			const tableName = event.target.innerText.trim();
			console.log(tableName);
			if (window.sessionStorage){
				const valueSession = sessionStorage.getItem(tableName);
				vm.outputJson = JSON.parse(valueSession);
				console.log(valueSession);
				alert("切换历史记录成功:"+tableName);
			}else{
				alert("浏览器不支持sessionStorage");
			}
			vm.outputStr=vm.outputJson[vm.currentSelect].trim();
			$.outputArea.setValue(vm.outputStr);
			//console.log(vm.outputStr);
			$.outputArea.setSize('auto', 'auto');
		},
		setHistoricalData : function (tableName){
			//add new table only
			if(vm.historicalData.indexOf(tableName)<0){
				vm.historicalData.unshift(tableName);
			}
			//remove last record , if more than N
			if(vm.historicalData.length>9){
				vm.historicalData.splice(9,1);
			}
			//get and set to session data
			const valueSession = sessionStorage.getItem(tableName);
			//remove if exists
			if(valueSession!==undefined && valueSession!=null){
				sessionStorage.removeItem(tableName);
			}
			//set data to session
			sessionStorage.setItem(tableName,JSON.stringify(vm.outputJson));
			//console.log(vm.historicalData);
		},
		//request with formData to generate the code 根据参数生成代码
		generate : function(){
			//get value from codemirror
			vm.formData.tableSql=$.inputArea.getValue();
			axios.post(basePath+"/code/generate",vm.formData).then(function(res){
				if(res.status===500||res.data.code===500){
					console.log(res);
					error("生成失败，请检查SQL语句!!!"+res.data.msg);
					return;
				}
				setAllCookie();
				//console.log(res.outputJson);
				vm.outputJson = res.data.data;
				//兼容后端返回数据格式
//				if(res.data){
//					vm.outputJson = res.data.outputJson;
//				}else {
//					vm.outputJson = res.outputJson;
//				}

				// console.log(vm.outputJson["bootstrap-ui"]);
				vm.outputStr=vm.outputJson[vm.currentSelect].trim();
				//console.log(vm.outputJson["bootstrap-ui"]);
				//console.log(vm.outputStr);
				$.outputArea.setValue(vm.outputStr);
				$.outputArea.setSize('auto', 'auto');
				//add to historicalData
				vm.setHistoricalData(vm.outputJson.tableName);
				alert("生成成功");
			});
		},
		copy : function (){
			navigator.clipboard.writeText(vm.outputStr.trim()).then(r => {alert("已复制")});
		},
		//download all generated code as ZIP
		downloadZip : function (){
			//get value from codemirror
			vm.formData.tableSql=$.inputArea.getValue();
			if(!vm.formData.tableSql || vm.formData.tableSql.trim().length<5){
				error("请先输入 SQL/JSON/INSERT 语句");
				return;
			}
			// 用 axios 发起请求，responseType: 'blob' 让浏览器把响应当作二进制流处理
			axios.post(basePath+"/code/generate-zip", vm.formData, {responseType: 'blob', timeout: 60000})
				.then(function(res){
					if(res.status !== 200){
						error("下载失败，HTTP 状态码："+res.status);
						return;
					}
					//尝试从 Content-Disposition 中解析文件名
					var dispo = res.headers && (res.headers['content-disposition'] || res.headers['Content-Disposition']);
					var fileName = "code-generator.zip";
					if(dispo){
						var matchStar = /filename\*=UTF-8''([^;]+)/i.exec(dispo);
						var matchQuoted = /filename="?([^";]+)"?/i.exec(dispo);
						if(matchStar && matchStar[1]){
							fileName = decodeURIComponent(matchStar[1]);
						}else if(matchQuoted && matchQuoted[1]){
							fileName = matchQuoted[1];
						}
					}
					// 创建 Blob 并触发浏览器下载
					var blob = new Blob([res.data], {type: 'application/zip'});
					if(window.navigator && window.navigator.msSaveBlob){
						window.navigator.msSaveBlob(blob, fileName);
					}else{
						var url = window.URL.createObjectURL(blob);
						var a = document.createElement('a');
						a.href = url;
						a.download = fileName;
						document.body.appendChild(a);
						a.click();
						document.body.removeChild(a);
						window.URL.revokeObjectURL(url);
					}
					alert("已下载："+fileName);
				})
				.catch(function(err){
					console.error(err);
					error("下载失败："+(err && err.message ? err.message : '未知错误'));
				});
		}
	},
	created: function () {
		//load all templates for selections 加载所有模板供选择
		axios.post(basePath+"/template/all",{
			id:1234
		}).then(function(res){
			//console.log(res.templates);
			// vm.templates = JSON.parse(res.templates);
			console.log('origin res',res);
			vm.templates = res.data.data
			console.log('templates',vm.templates);
			//兼容后端返回数据格式
//			if(res.data){
//				vm.templates = res.data.templates;
//			}else {
//				vm.templates = res.templates;
//			}
		});
	},
	updated: function () {
	}
});

/**
 * 将所有 需要 保留历史纪录的字段写入Cookie中
 */
function setAllCookie() {
	var arr = list_key_need_load();
	for (var str of arr){
		setOneCookie(str);
	}
}

function setOneCookie(key) {
	setCookie(key, vm.formData.options[key]);
}

/**
 * 将所有 历史纪录 重加载回页面
 */
function loadAllCookie() {
	//console.log(vm);
	var arr = list_key_need_load();
	for (var str of arr){
		loadOneCookie(str);
	}
}

function loadOneCookie(key) {
	if (getCookie(key)!==""){
		vm.formData.options[key] = getCookie(key);
	}
}

/**
 * 将 所有 需要 纪录的 字段写入数组
 * @returns {[string]}
 */
function list_key_need_load() {
	return ["authorName","packageName","returnUtilSuccess","returnUtilFailure","ignorePrefix","tinyintTransType","timeTransType"];
}
