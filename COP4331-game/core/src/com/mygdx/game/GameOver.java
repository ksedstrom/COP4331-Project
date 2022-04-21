package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;


public class GameOver implements Screen {
    private int delay;
    final MyGdxGame game;
    OrthographicCamera camera;
    private BitmapFont font;

    public GameOver(final MyGdxGame game) {
        this.game = game;
        deleteSaveData();
        delay = 120;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);


    }

    public void deleteSaveData(){
        game.socket.emit("delete_save", game.userID);
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
        game.fontHuge.draw(game.batch, "GAME OVER", 580, 400);
        game.batch.end();

        if(delay > 0){
            delay--;
        }
        else{
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
