import java.util.*;
import java.util.stream.Collectors;

public class Exam {
    private final String code;
    private final int students;
    private final boolean pc;
    private final Set<String> departments = new HashSet<>();

    public Exam(String code, int students, boolean pc, String[] departments) {
        this.code = code;
        this.students = students;
        this.pc = pc;
        this.departments.addAll(Arrays.stream(departments).collect(Collectors.toList()));
    }

    public boolean getPC() {
        return pc;
    }
    public int getStudents() {
        return students;
    }
    public String getCode() {
        return code;
    }
    public Set<String> getDepartments() {
        return departments;
    }
    public int getStudyYear() {
        return (int) code.charAt(5);
    }

    public boolean sameYearDepartment(Exam assignedExam) {
        return this.getStudyYear() == assignedExam.getStudyYear() &&
                this.departments.stream().anyMatch(assignedExam.getDepartments()::contains);
    }

    public boolean nextYearDepartment(Exam assignedExam) {
        return Math.abs(this.getStudyYear() - assignedExam.getStudyYear()) == 1 &&
                this.departments.stream().anyMatch(assignedExam.getDepartments()::contains);
    }
}
