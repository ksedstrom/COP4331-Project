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
    private BitmapFont font;
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
        btnCreate.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent e, float x, float y, int point, int button){
                btnLoginClicked();
            }
        });

        backToMenu.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent e, float x, float y, int point, int button){
                backToMenuClicked();
            }
        });
//        game.socket.on("create_success", new Emitter.Listener(){
//            @Override
//            public void call(Object... args){
//                Gdx.app.log("CreateAccount", "account is created");
//            }
//
//        });
        usernameText = new TextField("", skin);
        usernameText.setPosition(500, 480);
        usernameText.setSize(300,60);

        passwordText = new TextField("", skin);
        passwordText.setPosition(500, 400);
        passwordText.setSize(300,60);
        s.addActor(backToMenu);
        s.addActor(btnCreate);
        s.addActor(usernameText);
        s.addActor(passwordText);

    }


    // TextField usernameTextField = new TextField("",));
   // s.addActor(usernameTextField)
    //game.socket.on("create_success")
    public void registeredUser(){
        registered = 1;
    }
    public void dupUser(){
        registered = 2;
    }
    public void backToMenuClicked(){
        game.setScreen(new MainMenu(game));
    }
    public void btnLoginClicked(){
        game.socket.emit("create_account", usernameText.getText(), passwordText.getText());
        System.out.println(usernameText.getText());
        System.out.println(passwordText.getText());
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
                JSONObject data = (JSONObject) args[0];
                try{
                    registeredUser();
                    String id = data.getString("id");
                    System.out.println(id);
                }catch(JSONException e){
                    System.out.println("Didn't create");
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
