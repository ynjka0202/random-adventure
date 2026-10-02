package mn.adventure.model;

/** Өдрийн даалгаврын төлөв. */
public enum TaskStatus {
    PENDING("Хүлээгдэж буй"),
    COMPLETED("Гүйцэтгэсэн"),
    SKIPPED("Солигдсон");

    private final String label;
    TaskStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}
