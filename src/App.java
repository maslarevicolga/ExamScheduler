import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.List;

public class App {

    static final List<Time> times = new LinkedList<>();
    final static int[] inMinutes = { 8 * 60, 11 * 60 + 30, 15 * 60, 18 * 60 + 30 };
    static int durationInDays;

    private final List<ExamRoom> allExamRooms;
    private final List<Exam> allExams;

    private final List<Exam> examsToScheduled = new LinkedList<>();
    private final Map<Exam, RoomsAndTime> examsRoomsScheduled = new LinkedHashMap<>();

    private int iteration = 0;
    private final boolean oneIteration;
    private final int sec;
    private final boolean fc;
    private final boolean arc;
    static final int domainLimit = 5;

    private static final JSONReader reader = new JSONReader();
    private final String solutionPath, stepsPath;
    private final StringBuilder sb = new StringBuilder();
    private long startTime;

    public App(String examsPath, String roomsPath, String solutionPath, String stepsPath, boolean oneIteration, int sec, boolean fc, boolean arc) throws IOException, ParseException {
        this.solutionPath = solutionPath;
        this.oneIteration = oneIteration;
        this.sec = sec;
        this.fc = fc;
        this.arc = arc;
        this.stepsPath = stepsPath;

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
        sb.append("Recursive scheduling job started at ").append(new Date()).append("\n");

        boolean success = backtrack(assignment);

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        sb.append("Scheduling process took ").append(duration).append(" milli seconds").append("\n");


        if(success || Assignment.haveSolution()) {
            if(!oneIteration) {
                sb.append("Best solution: \n");
                Assignment.printSolution(sb, Assignment.getBestIndex());
            }
            new CSVWriter().writeSolution(solutionPath, allExamRooms);
        } else
            sb.append("\"Unable to find the solution, terminating the program\"").append("\n");
        CSVWriter.writeToFile(stepsPath, sb);
    }

    private boolean backtrack(Assignment assignment) {
        if(oneIteration)
            assignment.printState(examsRoomsScheduled, ++iteration, sb);
        if(examsToScheduled.isEmpty()) {
            Assignment.addSolution(examsRoomsScheduled, sb);
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
            if(oneIteration)
                sb.append("Trying to assign ").append(roomsAndTime).append(" to ").append(examToAssign.getCode()).append("\n");

            if(fc) {
                if(oneIteration) sb.append("FORWARD CHECKING").append("\n");
                if(!forwardChecking(examToAssign, roomsAndTime, copyAssignment)) {
                    if(oneIteration) sb.append("Can't assign ").append(roomsAndTime).append(" to ").append(examToAssign.getCode()).append(" due to forward check constraint").append("\n");
                    examsToScheduled.add(examToAssign);
                    examsRoomsScheduled.remove(examToAssign);
                    continue;
                }
            }
            else if(arc) {
                if(oneIteration) sb.append("ARC CHECKING").append("\n");
                if(!arc(examToAssign, roomsAndTime, copyAssignment)) {
                    if(oneIteration) sb.append("Can't assign ").append(roomsAndTime).append(" to ").append(examToAssign.getCode()).append(" due to arc checking").append("\n");
                    examsToScheduled.add(examToAssign);
                    examsRoomsScheduled.remove(examToAssign);
                    continue;
                }
            }
            else {
                if(!isConsistent(examToAssign, roomsAndTime)) {
                    if(oneIteration) sb.append("Can't assign ").append(roomsAndTime).append(" to ").append(examToAssign.getCode()).append(" due to inconsistent state").append("\n");
                    examsToScheduled.add(examToAssign);
                    examsRoomsScheduled.remove(examToAssign);
                    continue;
                }
            }
            if(backtrack(copyAssignment)) {
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
                    if(oneIteration) sb.append("Remove ").append(check).append(" from ").append(exam.getCode()).append(" due to same time and rooms").append("\n");
                    myDomain.remove(check);
                } else {
                    if(exam.sameYearDepartment(assignedExam)) {
                        if(check.getTime().getDay() != assignedValue.getTime().getDay()) { index++; continue; }
                        if(oneIteration) sb.append("Remove ").append(check).append(" from ").append(exam.getCode()).append(" due to same year and department").append("\n");
                        myDomain.remove(check);
                    } else if(exam.nextYearDepartment(assignedExam)) {
                        if(!check.getTime().same(assignedValue.getTime())) { index++; continue; }
                        if(oneIteration) sb.append("Remove ").append(check).append(" from ").append(exam.getCode()).append(" due to next year and same department").append("\n");
                        myDomain.remove(check);
                    } else
                        index++;
                }
            }
            if(myDomain.isEmpty())
                return false;

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
        List<Map.Entry<Exam, Exam>> arcMap = new LinkedList<>();
        addToArcMap(arcMap, assignedExam);

        while (!arcMap.isEmpty()) {
            Exam x = arcMap.get(0).getKey();
            Exam y = arcMap.get(0).getValue();
            arcMap.remove(0);

            List<RoomsAndTime> xDomain = copyAssignment.getExamDomain(x);
            List<RoomsAndTime> yDomain = copyAssignment.getExamDomain(y);
            List<RoomsAndTime> xValuesToDel = new LinkedList<>();

            for (RoomsAndTime rmX : xDomain) {
                boolean yNoValue = true;
                for (RoomsAndTime rmY : yDomain)
                    if (!rmX.areSubSet(rmY) && (!x.sameYearDepartment(y) || rmX.getTime().getDay() != rmY.getTime().getDay()) &&
                            (!x.nextYearDepartment(y) || !rmX.getTime().same(rmY.getTime()))) {
                        yNoValue = false;
                        break;
                    }
                if (yNoValue)
                    xValuesToDel.add(rmX);
            }
            if (!xValuesToDel.isEmpty()) {
                xDomain.removeAll(xValuesToDel);
                if (xDomain.isEmpty())
                    return false;

                addToArcMap(arcMap, x);
            }
        }
        return true;
    }

    private void addToArcMap(List<Map.Entry<Exam, Exam>> arcMap, Exam cause) {
        for(Exam exam : examsToScheduled) {
            if(exam == cause)
                continue;
            boolean exist = arcMap.stream().anyMatch(entry ->
                entry.getKey() == exam && entry.getValue() == cause
            );
            if(!exist)
                arcMap.add(new AbstractMap.SimpleEntry<>(exam, cause));
        }
    }

}
