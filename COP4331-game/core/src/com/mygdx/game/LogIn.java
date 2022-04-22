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

public class LogIn implements Screen {
    final MyGdxGame game;

    OrthographicCamera camera;
    Stage s;
    private TextButton btnLogIn;
    private TextButton backToMenu;
    private TextField usernameText;
    private TextField passwordText;
    private BitmapFont font;
    Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
    private int loggedin = 0;
    public LogIn(final MyGdxGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        configSocketEvents();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        camera.setToOrtho(false, 1280, 720);
        FitViewport viewp = new FitViewport(1280, 720, camera);
        s = new Stage(viewp, game.batch);
        Gdx.input.setInputProcessor(s);
        btnLogIn = new TextButton("Log In!", skin);
        btnLogIn.setPosition(500, 200);
        btnLogIn.setSize(300,60);

        backToMenu = new TextButton("Return to Main Menu", skin);
        backToMenu.setPosition(50, 650);
        backToMenu.setSize(200, 50);
        setupListener();


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
        s.addActor(backToMenu);
        s.addActor(btnLogIn);
        s.addActor(usernameText);
        s.addActor(passwordText);

    }

    public void setupListener(){
        btnLogIn.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent e, float x, float y, int point, int button){
                btnLoginClicked();
            }
        });
    }
    // TextField usernameTextField = new TextField("",));
    // s.addActor(usernameTextField)
    //game.socket.on("create_success")
    public void loggedIn(){
        loggedin = 1;
    }

    public void loginFailed(){
        loggedin = 2;
        setupListener();
    }
    public void backToMenuClicked(){

        game.setScreen(new MainMenu(game));
        dispose();
    }
    public void btnLoginClicked(){
        game.socket.emit("log_in", usernameText.getText(), passwordText.getText());
        System.out.println(usernameText.getText());
        System.out.println(passwordText.getText());
    }
    public void turnOffListeners(){
        game.socket.off("login_success");
        game.socket.off("login_failed");
    }
    public void configSocketEvents(){
        game.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener(){
            @Override
            public void call(Object... args){
                System.out.println("Connected to server");
            }
        }).on("login_success", new Emitter.Listener(){
            @Override
            public void call(Object... args){
                JSONObject data = (JSONObject) args[0];
                try{
                    turnOffListeners();
                    loggedIn();
                    String userID = data.getString("userID");
                    game.userID = Integer.parseInt(userID);
                    System.out.println(userID);
                    System.out.println("games user id: " + userID);
                }catch(JSONException e){
                    System.out.println("Json Exception at logging in");
                }
            }
        }).on("login_failed", new Emitter.Listener(){
            @Override
            public void call(Object... args){
                loginFailed();
                System.out.println("Failed to log in");
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
        switch(loggedin){
            case 0:
                break;
            case 1:
                font.draw(game.batch, "Successfully Logged In", 550, 80);
                break;
            case 2:
                font.draw(game.batch, "Failed to Log In", 550, 80);
                break;
            default:

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
