-- ============================================================
-- Random Adventure Generator and Personal Progress Tracking System
-- MySQL 8 схем. MySQL Workbench дээр бүтнээр нь нээгээд ⚡ (Execute) дарна.
-- ============================================================

SET NAMES utf8mb4;
DROP DATABASE IF EXISTS adventure_db;
CREATE DATABASE adventure_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE adventure_db;
SET NAMES utf8mb4;

-- ---------- 1. Хэрэглэгч ----------
CREATE TABLE users (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  username         VARCHAR(50)  NOT NULL UNIQUE,
  full_name        VARCHAR(100) NOT NULL,
  email            VARCHAR(120) NOT NULL UNIQUE,
  password_hash    VARCHAR(255) NOT NULL,
  total_xp         INT          NOT NULL DEFAULT 0,
  level            INT          NOT NULL DEFAULT 1,
  current_streak   INT          NOT NULL DEFAULT 0,
  best_streak      INT          NOT NULL DEFAULT 0,
  last_active_date DATE         NULL,
  theme            VARCHAR(10)  NOT NULL DEFAULT 'dark',
  created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------- 2. Сорилын ангилал ----------
CREATE TABLE categories (
  id     INT AUTO_INCREMENT PRIMARY KEY,
  code   VARCHAR(20) NOT NULL UNIQUE,
  name   VARCHAR(50) NOT NULL,
  icon   VARCHAR(8)  NOT NULL,
  color  CHAR(7)     NOT NULL
) ENGINE=InnoDB;

-- ---------- 3. Сорил (даалгаврын сан) ----------
CREATE TABLE challenges (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  title       VARCHAR(150) NOT NULL,
  description VARCHAR(500) NULL,
  category_id INT          NOT NULL,
  difficulty  ENUM('EASY','MEDIUM','HARD') NOT NULL DEFAULT 'EASY',
  xp_reward   INT          NOT NULL,
  created_by  INT          NULL,          -- NULL = системийн сорил, бусад = хэрэглэгчийн өөрийн сорил
  is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ch_cat  FOREIGN KEY (category_id) REFERENCES categories(id),
  CONSTRAINT fk_ch_user FOREIGN KEY (created_by)  REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT chk_xp CHECK (xp_reward > 0)
) ENGINE=InnoDB;

-- ---------- 4. Өдөр бүрийн даалгавар (хэрэглэгчид оноосон сорил) ----------
CREATE TABLE daily_tasks (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  user_id       INT  NOT NULL,
  challenge_id  INT  NOT NULL,
  assigned_date DATE NOT NULL,
  status        ENUM('PENDING','COMPLETED','SKIPPED') NOT NULL DEFAULT 'PENDING',
  is_bonus      BOOLEAN  NOT NULL DEFAULT FALSE,
  completed_at  DATETIME NULL,
  xp_earned     INT      NOT NULL DEFAULT 0,
  CONSTRAINT fk_dt_user FOREIGN KEY (user_id)      REFERENCES users(id)      ON DELETE CASCADE,
  CONSTRAINT fk_dt_ch   FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE,
  CONSTRAINT uq_dt UNIQUE (user_id, challenge_id, assigned_date),
  INDEX idx_dt_user_date (user_id, assigned_date)
) ENGINE=InnoDB;

-- ---------- 5. Амжилт (badge) ----------
CREATE TABLE achievements (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  code           VARCHAR(30)  NOT NULL UNIQUE,
  title          VARCHAR(80)  NOT NULL,
  description    VARCHAR(200) NOT NULL,
  icon           VARCHAR(8)   NOT NULL,
  condition_type ENUM('TASKS_COMPLETED','STREAK','LEVEL','TOTAL_XP','HARD_COMPLETED','CATEGORIES_TRIED') NOT NULL,
  threshold      INT NOT NULL,
  xp_bonus       INT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

CREATE TABLE user_achievements (
  user_id        INT NOT NULL,
  achievement_id INT NOT NULL,
  unlocked_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, achievement_id),
  CONSTRAINT fk_ua_user FOREIGN KEY (user_id)        REFERENCES users(id)        ON DELETE CASCADE,
  CONSTRAINT fk_ua_ach  FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Анхны өгөгдөл
-- ============================================================
INSERT INTO categories (code, name, icon, color) VALUES
 ('FITNESS',  'Биеийн тамир',  '⚡', '#ef4444'),
 ('MIND',     'Оюун ухаан',    '✎', '#6366f1'),
 ('CREATIVE', 'Бүтээлч',       '✦', '#ec4899'),
 ('SOCIAL',   'Нийгэм',        '☺', '#f59e0b'),
 ('HEALTH',   'Эрүүл мэнд',    '♥', '#10b981'),
 ('EXPLORE',  'Адал явдал',    '⚑', '#0ea5e9');

-- XP: EASY = 10, MEDIUM = 25, HARD = 50
INSERT INTO challenges (title, description, category_id, difficulty, xp_reward) VALUES
 ('20 суналт хий',                   'Өглөө эсвэл орой 20 удаа суниаарай.',                          1,'EASY',10),
 ('10 минут сунгалтын дасгал',       'Бүх биеэ сунгах энгийн дасгал хий.',                             1,'EASY',10),
 ('5000 алхам алх',                  'Утасныхаа алхам тоолуураар шалгаарай.',                           1,'MEDIUM',25),
 ('30 секунд планк × 3',             'Гурван удаа 30 секунд планк барь.',                              1,'MEDIUM',25),
 ('Шатаар 5 давхар өгс',             'Цахилгаан шат бүү ашигла.',                                      1,'EASY',10),
 ('3 км гүй',                        'Хурдаа биш, дуусгахаа зорь.',                                   1,'HARD',50),
 ('50 squat хий',                    'Хэд хэдэн сетэд хувааж болно.',                                 1,'MEDIUM',25),
 ('1 цаг дугуй унах эсвэл сэлэх',    'Идэвхтэй кардио хөдөлгөөн.',                                    1,'HARD',50),

 ('Номын 10 хуудас унш',             'Сонирхсон дурын номоо уншаарай.',                               2,'EASY',10),
 ('Шинэ 5 англи үг цээжил',          'Өгүүлбэрт хэрэглэж бичээрэй.',                                   2,'EASY',10),
 ('Судоку эсвэл оньсого бод',        'Нэг бүтэн судоку дуусга.',                                       2,'MEDIUM',25),
 ('Онлайн хичээлээс 1 бүлэг үз',     'Coursera, YouTube гэх мэт.',                                     2,'MEDIUM',25),
 ('Шинэ сэдвээр 1 нийтлэл унш',      'Өмнө нь огт мэдэхгүй сэдэв сонго.',                              2,'EASY',10),
 ('Жижиг программ бич',              'Өөрт хэрэгтэй жижиг script эсвэл тоглоом.',                      2,'HARD',50),
 ('Баримтат кино үз',                'Үзээд 3 шинэ зүйл тэмдэглэ.',                                   2,'MEDIUM',25),
 ('1 цаг шинэ ур чадвар сур',        'Гитар, хэл, код — юу ч болно.',                                 2,'HARD',50),

 ('Юу ч хамаагүй зур',               '10 минут зураг зур.',                                            3,'EASY',10),
 ('Шүлэг эсвэл богино өгүүллэг бич', 'Хамгийн багадаа 8 мөр.',                                         3,'MEDIUM',25),
 ('Гэрэл зургийн сорил',             'Нэг өнгөтэй 5 зүйлийн зураг ав.',                               3,'EASY',10),
 ('Шинэ хоол хий',                   'Өмнө нь хийж байгаагүй жор сонго.',                             3,'MEDIUM',25),
 ('Өдрийн тэмдэглэл бич',            'Өнөөдрийг 1 хуудсанд бич.',                                      3,'EASY',10),
 ('Гар урлалаар юм хий',             'Цаас, мод, даавуу — дуртай материал.',                          3,'HARD',50),
 ('1 минутын видео бүтээ',           'Өдрөө эсвэл сонирхлоо харуул.',                                 3,'MEDIUM',25),
 ('Дуу зохио эсвэл аялгуу тогло',    'Хөгжмийн зэмсэг эсвэл апп ашигла.',                              3,'HARD',50),

 ('Удаан ярилцаагүй найздаа залга',  'Сайн байгаа эсэхийг нь асуу.',                                   4,'EASY',10),
 ('Хэн нэгэнд талархлаа илэрхийл',   'Чин сэтгэлийн талархал бичих эсвэл хэлэх.',                     4,'EASY',10),
 ('Гэр бүлдээ хоол хийж өг',         'Хамтдаа хооллоорой.',                                            4,'MEDIUM',25),
 ('Сайн дурын ажилд оролц',          'Хүмүүст тус болох ямар нэг үйл хий.',                          4,'HARD',50),
 ('Шинэ хүнтэй танилц',              'Ангийн эсвэл ажлын шинэ хүнтэй ярилц.',                         4,'MEDIUM',25),
 ('Хэн нэгэнд тусламж үзүүл',        'Жижиг ч гэсэн тус бол.',                                         4,'EASY',10),
 ('Найзуудтайгаа тоглоом тогло',     'Ширээний эсвэл гадаа тоглоом.',                                  4,'MEDIUM',25),
 ('Уулзалт зохион байгуул',          'Найзуудаа нэг дор цуглуул.',                                     4,'HARD',50),

 ('8 аяга ус уу',                    'Өдөржин тэнцүү хувааж уугаарай.',                                5,'EASY',10),
 ('Чихэрлэг зүйлгүй өдөр',           'Өнөөдөр чихэр, амтат ундаа хэрэглэхгүй.',                       5,'MEDIUM',25),
 ('23:00-аас өмнө унт',              'Утсаа орондоо авч орохгүй.',                                    5,'MEDIUM',25),
 ('10 минут бясалга',                'Амьсгалдаа анхаарлаа төвлөрүүл.',                               5,'EASY',10),
 ('Жимс, ногоо 5 удаа ид',           'Өдрийн хоолондоо нэмээрэй.',                                    5,'MEDIUM',25),
 ('Сошиал медиагүй 1 өдөр',          'Бүтэн өдөр сошиал сүлжээ ашиглахгүй.',                         5,'HARD',50),
 ('Өглөө 6:30-д бос',                'Сэрүүлгээ хойшлуулахгүй.',                                       5,'HARD',50),
 ('Өрөөгөө цэвэрлэ',                 '15 минутад эмх цэгцтэй болго.',                                  5,'EASY',10),

 ('Очиж байгаагүй газраар алх',      'Хотынхоо шинэ гудамж, парк.',                                   6,'EASY',10),
 ('Шинэ кафе, ресторан туршаад үз',  'Өмнө нь амсаж байгаагүй хоол захиал.',                          6,'MEDIUM',25),
 ('Музей эсвэл үзэсгэлэн үз',        'Сонирхолтой 3 зүйл тэмдэглэ.',                                  6,'MEDIUM',25),
 ('Хотоос гадагш аялал хий',         'Байгальд нэг өдрийг өнгөрөө.',                                   6,'HARD',50),
 ('Нар мандахыг харж ав',            'Өндөр газар очвол бүр гоё.',                                    6,'HARD',50),
 ('Шинэ маршрутаар гэртээ хүр',      'Автобус эсвэл явганаар өөр замаар.',                           6,'EASY',10),
 ('Уул руу гар',                     'Ойролцоох ууланд авир.',                                        6,'HARD',50),
 ('Номын сан руу оч',                'Сонирхолтой ном олж уншаарай.',                                 6,'EASY',10);

INSERT INTO achievements (code, title, description, icon, condition_type, threshold, xp_bonus) VALUES
 ('FIRST_STEP',  'Анхны алхам',      'Анхны сорилоо гүйцэтгэ',                 '★', 'TASKS_COMPLETED', 1,   10),
 ('TASKS_10',    'Идэвхтэн',         '10 сорил гүйцэтгэ',                       '✪', 'TASKS_COMPLETED', 10,  30),
 ('TASKS_50',    'Шаргуу',           '50 сорил гүйцэтгэ',                       '♛', 'TASKS_COMPLETED', 50,  100),
 ('TASKS_100',   'Домогт баатар',    '100 сорил гүйцэтгэ',                      '♚', 'TASKS_COMPLETED', 100, 200),
 ('STREAK_3',    'Гал асаав',        '3 өдөр дараалан сорил гүйцэтгэ',           '♨', 'STREAK',          3,   20),
 ('STREAK_7',    'Долоо хоногийн од','7 өдөр дараалан сорил гүйцэтгэ',           '✹', 'STREAK',          7,   50),
 ('STREAK_30',   'Төмөр хүсэл',      '30 өдөр дараалан сорил гүйцэтгэ',          '✺', 'STREAK',          30,  200),
 ('LEVEL_5',     'Судлаач',          '5-р түвшинд хүр',                          '▲', 'LEVEL',           5,   50),
 ('LEVEL_10',    'Баатар',           '10-р түвшинд хүр',                         '♜', 'LEVEL',           10,  100),
 ('XP_1000',     'Мянгат',           'Нийт 1000 XP цуглуул',                     '◆', 'TOTAL_XP',        1000, 50),
 ('HARD_1',      'Зоригтон',         'Анхны хэцүү сорилоо гүйцэтгэ',             '⚔', 'HARD_COMPLETED',  1,   20),
 ('HARD_10',     'Эрэлхэг',          '10 хэцүү сорил гүйцэтгэ',                  '✚', 'HARD_COMPLETED',  10,  80),
 ('ALL_CATS',    'Бүх талт',         '6 ангилал бүрээс сорил гүйцэтгэ',          '✿', 'CATEGORIES_TRIED',6,   60);
