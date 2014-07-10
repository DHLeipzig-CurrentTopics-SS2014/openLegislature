<html>
<meta charset="utf-8"/>
	<head>
		<script src="./js/jquery-2.1.1.min.js"></script>
	</head>

	<body>

<?php
$m = new MongoClient();
// select a database
$db = $m->local;

//############
//speaker
//############
// select a collection (analogous to a relational database's table)
$speaker = $db->speakerlist;
$result = $speaker->find();

echo "Sprecher:<br/>";
echo "<div style=\"width:500px;height:400px;overflow-y:scroll;margin:10px;\" >";

if($result){ 
	foreach ($result as $document) {
		ksort($document);
		foreach ($document as $key => $val) {
			if($key !="_id"){
				echo "<a href=\"speaker.php?sp=".$key."\" >".$key . "</a> ";	
				echo "(<a href=\"party.php?pt=".$val."\" >".$val . "</a>)<br/>";
			}	
		}
	}
}
echo "</div><br />";

//############
//party
//############
// select a collection (analogous to a relational database's table)
$partys = $db->partystats;
$result = $partys->find();

echo "Parteien:<br/>";
echo "<div style=\"width:500px;height:400px;overflow-y:scroll;margin:10px;\" >";

if($result){
	$result->sort(array("name" => 1));
	foreach ($result as $document) {
		if($document["name"]==""){
			$pname="none";
		}
		else{
			$pname=$document["name"];	
		}
	    echo "<a href=\"party.php?pt=".$document["name"]."\" >".$pname . "</a><br/>";
	}
}
echo "</div><br />";
?>

<?php
echo "Perioden:<br />";
for($i=1;$i<19;$i++){
	echo "<a href=\"periode.php?lp=".$i."\" >Periode ".$i."</a><br />";
}

?>



	</body>
</html>