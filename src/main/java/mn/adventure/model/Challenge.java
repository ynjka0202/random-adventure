package mn.adventure.model;

/** Сорилын сан дахь нэг сорил. createdBy == null бол системийн сорил. */
public class Challenge {
    private int id;
    private String title;
    private String description;
    private Category category;
    private Difficulty difficulty;
    private int xpReward;
    private Integer createdBy;

    public Challenge() { }

    public Challenge(int id, String title, String description, Category category,
                     Difficulty difficulty, int xpReward, Integer createdBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.xpReward = xpReward;
        this.createdBy = createdBy;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }
    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    public boolean isCustom() { return createdBy != null; }
}
