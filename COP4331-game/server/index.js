//need to npm install
var app = require('express')();
//need to npm install express --save
var server = require('http').Server(app);
var io = require('socket.io')(server);
//need to npm install socket.io --save
var mysql = require('mysql');
//need to npm install mysql
const PORT = 8080;
server.listen(PORT, function(){
    console.log("server is now running...");
});

var con = mysql.createConnection({
    host: "cop4331-db.cj0jhrsdjgqb.us-east-1.rds.amazonaws.com",
    user: "admin",
    password: "Cop4331!!",
    database: "cop4331-game"
});

con.connect(function(err){
    if (err) throw err;
    console.log("Connected to database");
})

io.on('connection', function(socket){
    console.log("Player Connected!");
    socket.on('create_account', (uname, pword) => {
        var sql = "Insert INTO Users (username, password) VALUES (?)";
        let values = [
            uname,
            pword
        ]
        con.query(sql, [values], function(err, result){
            if (err) throw err;
            socket.emit("create_success", {id: socket.id});
            console.log(result);          
        })
    },
    socket.on('log_in', (uname, pword) =>{
        if(uname && pword){
            let sql = "Select * FROM Users Where username = BINARY '" + uname + "' AND password = BINARY'" + pword + "'";
            con.query(sql, function (error, data){
                if(error) throw error;
                console.log(data)
                if(data.length>0){
                    socket.emit("login_success", {userID: data[0].idUsers});
                } else {
                    socket.emit("login_failed", {message: "Authentication Failed"});
                }
            })
        }
    }),
    socket.on('disconnect',function(){
        console.log("Player Disconnected");
    })
    );
});

