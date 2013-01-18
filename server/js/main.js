var WebSocketServer = require('ws').Server;

var wss = new WebSocketServer({
	port: 8080
});

var child = require("child_process").fork("child.js");

var clients = [];

wss.on('connection', function(ws) {
    ws.on('message', function(message) 
    {
    	message = JSON.parse(message);
        
        if(message["type"] === "accelerometer")
        {
            console.log(message);
            var data = message["data"];

            for(var i = 0; i < wss.clients.length; i++)
            {
                wss.clients[i].send(JSON.stringify({
                    type: "accelerometer",
                    data: data,
                }));
            }

            console.log(data[0] + " " + data[1] + " " + data[2]);
        }
        else
        {
            var data = [];

            for(var i = 0; i < message.length; i++)
            {
                var sensor = [message[i]["OUT_X"],message[i]["OUT_Y"],message[i]["OUT_Z"]];
                data.push(sensor);
            }

            for(var i = 0; i < wss.clients.length; i++)
            {
                wss.clients[i].send(JSON.stringify({
                    type: "magnometers",
                    data: data,
                }));
            }

            for(var i = 0; i < message.length; i++)
            {
                console.log(i + ":" + message[i]["OUT_X"] + " " + message[i]["OUT_Y"] + " " + message[i]["OUT_Z"]);
            }
        }

        console.log("");
    });
});