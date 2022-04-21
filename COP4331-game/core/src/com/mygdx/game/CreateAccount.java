package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.socket.emitter.Emitter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateAccount implements Screen {
    final MyGdxGame game;

    OrthographicCamera camera;
    Stage s;
    private TextButton btnCreate;
    private TextButton backToMenu;
    private TextField usernameText;
    private TextField passwordText;
    private TextField confpwordText;
    private BitmapFont font;
    private boolean passmismatch;
    private boolean emptyfield;
    Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
    private int registered = 0; //0 for no create attempt //1 for successful creation //2 for duplicate username
    public CreateAccount(final MyGdxGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        configSocketEvents();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        camera.setToOrtho(false, 1280, 720);
        FitViewport viewp = new FitViewport(1280, 720, camera);
        s = new Stage(viewp, game.batch);
        Gdx.input.setInputProcessor(s);
        btnCreate = new TextButton("Create Account!", skin);
        btnCreate.setPosition(500, 200);
        btnCreate.setSize(300,60);

        backToMenu = new TextButton("Return to Main Menu", skin);
        backToMenu.setPosition(50, 650);
        backToMenu.setSize(200, 50);
        setListener();

        backToMenu.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent e, float x, float y, int point, int button){
                backToMenuClicked();
            }
        });

        usernameText = new TextField("", skin);
        usernameText.setPosition(500, 480);
        usernameText.setSize(300,60);
        usernameText.setMaxLength(20);

        passwordText = new TextField("", skin);
        passwordText.setPosition(500, 400);
        passwordText.setSize(300,60);
        passwordText.setPasswordMode(true);
        passwordText.setPasswordCharacter('*');
        passwordText.setMaxLength(20);

        confpwordText = new TextField("", skin);
        confpwordText.setPosition(500, 320);
        confpwordText.setSize(300, 60);
        confpwordText.setPasswordMode(true);
        confpwordText.setPasswordCharacter('*');
        confpwordText.setMaxLength(20);
        s.addActor(confpwordText);
        s.addActor(backToMenu);
        s.addActor(btnCreate);
        s.addActor(usernameText);
        s.addActor(passwordText);

    }

    public void setListener(){
        btnCreate.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent e, float x, float y, int point, int button){
                btnLoginClicked();
            }
        });
    }

    public void registeredUser(){
        registered = 1;
    }
    public void dupUser(){
        registered = 2;
    }
    public void backToMenuClicked(){

        game.setScreen(new MainMenu(game));
        dispose();
    }
    public void btnLoginClicked(){
        if(passwordText.getText().equals(confpwordText.getText()) && passwordText.getText().isEmpty() == false && usernameText.getText().isEmpty() == false) {
            passmismatch = false;
            emptyfield = false;
            game.socket.emit("create_account", usernameText.getText(), passwordText.getText());

        }
        else if(passwordText.getText().isEmpty() == true || usernameText.getText().isEmpty() == true || confpwordText.getText().isEmpty() == true){
            emptyfield = true;
            passmismatch = false;
            setListener();
        }
        else{
            emptyfield = false;
            passmismatch = true;
            setListener();
        }
    }
    public void turnOffListener(){
        game.socket.off("create_success");
        game.socket.off("duplicate_user");
    }
    public void configSocketEvents(){
        game.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener(){
            @Override
            public void call(Object... args){
                System.out.println("Connected to server");
            }
        }).on("create_success", new Emitter.Listener(){
            @Override
            public void call(Object... args){
                turnOffListener();
                JSONObject data = (JSONObject) args[0];
                try{
                    registeredUser();
                    int id = data.getInt("insertId");
                    System.out.println(id);
                }catch(JSONException e){
                    System.out.println("problem at json exception at creating account response");
                }
            }
        }).on("duplicate_user", new Emitter.Listener(){
            @Override
            public void call(Object... args){
                System.out.println("Username already taken");
                dupUser();
            }
        });
    }
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        font.draw(game.batch, "Username: ", 400, 515);
        font.draw(game.batch, "Password: ", 400, 435);
        font.draw(game.batch, "Confirm Password: ", 340, 355);
        if(passmismatch){
            font.draw(game.batch, "Passwords Do Not Match", 550, 80);
        }
        if(emptyfield){
            font.draw(game.batch, "One of your fields is empty", 550, 80);
        }
        if(registered == 1){
            font.draw(game.batch, "Successfully Registered a User", 550, 80);
        }
        if (registered == 2) {
            font.draw(game.batch, "Username is already Taken", 550, 80);
        }
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
