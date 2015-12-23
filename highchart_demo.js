	<div id="tip_container" class=" ">
                <div class="ofTipTitle">
                  <h5 class="ofLeft ofTxtLeft" style="width:95%;">
                    <div class="ofInlineBlock ofBorder " id="tiptitle">test</div>
                  </h5>
                  <a class="ofRight" href="javascript:void(0);">
                    <img class="ofIePng" alt="close" title="close" src="/static/common/images/close_red.gif" />
                  </a>
                </div>
                <div class="ofContent ofContainer">
                  <div class="ofMaxHeight ofAltBottomSpacing10">
                   <table cellspacing="0" class="ofFont13" style="table-layout:fixed;width: 287px;">
				     
                      <colgroup>
					  <col width="20%"/>
					  <col width="20%"/>
					  <col width="20%"/>
					  <col width="34%"/>
					  </colgroup>
                      <thead>
                       <tr>
					   <th class="ofAltLeftPadding10">test1</th>
					   <th class="ofAltLeftPadding10">test1</th>
					   <th class="ofAltLeftPadding10">test1</th>
					   <th class="ofTxtCenter">test1</th>
					   </tr>
                      </thead>
                      <tbody></tbody>
                    </table>
                  </div>
                 
                </div>
    </div> 
<div id="chart_div"/>
function tip(x, y, plotX, plotY, tooltipPos,containerId){
			 
				var $chart=$("#"+containerId);
			
				var off = $chart.offset(), tipData = tipData[x],
									rowspan, name, $tr, $tn1,$tn2,$tn3, $tt, $tv, i, n,cpid;

				$tip_container.css({
					"left": parseInt((tooltipPos[0] + Number(off.left))/*-100*/) + "px",
					"top": parseInt((tooltipPos[1] - 32 + Number(off.top))) + "px"
				});
				
				$b_pdc.children().remove();
				var  n = tipData.length;
				if(title_pdc[x]){
					$("#tiptitle").text(title_pdc[x]);
				 }else{
					$("#tiptitle").hide();
					
				 }
				var $trHtml="<tr></tr>",$tnHtml="<td class=''></td>",$ttHtml="<td class='ofAltLeftPadding10 ofTxtCenter'></td>";
				for (i = 0; i < n; i++) {
					$tr = $($trHtml);
					$tn1 = $($tnHtml);
					cpid=tipData[i]["product"];
					
					var $course=tipData[i]["course"],_tempKey="<span class='ofRelative' style='padding-left: 10px;'><span class='"+colorClass[cpid]+"'></span>"+categoryMap[cpid]+"</span>";
					
					
					$tn1.html(courseMap[$course]);
					$tt = $($ttHtml);
					var _temp=tipData[i]["percent"]?tipData[i]["percent"]+'%':'';
					$tt.html(_temp);
					$tr.append($tn1) ;
					 if($course!="9"){
						$tn2 = $($tnHtml);
						
						$tn2.html(_tempKey);
						
						$tr.append($tn2);
						
					 }else{
						$tn1.attr("colspan",2);
					 
					 }
					
					$tn3 = $($tnHtml);
					$tn3.html(tipData[i]["amt"]); 
					$tr.append($tn3);
					$tr.append($tt);
				    $b_pdc.append($tr); 
			}
			 $tip_container.show();
		}; 
function initChart() {
           
            
			 
			
			var color_arr  = [];
		 

			 var title_arr = [  ];
			var data_arr = [ ];
           
			if(data_arr.length == 0) {
               $('#chart_div').css(
                    'background',
                    'url(/static/common/images/pie_no_data.gif) 32px 23px no-repeat'
                );
            }else {
                 
				 var mychart = new Highcharts.Chart({
                    chart: {
                        renderTo: "chart_div",
                        defaultSeriesType: "pie",
                        plotBackgroundColor: null,
                        plotBorderWidth: null,
                        plotShadow: false,
                        /*margin: [0,0,0,-100]*/
						 margin:0
                    },
                    colors: color_arr,
                    exporting: {
                        enabled: false
                    },
                    credits: {
                        enabled: false
                    },
                    legend: {
                        enabled: false
                    },
                    title: {
                        text: ""
                    },
                    tooltip: {
                        enabled: false
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: "pointer",
                            dataLabels: {
                                enabled: false
                            },
                            shadow: false,
                            animation: false,
                            slicedOffset: 0,
                            size: "100%",
							innerSize:'25%',
                            events: {
                                click: function(e) {
                                    var p = e.point;
									var containerId=p.series.chart.options.chartId;
                                    tip(p.x, p.y, p.plotX, p.plotY, p.tooltipPos,containerId);
                                }
                            }
                        }
                    },
                    series: [{
                        type: "pie",
                        data: data_pdc
                    }]
                });
				
				
            };
        }
