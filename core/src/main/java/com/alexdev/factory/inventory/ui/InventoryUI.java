package com.alexdev.factory.inventory.ui;

import com.alexdev.factory.inventory.Inventory;
import com.alexdev.factory.inventory.Item;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

public class InventoryUI extends Table {

    private Inventory inventory;
    private Skin skin;
    private Table slotsContainer;
    private Label titleLabel;
    private Label infoLabel;
    private DragAndDrop dragAndDrop;

    private static final int COLS = 6;
    private static final int SLOT_SIZE = 64;
    private static final Color BG_COLOR = new Color(0.15f, 0.15f, 0.2f, 0.95f);

    public InventoryUI(Inventory inventory, Skin skin) {
        this.inventory = inventory;
        this.skin = skin;
        this.dragAndDrop = new DragAndDrop();

        setupUI();
        populateSlots();
    }

    private void setupUI() {
        setFillParent(false);
        setBackground(skin.newDrawable("white", BG_COLOR));
        pad(20);

        // Header avec titre
        Table header = new Table();
        header.setBackground(skin.newDrawable("white", new Color(0.1f, 0.1f, 0.15f, 1)));

        titleLabel = new Label("INVENTAIRE", skin);
        titleLabel.setFontScale(1.5f);
        titleLabel.setColor(Color.GOLD);

        Label capacityLabel = new Label(inventory.getItemCount() + " / " + inventory.size(), skin);
        capacityLabel.setColor(Color.LIGHT_GRAY);

        header.add(titleLabel).expandX().left().padLeft(10);
        header.add(capacityLabel).right().padRight(10);

        add(header).fillX().height(50).colspan(COLS).row();
        add(new Container<>()).height(10).colspan(COLS).row(); // Spacer

        // Container pour les slots
        slotsContainer = new Table();
        add(slotsContainer).colspan(COLS).row();

        // Footer avec info
        add(new Container<>()).height(10).colspan(COLS).row();

        infoLabel = new Label("Clic gauche: utiliser | Clic droit: info", skin);
        infoLabel.setColor(Color.GRAY);
        infoLabel.setFontScale(0.8f);
        add(infoLabel).center().colspan(COLS).padBottom(5);

        pack();
        // Position sera définie après l'ajout au Stage
    }

    public void centerOnScreen() {
        if (getStage() != null) {
            setPosition(
                (getStage().getWidth() - getWidth()) / 2,
                (getStage().getHeight() - getHeight()) / 2
            );
        }
    }

    private void populateSlots() {
        slotsContainer.clear();

        for (int i = 0; i < inventory.size(); i++) {
            SlotWidget slot = new SlotWidget(skin, SLOT_SIZE);
            Item item = inventory.get(i);

            if (item != null) {
                slot.setItem(item);
                setupSlotInteraction(slot, i);
            }

            slotsContainer.add(slot).size(SLOT_SIZE, SLOT_SIZE).pad(4);

            if ((i + 1) % COLS == 0) {
                slotsContainer.row();
            }
        }
    }

    private void setupSlotInteraction(SlotWidget slot, int index) {
        // Drag and Drop
        dragAndDrop.addSource(new DragAndDrop.Source(slot) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                payload.setObject(index);

                // Visual feedback pendant le drag
                SlotWidget dragActor = new SlotWidget(skin, SLOT_SIZE);
                dragActor.setItem(inventory.get(index));
                dragActor.getColor().a = 0.8f;
                payload.setDragActor(dragActor);

                return payload;
            }
        });

        dragAndDrop.addTarget(new DragAndDrop.Target(slot) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload,
                                float x, float y, int pointer) {
                slot.setHighlighted(true);
                return true;
            }

            @Override
            public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload) {
                slot.setHighlighted(false);
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload,
                             float x, float y, int pointer) {
                int fromIndex = (Integer) payload.getObject();
                inventory.swap(fromIndex, index);
                refresh();
            }
        });

        // Click listeners
        slot.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Item item = inventory.get(index);
                if (item != null) {
                    if (getTapCount() == 2) {
                        // Double-clic: utiliser l'item
                        useItem(item, index);
                    } else {
                        // Simple clic: sélectionner
                        selectSlot(slot, item);
                    }
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Clic droit: afficher info
                if (button == 1) {
                    Item item = inventory.get(index);
                    if (item != null) {
                        showItemInfo(item);
                    }
                    return true;
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });
    }

    private void selectSlot(SlotWidget slot, Item item) {
        infoLabel.setText(item.getName() + " - " + item.getDescription());
        infoLabel.setColor(Color.WHITE);
    }

    private void useItem(Item item, int index) {
        System.out.println("Utilisation: " + item.getName());
        // Logique d'utilisation de l'item
        if (item.isConsumable()) {
            inventory.remove(index);
            refresh();
        }
    }

    private void showItemInfo(Item item) {
        // Créer une popup avec les détails de l'item
        Dialog dialog = new Dialog("Info: " + item.getName(), skin) {
            @Override
            protected void result(Object object) {
                remove();
            }
        };

        dialog.text(item.getDescription() + "\n\n" +
            "Type: " + item.getType() + "\n" +
            "Quantité: " + item.getQuantity());
        dialog.button("OK", true);
        dialog.show(getStage());
    }

    public void refresh() {
        populateSlots();

        // Mettre à jour le compteur
        Table header = (Table) getChildren().get(0);
        Label capacityLabel = (Label) ((Table) header).getChildren().get(1);
        capacityLabel.setText(inventory.getItemCount() + " / " + inventory.size());
    }

    public void toggle() {
        setVisible(!isVisible());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Ombre portée
        batch.setColor(0, 0, 0, 0.5f * parentAlpha);
        batch.draw(skin.getRegion("white"),
            getX() + 5, getY() - 5, getWidth(), getHeight());
        batch.setColor(Color.WHITE);

        super.draw(batch, parentAlpha);
    }
}
