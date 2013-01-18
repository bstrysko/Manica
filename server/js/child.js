var Connect = require('connect');

Connect.createServer(
    Connect.static(__dirname + "/static")
).listen(80);