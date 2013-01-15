var WebSocketServer = require('ws').Server;

function short_to_ushort(n)
{
	return ((((n << 8) | n) << 16) >> 16);
}

var count = 0;

var wss = new WebSocketServer({
	port: 8080
});

wss.on('connection', function(ws) {
	console.log("Client Connected");

    ws.on('message', function(message) 
    {
    	message = JSON.parse(message);
        
        console.log(count + ":");
        console.log("X: " + short_to_ushort(message["OUT_X"]));
        console.log("Y: " + short_to_ushort(message["OUT_Y"]));
        console.log("Z: " + short_to_ushort(message["OUT_Z"]));

        count++;
    });
});