package com.alexdev.factory.input;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class MouseScrollHandler implements InputProcessor {

    private OrthographicCamera camera;

    public MouseScrollHandler(OrthographicCamera camera) {
        this.camera = camera;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // Zoomer la caméra avec la molette
        camera.zoom += amountY * 0.1f; // ajuster 0.1f pour vitesse zoom
        if(camera.zoom < 0.1f) camera.zoom = 0.1f; // limite zoom minimal
        if(camera.zoom > 5f) camera.zoom = 5f;     // limite zoom maximal
        camera.update();
        return true;
    }

    // Les autres méthodes doivent être implémentées mais peuvent rester vides
    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
}
