package com.alexdev.factory.inventory.ui;

import com.alexdev.factory.inventory.Item;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class SlotWidget extends Widget {

    private Skin skin;
    private Item item;
    private boolean highlighted;
    private boolean hovered;

    private int size;
    private Drawable slotBackground;
    private Drawable slotHighlight;
    private Drawable slotHover;
    private BitmapFont font;

    private static final Color EMPTY_COLOR = new Color(0.2f, 0.2f, 0.25f, 1);
    private static final Color FILLED_COLOR = new Color(0.25f, 0.25f, 0.3f, 1);
    private static final Color HIGHLIGHT_COLOR = new Color(0.8f, 0.6f, 0.2f, 0.6f);
    private static final Color HOVER_COLOR = new Color(1f, 1f, 1f, 0.2f);
    private static final Color BORDER_COLOR = new Color(0.4f, 0.4f, 0.5f, 1);

    public SlotWidget(Skin skin, int size) {
        this.skin = skin;
        this.size = size;

        // Essayer de récupérer la font, sinon utiliser celle par défaut
        try {
            this.font = skin.getFont("default-font");
        } catch (Exception e) {
            // Utiliser la première font disponible
            this.font = skin.get(BitmapFont.class);
        }

        // Créer les drawables pour le fond
        slotBackground = skin.newDrawable("white", EMPTY_COLOR);
        slotHighlight = skin.newDrawable("white", HIGHLIGHT_COLOR);
        slotHover = skin.newDrawable("white", HOVER_COLOR);

        setSize(size, size);

        // Détection du survol
        addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                              float x, float y, int pointer,
                              com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                hovered = true;
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                             float x, float y, int pointer,
                             com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                hovered = false;
            }
        });
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        // Fond du slot
        Color bgColor = item != null ? FILLED_COLOR : EMPTY_COLOR;
        batch.setColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a * parentAlpha);
        slotBackground.draw(batch, x, y, width, height);

        // Bordure
        batch.setColor(BORDER_COLOR.r, BORDER_COLOR.g, BORDER_COLOR.b,
            BORDER_COLOR.a * parentAlpha);
        drawBorder(batch, x, y, width, height, 2);

        // Highlight si drag en cours
        if (highlighted) {
            batch.setColor(HIGHLIGHT_COLOR.r, HIGHLIGHT_COLOR.g, HIGHLIGHT_COLOR.b,
                HIGHLIGHT_COLOR.a * parentAlpha);
            slotHighlight.draw(batch, x, y, width, height);
        }

        // Hover effect
        if (hovered && item != null) {
            batch.setColor(HOVER_COLOR.r, HOVER_COLOR.g, HOVER_COLOR.b,
                HOVER_COLOR.a * parentAlpha);
            slotHover.draw(batch, x, y, width, height);
        }

        // Dessiner l'item s'il existe
        if (item != null) {
            drawItem(batch, x, y, width, height, parentAlpha);
        }

        batch.setColor(Color.WHITE);
    }

    private void drawItem(Batch batch, float x, float y, float width, float height,
                          float parentAlpha) {
        // Icône de l'item (placeholder coloré selon le type)
        Color itemColor = getItemColor(item.getType());
        batch.setColor(itemColor.r, itemColor.g, itemColor.b, itemColor.a * parentAlpha);

        float iconSize = width * 0.6f;
        float iconX = x + (width - iconSize) / 2;
        float iconY = y + (height - iconSize) / 2 + height * 0.1f;

        // Dessiner un cercle/carré selon le type
        if (item.getType().equals("consumable")) {
            drawCircle(batch, iconX + iconSize / 2, iconY + iconSize / 2, iconSize / 2);
        } else {
            batch.draw(skin.getRegion("white"), iconX, iconY, iconSize, iconSize);
        }

        // Quantité en bas à droite
        if (item.getQuantity() > 1) {
            batch.setColor(1, 1, 1, parentAlpha);
            String quantityText = String.valueOf(item.getQuantity());
            font.setColor(Color.WHITE);
            font.getData().setScale(0.8f);
            font.draw(batch, quantityText,
                x + width - font.getSpaceXadvance() * quantityText.length() - 4,
                y + 16);
            font.getData().setScale(1f);
        }

        // Nom de l'item (première lettre)
        batch.setColor(1, 1, 1, parentAlpha * 0.9f);
        font.setColor(Color.WHITE);
        String initial = item.getName().substring(0, 1).toUpperCase();
        float textWidth = font.getSpaceXadvance() * initial.length();
        font.draw(batch, initial,
            x + (width - textWidth) / 2,
            y + height / 2 + font.getCapHeight() / 2);
    }

    private void drawBorder(Batch batch, float x, float y, float width, float height,
                            float thickness) {
        Drawable border = skin.getDrawable("white");
        // Haut
        border.draw(batch, x, y + height - thickness, width, thickness);
        // Bas
        border.draw(batch, x, y, width, thickness);
        // Gauche
        border.draw(batch, x, y, thickness, height);
        // Droite
        border.draw(batch, x + width - thickness, y, thickness, height);
    }

    private void drawCircle(Batch batch, float centerX, float centerY, float radius) {
        // Approximation d'un cercle avec des carrés (simple pour libGDX sans ShapeRenderer)
        int segments = 20;
        float segmentSize = radius * 0.3f;

        for (int i = 0; i < segments; i++) {
            float angle = (float) (i * Math.PI * 2 / segments);
            float x = centerX + (float) Math.cos(angle) * radius - segmentSize / 2;
            float y = centerY + (float) Math.sin(angle) * radius - segmentSize / 2;
            batch.draw(skin.getRegion("white"), x, y, segmentSize, segmentSize);
        }
    }

    private Color getItemColor(String type) {
        switch (type.toLowerCase()) {
            case "weapon":      return new Color(0.9f, 0.3f, 0.3f, 1);  // Rouge
            case "armor":       return new Color(0.4f, 0.6f, 0.9f, 1);  // Bleu
            case "consumable":  return new Color(0.3f, 0.9f, 0.4f, 1);  // Vert
            case "material":    return new Color(0.7f, 0.7f, 0.7f, 1);  // Gris
            case "quest":       return new Color(0.9f, 0.7f, 0.2f, 1);  // Or
            case "rare":        return new Color(0.8f, 0.3f, 0.9f, 1);  // Violet
            default:            return new Color(0.6f, 0.6f, 0.6f, 1);  // Gris par défaut
        }
    }

    @Override
    public float getPrefWidth() {
        return size;
    }

    @Override
    public float getPrefHeight() {
        return size;
    }
}
