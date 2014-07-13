<html>
	<meta charset="utf-8"/>
	<head>
			<script src="./js/jquery-2.1.1.min.js"></script>
			<script src="./js/d3.min.js" charset="utf-8"></script>
			<script src="./js/donut.js" charset="utf-8"></script>
			<script src="./js/Donut3D.js" charset="utf-8"></script>
			<script src="./js/bubble.js"></script>
	</head>

	<body>

	<?php
$legislaturperiode=$_GET["lp"];
$m = new MongoClient();
// select a database
$db = $m->local;
$partystats = $db->partystats;

$periode = $_GET["lp"];
$result = $partystats->find();

$wordcloud =array();
$speeches = array();
$numberofspeeches=0;
$numberofspeeches2=0;
if($result){ 

	foreach ($result as $document) {//var_dump($document);break;

		$numberofspeeches+=sizeof($document['speeches'][$legislaturperiode]);
		if( sizeof($document['speeches'][$legislaturperiode]) >0 && $document['name']!=""){
			$numberofspeeches2+=sizeof($document['speeches'][$legislaturperiode]);
			//data for donut	
			$party =array();
			$party['label'] = $document['name'];
			$party['value'] = sizeof($document['speeches'][$legislaturperiode]);
			$party['color'] = "#".substr(md5(rand()),0,6);
			$party['speechcount'] = sizeof($document['speeches'][$legislaturperiode]);

			$speeches[] = $party;	

			//data for cloud
			$cloud = array();
			
		}		


	}
} 
echo "number of speeches: ".$numberofspeeches."<br />";
echo "number of speeches with party: ".$numberofspeeches2."<br />";
?>

<div id="bar1"></div>
<hr />
<div id="bar2"></div>

	</body>
</html>


<script type="text/javascript">

//build json
	var speeches = <?php echo json_encode($speeches);?>;
	var json= { "name": "flare", "children": [  ]};	
	var counter = 0;
	for(party in speeches){
		//json.children[counter] = {"name":  part['name'], "size": 3938};
		
		json.children[counter] = { };
		json.children[counter].name = speeches[party]['label'];
		json.children[counter].children = [];

		json.children[counter].children[0] = {};
		json.children[counter].children[0].name =  speeches[party]['label'];
		json.children[counter].children[0].size = speeches[party]['speechcount'];
		counter++;
	}
	
	
	
	
	donut(speeches,"700","500","Speeches per Party", "#bar1");
	bubble(json, "","#bar2")
</script>

