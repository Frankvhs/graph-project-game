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
import io.game.components.Direction;
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
        float roomsVisibleX = 2.5f; // Habitaciones más grandes (antes 3)
        tileW = screenW / roomsVisibleX;
        tileH = tileW;

        player.size.set(tileW / 7f, tileH / 7f);

        // Load dungeon textures
        Resources.loadTexture("background", "graphics/tilesets/dungeons_tilesets");
        Resources.loadTexture("down_stairs", "graphics/sprites/world_objects");
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

        // update player with collision
        updatePlayerWithCollision(delta);

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
        
        // DEBUG: Dibujar colisiones (descomentar para visualizar)
        // renderCollisionDebug(batch);
        
        player.render(batch);
        
        batch.end();
    }
    
    // ----------------------------
    // Actualiza el jugador con detección de colisiones contra paredes
    // ----------------------------
    private void updatePlayerWithCollision(float delta) {
        // Guardar posición anterior
        float oldX = player.position.x;
        float oldY = player.position.y;
        
        // Actualizar jugador (calcula movimiento)
        player.update(delta);
        
        // Si no hay movimiento, no hay colisión que verificar
        if (player.movement.isZero()) {
            return;
        }
        
        // Verificar colisión y ajustar posición si es necesario
        if (checkCollisionWithWalls()) {
            // Colisión detectada, intentar movimiento en X solo
            player.position.set(oldX, oldY);
            player.position.x += player.movement.x * delta;
            
            if (checkCollisionWithWalls()) {
                // Aún hay colisión, intentar solo Y
                player.position.x = oldX;
                player.position.y = oldY + player.movement.y * delta;
                
                if (checkCollisionWithWalls()) {
                    // Colisión en ambos ejes, restaurar posición original
                    player.position.set(oldX, oldY);
                }
            }
        }
    }
    
    // ----------------------------
    // Verifica si el jugador colisiona con paredes de habitaciones
    // ----------------------------
    private boolean checkCollisionWithWalls() {
        // player.position es la esquina inferior izquierda del sprite
        // El hitbox será un rectángulo centrado en el sprite
        float playerCenterX = player.position.x + player.size.x * 0.5f;
        float playerCenterY = player.position.y + player.size.y * 0.5f;
        // Hitbox: 40% del tamaño del jugador (20% de radio = 40% de ancho)
        float hitboxRadius = player.size.x * 0.2f;
        float px1 = playerCenterX - hitboxRadius;
        float py1 = playerCenterY - hitboxRadius;
        float px2 = playerCenterX + hitboxRadius;
        float py2 = playerCenterY + hitboxRadius;
        
        // Verificar colisión con cada habitación
        for (Room room : dungeon) {
            float rx = room.x * tileW;
            float ry = room.y * tileH;
            
            // Grosor de pared visible en sprites (aproximadamente 14% del tile)
            float wallThickness = tileW * 0.14f;
            // Ancho de la puerta (11% desde el centro = 22% apertura total)
            float doorHalfWidth = tileW * 0.11f;
            
            // PARED NORTE (arriba) - borde interno
            if (!room.hasDoor(Direction.N)) {
                // Pared completa: desde borde interno hacia adentro
                float wallInnerY = ry + tileH - wallThickness;
                if (intersects(px1, py1, px2, py2,
                              rx, wallInnerY, rx + tileW, ry + tileH)) {
                    return true;
                }
            } else {
                // Pared con puerta - dos segmentos a los lados (extendidos hasta el borde)
                float centerX = rx + tileW * 0.5f;
                float wallInnerY = ry + tileH - wallThickness;
                // Segmento izquierdo (hasta el borde izquierdo completo)
                if (intersects(px1, py1, px2, py2,
                              rx, wallInnerY, centerX - doorHalfWidth, ry + tileH)) {
                    return true;
                }
                // Segmento derecho (hasta el borde derecho completo)
                if (intersects(px1, py1, px2, py2,
                              centerX + doorHalfWidth, wallInnerY, rx + tileW, ry + tileH)) {
                    return true;
                }
            }
            
            // PARED SUR (abajo) - borde interno
            if (!room.hasDoor(Direction.S)) {
                // Pared completa
                float wallInnerY = ry + wallThickness;
                if (intersects(px1, py1, px2, py2,
                              rx, ry, rx + tileW, wallInnerY)) {
                    return true;
                }
            } else {
                // Pared con puerta - dos segmentos a los lados (extendidos hasta los bordes)
                float centerX = rx + tileW * 0.5f;
                float wallInnerY = ry + wallThickness;
                // Segmento izquierdo (hasta el borde izquierdo completo)
                if (intersects(px1, py1, px2, py2,
                              rx, ry, centerX - doorHalfWidth, wallInnerY)) {
                    return true;
                }
                // Segmento derecho (hasta el borde derecho completo)
                if (intersects(px1, py1, px2, py2,
                              centerX + doorHalfWidth, ry, rx + tileW, wallInnerY)) {
                    return true;
                }
            }
            
            // PARED ESTE (derecha) - borde interno
            if (!room.hasDoor(Direction.E)) {
                // Pared completa
                float wallInnerX = rx + tileW - wallThickness;
                if (intersects(px1, py1, px2, py2,
                              wallInnerX, ry, rx + tileW, ry + tileH)) {
                    return true;
                }
            } else {
                // Pared con puerta - dos segmentos arriba y abajo (extendidos hasta los bordes)
                float centerY = ry + tileH * 0.5f;
                float wallInnerX = rx + tileW - wallThickness;
                // Segmento inferior (hasta el borde inferior completo)
                if (intersects(px1, py1, px2, py2,
                              wallInnerX, ry, rx + tileW, centerY - doorHalfWidth)) {
                    return true;
                }
                // Segmento superior (hasta el borde superior completo)
                if (intersects(px1, py1, px2, py2,
                              wallInnerX, centerY + doorHalfWidth, rx + tileW, ry + tileH)) {
                    return true;
                }
            }
            
            // PARED OESTE (izquierda) - borde interno
            if (!room.hasDoor(Direction.O)) {
                // Pared completa
                float wallInnerX = rx + wallThickness;
                if (intersects(px1, py1, px2, py2,
                              rx, ry, wallInnerX, ry + tileH)) {
                    return true;
                }
            } else {
                // Pared con puerta - dos segmentos arriba y abajo (extendidos hasta los bordes)
                float centerY = ry + tileH * 0.5f;
                float wallInnerX = rx + wallThickness;
                // Segmento inferior (hasta el borde inferior completo)
                if (intersects(px1, py1, px2, py2,
                              rx, ry, wallInnerX, centerY - doorHalfWidth)) {
                    return true;
                }
                // Segmento superior (hasta el borde superior completo)
                if (intersects(px1, py1, px2, py2,
                              rx, centerY + doorHalfWidth, wallInnerX, ry + tileH)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // Helper: verifica si dos rectángulos AABB se intersectan
    private boolean intersects(float x1, float y1, float x2, float y2,
                               float x3, float y3, float x4, float y4) {
        return x1 < x4 && x2 > x3 && y1 < y4 && y2 > y3;
    }
    
    // DEBUG: Renderizar las cajas de colisión para visualizar dónde están
    private void renderCollisionDebug(SpriteBatch batch) {
        batch.end();
        
        // Usar ShapeRenderer para dibujar rectángulos
        com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer = 
            new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        
        for (Room room : dungeon) {
            float rx = room.x * tileW;
            float ry = room.y * tileH;
            float wallThickness = tileW * 0.14f;
            float doorHalfWidth = tileW * 0.11f;
            
            // Dibujar paredes según tengan o no puertas (en borde interno)
            // NORTE
            if (!room.hasDoor(Direction.N)) {
                float wallInnerY = ry + tileH - wallThickness;
                shapeRenderer.rect(rx, wallInnerY, tileW, wallThickness);
            } else {
                float centerX = rx + tileW * 0.5f;
                float wallInnerY = ry + tileH - wallThickness;
                shapeRenderer.rect(rx, wallInnerY, centerX - doorHalfWidth - rx, wallThickness);
                shapeRenderer.rect(centerX + doorHalfWidth, wallInnerY, rx + tileW - (centerX + doorHalfWidth), wallThickness);
            }
            
            // SUR
            if (!room.hasDoor(Direction.S)) {
                float wallInnerY = ry + wallThickness;
                shapeRenderer.rect(rx, ry, tileW, wallThickness);
            } else {
                float centerX = rx + tileW * 0.5f;
                float wallInnerY = ry + wallThickness;
                shapeRenderer.rect(rx, ry, centerX - doorHalfWidth - rx, wallThickness);
                shapeRenderer.rect(centerX + doorHalfWidth, ry, rx + tileW - (centerX + doorHalfWidth), wallThickness);
            }
            
            // ESTE
            if (!room.hasDoor(Direction.E)) {
                float wallInnerX = rx + tileW - wallThickness;
                shapeRenderer.rect(wallInnerX, ry, wallThickness, tileH);
            } else {
                float centerY = ry + tileH * 0.5f;
                float wallInnerX = rx + tileW - wallThickness;
                shapeRenderer.rect(wallInnerX, ry, wallThickness, centerY - doorHalfWidth - ry);
                shapeRenderer.rect(wallInnerX, centerY + doorHalfWidth, wallThickness, ry + tileH - (centerY + doorHalfWidth));
            }
            
            // OESTE
            if (!room.hasDoor(Direction.O)) {
                float wallInnerX = rx + wallThickness;
                shapeRenderer.rect(rx, ry, wallThickness, tileH);
            } else {
                float centerY = ry + tileH * 0.5f;
                float wallInnerX = rx + wallThickness;
                shapeRenderer.rect(rx, ry, wallThickness, centerY - doorHalfWidth - ry);
                shapeRenderer.rect(rx, centerY + doorHalfWidth, wallThickness, ry + tileH - (centerY + doorHalfWidth));
            }
        }
        
        // Dibujar hitbox del jugador (centrado en el sprite)
        shapeRenderer.setColor(Color.GREEN);
        float playerCenterX = player.position.x + player.size.x * 0.5f;
        float playerCenterY = player.position.y + player.size.y * 0.5f;
        float hitboxRadius = player.size.x * 0.2f;
        shapeRenderer.rect(playerCenterX - hitboxRadius, playerCenterY - hitboxRadius, 
                          hitboxRadius * 2, hitboxRadius * 2);
        
        shapeRenderer.end();
        batch.begin();
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