import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;

public class App {

    static final List<Time> times = new LinkedList<>();
    final static int[] inMinutes = { 8 * 60, 11 * 60 + 30, 15 * 60, 18 * 60 + 30 };
    static int durationInDays;

    private final List<ExamRoom> allExamRooms;
    private final List<Exam> allExams;

    private final List<Exam> examsToScheduled = new LinkedList<>();
    private final Map<Exam, RoomsAndTime> examsRoomsScheduled = new LinkedHashMap<>();

    private final boolean oneIteration;
    private final int sec;
    private final boolean fc;
    private final boolean arc;
    static final int domainLimit = 5;

    private static final JSONReader reader = new JSONReader();
    private final String solutionPath;

    private long startTime;

    public App(String examsPath, String roomsPath, String solutionPath, boolean oneIteration, int sec, boolean fc, boolean arc) throws IOException, ParseException {
        this.solutionPath = solutionPath;
        this.oneIteration = oneIteration;
        this.sec = sec;
        this.fc = fc;
        this.arc = arc;

        allExamRooms = reader.readJSONExamRooms(roomsPath);
        allExams = reader.readJSONExam(examsPath);
        examsToScheduled.addAll(allExams);
        for(int day = 1; day <= durationInDays; day++) {
            for(int inMin : inMinutes)
                times.add(new Time(day, inMin / 60, inMin % 60));
        }
    }

    public void solve() throws IOException {
        Assignment assignment = new Assignment();
        for(Exam exam : allExams) {
            assignment.createExamDomain(exam, allExamRooms);
        }

        startTime = System.currentTimeMillis();
        System.out.println("Recursive scheduling job started at " + new Date() + "\n");

        boolean success = backtrack(assignment, 1);

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        System.out.println("Scheduling process took " + duration + " milli seconds");

        if(success)
            new CSVWriter().writeSolution(solutionPath, allExamRooms);
        else
            System.out.println("\"Unable to find the solution, terminating the program\"");
    }

    private boolean backtrack(Assignment assignment, long iteration) {
        if(oneIteration) assignment.printState(examsRoomsScheduled, iteration);
        if(examsToScheduled.isEmpty()) {
            if(oneIteration)  System.out.println("Scheduling job completed at " + new Date());
            Assignment.addSolution(examsRoomsScheduled);
            return true;
        }

        Exam examToAssign = getExamToAssign(assignment);
        List<RoomsAndTime> applicableRoomsAndTime = assignment.getExamDomain(examToAssign);
        for(RoomsAndTime roomsAndTime : applicableRoomsAndTime) {
            examsToScheduled.remove(examToAssign);
            examsRoomsScheduled.put(examToAssign, roomsAndTime);

            Assignment copyAssignment = new Assignment(assignment);
            copyAssignment.getExamDomain(examToAssign).clear();
            copyAssignment.getExamDomain(examToAssign).add(roomsAndTime);
            System.out.println("Trying to assign " + roomsAndTime + " to " + examToAssign.getCode());

            if(fc) {
                if(oneIteration) System.out.println("FORWARD CHECKING");
                if(!forwardChecking(examToAssign, roomsAndTime, copyAssignment)) {
                    examsToScheduled.add(examToAssign);
                    examsRoomsScheduled.remove(examToAssign);
                    continue;
                }
            }
            else if(arc) {
                if(oneIteration) System.out.println("ARC CHECKING");
                if(!arc(examToAssign, roomsAndTime, copyAssignment)) {
                    examsToScheduled.add(examToAssign);
                    examsRoomsScheduled.remove(examToAssign);
                    continue;
                }
            }
            else {
                if(!isConsistent(examToAssign, roomsAndTime)) {
                    System.out.println("Can't assign " + roomsAndTime + " to " + examToAssign.getCode() + " due to inconsistent state");
                    examsToScheduled.add(examToAssign);
                    examsRoomsScheduled.remove(examToAssign);
                    continue;
                }
            }
            if(backtrack(copyAssignment, ++iteration)) {
                if(System.currentTimeMillis() - startTime > sec * 1000 || oneIteration) return true;
            }
            examsToScheduled.add(examToAssign);
            examsRoomsScheduled.remove(examToAssign);
        }
        return false;
    }
    private boolean isConsistent(Exam assignedExam, RoomsAndTime assignedValue) {
        for(Exam exam : examsRoomsScheduled.keySet()) {
            if(exam == assignedExam) continue;
            RoomsAndTime check = examsRoomsScheduled.get(exam);
            if(check.areSubSet(assignedValue))
                return false;
            if(exam.sameYearDepartment(assignedExam))
                if(check.getTime().getDay() == assignedValue.getTime().getDay())
                    return false;
            if(exam.nextYearDepartment(assignedExam))
                if(check.getTime().same(assignedValue.getTime()))
                    return false;
        }
        return true;
    }
    private boolean forwardChecking(Exam assignedExam, RoomsAndTime assignedValue, Assignment copyAssignment) {
        for(Exam exam : examsToScheduled) {
            List<RoomsAndTime> myDomain = copyAssignment.getExamDomain(exam);
            int index = 0;
            while (index < myDomain.size()) {
                RoomsAndTime check = myDomain.get(index);
                if(check.areSubSet(assignedValue)) {
                    if(oneIteration) System.out.println("Remove " + check + " from " + exam.getCode() + " due to same time and rooms");
                    myDomain.remove(check);
                } else {
                    if(exam.sameYearDepartment(assignedExam)) {
                        if(check.getTime().getDay() != assignedValue.getTime().getDay()) { index++; continue; }
                        if(oneIteration) System.out.println("Remove " + check + " from " + exam.getCode() + " due to same year and department");
                        myDomain.remove(check);
                    } else if(exam.nextYearDepartment(assignedExam)) {
                        if(!check.getTime().same(assignedValue.getTime())) { index++; continue; }
                        if(oneIteration) System.out.println("Remove " + check + " from " + exam.getCode() + " due to next year and same department");
                        myDomain.remove(check);
                    } else
                        index++;
                }
            }
            if(myDomain.isEmpty()) {
                if (oneIteration)
                    System.out.println("Can't assign " + assignedValue +
                            " to " + assignedExam + " due to forward check constraint");
                return false;
            }
        }
        return true;
    }
    private Exam getExamToAssign(Assignment assignment) {
        int minDomain = Integer.MAX_VALUE;
        Exam returnExam = null;
        for(Exam exam : examsToScheduled) {
            if(returnExam == null || assignment.getExamDomain(exam).size() < minDomain) {
                minDomain = assignment.getExamDomain(exam).size();
                returnExam = exam;
            }
        }
        return returnExam;
    }

