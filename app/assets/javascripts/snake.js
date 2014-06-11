 var host = window.location.host
 var wsUri = "ws://" + host + "/";
 var websocket = new Object()
 var snake_counter = 0;
 var snake_pool = "";

 window.onbeforeunload = function () {
     websocket.onclose = function () {}; // disable onclose handler first
     websocket.close()
 }

 function addSnake() {
	 var snake_name = "snake" + snake_counter++
     $.ajax({
         url: "http://" + host + "/addsnake/" + snake_pool + "?snake_name=" + snake_name,
         context: document.body
     }).done(function () {});
 }

 function startGame(row, col) {
	 for ( i = 1; i <= row*col; i++) {		  
		 off(i)
	 }     
     websocket = new WebSocket(wsUri + "startgame");

     websocket.onopen = function (evt) {
    	 onOpen(evt)
     };
     websocket.onclose = function (evt) {
         onClose(evt)
     };
     websocket.onmessage = function (evt) {
         onMessage(evt)
     };
 }

 function onOpen(evt) {
     writeToScreen("CONNECTED");
     websocket.send("GET POOL");
     enableSnakesButton();
 }

 function enableSnakesButton(){
	 document.getElementById("startSnakeLink").disabled = false;
	 document.getElementById("gameOverLink").disabled = false
	 document.getElementById("startGameLink").disabled = true;

 }
 
 function onClose(evt) {
     gameOver();
     writeToScreen("DISCONNECTED" +  + evt.code + evt.reason);
     disableSnakesButton();
 }

 function disableSnakesButton(){
	 document.getElementById("startSnakeLink").disabled = true;
	 document.getElementById("startGameLink").disabled = false;
	 document.getElementById("gameOverLink").disabled = true;
 }
 function onMessage(evt) {
	 if (evt.data.lastIndexOf("POOL:", 0) === 0) { snake_pool = evt.data.replace("POOL:","");}
	 else if (evt.data.lastIndexOf("APPLE:", 0) === 0) { apple(evt.data.replace("APPLE:",""));}
	 else if (evt.data == "Game Over") { 
		 writeToScreen("GAME OVER");
		 disableSnakesButton();
	 }
     else if (evt.data > 0) on(evt.data);
     else off(Math.abs(evt.data));
 }

 function writeToScreen(message) {
     var pre = document.createElement("p");
     var status = document.getElementById("status");
     pre.style.wordWrap = "break-word";
     pre.innerHTML = message + " " + new Date();
     status.appendChild(pre);
 }

 function hideButton(buttonId) {
     var styleId = document.getElementById(buttonId);
     styleId.style.visibility = "hidden"; 
 }
 
 function showButton(buttonId) {
     var styleId = document.getElementById(buttonId);
     styleId.style.visibility = "	"; 
 }
 
 function changeBackground(newBackground, element) {
     var styleId = document.getElementById("snakeYardCell" + element);
     styleId.style.background = newBackground;
 }

 function on(element) {
     changeBackground("#FF0000", element);
 }

 function off(element) {
     changeBackground("#bbbbbb", element);
 }
 
 function apple(element) {
     changeBackground("#9ACD32", element);
 }

 var clicked = false
 function onCellMouseClick(coordinates) {
	 if (clicked) {
		 return
	 }
	 clicked = true
     $.ajax({
         url: "http://" + host + "/movesnake/" + snake_pool + "?coordinates=" + coordinates,
         context: document.body
     }).done(function () {});
     clicked = false
 }

 function gameOver() {
     websocket.close()
     $.ajax({
         url: "http://" + host + "/stopgame/" + snake_pool,
         context: document.body
     }).done(function () {});
 }