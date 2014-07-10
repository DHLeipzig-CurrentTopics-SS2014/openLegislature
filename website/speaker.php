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
$speaker = $db->speakerstats;

$query = array("name" => $_GET["sp"]);

$result = $speaker->findOne($query);
$words = array();
$top50words = array();
if($result){//var_dump($result);
	
	
	echo( "Name: ".$result["name"]."<br/>");
	echo( "Partei: <a href=\"party.php?pt=".$result["party"]."\" >".$result["party"]."</a><br/>");
	echo( "erste Rede: ".$result["legfrom"]."<br/>");
	echo( "letzte Rede: ".$result["legto"]."<br/>");
	echo( "Anzahl Reden: ".$result["speechcount"]."<br/>");
	echo( "Anzahl Tokens: ".$result["tokencount"]."<br/>");
	echo( "Anzahl Types: ".$result["typecount"]."<br/>");
	echo "<hr><br />";
	
	echo "Speeches:<br />";
	foreach($result["speeches"] as $lspeeches){
		foreach ($lspeeches as $spkey => $spvalue) {
			echo "<a href=\"speech.php?sp=".$spvalue."\" >".$spvalue."</a><br />";
		}
		
	}
	echo "<hr><br />";

	$words=$result["wordscount"];
	arsort($words);
	$counter=0;
	echo "Top 50 words<br />";
	foreach($words as $key => $val){
		$top50words[$key]=$val;
		echo $key." ".$val."<br/>";
		$counter++;
		if($counter>=50)break;
	}

	$tw5=array();
	$cc=0;
	foreach($top50words as $key => $val){
		$tw5[$cc] = array();
	    $tw5[$cc][0]=$key;
	    $tw5[$cc][1]=$val; echo $top50words[$cc];
	    $cc++;
	}
	echo "<hr><br />";


	$tt = array();
  	for($i=0;$i<19;$i++){
  		$tt[$i] = array();
	    $tt[$i][0]=$i;
	    $tt[$i][1]=$result['tokenscount'][$i];
	    $tt[$i][2]=$result['typescount'][$i];
	}
	
	$speeches = array();
	for($i=0;$i<19;$i++){
  		$speeches[$i] = array();
	    $speeches[$i][0]=$i;
	    $speeches[$i][1]=count($result['speeches'][$i]);
	}
}



?>
<hr />
<div id="bar1"></div>
<hr />
<div id="bar2"></div>
<hr />
<div id="bar3"></div>

<script>
var tw5 = <?php echo json_encode($tw5); ?>;
var tt = <?php echo json_encode($tt); ?>;
var speeches = <?php echo json_encode($speeches); ?>;

render(tw5,"legislaturperiode","count","500","1500","Top 50 words", ["words"],"#bar1");

render(tt,"legislaturperiode","anzahl","500","1500","Tokens / Types pro Legislaturperiode", ["tokens","types"],"#bar2");
render(speeches,"legislaturperiode","anzahl","500","1500","Speechcount", ["speeches"],"#bar3");

</script>


	</body>
</html>