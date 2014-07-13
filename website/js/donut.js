function donut(owndata,dwith,dheight,titledescription,dom){

var legende = $("<p/>");
	for( var i in owndata ){
		var a ="<a href=\"party.php?pt="+owndata[i]["label"]+"\" >"+owndata[i]["label"]+"</a>";
		var link =$("<div />").append(a).css("color",owndata[i]["color"]).append(" "+owndata[i]["value"]);
		legende.append(  link);

	}

$(dom).append(titledescription);
$(dom).append(legende);
	var svg = d3.select(dom).append("svg").attr("width",dwith).attr("height",dheight);

	svg.append("g").attr("id","salesDonut");


	Donut3D.draw("salesDonut", owndata, 150, 150, 130, 100, 30, 0.3);

	

	
		


}