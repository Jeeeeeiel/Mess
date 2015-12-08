function submit(){
    //alert("submit");
	$('#submit').attr({"disabled":"disabled"});
    var jsonObj=new Object();
    jsonObj.id=$("#id").val();
    jsonObj.JudgeName=getCookie('name');
    jsonObj.VoteName=$("#VoteName").val();
    jsonObj.time=getTime();
    
    var arrayObj = new Array();
    var ul=document.getElementById("list-group");
    if(ul!=null){
        for(var i = 0; i < ul.children.length; i++){
            var item=new Object();
            item.id=ul.children[i].getElementsByTagName('label')[0].innerHTML.replace(".","");
            item.Organization=ul.children[i].getElementsByTagName('p')[0].innerHTML;
            item.ProjectName=ul.children[i].getElementsByTagName('font')[0].innerHTML;
            item.Result="0";
            if($('input[name="iCheck'+(i+1)+'"]')!=null){
                for(var j = 0; j < $('input[name="iCheck'+(i+1)+'"]').length; j++){
                    if($('input[name="iCheck'+(i+1)+'"]')[j].parentNode.className.indexOf('checked')>0){
                        item.Result=$('input[name="iCheck'+(i+1)+'"]')[j].value;
                        break;
                    }
                }
            }
            //alert(item.Result);
            arrayObj[i]=item;
        }
    }
    jsonObj.VoteData=arrayObj;
    //alert(JSON.stringify(jsonObj));
    //console.log(JSON.stringify(jsonObj));
    $.ajax({
        type:'post',
        url:'./servlet/DataServlet',
        data:{'data':JSON.stringify(jsonObj)},
        dataType:'json',
        cache:false,
        async : true, //异步
        success:function(data){
            var result = eval(data.result);
            if(result.success==true){
            	alert("提交成功!");
            }else if(result.success=="outdate"){
            	alert("该投票已结束，请刷新获取新投票!");

            }else if(result.success==false){
            	alert("提交失败!");
            }
            $('#submit').removeAttr("disabled");
        },
        error:function(){
            alert("提交失败!");
            $('#submit').removeAttr("disabled");
        }
    });
}


function checkCondition(){
	var condition = $('#condition').val();
	for(var i=0,realI = 0;i<condition.split(";").length;i++,realI++){
		var option = condition.split(";")[i];
		var limitMin = parseInt(option.split(':')[1].split('-')[0]);
		var limitMax = parseInt(option.split(':')[1].split('-')[1]);
		if(limitMax<=0){
			realI--;
			continue;
		}
		option = option.split(':')[0];
		var count = 0;
		var ul=document.getElementById("list-group");
	    if(ul!=null){
	        for(var j = 0; j < ul.children.length; j++){
	            if($('input[name="iCheck'+(j+1)+'"]')!=null){
                    if($('input[name="iCheck'+(j+1)+'"]')[realI].parentNode.className.indexOf('checked')>0){
                        count ++;
                    }
	            }
	        }
	    }
	    if(count<limitMin||count>limitMax){
	    	alert('选项：'+option+'\n'+
	    			"最小可选个数："+limitMin+'\n'+
	    			"最大要选个数："+limitMax);
	    	return false;
	    }
	}
	return true;
	
}

function initData(){
    //alert("initData");
    //var jsonObj=new Object();
	$('#spinner').show();
	$('.jumbotron').hide();
    $.ajax({  
        type:'post',      
        url:'./servlet/NewVoteServlet', 
        dataType:'json', 
        cache:false,
        async : true, //异步
        success:function(data){
            var json = eval(data.result);
            //alert(JSON.stringify(json));
            if(json.VoteName.length>0){
            	$("#head").html(json.VoteName.split("|")[0] + '<input id="condition" type="hidden" value='+json.VoteName.split("|")[1].toUpperCase()+'>');
            	//$("#list-group").before('<h1 class="press" style="font-weight:bold;font-family:宋体">'+json.VoteName+'</h>');
            	
            }
            $.each(json.VoteSubjects, function(key, value){
            	var optionList = "";
            	for(var i=0;i<$("#condition").val().split(";").length;i++){
            		var option = $("#condition").val().split(";")[i];
            		var limitMax = parseInt(option.split(':')[1].split('-')[1]);
            		if(limitMax<=0){
            			continue;
            		}
            		optionList += ('<span style="margin:10px"><input type="radio" name="iCheck'+(key+1)+'" value="'+(i+1)+'"> <label style="left-margin:20px;font-size:150%">'+option.split(":")[0]+'</label></span>');
            	}
            	optionList += ('<span style="margin:10px"><button type="button" class="btn btn-primary" onclick="unckeck(\'iCheck'+(key+1)+'\')">重置</button></span>');
                $("#list-group").append('<li class="list-group-item">'+
                    '<h4><label style="font-size:150%">'+(key+1)+'.</label><font style="font-size:150%;font-weight:bold;font-family:宋体">'+value.ProjectName+'</font></h4>'+
                    '<p style="text-align:left;font-size:120%;font-weight:bold;font-family:宋体">'+value.Organization+'</p>'+
                    optionList+
                    '</li>');
            }); 
            if(json.VoteSubjects.length>0){
            	$("#data_container").append('<input type="hidden" id="id" value="'+json.id+'"/>');
                $("#data_container").append('<input type="hidden" id="time" value="'+json.time+'"/>');
                $("#data_container").append('<input type="hidden" id="VoteName" value="'+json.VoteName+'"/>');
                $("#list-group").after('<input id="submit" class="btn btn-primary" type="button" style="width:100%;font-size:220%;font-weight:bold;font-family:宋体" value="提 交" onclick="if(checkCondition())submit();"/>');
                $('input').iCheck({
                    checkboxClass: 'icheckbox_square',
                    radioClass: 'iradio_square-blue',
                    increaseArea: '50' // optional
                });
            }
            $('#spinner').hide();
            $('.jumbotron').show();
        },
        error:function(){
            alert("获取待投票信息失败!");
            $('#spinner').hide();
        }
    }); 
    
}

