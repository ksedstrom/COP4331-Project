package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
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

public class CreateAccount implements Screen {
    final MyGdxGame game;

    OrthographicCamera camera;
    Stage s;
    private TextButton btnLogin;
    private TextField usernameText;
    private TextField passwordText;
    Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
    public CreateAccount(final MyGdxGame game) {
        this.game = game;
        camera = new OrthographicCamera();

        camera.setToOrtho(false, 1280, 720);
        FitViewport viewp = new FitViewport(1280, 720, camera);
        s = new Stage(viewp, game.batch);
        Gdx.input.setInputProcessor(s);
        btnLogin = new TextButton("Create Account!", skin);
        btnLogin.setPosition(500, 200);
        btnLogin.setSize(300,60);
        btnLogin.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent e, float x, float y, int point, int button){
                btnLoginClicked();
            }
        });
        usernameText = new TextField("", skin);
        usernameText.setPosition(500, 480);
        usernameText.setSize(300,60);

        passwordText = new TextField("", skin);
        passwordText.setPosition(500, 400);
        passwordText.setSize(300,60);
        s.addActor(btnLogin);
        s.addActor(usernameText);
        s.addActor(passwordText);

    }
   // TextField usernameTextField = new TextField("",));
   // s.addActor(usernameTextField)

    public void btnLoginClicked(){
        game.socket.emit("create_account", usernameText.getText(), passwordText.getText());
        System.out.println(usernameText.getText());
        System.out.println(passwordText.getText());
    }
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.end();
        s.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.setScreen(new MainMenu(this.game));
            dispose();
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
