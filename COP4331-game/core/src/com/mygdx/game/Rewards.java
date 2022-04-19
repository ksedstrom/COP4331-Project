package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Random;

public class Rewards implements Screen {
    final MyGdxGame game;
    private RunData runData;

    OrthographicCamera camera;

    private Card[] rewards = new Card[3];
    private int xCor = 0;
    private int yCor = 0;
    private int cursorPos = 0;

    private Texture cursor;
    private Texture background;

    public Rewards (final MyGdxGame game, final RunData data){
        this.game = game;
        runData = data;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        // initialize cursor
        cursor = new Texture(Gdx.files.internal("upCursor.png"));
        if(runData.getLevel() < 5){
            background = new Texture(Gdx.files.internal("DesertBackground.png"));
        }
        else{
            background = new Texture(Gdx.files.internal("ForestBackground.png"));
        }

        // generate reward IDs using the seed from RunData and the current level.
        Random rand = new Random(runData.getSeed());
        for (int i = 0; i < 3; i++){
            // reward IDs will be from 0 to 33, does not avoid repeated values
            int id = rand.nextInt(34);
            // IDs 0, 5, 6, and 7 are all basic starting cards that are going to be replaced with other rewards
            // 0 and 5 will be a heal for some amount of HP and 6 and 7 will be card removal.
            // These IDs will be set to a special value to represent those rewards
            if(id == 0 || id == 5){id = 34;}
            if(id == 6 || id == 7){id = 35;}
            // generate associated card objects
            rewards[i] = new Card(id);
            rewards[i].updateDescription(null);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        // Render Stuff
        game.batch.begin();

        // Render Background
        game.batch.draw(background, 0, 0);

        // Render Rewards
        xCor = 128;
        yCor = 168;
        for(int i = 0; i < 3; i++){
            rewards[i].render(xCor, yCor, game, 2);
            // render cursor
            if(i == cursorPos){
                game.batch.draw(cursor, xCor + 64, 48, 128, 128);
            }
            xCor += 384;
        }
        game.batch.end();

        // Process User Input
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
            if(cursorPos > 0){
                cursorPos--;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (cursorPos < 3) {
                cursorPos++;
            }
        }
    }


    // Default Screen Methods
    @Override
    public void show() {

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
