package io.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
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
    private boolean canUseStairs = true;
    private float stairsCooldown = 0.5f;
    private float currentCooldown = 0f;

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
        generator = new DungeonGenerator(level);
        generator.generate();
        updateDungeonData();

        // center camera on player initially
        camera.position.set(player.position.x, player.position.y, 0);
        camera.update();

        //musica
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/music/S2.mp3"));
        gameMusic.setLooping(true);
        gameMusic.setVolume(0.7f);
        
        // Configurar input processor
        if (player instanceof InputProcessor) {
            Gdx.input.setInputProcessor((InputProcessor) player);
        }
    }
    
    /**
     * Actualiza los datos del dungeon después de generarlo
     */
    private void updateDungeonData() {
        graph = generator.getGraph();
        dungeon = new ArrayList<>(graph.getRooms());
        
        // Colocar al jugador en la habitación inicial
        Room startRoom = generator.getStartRoom();
        if (startRoom != null) {
            float startX = startRoom.centerRoomX() * tileW;
            float startY = startRoom.centerRoomY() * tileH;
            player.position.set(startX, startY);
        } else {
            // Fallback
            player.position.set(tileW * 0.5f, tileH * 0.5f);
        }
        
        player.movement.set(0f, 0f);
        
        // Resetear cooldown de escaleras
        canUseStairs = true;
        currentCooldown = 0f;
    }

    @Override
    public void render(float delta) {
        // Actualizar cooldown de escaleras
        if (!canUseStairs) {
            currentCooldown += delta;
            if (currentCooldown >= stairsCooldown) {
                canUseStairs = true;
                currentCooldown = 0f;
            }
        }
        
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
        if (current != null && current.hasStairs && player.wantsNextLevel() && canUseStairs) {
            canUseStairs = false;
            
            // Generar nuevo nivel
            generator.onStairsFound();
            level = generator.getCurrentLevel();
            updateDungeonData();
            
            // center camera on player
            camera.position.set(player.position.x, player.position.y, 0);
            camera.update();
            
            return;
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

    @Override 
    public void resize(int width, int height) { 
        viewport.update(width, height, true);
        camera.update();
    }
    
    @Override 
    public void show() { 
        gameMusic.play(); 
    }
    
    @Override 
    public void hide() {
        gameMusic.stop();
    }
    
    @Override 
    public void pause() {
        gameMusic.pause();
    }
    
    @Override 
    public void resume() {
        gameMusic.play();
    }
    
    @Override 
    public void dispose() {
        if (gameMusic != null) {
            gameMusic.dispose();
        }
        if (renderer != null) {
            renderer.dispose();
        }
        // No es necesario destruir camera o viewport, LibGDX los maneja
    }
}