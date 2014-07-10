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
	    echo "Anzahl Sprecher: ".sizeof($result["member"]) . "<br/>";
	    echo "Anzahl Types: ".$result["typecount"] . "<br/>";
	    echo "Anzahl Tokens: ".$result["tokencount"] . "<br/>";

	    echo "Sprecher: <br />";
	    echo "<div style=\"width:500px;max-height:400px;overflow-y:scroll;margin:10px;\" >";
	    sort($result["member"]);
	    foreach($result["member"] as $lmem){
	    	echo "<a href=\"speaker.php?sp=".$lmem."\" >".$lmem . "</a><br /> ";	
	    }
	    echo "</div>";


	    echo "Tokens / Types pro Legislaturperiode: <br />";
	    for($i=1;$i<19;$i++){
	    	echo $i.": Tokens: ".$result['tokens'][$i].", Types: ".$result['types'][$i]." Speeches: ".count($result['speeches'][$i])."<br/>";
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
  	

render(periods,"legislaturperiode","anzahl","500","1500","Tokens / Types pro Legislaturperiode", ["tokens","types"],"#bar1");
render(speeches,"legislaturperiode","anzahl","500","1500","Speechcount", ["speeches"],"#bar2");
render(ttpersp,"legislaturperiode","anzahl","500","1500","Avg. Tokens per Speech pro Legislaturperiode", ["tokens"],"#bar3");
</script>


	</body>
</html>