    private boolean arc(Exam assignedExam, RoomsAndTime assignedValue, Assignment copyAssignment) {
        List<Exam> arcList = new LinkedList<>(examsToScheduled);

        while(!arcList.isEmpty()) {
            Exam exam = arcList.remove(0);
            List<RoomsAndTime> myDomain = copyAssignment.getExamDomain(exam);
            int index = 0;
            boolean addToArc = false;
            while (index < myDomain.size()) {
                RoomsAndTime check = myDomain.get(index);
                if (check.areSubSet(assignedValue)) {
                    if (oneIteration)
                        System.out.println("Remove " + check + " from " + exam.getCode() + " due to same time and rooms");
                    myDomain.remove(check);
                    addToArc = true;
                } else {
                    if (exam.sameYearDepartment(assignedExam)) {
                        if (check.getTime().getDay() != assignedValue.getTime().getDay()) {
                            index++;
                            continue;
                        }
                        if (oneIteration)
                            System.out.println("Remove " + check + " from " + exam.getCode() + " due to same year and department");
                        myDomain.remove(check);
                        addToArc = true;
                    } else if (exam.nextYearDepartment(assignedExam)) {
                        if (!check.getTime().same(assignedValue.getTime())) {
                            index++;
                            continue;
                        }
                        if (oneIteration)
                            System.out.println("Remove " + check + " from " + exam.getCode() + " due to next year and same department");
                        myDomain.remove(check);
                        addToArc = true;
                    } else
                        index++;
                }
            }
            if(myDomain.isEmpty()) {
                if (oneIteration)
                    System.out.println("Can't assign " + assignedValue +
                            " to " + assignedExam + " due to arc check constraint");
                return false;
            }
            if(addToArc)
                addToArcList(arcList, exam);
        }
        return true;
    }
    private void addToArcList(List<Exam> arcList, Exam cause) {
        for(Exam exam : examsToScheduled) {
            if(arcList.contains(exam) || exam.equals(cause))
                continue;
            arcList.add(exam);
        }
    }

}
