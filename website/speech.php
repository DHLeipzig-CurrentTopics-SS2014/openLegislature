<html>
	<meta charset="utf-8"/>
	<head>
		<script src="./js/jquery-2.1.1.min.js"></script>
		<script src="./js/d3.min.js" charset="utf-8"></script>
		<script src="./js/bar.js" charset="utf-8"></script>
	</head>

	<body>


<?php

$m = new MongoClient();
// select a database
$db = $m->local;

// select a collection (analogous to a relational database's table)
$speaker = $db->ollspeeches;
$docid="";
$query = array("_id" => new MongoId($_GET["sp"]));
$sname="";
$result = $speaker->findOne($query);
$words=array();
$top50words=array();
if($result){
		
	//var_dump($result);
	$docid = $result["docid"];
	$sname=$result["speaker"];
	echo "docid: ".$result["docid"]."<br/>";
	echo "speaker: <a href=\"speaker.php?sp=".$result["speaker"]."\">".$result["speaker"]."</a><br/>";
	echo "sessionumber: ".$result["sessionumber"]."<br/>";

	echo "<br/>Top 50 words:<br/>";
	foreach($result["data"] as $dkey => $dval){
		//echo($dkey." - ".$dval[$dkey]."<br/>");
		$words[$dkey] =$dval[$dkey];
	}
	
	arsort($words);
	$counter=0;
	foreach($words as $key => $val){
		$top50words[$key]=$val;
		echo $key." ".$val."<br/>";
		$counter++;
		if($counter>=50)break;
	}

}


$tw5=array();
$cc=0;
foreach($top50words as $key => $val){
	$tw5[$cc] = array();
    $tw5[$cc][0]=$key;
    $tw5[$cc][1]=$val; echo $top50words[$cc];
    $cc++;
}


?>

<hr />
<div id="bar1"></div>
<script>
 	var tw5 = <?php echo json_encode($tw5); ?>;
  	

render(tw5,"","count","500","1500","Top 50 words", ["words"],"#bar1");
</script>


<?php
/*
echo "<hr/>";
echo "plenarprotokoll:<br/>";
$doc = $db->plenarprotokolle;
$query2 = array("_id" => new MongoId($docid));

$result2 = $doc->findOne($query2);
if($result2){ //var_dump($result2['protocol']['session']) ;
	foreach ($result2['protocol']['session']['speech'] as $key => $value) {
		if( $value['speaker']['name']=$sname){
			echo $value['speaker']['name']."<br/>";
			echo $value['content']."<hr/>";
		}
		
	}
	
}*/

?>

	</body>
</html>

