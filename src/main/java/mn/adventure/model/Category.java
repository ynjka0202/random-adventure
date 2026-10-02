package mn.adventure.model;

/** Сорилын ангилал (Биеийн тамир, Оюун ухаан ...). */
public class Category {
    private final int id;
    private final String code;
    private final String name;
    private final String icon;
    private final String color;

    public Category(int id, String code, String name, String icon, String color) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public String getColor() { return color; }

    @Override public String toString() { return icon + "  " + name; }
    @Override public boolean equals(Object o) { return o instanceof Category c && c.id == id; }
    @Override public int hashCode() { return Integer.hashCode(id); }
}
