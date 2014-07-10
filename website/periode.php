<html>
	<meta charset="utf-8"/>
	<head>
			<script src="./js/jquery-2.1.1.min.js"></script>
			<script src="./js/d3.min.js" charset="utf-8"></script>
			<script src="./js/donut.js" charset="utf-8"></script>
			<script src="./js/Donut3D.js" charset="utf-8"></script>
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

$speeches = array();
if($result){ 

	foreach ($result as $document) {//var_dump($document);break;

		if( sizeof($document['speeches'][$legislaturperiode]) >0 && $document['name']!=""){
			
			$party =array();
			$party['label'] = $document['name'];
			$party['value'] = sizeof($document['speeches'][$legislaturperiode]);
			$party['color'] = "#".substr(md5(rand()),0,6);
			
			$speeches[] = $party;	
		}		
	}
} 

?>
<div id="bar1"></div>

	</body>
</html>


<script type="text/javascript">
		
var speeches = <?php echo json_encode($speeches);?>;
	
	donut(speeches,"700","500","Speeches per Party", "#bar1");
	
</script>
