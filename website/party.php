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
$partystats = $db->partystats;

$query = array("name" => $_GET["pt"]);
$result = $partystats->findOne($query);
//var_dump($result);

if($result){
	
	    echo "Name: ".$result["name"] . "<br/>";
	    echo "number of speaker: ".sizeof($result["member"]) . "<br/>";
	    echo "typecount: ".$result["typecount"] . "<br/>";
	    echo "tokencount: ".$result["tokencount"] . "<br/>";

	    echo "speaker: <br />";
	    echo "<div style=\"width:500px;max-height:400px;overflow-y:scroll;margin:10px;\" >";
	    sort($result["member"]);
	    foreach($result["member"] as $lmem){
	    	echo "<a href=\"speaker.php?sp=".$lmem."\" >".$lmem . "</a><br /> ";	
	    }
	    echo "</div>";


	    echo "tokens / types by election period: <br />";
	    for($i=1;$i<19;$i++){
	    	echo $i.": tokens: ".$result['tokens'][$i].", types: ".$result['types'][$i]." speeches: ".count($result['speeches'][$i])."<br/>";
	    }
/*
	    echo "Speeches: <br />";
	    echo "<div style=\"width:500px;max-height:400px;overflow-y:scroll;margin:10px;\" >";
	    for($i=1;$i<19;$i++){
	    	echo $i.": ";
	    	foreach( $result['speeches'][$i] as $j ){
	    		echo $j."<br />";	
	    	}
	    	//$result['tokens'][$i].", Types: ".$result['types'][$i]." Speeches: ".count($result['speeches'][$i])."<br/>";
	    }
	    echo "</div>";*/
	    

		
		$periods = array();
	  	for($i=0;$i<19;$i++){
	  		$periods[$i] = array();
		    $periods[$i][0]=$i;
		    $periods[$i][1]=$result['tokens'][$i];
		    $periods[$i][2]=$result['types'][$i];
		}
		
		$speeches = array();
		for($i=0;$i<19;$i++){
	  		$speeches[$i] = array();
		    $speeches[$i][0]=$i;
		    $speeches[$i][1]=count($result['speeches'][$i]);
		}

		$ttpersp = array();
		for($i=0;$i<19;$i++){
	  		$ttpersp[$i] = array();
		    $ttpersp[$i][0]=$i;
		    $ttpersp[$i][1]=$result['tokens'][$i]/count($result['speeches'][$i]);
		}

}



?>
<hr />
<div id="bar1"></div>
<hr />
<div id="bar2" /></div>
<hr />
<div id="bar3" /></div>

<script>

  	var documents = <?php echo json_encode($documents); ?>;
  	var periods = <?php echo json_encode($periods); ?>;
  	var speeches = <?php echo json_encode($speeches); ?>;
  	var ttpersp = <?php echo json_encode($ttpersp); ?>;
  	

render(periods,"legislaturperiode","count","500","1500","tokens / types by election period", ["tokens","types"],"#bar1");
render(speeches,"legislaturperiode","count","500","1500","speechcount", ["speeches"],"#bar2");
render(ttpersp,"legislaturperiode","count","500","1500","avg. tokens per Speech by election period", ["tokens"],"#bar3");
</script>


	</body>
</html>
