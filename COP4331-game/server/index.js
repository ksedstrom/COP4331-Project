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
            if (err){
                if(err.code === 'ER_DUP_ENTRY'){
                    socket.emit("duplicate_user", {message: "Duplicate User"})
                }
                else
                    throw err;  
            }
            else{
            socket.emit("create_success", result);
            console.log([result])

            }          
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
    socket.on('load_game', (userID) => {
        let sql = "Select * From RunData Where idUsers = "+ userID;
        con.query(sql, function(error, data){
            if(error) throw error;
            console.log(data)
            if(data.length>0){
                socket.emit("game_loaded", data[0]);
            }else{
                socket.emit("no_save_found", {message: "no save"});
            }
        })
    }),
    socket.on('save_game', (userID, seed, health, maxHealth, level, deckList, combatclear) =>{
        let sql = "Insert INTO RunData (idUsers, seed, health, maxHealth, currentLevel, deckString, combatCleared) VALUES (?) ON DUPLICATE KEY UPDATE seed = " + seed + ", health = "+ health + ", maxHealth = "+ maxHealth+ ", currentLevel = "+ level + ", deckString = '"+ deckList+"', combatCleared = " + combatclear;
        let values =[
            userID,
            seed,
            health,
            maxHealth,
            level,
            deckList,
            combatclear
        ]
        con.query(sql, [values], function(err, result){
            if(err) throw err;
            console.log(result);
        })
    }),
    socket.on('delete_save', (userID) =>{
        let sql = "Delete FROM RunData WHERE idUsers =" + userID;
        con.query(sql, function(error, data){
            if(error) throw error;
            console.log(data)
        })
    }),
    socket.on('initialize_leaderboard', (userID, uname) =>{
        let sql = "Insert INTO leaderboard (idUsers, username, runscompleted) VALUES (?)";
        let values = [
            userID,
            uname,
            0
        ]
        con.query(sql, [values], function(err, result){
            if (err) throw err;
            console.log(result);
        })
    }),
    socket.on('load_runs', (userID) =>{
        let sql = "Select * From leaderboard Where idUsers =" + userID;
        con.query(sql, function(error,data){
            if(error) throw error;
            console.log(data)
            socket.emit("loaded_runscomplete",{runscomplete: data[0].runscompleted})
        })
    }),
    socket.on('update_runs',(userID, runscomplete) =>{
        let sql = "Update leaderboard SET runscompleted = ? WHERE idUsers = ?";
        let values = [
            runscomplete,
            userID
        ]
        con.query(sql, values, function(err, result){
            if(err) throw err;
            console.log(result);
        })
    }),
    socket.on('pull_leaderboard', () =>{
        let sql = "Select * From leaderboard ORDER BY runscompleted";
        con.query(sql, function(error, data){
            if(error) throw error;
            console.log(data)
            socket.emit("leaderboard", data)
        })
    }),
    socket.on('disconnect',function(){
        console.log("Player Disconnected");
    })
    );
});

