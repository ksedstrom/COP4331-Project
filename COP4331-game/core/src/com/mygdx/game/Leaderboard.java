package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.socket.emitter.Emitter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Leaderboard implements Screen {
    final MyGdxGame game;
    String[][] tables;
    OrthographicCamera camera;
    Stage s;
    private TextButton backToMenu;
    private BitmapFont font;
    private boolean tableFinished;
    Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

    public Leaderboard(final MyGdxGame game) {
        this.game = game;
        game.socket.emit("pull_leaderboard");
        camera = new OrthographicCamera();
        configSocketEvents();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        camera.setToOrtho(false, 1280, 720);
        FitViewport viewp = new FitViewport(1280, 720, camera);
        s = new Stage(viewp, game.batch);
        Gdx.input.setInputProcessor(s);

        backToMenu = new TextButton("Return to Main Menu", skin);
        backToMenu.setPosition(50, 650);
        backToMenu.setSize(200, 50);



        backToMenu.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent e, float x, float y, int point, int button){
                backToMenuClicked();
            }
        });
        s.addActor(backToMenu);


    }

    public void initializeTables(int length){
        tables = new String[length][2];
    }
    public void setFinishedTable(boolean x){
        tableFinished = true;
    }
    public void addToTable(int row, String Username, String runscomplete){
        tables[row][0] = Username;
        tables[row][1] = runscomplete;
    }
    public void backToMenuClicked(){
        game.setScreen(new MainMenu(game));
    }
    public void printTable(){
        for (int x = 0; x < tables.length; x++){
            System.out.println(tables[x][0]);
            System.out.println(tables[x][1]);
        }
    }
    public void configSocketEvents(){
        game.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener(){
            @Override
            public void call(Object... args){
                System.out.println("Connected to server");
            }
        }).on("leaderboard", new Emitter.Listener(){
            @Override
            public void call(Object... args){
                JSONArray data = (JSONArray) args[0];
                initializeTables(data.length());
                try{
                    for(int i = data.length() - 1; i >=0; --i){
                        JSONObject dat = data.getJSONObject(i);
                        String username = dat.getString("username");
                        String runscompleted = dat.getString("runscompleted");
                        addToTable(data.length()-1-i, username, runscompleted);
                    }
                    setFinishedTable(true);
                    printTable();

                }catch(JSONException e){
                    System.out.println("Json Exception at loading leaderboard");
                }
            }
        });
    }
    @Override
    public void render(float delta) {
        ScreenUtils.clear(1, 1, 1, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.fontLarge.draw(game.batch, "UserName", 300, 690);
        game.fontLarge.draw(game.batch, "Runs Completed", 500, 690);
        if(tableFinished) {
            for (int x = 0; x < tables.length; x++){
                game.fontLarge.draw(game.batch, tables[x][0], 300, 650 - (x * 30));
                game.fontLarge.draw(game.batch, tables[x][1], 500, 650 - (x * 30));
                String temp = "" + (x+1);
                game.fontLarge.draw(game.batch, temp, 270, 650 - (x*30));

            }
        }
//        font.draw(game.batch, "Username: ", 400, 515);
//        font.draw(game.batch, "Password: ", 400, 435);
//        switch(loggedin){
//            case 0:
//                break;
//            case 1:
//                font.draw(game.batch, "Successfully Logged In", 550, 80);
//                break;
//            case 2:
//                font.draw(game.batch, "Failed to Log In", 550, 80);
//                break;
//            default:
//
//        }
        game.batch.end();
        s.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.setScreen(new MainMenu(game));
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        game.batch.dispose();
        s.dispose();
        font.dispose();
    }

    @Override
    public void hide() {
    }

    @Override
    public void show() {
    }

    @Override
    public void pause() {
    }
}
