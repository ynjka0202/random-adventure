# ⚄ Random Adventure Generator and Personal Progress Tracking System

Java 17 + Maven + JavaFX 21 + MySQL 8 ашигласан, өдөр бүр санамсаргүй сорил өгч XP, түвшин, streak, амжилтаар урамшуулдаг gamified ширээний програм.

## Боломжууд

- Бүртгүүлэх / нэвтрэх (нууц үг PBKDF2 hash)
- Өдөрт 3 санамсаргүй сорил (6 ангилал, өөр өөр ангиллаас), шоо эргэлдэх анимацтай
- Сорил солих (өдөрт 2 удаа), бүгдийг дуусгавал бонус сорил (×1.5 XP)
- XP, түвшин, streak бонус (+10%/өдөр, дээд тал +50%), 13 амжилт
- Нүүр (dashboard), Амжилт, Статистик (BarChart, PieChart), Түүх (шүүлттэй хүснэгт)
- Өөрийн сорил нэмэх / устгах
- Dark / Light горим (хэрэглэгч бүрээр хадгална)

## Хавтасны бүтэц

```
random-adventure/
├── pom.xml
├── database/schema.sql            ← MySQL Workbench дээр ажиллуулна
├── src/main/java/mn/adventure/
│   ├── App.java, Launcher.java    ← эхлэх классууд
│   ├── db/Database.java           ← JDBC холболт (Singleton)
│   ├── model/                     ← User, Challenge, Category, DailyTask, Achievement, enum-ууд
│   ├── dao/                       ← SQL асуулгууд (UserDao, ChallengeDao, TaskDao …)
│   ├── service/                   ← AuthService, ChallengeGenerator, ProgressService
│   ├── util/                      ← LevelSystem, PasswordUtil, Session
│   ├── controller/                ← FXML бүрийн Controller (AuthController, MainController, TodayController …)
│   └── ui/                        ← Theme (Dark/Light), UiKit (жижиг UI бүрэлдэхүүн)
├── src/main/resources/
│   ├── fxml/                      ← Scene Builder-ээр нээх 8 дэлгэц (auth, main, dashboard, today …)
│   ├── db.properties              ← MySQL нууц үгээ ЭНД бичнэ
│   └── css/ base.css, dark.css, light.css
├── src/test/java/…/GameLogicTest.java
└── docs/
    └── diagrams/  *.drawio + *.png    ← ER, класс, дэлгэцийн зохиомж
```

---

## 1. MySQL суулгах

MySQL Server (өгөгдлийн сан)-ийг суулгаагүй бол зөвхөн Workbench суулгаад өгөгдлийн сан үүсгэж чадахгүй. Тиймээс хоёуланг нь хамт суулгана.

1. <https://www.mysql.com/downloads/> хаягаар орно.
2. **MySQL Community (GPL) Downloads** → **MySQL Installer for Windows** сонгоно.
3. Том хэмжээтэй (`mysql-installer-community-…msi`) файлыг татна. "No thanks, just start my download" дээр дарж болно.
4. Installer дээр **Custom** (эсвэл *Developer Default*) сонгоод дараах хоёрыг заавал нэмнэ:
   - **MySQL Server 8.x**
   - **MySQL Workbench 8.x**
5. *Accounts and Roles* алхамд **root нууц үг** өгнө. Жишээ нь `root`. Энэ нууц үгээ мартаж болохгүй.
6. *Windows Service* алхамд "Start the MySQL Server at System Startup"-ийг идэвхтэй үлдээнэ.
7. Суулгалт дуусна.

## 2. Өгөгдлийн сан үүсгэх

1. **MySQL Workbench** нээгээд `Local instance MySQL80` дээр дарж root нууц үгээрээ холбогдоно.
2. **File → Open SQL Script…** цэснээс `database/schema.sql` файлыг сонгоно.
3. ⚡ (**Execute**) товч дарна.
4. Зүүн талын *Schemas* хэсэгт ↻ дарахад `adventure_db` болон 6 хүснэгт харагдах ёстой.

