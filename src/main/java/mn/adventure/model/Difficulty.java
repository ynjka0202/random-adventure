package mn.adventure.model;

/** Сорилын хүндрэлийн түвшин ба үндсэн XP шагнал. */
public enum Difficulty {
    EASY("Хялбар", 1, 10),
    MEDIUM("Дунд", 2, 25),
    HARD("Хэцүү", 3, 50);

    private final String label;
    private final int stars;
    private final int baseXp;

    Difficulty(String label, int stars, int baseXp) {
        this.label = label;
        this.stars = stars;
        this.baseXp = baseXp;
    }

    public String getLabel() { return label; }
    public int getStars() { return stars; }
    public int getBaseXp() { return baseXp; }

    /** ★★☆ хэлбэрээр харуулна. */
    public String starText() {
        return "★".repeat(stars) + "☆".repeat(3 - stars);
    }

    @Override public String toString() { return label; }
}
