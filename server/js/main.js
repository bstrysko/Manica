var WebSocketServer = require('ws').Server;

var wss = new WebSocketServer({
	port: 8080
});

wss.on('connection', function(ws) {
	console.log("Client Connected");

    ws.on('message', function(message) 
    {
    	message = JSON.parse(message);
        
        for(var i = 0; i < message.length; i++)
        {
            console.log(i + ":" + message[i]["OUT_X"] + " " + message[i]["OUT_Y"] + " " + message[i]["OUT_Z"]);
        }

        console.log("");
    });
});