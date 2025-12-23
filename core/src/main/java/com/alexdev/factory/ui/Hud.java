package com.alexdev.factory.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Hud {

    public Stage stage;
    private Label zoomLabel;

    public Hud() {
        stage = new Stage(new ScreenViewport());

        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Barre du haut
        Table topBar = new Table();
//        topBar.setBackground(skin.newDrawable("white", Color.DARK_GRAY));

        zoomLabel = new Label("Zoom: 1.0", skin);

        topBar.add().expandX();
        topBar.add(zoomLabel).pad(10);

        root.top().add(topBar).expandX().fillX();
    }

    public void update(float zoom) {
        zoomLabel.setText(String.format("Zoom: %.2f", zoom));
    }

    public void draw() {
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
    }
}

