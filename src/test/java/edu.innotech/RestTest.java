package edu.innotech;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestTest {
    static final int EXIST_STUDENT_ID = 1;
    static final int NOT_EXIST_STUDENT_ID = 2;

    @AfterEach
    public void clearAll() {
        Student[] students;

        do {
            Response response = RestAssured.given()
                    .baseUri("http://localhost:8080")
                    .header("content-type", "application\\json")
                    .when()
                    .get("/topStudent/");
            String content = response.then().extract().body().asString();
            if (content.length() == 0) {
                break;
            }

            students = response
                    .then()
                    .contentType(ContentType.JSON)
                    .extract().as(Student[].class);

            for (Student student : students) {
                RestAssured.given()
                        .baseUri("http://localhost:8080/student/" + student.getId())
                        .when()
                        .delete()
                        .then();
            }
        } while (students.length > 0);
    }

    @BeforeEach
    public void prepage() throws JsonProcessingException {
        Student student1 = new Student();
        student1.setId(EXIST_STUDENT_ID);
        student1.setName("Padre");
        student1.setMarks(new ArrayList<>(List.of(3, 4, 5, 1)));
        ObjectMapper objectMapper = new ObjectMapper();
        String studentJSON = objectMapper.writeValueAsString(student1);

        RestAssured.given()
                .baseUri("http://localhost:8080/student")
                .contentType(ContentType.JSON)
                .body(studentJSON)
                .when()
                .post()
                .then()
                .statusCode(201);
    }

    //1. get /student/{id} возвращает JSON студента с указанным ID и заполненным именем, если такой есть в базе, код 200.
    @Test
    public void checkExistStudent() throws JsonProcessingException {
        Student student =
                RestAssured.given()
                        .baseUri("http://localhost:8080")
                        //.header("content-type", "application\\json")
                        .contentType(ContentType.JSON)
                        //.body(studentJSON)
                        .when()
                        .get("/student/" + EXIST_STUDENT_ID)
                        .then()
                        .statusCode(200)
                        .contentType(ContentType.JSON)
                        .body("id", Matchers.equalTo(EXIST_STUDENT_ID))
                        .extract().as(Student.class);//создает обьекты нашего класса

        System.out.println(student);
    }

    //2. get /student/{id} возвращает код 404, если студента с данным ID в базе нет.
    @DisplayName("Проверка работоспособности GET student/{id}")
    @Test
    public void getNotExistStudent() {
        String response = RestAssured.given()
                .baseUri("http://localhost:8080/student/" + NOT_EXIST_STUDENT_ID)
                //.header("content-type", "application\\json")
                .when()
                .get()
                .then()
                .statusCode(404)
                .body(Matchers.describedAs("Получили студента",Matchers.isEmptyString()))//пустой ли результат?
                .extract()
                .body().asString();
        System.out.println(response);
    }

    @DisplayName("Проверка работоспособности GET student/{id} при полном отсутствии данных в БД")
    @Test
    public void getNotExisStudentOnEmptyBD() {
        clearAll();
        String response = RestAssured.given()
                .baseUri("http://localhost:8080/student/" + NOT_EXIST_STUDENT_ID)
                //.header("content-type", "application\\json")
                .when()
                .get()
                .then()
                .statusCode(404)
                .body(Matchers.describedAs("Получили студента",Matchers.isEmptyString()))//пустой ли результат?
                .extract()
                .body().asString();
        System.out.println(response);
    }

    @Test
    //3. post /student добавляет студента в базу, если студента с таким ID ранее не было, при этом имя заполнено, код 201.
    public void createStudent() throws JsonProcessingException {
        Student student1 = new Student();
        student1.setId(NOT_EXIST_STUDENT_ID);
        student1.setName("Padre");
        student1.setMarks(new ArrayList<>(List.of(3, 4, 5, 1)));
        ObjectMapper objectMapper = new ObjectMapper();
        String studentJSON = objectMapper.writeValueAsString(student1);//преобразуем в строку

        //Создаем студента
        RestAssured.given()
                .baseUri("http://localhost:8080")
                .contentType(ContentType.JSON)//чтобы сервер знал как парсить
                .body(studentJSON)
                .when()
                .post("/student")
                .then()
                .statusCode(201);
    }

    //4. post /student обновляет студента в базе, если студент с таким ID ранее был, при этом имя заполнено, код 201.
    @Test
    public void updateStudent() throws JsonProcessingException {
        Student updatedStudent = new Student();
        updatedStudent.setId(EXIST_STUDENT_ID);
        updatedStudent.setName("Update Padre");
        updatedStudent.setMarks(new ArrayList<>(List.of(3, 4, 5, 1)));

        // Выполнение: обновляем студента
        RestAssured.given()
                .baseUri("http://localhost:8080")
                .contentType(ContentType.JSON)
                .body(updatedStudent)
                .when()
                .post("/student/")
                .then()
                .statusCode(201);

        RestAssured.given()
                        .baseUri("http://localhost:8080")
                        .contentType(ContentType.JSON)
                        .when()
                        .get("/student/" + EXIST_STUDENT_ID)
                        .then()
                        .statusCode(200)
                        .contentType(ContentType.JSON)
                        .body("name", Matchers.equalTo("Update Padre"));

        System.out.println("UpdatedStudent: " + updatedStudent);
    }

    //5. post /student добавляет студента в базу, если ID null, то возвращается назначенный ID, код 201.
    @Test
    public void createStudentWithCheckId() throws JsonProcessingException {
        Student student = new Student();
        student.setId(null);
        student.setName("Vovochka");
        student.setMarks(new ArrayList<>(List.of(3, 4, 5, 1)));

        //Создаем студента
        String result = RestAssured.given()
                .baseUri("http://localhost:8080")
                .contentType(ContentType.JSON)//чтобы сервер знал как парсить
                .body(student)
                .when()
                .post("/student")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                //.body(Matchers.isA(Integer.class))
                .body(Matchers.matchesPattern("\\d+"))//регулярное выражение проверяет набор d+ 1 и более, d. - набот от 0 - 9
                .extract().asString();

        System.out.println(result);
    }

    //6. post /student возвращает код 400, если имя не заполнено.
    @Test
    public void createStudentWithoutName() {
        Student student = new Student();
        student.setId(null);
        student.setName(null);
        student.setMarks(new ArrayList<>(List.of(3, 4, 5, 1)));

        //Создаем студента
        RestAssured.given()
                .baseUri("http://localhost:8080")
                .contentType(ContentType.JSON)//чтобы сервер знал как парсить
                .body(student)
                .when()
                .post("/student")
                .then()
                .statusCode(400);
    }

    //7. delete /student/{id} удаляет студента с указанным ID из базы, код 200.
    @Test
    public void deleteStudent() {
        RestAssured.given()
                .baseUri("http://localhost:8080")
                .header("content-type", "application\\json")
                .when()
                .delete("/student/" + EXIST_STUDENT_ID)
                .then()
                .statusCode(200)
                .extract().asString();
    }

    //8. delete /student/{id} возвращает код 404, если студента с таким ID в базе нет.
    @Test
    public void deleteNotExistStudent() {
        RestAssured.given()
                .baseUri("http://localhost:8080")
                .header("content-type", "application\\json")
                .when()
                .delete("/student/" + NOT_EXIST_STUDENT_ID)
                .then()
                .statusCode(404)
                .extract().asString();
    }

    //9. get /topStudent код 200 и пустое тело, если студентов в базе нет.
    @Test
    public void getEmptyTopStudent() {
        clearAll();
        RestAssured.given()
                .baseUri("http://localhost:8080")
                .header("content-type", "application\\json")
                .when()
                .get("/topStudent/")
                .then()
                .statusCode(200)
                .body(Matchers.isEmptyString());
    }

    //10. get /topStudent код 200 и пустое тело, если ни у кого из студентов в базе нет оценок.
    @Test
    public void getEmptyTopStudentIfStudentsWithoutMarks() {
        Student student1 = new Student();
        student1.setId(EXIST_STUDENT_ID);
        student1.setName("Padre");
        student1.setMarks(null);

        RestAssured.given()
                .baseUri("http://localhost:8080/student")
                .contentType(ContentType.JSON)
                .body(student1)
                .when()
                .post()
                .then()
                .statusCode(201);

        RestAssured.given()
                .baseUri("http://localhost:8080")
                .header("content-type", "application\\json")
                .when()
                .get("/topStudent/")
                .then()
                .statusCode(200)
                .body(Matchers.isEmptyString());
    }

    //11. get /topStudent код 200 и один студент, если у него максимальная средняя оценка, либо же среди всех студентов с максимальной средней у него их больше всего.
    @DisplayName("Проверка корректности отбора лучшего студента")
    @Test
    public void getOneStudentAsBest() {
        final int BEST_OF_THE_BEST_STUDENT_ID = 10;
        List<Student> studentsForCreate = new ArrayList<>();

        studentsForCreate.add(
                (new Student()).setId(BEST_OF_THE_BEST_STUDENT_ID).setName("Padre").setMarks(List.of(5, 5))
        );
        studentsForCreate.add(
                (new Student()).setId(null).setName("Vovka").setMarks(List.of(3, 3))
        );
        studentsForCreate.add(
                (new Student()).setId(null).setName("Vovka").setMarks(List.of(5))
        );
        //Создаем студентов
        for (Student student : studentsForCreate) {
            RestAssured.given()
                    .baseUri("http://localhost:8080/student")
                    .contentType(ContentType.JSON)
                    .body(student)
                    .when()
                    .post()
                    .then()
                    .statusCode(201);
        }

        Student[] students =
                RestAssured.given()
                        .baseUri("http://localhost:8080")
                        .header("content-type", "application\\json")
                        .when().get("/topStudent/")
                        .then().statusCode(200)
                        .contentType(ContentType.JSON)
                        .extract().as(Student[].class);

        System.out.println(Arrays.toString(students));

        Assertions.assertEquals(1, students.length, "не один студент");
        //Сверка лучшего студента с ожидающим
        Assertions.assertEquals(BEST_OF_THE_BEST_STUDENT_ID, students[0].getId(), "Студент с не максимальной средней оценкой, либо же среди всех студентов с максимальной средней у него их не больше всего в topStudent");
    }

    @Test
    //12.get /topStudent код 200 и несколько студентов, если у них всех эта оценка максимальная и при этом они равны по количеству оценок
    public void getFewStudentAsBest() {
        final int BEST_OF_THE_BEST_STUDENT_ID = 10;
        List<Student> studentsForCreate = new ArrayList<>();

        studentsForCreate.add(
                (new Student()).setId(BEST_OF_THE_BEST_STUDENT_ID).setName("Padre").setMarks(List.of(5, 5))
        );
        studentsForCreate.add(
                (new Student()).setId(null).setName("Vovka").setMarks(List.of(3, 3))
        );
        studentsForCreate.add(
                (new Student()).setId(null).setName("Vovka").setMarks(List.of(5, 5))
        );
        //Создаем студентов
        for (Student student : studentsForCreate) {
            RestAssured.given()
                    .baseUri("http://localhost:8080/student")
                    .contentType(ContentType.JSON)
                    .body(student)
                    .when()
                    .post()
                    .then()
                    .statusCode(201);
        }

        Student[] students =
                RestAssured.given()
                        .baseUri("http://localhost:8080")
                        .header("content-type", "application\\json")
                        .when().get("/topStudent/")
                        .then().statusCode(200)
                        .contentType(ContentType.JSON)
                        .extract().as(Student[].class);

        System.out.println(Arrays.toString(students));

        Assertions.assertTrue(students.length > 1, "не несколько студентов, если у них всех эта оценка максимальная и при этом они равны по количеству оценок");
    }
}
