package mn.adventure;

/**
 * IntelliJ-ээс шууд "Run" дарах болон fat jar-д зориулсан эхлэлийн класс.
 * (Application-ээс удамшаагүй класс main-ийг дуудах нь
 *  "JavaFX runtime components are missing" алдаанаас сэргийлнэ.)
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
