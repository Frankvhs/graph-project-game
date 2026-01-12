package io.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.game.GameMain;
import io.game.components.Direction;
import io.game.entities.AnimatedEntity;
import io.game.entities.characters.Player;
import io.game.entities.characters.Orc;
import io.game.generator.DungeonGenerator;
import io.game.maps.Room;
import io.game.maps.DungeonGraph;
import io.game.managers.Resources;
import io.game.managers.RoomManager;
import io.game.ui.GameOverScreen;
import io.game.ui.HealthBar;
import io.game.ui.PauseMenu;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {

    private static final int MAX_DUNGEONS = 5;

    private SpriteBatch batch;
    private Player player;
    private List<Orc> enemies;

    private DungeonGenerator generator;
    private DungeonRenderer renderer;
    private List<Room> dungeon;
    private DungeonGraph graph;

    private OrthographicCamera camera;
    private ScreenViewport viewport;

    private float tileW, tileH;
    private int level = 1;

    private Music gameMusic;
    private PauseMenu pauseMenu;
    private final GameMain game;
    private HealthBar healthBar;
    private GameOverScreen gameOverScreen;
    private com.badlogic.gdx.graphics.g2d.BitmapFont uiFont;

    public GameScreen(GameMain game) {
        this.game = game;
        this.batch = game.batch;

        Player.loadTextures();
        player = new Player();
        
        // Cargar texturas de enemigos
        Orc.loadTextures();
        enemies = new ArrayList<>();

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
        Resources.loadTexture("chest_close", "graphics/sprites/world_objects");
        Resources.loadTexture("chest_open", "graphics/sprites/world_objects");
        Resources.loadTexture("key1", "graphics/sprites/objects");
        Resources.loadTexture("flasks_1_1", "graphics/sprites/objects");
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
        
        // Crear menú de pausa
        pauseMenu = new PauseMenu(
            batch,
            () -> { /* Continuar juego */ },
            () -> { this.game.setScreen(this.game.menuScreen); },
            () -> { Gdx.app.exit(); }
        );
        
        // Crear barra de vida
        healthBar = new HealthBar(20, Gdx.graphics.getHeight() - 70, 200, 50);
        
        // Crear pantalla de game over
        gameOverScreen = new GameOverScreen(
            batch,
            () -> { restart(); }, // Reiniciar en nivel 1 con vida completa
            () -> { Gdx.app.exit(); } // Salir del juego
        );
        
        // Crear fuente para UI
        uiFont = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        uiFont.getData().setScale(2.0f);
        uiFont.setColor(Color.WHITE);
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
        
        // Resetear llaves al cambiar de nivel
        player.resetKeys();
        
        // Ocultar game over si está visible
        if (gameOverScreen != null && gameOverScreen.isVisible()) {
            gameOverScreen.hide();
        }
        
        // Generar enemigos aleatoriamente en las habitaciones
        generateEnemies();

        // center camera on player
        camera.position.set(player.position.x, player.position.y, 0);
        camera.update();
    }
    
    /**
     * Método público para reiniciar el juego desde el nivel 1
     */
    public void restart() {
        // Restaurar la vida completa al reiniciar desde game over
        player.health.heal(player.health.getMaxHealth());
        regenerate(1);
    }

    @Override
    public void render(float delta) {
        // Detectar tecla ESC para pausar/despausar
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseMenu.toggle();
        }
        
        // Verificar si el jugador murió
        if (player.health.isDead() && !gameOverScreen.isVisible()) {
            gameOverScreen.show();
        }
        
        ScreenUtils.clear(Color.BLACK);

        // move camera with player
        camera.position.set(player.position.x, player.position.y, 0);
        camera.update();

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        // Solo actualizar el juego si el menú de pausa y game over no están visibles
        if (!pauseMenu.isVisible() && !gameOverScreen.isVisible()) {
            // update player with collision
            updatePlayerWithCollision(delta);
            
            // Actualizar enemigos
            updateEnemies(delta);
            
            // Resolver colisiones entre entidades (empujar para evitar solapamiento)
            resolveEntityCollisions();
            
            // Verificar combate entre jugador y enemigos
            checkCombat();

            // check if player stands over a room with stairs and pressed E
            Room current = getRoomAtPlayer();
            if (current != null && player.wantsNextLevel()) {
                // Verificar si hay una llave en la habitación
                if (current.hasKey && !current.keyCollected) {
                    current.keyCollected = true;
                    player.addKey();
                    System.out.println("¡Llave recogida! Total: " + player.getKeys());
                }
                // Verificar si hay un cofre cerrado en la habitación
                else if (current.hasChest && !current.chestOpened) {
                    if (player.useKey()) {
                        current.chestOpened = true;
                        // Dar frascos al abrir el cofre (1-3 frascos aleatorios)
                        int flasksFound = 1 + (int)(Math.random() * 3);
                        for (int i = 0; i < flasksFound; i++) {
                            player.addFlask();
                        }
                        System.out.println("¡Cofre abierto! Encontraste " + flasksFound + " flask(s). Total: " + player.getFlasks() + " - Llaves restantes: " + player.getKeys());
                    } else {
                        System.out.println("¡Necesitas una llave para abrir el cofre!");
                    }
                }
                // Verificar escaleras
                else if (current.hasStairs) {
                    if (level >= MAX_DUNGEONS) {
                        gameOverScreen.show();
                    } else {
                        regenerate(level + 1);
                    }
                    return; // skip one frame to avoid input repeat
                }
            }
        }

        // render
        batch.begin();
        renderer.render(batch, dungeon);
        
        // DEBUG: Dibujar colisiones (descomentar para visualizar)
        // renderCollisionDebug(batch);
        
        // Renderizar enemigos
        for (Orc enemy : enemies) {
            enemy.render(batch);
        }
        
        player.render(batch);
        
        // Renderizar UI (barra de vida) usando la cámara de UI
        batch.setProjectionMatrix(pauseMenu.getStage().getCamera().combined);
        healthBar.render(batch, player.health, 20);
        
        // Renderizar contadores de llaves y frascos
        renderInventoryUI(batch);
        
        batch.end();
        
        // Renderizar menú de pausa sobre el juego
        pauseMenu.render(delta);
        
        // Renderizar game over sobre todo
        gameOverScreen.render(delta);
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
        return checkEntityCollisionWithWalls(player);
    }
    
    // ----------------------------
    // Verifica si una entidad (jugador o enemigo) colisiona con paredes
    // ----------------------------
    private boolean checkEntityCollisionWithWalls(AnimatedEntity entity) {
        // entity.position es la esquina inferior izquierda del sprite
        // El hitbox será un rectángulo centrado en el sprite
        float entityCenterX = entity.position.x + entity.size.x * 0.5f;
        float entityCenterY = entity.position.y + entity.size.y * 0.5f;
        // Hitbox: 40% del tamaño de la entidad (20% de radio = 40% de ancho)
        float hitboxRadius = entity.size.x * 0.2f;
        float px1 = entityCenterX - hitboxRadius;
        float py1 = entityCenterY - hitboxRadius;
        float px2 = entityCenterX + hitboxRadius;
        float py2 = entityCenterY + hitboxRadius;
        
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
            
            // COLISIÓN CON COFRE (si existe en la habitación)
            if (room.hasChest) {
                float chestSize = tileW / 6f;
                float chestX = rx + tileW * 0.7f - chestSize * 0.5f;
                float chestY = ry + tileH * 0.7f - chestSize * 0.5f;
                
                // Hitbox del cofre (ligeramente más pequeño para mejor jugabilidad)
                float chestHitboxSize = chestSize * 0.7f;
                float chestCenterX = chestX + chestSize * 0.5f;
                float chestCenterY = chestY + chestSize * 0.5f;
                float cx1 = chestCenterX - chestHitboxSize * 0.5f;
                float cy1 = chestCenterY - chestHitboxSize * 0.5f;
                float cx2 = chestCenterX + chestHitboxSize * 0.5f;
                float cy2 = chestCenterY + chestHitboxSize * 0.5f;
                
                if (intersects(px1, py1, px2, py2, cx1, cy1, cx2, cy2)) {
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
                shapeRenderer.rect(rx, ry, tileW, wallThickness);
            } else {
                float centerX = rx + tileW * 0.5f;
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
            
            // Dibujar hitbox del cofre si existe
            if (room.hasChest) {
                shapeRenderer.setColor(Color.YELLOW);
                float chestSize = tileW / 6f;
                float chestX = rx + tileW * 0.7f - chestSize * 0.5f;
                float chestY = ry + tileH * 0.7f - chestSize * 0.5f;
                
                float chestHitboxSize = chestSize * 0.7f;
                float chestCenterX = chestX + chestSize * 0.5f;
                float chestCenterY = chestY + chestSize * 0.5f;
                float cx1 = chestCenterX - chestHitboxSize * 0.5f;
                float cy1 = chestCenterY - chestHitboxSize * 0.5f;
                
                shapeRenderer.rect(cx1, cy1, chestHitboxSize, chestHitboxSize);
                shapeRenderer.setColor(Color.RED); // restaurar color
            }
        }
        
        // Dibujar hitbox del jugador (centrado en el sprite)
        shapeRenderer.setColor(Color.GREEN);
        float playerCenterX = player.position.x + player.size.x * 0.5f;
        float playerCenterY = player.position.y + player.size.y * 0.5f;
        float hitboxRadius = player.size.x * 0.2f;
        shapeRenderer.rect(playerCenterX - hitboxRadius, playerCenterY - hitboxRadius, 
                          hitboxRadius * 2, hitboxRadius * 2);
        
        // Dibujar hitboxes de los enemigos
        shapeRenderer.setColor(Color.RED);
        for (Orc enemy : enemies) {
            float enemyCenterX = enemy.position.x + enemy.size.x * 0.5f;
            float enemyCenterY = enemy.position.y + enemy.size.y * 0.5f;
            shapeRenderer.rect(enemyCenterX - hitboxRadius, enemyCenterY - hitboxRadius, 
                              hitboxRadius * 2, hitboxRadius * 2);
        }
        
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
    
    // ----------------------------
    // Renderiza el inventario del jugador (llaves y frascos)
    // ----------------------------
    private void renderInventoryUI(SpriteBatch batch) {
        // Renderizar icono y contador de llaves
        com.badlogic.gdx.graphics.Texture keyTex = Resources.getTexture("key1", "graphics/sprites/objects");
        if (keyTex != null) {
            float iconSize = 40;
            float keyX = 20;
            float keyY = Gdx.graphics.getHeight() - 140;
            batch.setColor(Color.WHITE);
            batch.draw(keyTex, keyX, keyY, iconSize, iconSize);
            uiFont.draw(batch, "x" + player.getKeys(), keyX + iconSize + 10, keyY + iconSize * 0.7f);
        }
        
        // Renderizar icono y contador de frascos
        com.badlogic.gdx.graphics.Texture flaskTex = Resources.getTexture("flasks_1_1", "graphics/sprites/objects");
        if (flaskTex != null) {
            float iconSize = 40;
            float flaskX = 20;
            float flaskY = Gdx.graphics.getHeight() - 200;
            batch.setColor(Color.WHITE);
            batch.draw(flaskTex, flaskX, flaskY, iconSize, iconSize);
            uiFont.draw(batch, "x" + player.getFlasks() + " (Q)", flaskX + iconSize + 10, flaskY + iconSize * 0.7f);
        }
    }
    
    // ----------------------------
    // Genera enemigos aleatoriamente en las habitaciones (excepto la inicial)
    // ----------------------------
    private void generateEnemies() {
        enemies.clear();
        
        // Número de enemigos basado en el nivel
        int enemiesPerRoom = 1 + (level / 3); // más enemigos en niveles altos
        
        for (Room room : dungeon) {
            // No generar enemigos en la habitación inicial (0,0)
            if (room.x == 0 && room.y == 0) continue;
            
            // Probabilidad de que aparezcan enemigos (80%)
            if (Math.random() < 0.8) {
                int numEnemies = (int)(Math.random() * enemiesPerRoom) + 1;
                
                for (int i = 0; i < numEnemies; i++) {
                    // Posición en el centro de la habitación
                    float centerX = room.x * tileW + tileW * 0.5f;
                    float centerY = room.y * tileH + tileH * 0.5f;
                    
                    // Pequeña variación aleatoria desde el centro (±20% del tamaño)
                    float offsetX = (float)(Math.random() - 0.5) * tileW * 0.4f;
                    float offsetY = (float)(Math.random() - 0.5) * tileH * 0.4f;
                    
                    float x = centerX + offsetX;
                    float y = centerY + offsetY;
                    
                    Orc orc = new Orc(x, y);
                    orc.size.set(tileW / 7f, tileH / 7f);
                    enemies.add(orc);
                }
            }
        }
    }
    
    // ----------------------------
    // Actualiza la IA de todos los enemigos con colisiones
    // ----------------------------
    private void updateEnemies(float delta) {
        for (Orc enemy : enemies) {
            // Si el enemigo está muerto, solo actualizar su animación
            if (enemy.health.isDead()) {
                enemy.update(delta);
                continue;
            }
            
            // Guardar posición anterior
            float oldX = enemy.position.x;
            float oldY = enemy.position.y;
            
            // Actualizar IA y movimiento
            enemy.updateAI(delta, player.position);
            enemy.update(delta);
            
            // Si no hay movimiento, no verificar colisión
            if (enemy.movement.isZero()) {
                continue;
            }
            
            // Verificar colisión con paredes
            if (checkEntityCollisionWithWalls(enemy)) {
                // Colisión detectada, intentar movimiento en X solo
                enemy.position.set(oldX, oldY);
                enemy.position.x += enemy.movement.x * delta;
                
                if (checkEntityCollisionWithWalls(enemy)) {
                    // Aún hay colisión, intentar solo Y
                    enemy.position.x = oldX;
                    enemy.position.y = oldY + enemy.movement.y * delta;
                    
                    if (checkEntityCollisionWithWalls(enemy)) {
                        // Colisión en ambos ejes, restaurar posición original
                        enemy.position.set(oldX, oldY);
                    }
                }
            }
        }
    }
    
    // ----------------------------
    // Resuelve colisiones entre entidades (jugador y enemigos)
    // Empuja las entidades para evitar que se solapen
    // ----------------------------
    private void resolveEntityCollisions() {
        float collisionRadius = player.size.x * 0.3f; // Radio de colisión
        
        // Colisión entre jugador y enemigos
        for (Orc enemy : enemies) {
            if (enemy.health.isDead()) continue;
            
            float dx = player.position.x - enemy.position.x;
            float dy = player.position.y - enemy.position.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance < collisionRadius && distance > 0) {
                // Las entidades están solapadas, empujar
                float overlap = collisionRadius - distance;
                float pushX = (dx / distance) * overlap * 0.5f;
                float pushY = (dy / distance) * overlap * 0.5f;
                
                // Empujar ambas entidades en direcciones opuestas
                player.position.x += pushX;
                player.position.y += pushY;
                enemy.position.x -= pushX;
                enemy.position.y -= pushY;
            }
        }
        
        // Colisiones entre enemigos
        for (int i = 0; i < enemies.size(); i++) {
            Orc enemy1 = enemies.get(i);
            if (enemy1.health.isDead()) continue;
            
            for (int j = i + 1; j < enemies.size(); j++) {
                Orc enemy2 = enemies.get(j);
                if (enemy2.health.isDead()) continue;
                
                float dx = enemy1.position.x - enemy2.position.x;
                float dy = enemy1.position.y - enemy2.position.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distance < collisionRadius && distance > 0) {
                    // Empujar enemigos
                    float overlap = collisionRadius - distance;
                    float pushX = (dx / distance) * overlap * 0.5f;
                    float pushY = (dy / distance) * overlap * 0.5f;
                    
                    enemy1.position.x += pushX;
                    enemy1.position.y += pushY;
                    enemy2.position.x -= pushX;
                    enemy2.position.y -= pushY;
                }
            }
        }
    }
    
    // ----------------------------
    // Verifica combate entre jugador y enemigos
    // ----------------------------
    private void checkCombat() {
        // Verificar si el jugador golpea a enemigos
        for (Orc enemy : enemies) {
            if (enemy.health.isDead()) continue;
            
            if (player.canDamageEnemy(enemy.position)) {
                enemy.takeDamage(player.combat.getDamage());
            }
        }
        
        // Verificar si enemigos golpean al jugador
        for (Orc enemy : enemies) {
            if (enemy.health.isDead()) continue;
            
            if (enemy.canDamagePlayer(player.position)) {
                player.takeDamage(enemy.combat.getDamage());
            }
        }
    }

    @Override public void resize(int width, int height) { 
        viewport.update(width, height);
        pauseMenu.resize(width, height);
        gameOverScreen.resize(width, height);
    }
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
        if (pauseMenu != null) pauseMenu.dispose();
        if (gameOverScreen != null) gameOverScreen.dispose();
        if (uiFont != null) uiFont.dispose();
        if (viewport != null) viewport = null;
        if (camera != null) camera = null;
    }
}
