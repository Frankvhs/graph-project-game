package io.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.game.GameMain;
import io.game.entities.characters.Player;
import io.game.generator.DungeonGenerator;
import io.game.maps.Room;
import io.game.maps.DungeonGraph;
import io.game.managers.Resources;
import io.game.managers.RoomManager;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {

    private SpriteBatch batch;
    private Player player;

    private DungeonGenerator generator;
    private DungeonRenderer renderer;
    private List<Room> dungeon;
    private DungeonGraph graph;

    private OrthographicCamera camera;
    private ScreenViewport viewport;

    private float tileW, tileH;
    private int level = 1;

    private Music gameMusic;

    public GameScreen(GameMain game) {
        this.batch = game.batch;

        Player.loadTextures();
        player = new Player();

        // Load rooms
        RoomManager.load();

        // === initial scaling and camera ===
        float screenW = Gdx.graphics.getWidth();
        int roomsVisibleX = 3;
        tileW = screenW / roomsVisibleX;
        tileH = tileW;

        player.size.set(tileW / 7f, tileH / 7f);

        // Load dungeon textures
        Resources.loadTexture("background", "graphics/tilesets/dungeons_tilesets");
        Resources.loadTexture("stairs", "graphics/tilesets/dungeons_tilesets");
        Resources.finish();

        renderer = new DungeonRenderer(tileW, tileH);

        // setup camera and viewport
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.update((int) screenW, (int) Gdx.graphics.getHeight(), true);

        // generate first dungeon
        generator = new DungeonGenerator();
        regenerate(level);

        // center camera on player initially
        camera.position.set(player.position.x, player.position.y, 0);
        camera.update();

        //musica
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/music/S2.mp3"));
        gameMusic.setLooping(true);
        gameMusic.setVolume(0.7f);
    }

    private void regenerate(int newLevel) {
        this.level = newLevel;

        // generate into generator.graph (generator clears graph internally)
        generator.generate(level);
        graph = generator.getGraph();
        dungeon = new ArrayList<>(graph.getRooms());

        // put player in the start room (0,0) center
        player.position.set(0f + tileW * 0.5f, 0f + tileH * 0.5f);
        player.movement.set(0f, 0f);

        // center camera on player
        camera.position.set(player.position.x, player.position.y, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        // move camera with player
        camera.position.set(player.position.x, player.position.y, 0);
        camera.update();

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        // update player
        player.update(delta);

        // check if player stands over a room with stairs and pressed E
        Room current = getRoomAtPlayer();
        if (current != null && current.hasStairs && player.wantsNextLevel()) {
            regenerate(level + 1);
            return; // skip one frame to avoid input repeat
        }

        // render
        batch.begin();
        renderer.render(batch, dungeon);
        player.render(batch);
        batch.end();
    }

    private Room getRoomAtPlayer() {
        float px = player.position.x;
        float py = player.position.y;

        for (Room r : dungeon) {
            float rx = r.x * tileW;
            float ry = r.y * tileH;
            if (px >= rx && px <= rx + tileW &&
                py >= ry && py <= ry + tileH) {
                return r;
            }
        }
        return null;
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override public void show() { 
        gameMusic.play(); 
        Gdx.input.setInputProcessor(null); 
    }
    @Override public void hide() {gameMusic.stop();}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        if (gameMusic != null) gameMusic.dispose();
        if (renderer != null) renderer.dispose();
        if (viewport != null) viewport = null;
        if (camera != null) camera = null;
    }
}