function unckeck(groupName){
	for(var i=0;i<$('input[name="'+groupName+'"]').length;i++){
		$('input[name="'+groupName+'"]').iCheck('uncheck');
	}
}

function login(){
	if($('#name').val().length>0&&isChinese($('#name').val())&&$('#password').val()=='szjrcx2015'){
		$('#loginModal').modal('hide');
		setCookie($('#name').val(), $('#password').val());
		initData();
	}else{
		alert('姓名、密码错误');
	}
}

function setCookie(name,password)
{
	var Days = 1;
	var exp = new Date();
	exp.setTime(exp.getTime() + Days*24*60*60*1000);
	document.cookie ="name="+ escape(name) + ";password="+ escape(password) + ";expires=" + exp.toGMTString();
}
function getCookie(c_name)
{
	if (document.cookie.length>0)
	{
		c_start=document.cookie.indexOf(c_name + "=");
		if (c_start!=-1)
		{
			c_start=c_start + c_name.length+1;
			c_end=document.cookie.indexOf(";",c_start);
			if (c_end==-1) c_end=document.cookie.length;
			return unescape(document.cookie.substring(c_start,c_end));
		} 
	}
	return "";
}
function checkCookie()
{
	$('.jumbotron').hide();
	getTime();
	username=getCookie('name');
	if (username!=null && username!=""){
		initData();
	}else{
		$('#loginModal').modal({
			keyboard:false,
			backdrop:'static',
			show:true,
		});
	}

}
function getTime(){
	return (formatDate(new Date(), 'yyyy-MM-dd HH:mm:ss'));
}
function formatDate(date, format) {   
    if (!date) return;   
    if (!format) format = "yyyy-MM-dd HH:mm:ss";   
    switch(typeof date) {   
        case "string":   
            date = new Date(date.replace(/-/, "/"));   
            break;   
        case "number":   
            date = new Date(date);   
            break;   
    }    
    if (!date instanceof Date) return;   
    var dict = {   
        "yyyy": date.getFullYear(),   
        "M": date.getMonth() + 1,   
        "d": date.getDate(),   
        "H": date.getHours(),   
        "m": date.getMinutes(),   
        "s": date.getSeconds(),   
        "MM": ("" + (date.getMonth() + 101)).substr(1),   
        "dd": ("" + (date.getDate() + 100)).substr(1),   
        "HH": ("" + (date.getHours() + 100)).substr(1),   
        "mm": ("" + (date.getMinutes() + 100)).substr(1),   
        "ss": ("" + (date.getSeconds() + 100)).substr(1)   
    };       
    return format.replace(/(yyyy|MM?|dd?|HH?|ss?|mm?)/g, function() {   
        return dict[arguments[0]];   
    });                   
} 

function isChinese(temp)
{
	var reg = /^[\u4E00-\u9FA5]+$/;
	if (reg.test(temp))return true;
	return false;
}

function generateXLS(){
	//alert("hehe");
	$('#genExcel').attr({"disabled":"disabled"});
	$.ajax({  
        type:'post',      
        url:'./servlet/GenExcelServlet', 
        dataType:'json', 
        cache:false,
        async : true, //异步
        success:function(data){
            var json = eval(data.result);
            //alert(JSON.stringify(json));
            if(json.genStatus==true){
            	alert('生成成功');
            	$('#download').append('<a href="'+json.fileURL+'" >"'+json.fileName+'"<\a>');
            }else{
            	alert('生成失败');
            }
        	$('#genExcel').removeAttr("disabled");
        },
        error:function(){
        	alert('生成失败');
        	$('#genExcel').removeAttr("disabled");
        }
    }); 
}