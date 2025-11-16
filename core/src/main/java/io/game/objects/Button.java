package io.game.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Button {

    private Texture normal;
    private Texture hover;
    private Texture click;

    private Sprite sprite;
    private Rectangle rect;

    // Nuevo constructor con ruta base
    public Button(String name, float x, float y, float w, float h, String basePath) {
        normal = new Texture(Gdx.files.internal(basePath + name + "_normal.png"));
        hover  = new Texture(Gdx.files.internal(basePath + name + "_hover.png"));
        click  = new Texture(Gdx.files.internal(basePath + name + "_click.png"));

        sprite = new Sprite(normal);
        sprite.setSize(w, h);
        sprite.setPosition(x, y);

        rect = new Rectangle(x, y, w, h);
    }

    public void update(float mx, float my) {
        if (rect.contains(mx, my)) {
            sprite.setTexture(hover);
        } else {
            sprite.setTexture(normal);
        }
    }

    public void click() {
        sprite.setTexture(click);
    }

    public boolean isHover(float mx, float my) {
        return rect.contains(mx, my);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public void dispose() {
        normal.dispose();
        hover.dispose();
        click.dispose();
    }
}