## 3. Тохиргоо

`src/main/resources/db.properties` файлд root нууц үгээ бичнэ:

```properties
db.url=jdbc:mysql://localhost:3306/adventure_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ulaanbaatar
db.user=root
db.password=ТАНЫ_НУУЦ_ҮГ
```

## 4. Ажиллуулах

JDK 17 буюу түүнээс шинэ хувилбар хэрэгтэй.

### IntelliJ IDEA (санал болгож байна)

1. **File → Open** цэснээс `random-adventure` хавтас (pom.xml байгаа хавтас)-ыг нээнэ. Maven хамаарлууд автоматаар татагдана (эхний удаа 1–3 минут).
2. *Project Structure → SDK* хэсэгт JDK 17 эсвэл 21 сонгоно.
3. Хоёр аргын аль нэгээр ажиллуулна:
   - `src/main/java/mn/adventure/Launcher.java` файлыг нээгээд ▶ **Run** дарна.
   - Эсвэл баруун талын **Maven** цонхноос **Plugins → javafx → javafx:run**-ийг ажиллуулна.

> `App.java`-г шууд ажиллуулбал "JavaFX runtime components are missing" алдаа гарч болно. Тиймээс **Launcher.java**-г ажиллуулаарай.

### Командын мөрөөс

```bash
mvn javafx:run          # ажиллуулах
mvn test                # unit тест (6 тест)
mvn package             # target/random-adventure-1.0.0-all.jar үүснэ
java -jar target/random-adventure-1.0.0-all.jar
```

## Scene Builder-ээр дэлгэц засах

1. [Scene Builder](https://gluonhq.com/products/scene-builder/)-ийг суулгана (IntelliJ: *Settings → Languages & Frameworks → JavaFX* хэсэгт замыг нь заана).
2. `src/main/resources/fxml/` доторх `.fxml` файл дээр баруун товч → **Open In SceneBuilder**.
3. Өнгө зөв харагдуулахын тулд Scene Builder-ийн **Preview → Scene Style Sheets → Add a Style Sheet** цэснээс `css/dark.css`, дараа нь `css/base.css`-ийг нэмнэ. Өнгөний хувьсагчид dark.css-д байдаг тул эхлээд түүнийг нэмэх хэрэгтэй.
4. `fx:id` нь Controller доторх `@FXML` талбартай, `On Action` (`#roll` гэх мэт) нь Controller-ийн методтой нэрээрээ холбогдоно. Нэрийг өөрчилбөл Controller-т мөн өөрчилнө.

Сорилын карт, амжилтын badge, графикийн өгөгдөл зэрэг тоо нь өөрчлөгддөг хэсгүүдийг Controller кодоор үүсгэдэг тул Scene Builder дээр хоосон контейнер (TilePane, FlowPane, VBox) хэлбэрээр харагдана.

## Түгээмэл алдаа

| Алдаа | Шийдэл |
|---|---|
| "Өгөгдлийн сантай холбогдож чадсангүй" | Windows *Services* цонхонд **MySQL80** ажиллаж байгаа эсэх, `db.properties`-ийн нууц үгийг шалгана |
| `Unknown database 'adventure_db'` | `schema.sql`-ийг Workbench дээр ажиллуулна |
| `Access denied for user 'root'` | `db.password` буруу байна |
| Монгол үсэг `????` болж харагдана | `schema.sql`-ийг дахин ажиллуулна (дотор нь `SET NAMES utf8mb4` бий) |
| JavaFX runtime components are missing | `Launcher.java`-г ажиллуулах эсвэл `mvn javafx:run` |

## Диаграммууд засах

`docs/diagrams/*.drawio` файлуудыг <https://app.diagrams.net> дээр нээнэ (**File → Open from → Device**). Засаад **File → Export as → PNG** хийж тайланд оруулна.
