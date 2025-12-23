package com.alexdev.factory.inventory;

public class Item {
    private final String id;
    private final String name;
    private final String description;
    private final String type;
    private int quantity;
    private final boolean consumable;
    private final int maxStack;

    // Constructeur simple (pour compatibilité avec ton code existant)
    public Item(String id, String name) {
        this(id, name, "Un objet mystérieux", "material", 1, false, 99);
    }

    // Constructeur complet
    public Item(String id, String name, String description, String type,
                int quantity, boolean consumable, int maxStack) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.quantity = quantity;
        this.consumable = consumable;
        this.maxStack = maxStack;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isConsumable() {
        return consumable;
    }

    public int getMaxStack() {
        return maxStack;
    }

    // Méthodes utiles
    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, Math.min(quantity, maxStack));
    }

    public void addQuantity(int amount) {
        setQuantity(this.quantity + amount);
    }

    public void removeQuantity(int amount) {
        setQuantity(this.quantity - amount);
    }

    public boolean canStack() {
        return quantity < maxStack;
    }

    public boolean isEmpty() {
        return quantity <= 0;
    }

    // Créer une copie de l'item
    public Item copy() {
        return new Item(id, name, description, type, quantity, consumable, maxStack);
    }

    // Méthode pour fusionner deux stacks du même item
    public int mergeWith(Item other) {
        if (!this.id.equals(other.id)) return 0;

        int space = maxStack - this.quantity;
        int toTransfer = Math.min(space, other.quantity);

        this.addQuantity(toTransfer);
        other.removeQuantity(toTransfer);

        return toTransfer;
    }

    @Override
    public String toString() {
        return name + (quantity > 1 ? " x" + quantity : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Item)) return false;
        Item other = (Item) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    // Factory methods pour créer facilement des items
    public static class Builder {
        private String id;
        private String name;
        private String description = "Aucune description";
        private String type = "material";
        private int quantity = 1;
        private boolean consumable = false;
        private int maxStack = 99;

        public Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder consumable(boolean consumable) {
            this.consumable = consumable;
            return this;
        }

        public Builder maxStack(int maxStack) {
            this.maxStack = maxStack;
            return this;
        }

        public Item build() {
            return new Item(id, name, description, type, quantity, consumable, maxStack);
        }
    }

    // Exemples d'items prédéfinis
    public static Item createPotion() {
        return new Builder("potion_health", "Potion de Vie")
            .description("Restaure 50 PV")
            .type("consumable")
            .consumable(true)
            .maxStack(20)
            .build();
    }

    public static Item createSword() {
        return new Builder("sword_iron", "Épée en Fer")
            .description("Une épée robuste (+10 ATK)")
            .type("weapon")
            .maxStack(1)
            .build();
    }

    public static Item createWood() {
        return new Builder("wood_oak", "Bois de Chêne")
            .description("Matériau de construction basique")
            .type("material")
            .maxStack(99)
            .build();
    }

    public static Item createGold() {
        return new Builder("gold_coin", "Pièce d'Or")
            .description("Monnaie du royaume")
            .type("currency")
            .maxStack(999)
            .build();
    }

    public static Item createArmor() {
        return new Builder("armor_leather", "Armure en Cuir")
            .description("Protection légère (+5 DEF)")
            .type("armor")
            .maxStack(1)
            .build();
    }

    public static Item createQuestItem() {
        return new Builder("quest_key", "Clé Ancienne")
            .description("Ouvre la porte du donjon")
            .type("quest")
            .maxStack(1)
            .build();
    }

    public static Item createRareGem() {
        return new Builder("gem_ruby", "Rubis Étincelant")
            .description("Pierre précieuse rare et puissante")
            .type("rare")
            .maxStack(10)
            .build();
    }
}
