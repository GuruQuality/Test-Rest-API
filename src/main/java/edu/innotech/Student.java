package edu.innotech;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

public class Student {
    Integer id;
    String name;
    List<Integer> marks = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public Student setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Student setName(String name) {
        this.name = name;
        return this;
    }

    public List<Integer> getMarks() {
        return marks;
    }

    public Student setMarks(List<Integer> marks) {
        this.marks = marks;
        return this;
    }

//    public Double getAverage() {
//        return marks.stream().mapToInt(Integer::intValue).average().orElse(0);
//    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", marks=" + marks +
                '}';
    }
}
