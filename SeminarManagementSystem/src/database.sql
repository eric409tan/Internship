PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL, 
    name TEXT NOT NULL,
    role TEXT CHECK(role IN ('Student', 'Evaluator', 'Coordinator', 'Admin')) NOT NULL
);
INSERT INTO users VALUES(1,'coord','123','Dr. Coordinator','Coordinator');
INSERT INTO users VALUES(2,'eval1','123','Prof. Smith','Evaluator');
INSERT INTO users VALUES(3,'eval2','123','Dr. Jones','Evaluator');
INSERT INTO users VALUES(4,'eval3','123','Dr. Emily','Evaluator');
INSERT INTO users VALUES(5,'ali','123','Ali Ahmad','Student');
INSERT INTO users VALUES(6,'bob','123','Bob Lee','Student');
INSERT INTO users VALUES(7,'cat','123','Catherine Lim','Student');
INSERT INTO users VALUES(8,'dan','123','Daniel Tan','Student');
INSERT INTO users VALUES(9,'eli','123','Eliana Ross','Student');
INSERT INTO users VALUES(10,'far','123','Farid Kamil','Student');
CREATE TABLE sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    "date" TEXT NOT NULL,
    venue TEXT NOT NULL,
    "type" TEXT NOT NULL CHECK("type" IN ('Oral', 'Poster')), 
    start_time TEXT NOT NULL, 
    end_time TEXT NOT NULL, 
    slots INTEGER NOT NULL, 
    time_slot INTEGER NOT NULL
);
INSERT INTO sessions VALUES(1,'2026-03-10','Hall A','Oral','09:00','10:00',4,15);
INSERT INTO sessions VALUES(2,'2026-03-11','Lobby','Poster','14:00','15:15',5,15);
INSERT INTO sessions VALUES(3,'2026-03-12','Hall B','Oral','16:00','17:00',4,15);
CREATE TABLE submissions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    session_id INTEGER, -- Nullable if not yet assigned
    title TEXT NOT NULL,
    abstract TEXT,
    supervisor TEXT,
    presentation_type TEXT CHECK(presentation_type IN ('Oral', 'Poster')),
    filePath TEXT,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (session_id) REFERENCES sessions(id)
);
INSERT INTO submissions VALUES(1,5,1,'AI','Using AI to...','Dr. Strange','Oral','uploads\5_material.jpg');
INSERT INTO submissions VALUES(2,6,NULL,'Crypto','New encryption...','Prof. X','Oral','uploads\6_material.jpg');
INSERT INTO submissions VALUES(3,7,2,'Solar Efficiency','Improving panels...','Mr. Green','Poster','uploads\7_material.jpg');
INSERT INTO submissions VALUES(4,8,NULL,'Blockchain','Secure voting...','Satoshi','Poster','uploads\8_material.jpg');
INSERT INTO submissions VALUES(5,9,NULL,'VR','Learning in VR...','Dr. Oculus','Oral','uploads\9_material.jpg');
INSERT INTO submissions VALUES(6,10,NULL,'Farming','IoT sensors...','Prof. Sprout','Poster','uploads\10_material.jpg');
INSERT INTO submissions VALUES(7,6,NULL,'ABX','ABSTRACT ..','Dr. Chong','Oral','uploads\6_material.jpg');
INSERT INTO submissions VALUES(9,6,2,'ABC','ABSTRACT ..','Prof. Sprout','Poster','uploads\6_material.jpg');
CREATE TABLE evaluator (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    evaluator_id INTEGER NOT NULL,
    session_id INTEGER NOT NULL,
    FOREIGN KEY (evaluator_id) REFERENCES users(id),
    FOREIGN KEY (session_id) REFERENCES sessions(id)
);
INSERT INTO evaluator VALUES(1,2,1);
INSERT INTO evaluator VALUES(2,3,1);
INSERT INTO evaluator VALUES(3,4,2);
INSERT INTO evaluator VALUES(4,2,3);
INSERT INTO evaluator VALUES(5,3,3);
CREATE TABLE assign (
    assign_id INTEGER PRIMARY KEY AUTOINCREMENT, 
    session_id INTEGER REFERENCES sessions(id), 
    submission_id INTEGER REFERENCES submissions(id), 
    eval_id INTEGER REFERENCES evaluator(id), 
    time_slot TEXT
);
INSERT INTO assign VALUES(1,1,1,1,'09:00 - 09:15');
INSERT INTO assign VALUES(2,2,3,3,'14:00 - 14:15');
INSERT INTO assign VALUES(3,2,9,3,'14:30-14:45');
CREATE TABLE IF NOT EXISTS "evaluations" (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    evaluator_id INTEGER NOT NULL,
    submission_id INTEGER NOT NULL,
    methodology_score INTEGER CHECK(methodology_score BETWEEN 0 AND 25),
    results_score INTEGER CHECK(results_score BETWEEN 0 AND 25),
    presentation_score INTEGER CHECK(presentation_score BETWEEN 0 AND 25),
    comments TEXT, `problem_clarity` INTEGER CHECK(results_score BETWEEN 0 AND 25),
    FOREIGN KEY (evaluator_id) REFERENCES users(id),
    FOREIGN KEY (submission_id) REFERENCES submissions(id),
    UNIQUE(evaluator_id, submission_id)
);
INSERT INTO evaluations VALUES(1,2,1,8,9,8,'Great presentation.',2);
INSERT INTO evaluations VALUES(2,4,3,7,6,8,'Good visuals, data lacks depth.',10);
INSERT INTO sqlite_sequence VALUES('users',10);
INSERT INTO sqlite_sequence VALUES('sessions',3);
INSERT INTO sqlite_sequence VALUES('submissions',10);
INSERT INTO sqlite_sequence VALUES('evaluator',5);
INSERT INTO sqlite_sequence VALUES('assign',3);
INSERT INTO sqlite_sequence VALUES('evaluations',2);
COMMIT;
