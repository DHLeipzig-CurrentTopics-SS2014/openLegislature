<html>
	<meta charset="utf-8"/>
	<head>
		<script src="./js/jquery-2.1.1.min.js"></script>
		<script src="./js/d3.min.js" charset="utf-8"></script>
		<script src="./js/bar.js" charset="utf-8"></script>
		<script src="./js/bubble.js"></script>
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
$top50wordsWithoutStoppwords = array();
$top50wordsWithoutStoppwords2 = array();
if($result){//var_dump($result);
	
	
	echo( "name: ".$result["name"]."<br/>");
	echo( "party: <a href=\"party.php?pt=".$result["party"]."\" >".$result["party"]."</a><br/>");
	echo( "first speech: ".$result["legfrom"]."<br/>");
	echo( "last speech: ".$result["legto"]."<br/>");
	echo( "number of speeches: ".$result["speechcount"]."<br/>");
	echo( "tokencount: ".$result["tokencount"]."<br/>");
	echo( "typecount: ".$result["typecount"]."<br/>");
	echo "<hr><br />";
	
	echo "speeches:<br />";
	foreach($result["speeches"] as $lspeeches){
		foreach ($lspeeches as $spkey => $spvalue) {
			echo "<a href=\"speech.php?sp=".$spvalue."\" >".$spvalue."</a><br />";
		}
		
	}
	echo "<hr><br />";

	//read stoppwords
	$stoppwords = array();
	$handle = fopen("./csv/stopwordlist_german","r");
	while(!feof($handle)){
		$buffer = fgets($handle);
		$stoppwords[] = trim(preg_replace('/\s+/', '', $buffer));
	}
	fclose($handle);
	//read stoppwords2
	$stoppwords2 = array();
	$handle = fopen("./csv/stopwordlist_german2","r");
	while(!feof($handle)){
		$buffer = fgets($handle);
		$stoppwords2[] = trim(preg_replace('/\s+/', '', $buffer));
	}
	fclose($handle);



	//gen topwords
	$words=$result["wordscount"];
	arsort($words);
	$counter=0;
	echo "Top 75 words<br />";
	foreach($words as $key => $val){
		$top50words[$key]=$val;

		echo $key." ".$val."<br/>";
		$counter++;
		if($counter>=75)break;
	}
//top75 wo sw
	echo "<br/>Top 75 words without stoppwords<br />";
	$counter=0;
	foreach($words as $key => $val){
		if(in_array($key, $stoppwords)){}
		else{
				$top50wordsWithoutStoppwords[$key]=$val;
				echo $key." ".$val."<br/>";
				$counter++;
		}
		if($counter>=75)break;
	}

	//top75 wo sw2
	echo "<br/>Top 75 words without stoppwords 2<br />";
	$counter=0;
	foreach($words as $key => $val){
		if(in_array($key, $stoppwords2)){}
		else{
				$top50wordsWithoutStoppwords2[$key]=$val;
				echo $key." ".$val."<br/>";
				$counter++;
		}
		if($counter>=75)break;
	}


//bar1 top75 words
	$tw5=array();
	$cc=0;
	foreach($top50words as $key => $val){
		$tw5[$cc] = array();
	    $tw5[$cc][0]=$key;
	    $tw5[$cc][1]=$val; echo $top50words[$cc];
	    $cc++;
	}
	echo "<hr><br />";
//bar2 top50 words without stoppwords
	$tw5ws=array();
	$cc=0;
	foreach($top50wordsWithoutStoppwords as $key => $val){
		$tw5ws[$cc] = array();
	    $tw5ws[$cc][0]=$key;
	    $tw5ws[$cc][1]=$val; echo $top50wordsWithoutStoppwords[$cc];
	    $cc++;
	}

//bar3 top50 words without stoppwords2
	$tw5ws2=array();
	$cc=0;
	foreach($top50wordsWithoutStoppwords2 as $key => $val){
		$tw5ws2[$cc] = array();
	    $tw5ws2[$cc][0]=$key;
	    $tw5ws2[$cc][1]=$val; echo $top50wordsWithoutStoppwords2[$cc];
	    $cc++;
	}

//bar4
	$tt = array();
  	for($i=0;$i<19;$i++){
  		$tt[$i] = array();
	    $tt[$i][0]=$i;
	    $tt[$i][1]=$result['tokenscount'][$i];
	    $tt[$i][2]=$result['typescount'][$i];
	}
//bar5	
	$speeches = array();
	for($i=0;$i<19;$i++){
  		$speeches[$i] = array();
	    $speeches[$i][0]=$i;
	    $speeches[$i][1]=count($result['speeches'][$i]);
	}
//bar6
	$ttpersp = array();
	for($i=0;$i<19;$i++){
  		$ttpersp[$i] = array();
	    $ttpersp[$i][0]=$i;
	    $ttpersp[$i][1]=$result['tokenscount'][$i]/count($result['speeches'][$i]);
	}

//bar7
	$bubble1=array();
	$cc1=0;
	foreach($words as $key => $val){
		if(in_array($key, $stoppwords2)){
			
		}
		else{
			if($val>10){
				$bubble1[$cc1] = array();
		    	$bubble1[$cc1][0]=$key;
		    	$bubble1[$cc1][1]=$val; 
		    	$cc1++;	
			}
		}
	}
}



?>
<hr />
<div id="bar1"></div>
<hr />
<div id="bar2"></div>
<hr />
<div id="bar3"></div>
<hr />
<div id="bar4"></div>
<hr />
<div id="bar5"></div>
<hr />
<div id="bar6"></div>
<hr />
<div id="bar7"></div>

<script>
var tw5 = <?php echo json_encode($tw5); ?>;
var tw5ws = <?php echo json_encode($tw5ws); ?>;
var tt = <?php echo json_encode($tt); ?>;
var speeches = <?php echo json_encode($speeches); ?>;
var ttpersp = <?php echo json_encode($ttpersp); ?>;
var tw5ws2 = <?php echo json_encode($tw5ws2); ?>;

var bubble1 = <?php echo json_encode($bubble1); ?>;


render(tw5,"legislaturperiode","count","500","1500","Top 75 words", ["words"],"#bar1");
render(tw5ws,"legislaturperiode","count","500","1500","Top 75 words without stoppwords", ["words"],"#bar2");
render(tw5ws2,"legislaturperiode","count","500","1500","Top 75 words without stoppwords2", ["words"],"#bar3");
render(tt,"legislaturperiode","count","500","1500","Tokens / Types by Legislaturperiode", ["tokens","types"],"#bar4");
render(speeches,"legislaturperiode","count","500","1500","Speechcount", ["speeches"],"#bar5");
render(ttpersp,"legislaturperiode","count","500","1500","Avg. Tokens per Speech by Legislaturperiode", ["tokens"],"#bar6");

//build json
var json= { "name": "flare", "children": [  ]};	
json.children[0] = { };
json.children[0].name = 'words';
json.children[0].children = [];






var counter =0;
for(b in bubble1){
	var cluster =0;

	json.children[cluster].children[counter] = {};
	json.children[cluster].children[counter].name =  bubble1[b][0];
	json.children[cluster].children[counter].size = ""+bubble1[b][1];
	counter++;
}


bubble(json, "top words, without stoppwords2 and min occur > 10","#bar7")
</script>


	</body>
</html>