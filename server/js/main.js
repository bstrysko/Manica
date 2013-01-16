var WebSocketServer = require('ws').Server;

var count = 0;

var wss = new WebSocketServer({
	port: 8080
});

wss.on('connection', function(ws) {
	console.log("Client Connected");

    ws.on('message', function(message) 
    {
    	message = JSON.parse(message);
        
        console.log(count + ":" + message["OUT_X"] + " " + message["OUT_Y"] + " " + message["OUT_Z"]);

        count++;
    });
});