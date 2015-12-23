var $ajaxResultCache={},isAjaxDone=1;	//1 done,0 doing;

var testTagrtHtml=function(html,pageTitleForTest){
				 
					var $titleObj=$(html).filter("title"), $title=$titleObj&&$titleObj.text();
					
					if(!$title||$title===pageTitleForTest) {
						location&&location.reload();
					}
				};	
function addTag(d,s,id,text){
			var js,fjs=d.getElementsByTagName(s)[0];
			if(!d.getElementById(id)){
			js=d.createElement(s);
			js.id=id;
			//js.src="//platform.twitter.com/widgets.js";
			js.innerHTML =text;
			fjs.parentNode.insertBefore(js,fjs);}}//(document,"script","twitter-wjs");	
			
			
			
function replaceDomAndExcecuteScript(/*html,divId,scriptId*/){ 
			var argsLen,html,my_html_a,my_html_b,scriptId,$domScript,$domDiv,$divContent,$chartScript;
				//debug the dom 
				//if(console&&console.log){console.log($chartScript);console.log($divContent);}
				
				argsLen=arguments&&arguments.length;
				
				if(!argsLen){
					return ;				
				}

				if(argsLen>0){
					html=arguments[0];				
					my_html_a=$(html);
					my_html_b=$(html);			
				}
				if(argsLen>1){
					$domDiv=$("#"+arguments[1]);
					//$divContent=my_html_b.find("div#"+arguments[1]);;
					$divContent=my_html_b.find("#"+arguments[1]);;
				}
				if(argsLen>2){
					$domScript=$("#"+arguments[2]);
					$scriptId=arguments[2];
					$chartScript=my_html_a.filter("script#"+$scriptId);
				}
				$domScript&&$domScript.remove();
				
				// 1.add dom 
				$domDiv&&$domDiv.replaceWith($divContent);
				
				// 2.add script tag second
				$domDiv&&$scriptId&&addTag(document,"script",$scriptId,$chartScript.html());
				var _newDom=$("#"+arguments[1]);
				if(_newDom&&_newDom.hasClass("ofHidden")){				
					_newDom.removeClass("ofHidden");
				}
		}
		
  	
function switchButtonForChartA(referUrl,chartDomId,scriptId,pageTitleForTest){	
						
			if($ajaxResultCache[referUrl]){
				var html=$ajaxResultCache[referUrl];//from cache
				 
				return replaceDomAndExcecuteScript(html,chartDomId,scriptId);
			}
			// when ajax is running. isAjaxDone is 0
			if(!isAjaxDone) return;
			
			isAjaxDone=0;
			pageTitleForTest=pageTitleForTest||"login";
			
			
			
			 $.ajax({cache:false,type:"get",dataType:"html",url:referUrl,error:function(err){
				console&&console.error&&console.error(err);
			},success:function(html){
				testTagrtHtml(html,pageTitleForTest);
				 
				$ajaxResultCache[referUrl]=html//to cache
				replaceDomAndExcecuteScript(html,chartDomId,scriptId);
			}}).always(function() {
				isAjaxDone=1;
			  }); 
		}

