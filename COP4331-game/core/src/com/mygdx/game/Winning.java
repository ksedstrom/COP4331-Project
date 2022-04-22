package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;


public class Winning implements Screen {
    private int delay;
    final MyGdxGame game;
    OrthographicCamera camera;
    private BitmapFont font;

    public Winning(final MyGdxGame game) {
        this.game = game;
        deleteSaveData();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        game.runscompleted++;
        game.socket.emit("update_runs", game.userID, game.runscompleted);

    }

    public void deleteSaveData(){
        game.socket.emit("delete_save", game.userID);
        game.prefs.clear();
        game.prefs.flush();
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(1, 1, 1, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.fontHuge.draw(game.batch, "You Won", 580, 400);
        game.batch.end();


        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.setScreen(new MainMenu(game));
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}