package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
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
    Table leaderboard;
    ScrollPane scroll;
    Table container;

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
    public void setUpTable(){
        leaderboard = new Table();
        Label rank = new Label("Rank", skin);
        Label userheader = new Label("Username", skin);
        Label scoreheader = new Label("Runs Completed", skin);
        leaderboard.top().left();
        Label esc = new Label("Press [Esc] to Return", skin);
        leaderboard.add(esc).left().top().padLeft(10);
        leaderboard.row();
        leaderboard.add(rank).padLeft(350).left().width(150);
        leaderboard.add(userheader).padLeft(10).left().width(300);
        leaderboard.add(scoreheader).padLeft(10).left().width(200);
        for(int x = 0; x < tables.length; x++){
            leaderboard.row();
            Label position = new Label(""+ (x+1), skin);
            Label username = new Label(tables[x][0], skin);
            Label score = new Label(tables[x][1], skin);
            if(x == tables.length - 1){
                leaderboard.add(position).padLeft(350).left().padBottom(40);
                leaderboard.add(username).padLeft(10).left().padBottom(40);
                leaderboard.add(score).padLeft(10).left().padBottom(40);
            }
            else{
                leaderboard.add(position).padLeft(350).left();
                leaderboard.add(username).padLeft(10).left();
                leaderboard.add(score).padLeft(10).left();
            }


        }
        scroll = new ScrollPane(leaderboard, skin);
        scroll.setHeight(720);
        scroll.setWidth(1280);
        scroll.setScrollbarsVisible(true);
        container = new Table();
        container.setPosition(640,360);
        container.add(scroll).width(1280).height(720);
        scroll.setFadeScrollBars(false);
        s.addActor(container);
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
        dispose();
    }
    public void printTable(){
        for (int x = 0; x < tables.length; x++){
            System.out.println(tables[x][0]);
            System.out.println(tables[x][1]);
        }
    }
    public void turnOffListener(){
        game.socket.off("leaderboard");
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
                turnOffListener();
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
                    setUpTable();
                    printTable();


                }catch(JSONException e){
                    System.out.println("Json Exception at loading leaderboard");
                }
            }
        });
    }
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.end();
        s.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
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