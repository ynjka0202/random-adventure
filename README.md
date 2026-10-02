Random Adventure

Программчлалын дадлагын (F.CSM360) хүрээнд хийсэн JavaFX програм.

Програм өдөр бүр хэрэглэгчид 3 санамсаргүй сорил өгдөг. Сорилыг гүйцэтгэвэл XP оноо авч, түвшин ахина. Өдөр дараалан идэвхтэй байвал streak бонус авна, мөн амжилт (badge) нээгдэнэ.

Гүйцэтгэсэн: У.Янжинлхам (B242270131)

Ашигласан технологи
Java 17, Maven
JavaFX 21 (дэлгэцүүдийг Scene Builder дээр FXML-ээр хийсэн)
MySQL 8 (JDBC)
Боломжууд
Бүртгүүлэх, нэвтрэх
Өдрийн 3 сорил сугалах, солих (өдөрт 2 удаа)
Сорилыг гүйцэтгэсэн гэж тэмдэглэх, XP ба түвшин
Амжилтууд
Статистик (график)
Түүх
Өөрийн сорил нэмэх
Dark / Light горим
Ажиллуулах
MySQL Workbench дээр database/schema.sql файлыг ажиллуулна.
src/main/resources/db.properties.example файлыг хуулж db.properties гэж нэрлээд MySQL-ийн нууц үгээ бичнэ.
Ажиллуулна:
mvn javafx:run

Эсвэл IntelliJ / VS Code дээр Launcher.java-г ажиллуулна.

Бүтэц
src/main/java/mn/adventure/
  controller/   FXML дэлгэц бүрийн controller
  service/      сорил сугалах, XP тооцох логик
  dao/          MySQL-тэй ажиллах классууд
  model/        өгөгдлийн классууд
src/main/resources/
  fxml/         дэлгэцүүд
  css/          загвар
database/schema.sql

Диаграммууд болон дэлгэцийн зургууд docs/ хавтсанд байгаа.