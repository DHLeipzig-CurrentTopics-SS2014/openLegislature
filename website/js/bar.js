function displaybar(values, xterm, yterm, ysize, xsize,titledescription,legend){
//alert(legend);
render(values, xterm, yterm, ysize, xsize,titledescription,legend);
}

/*
<ul class="nav nav-pills">
  <li class='<?php echo Arr::get($subnav, "index" ); ?>'><?php echo Html::anchor('../public','Main');?></li>

</ul>


<style>

body {
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  margin: auto;
  position: relative;
  width: 1000px;
}

text {
  font: 10px sans-serif;
}

.axis path,
.axis line {
  fill: none;
  stroke: #000;
  shape-rendering: crispEdges;
}
</style>

*/

/*
	var origindata= <?php echo $d3data; ?>;
	var markeddata=new Array();

	$("#btnrefresh").click( function()
			{var j=0;
			for (var i=0;i<data.length;i++)
			{if(document.content.friut[i].checked){markeddata[j]=data[i];j++;}}
		
			//redraw the svg
			d3.select("svg").remove();
			render(markeddata);
			}
			)
	render(origindata);
*/	//auswahl sprachen


	function render(owndata, xterm, yterm, ysize, xsize,titledescription,legend,dom){
	var yachsentitel=yterm;

	var d=-1; //speichert position im array
	var n = owndata[0].length-1, // number of layers
	    m = owndata.length, // number of samples per layer
	    stack = d3.layout.stack(),
	    layers = stack(d3.range(n).map(function() { d++; return bumpLayer(m, /*.1*/1,d); })),
	    yGroupMax = d3.max(layers, function(layer) { return d3.max(layer, function(d) { return d.y; }); }),
	    yStackMax = d3.max(layers, function(layer) { return d3.max(layer, function(d) { /*alert(d.y0 + d.y);*/return d.y0 + d.y; }); });

	var margin = {top: 40, right: 10, bottom: 20, left: 140},
	    width = xsize - margin.left - margin.right,
	    height = ysize - margin.top - margin.bottom;


	var arr=new Array(); 
	arr[0]=new Array();
	arr[1]=new Array();


	for(var i=0;i<=m;i++){arr[0][i]=i;};
	for(var i = 0; i < owndata.length; i++){arr[1][i]=owndata[i][0];};
	


	var x = d3.scale.ordinal()
		.domain(arr[0])
		.rangeRoundBands([0, width-100], .3)
		;


	var y = d3.scale.linear()
	    .domain([0, yStackMax])
	    .range([height-180, 0]);


	var color = d3.scale.category20c();

	var xAxis = d3.svg.axis()
	    .scale(x)
	    .tickPadding(6)
	    .orient("bottom")
	    .tickFormat(function(d) { return arr[1][d]; })
	    ;

	var yAxis = d3.svg.axis()
	    .scale(y)
	    .tickSize(0)
	    .tickPadding(6)
	    .orient("left")
	    ;

	//Abstand von x-Achse zum unteren Ende des SVG
	var heightbars=180;
	var heightxaxis=height-heightbars;

	var svg = d3.select(dom).append("svg")
	    .attr("width", width + margin.left + margin.right)
	    .attr("height", height + margin.top + margin.bottom)
	    .append("g")
	    .attr("transform", "translate(" + margin.left + "," + 100 + ")")
	    ;
	    
	//title
	svg.append("text")
        .attr("x", (width / 2))             
        .attr("y", 10 - (margin.top / 2))
        .attr("text-anchor", "middle")  
        .style("font-size", "16px") 
        .style("text-decoration", "underline")  
        .text(titledescription);    

	var layer = svg.selectAll(".layer")
	    .data(layers)
	    .enter().append("g")
	    .attr("class", "layer")
	    .style("fill", function(d, i) { return color(i); });

	var rect = layer.selectAll("rect")
	    .data(function(d) { return d; })
	    .enter().append("rect")
	    .attr("x", function(d) { return x(d.x); })
	    .attr("y", height)
	    .attr("width", x.rangeBand())
	    .attr("height", 0);

	rect.transition()
	    .delay(function(d, i) { return i * 10; })
	    .attr("y", function(d) { return y(d.y0 + d.y); })
	    .attr("height", function(d) { return y(d.y0) - y(d.y0 + d.y); });


	svg.append("g")
	    .attr("class", "x axis")
	    .attr("transform", "translate(0," + heightxaxis + ")")
	    .call(xAxis)
		.selectAll("text")
		.style("text-anchor", "end")  
		.attr("transform", "translate(" + -10 + ")"+"rotate(-65)")
	    ;

	  svg.append("g")
	      .attr("class", "y axis")
	      .call(yAxis)
	      .append("text")
	      .attr("transform", "rotate(-90)")
	      .attr("y", 6)
	      .attr("dy", ".71em")
	      .style("text-anchor", "end")
	      .text(yachsentitel);

	d3.selectAll("input").on("change", change);

	var timeout = setTimeout(function() {
		d3.select("input[value=\"grouped\"]").property("checked", true).each(change);}, 2000);

	function change() {
	  clearTimeout(timeout);
	  if (this.value === "grouped") transitionGrouped();
	  else transitionStacked();
	  }

	function transitionGrouped() {
	  y.domain([0, yGroupMax]);

	  rect.transition()
	      .duration(500)
	      .delay(function(d, i) { return i * 10; })
	      .attr("x", function(d, i, j) { return x(d.x) + x.rangeBand() / n * j; })
	      .attr("width", x.rangeBand() / n)
	      .transition()
	      .attr("y", function(d) { return y(d.y); })
	      .attr("height", function(d) { return height - y(d.y)-heightbars; });
	   }

	function transitionStacked() 
	  {
		  y.domain([0, yStackMax]);

		  rect.transition()
		      .duration(500)
		      .delay(function(d, i) { return i * 10; })
		      .attr("y", function(d) { return y(d.y0 + d.y); })
		      .attr("height", function(d) { return y(d.y0) - y(d.y0 + d.y); })
		      .transition()
		      .attr("x", function(d) { return x(d.x); })
		      .attr("width", x.rangeBand());
	  }

	// Inspired by Lee Byron's test data generator.
	function bumpLayer(n, o,d) 
		{

		  var a = [], i;
		  for (i = 0; i < owndata.length; i++){
		    a[i]= o * owndata[i][d];
		    a[i]= o * owndata[i][d+1];
		  }
		  
		  return a.map(function(d, i) { return {x: i, y: Math.max(0, d)}; });
		}
///* legend
	//var dump=<?php echo (isset($subbar)) ? json_encode($subbar):json_encode(['none']);?>;
	var dump=legend.slice(0);

	  //Erstellung der Legende am rechten Rand
	  legendarray = new Array();
	  legendarray[0] = new Array();
	  legendarray[1] = new Array();
	  legendarray[1]=dump;


	for(var i=0;i<dump.length;i++){legendarray[0][i]=i;}

	  var legend = svg.selectAll(".legend")
	      .data(legendarray[0])
	      .enter().append("g")
	      .attr("class", "legend")
	      .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

	  legend.append("rect")
	      .attr("x", width - 18)
	      .attr("width", 18)
	      .attr("height", 18)
	      .style("fill", color);

	  legend.append("text")
	      .attr("x", width - 24)
	      .attr("y", 9)
	      .attr("dy", ".35em")
	      .style("text-anchor", "end")
	      .text(function(d, i) { return legendarray[1][i]; });

	var bottomlegend =  new Array();
	for (var i = 0; i < owndata.length; i++) 
		{
		  bottomlegend[i]=i +" - "+owndata[i][owndata[i].length-1];
		}
//*/legend end

	transitionGrouped();
	}//Ende von Render Funktion
