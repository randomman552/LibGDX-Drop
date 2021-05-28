package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen implements Screen {
    final Drop game;

    private Texture dropImage;
    private Texture bucketImage;

    private Sound dropSound;

    private Music rainMusic;

    private OrthographicCamera camera;

    private Rectangle bucket;

    // Variables for raindrop creation and storage
    private Array<Rectangle> raindrops;
    private long lastDropTime;

    // Variable to store how many raindrops have been caught
    private int dropsCaught = 0;

    public GameScreen(final Drop game)
    {
        this.game = game;

        // Load images
        dropImage = new Texture(Gdx.files.internal("drop.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        // Load sound effects
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));

        // Load music
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        // Create camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // Initialise bucket
        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2;
        bucket.y = 20;
        bucket.width = 64;
        bucket.height = 64;

        // Initialise raindrops array
        raindrops = new Array<Rectangle>();
        spawnRaindrop();

        // Start playback of background music
        rainMusic.setLooping(true);
    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800 - 64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void show() {
        rainMusic.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);

        // Draw sprites
        game.batch.begin();

        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for(Rectangle raindrop: raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y);
        }

        // Draw current score
        game.font.draw(game.batch, "Drops Collected: " + dropsCaught, 0, 480);

        game.batch.end();

        // Handle mouse or touch screen based movement
        // Move bucket to touched position
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
        }

        // Handle keyboard based movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

        // Ensure bucket is within max bounds
        if (bucket.x < 0) bucket.x = 0;
        if (bucket.x > 800 - 64) bucket.x = 800 - 64;

        // Spawn new raindrop if we need to
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

        // Move all raindrops down the screen
        for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext();) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if (raindrop.y < 0) iter.remove();

            // Check for caught raindrops
            if(raindrop.overlaps(bucket)) {
                dropSound.play();
                iter.remove();
                dropsCaught++;
            }
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
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }
}
