import java.util.*;

public class Assignment {

    private final Map<Exam, List<RoomsAndTime>> domain;
    private static final List<LinkedHashMap<Exam, RoomsAndTime>> solutions = new LinkedList<>();
    private static int bestSolutionIndex = 0;
    private static double bestQuality = Integer.MAX_VALUE;

    public Assignment() {
        domain = new HashMap<>();
    }

    public Assignment(Assignment assignment) {
        this.domain = new HashMap<>();
        for(Map.Entry<Exam, List<RoomsAndTime>> entry : assignment.domain.entrySet()) {
            List<RoomsAndTime> original = entry.getValue();
            List<RoomsAndTime> examRooms = new LinkedList<>(original);
            this.domain.put(entry.getKey(), examRooms);
        }
    }

    private void removeRoomsPC(List<ExamRoom> copyAllRooms) {
        for(int i = 0; i < copyAllRooms.size(); )
            if(!copyAllRooms.get(i).getPC())
                copyAllRooms.remove(copyAllRooms.get(i));
            else
                i++;
    }
    private void checkDomainOK(List<List<ExamRoom>> domain, ArrayDeque<ExamRoom> toAdd) {
        int index = 0;
        while(index < domain.size()) {
            List<ExamRoom> list = domain.get(index);
            if(toAdd.containsAll(list)) return; // no insert
            if(list.containsAll(toAdd))
                domain.remove(list);
            else index++;
        }
        domain.add(new ArrayList<>(toAdd));
    }
    private void getDomain(int students, ArrayDeque<ExamRoom> examHalls, ExamRoom[] array, int index, List<List<ExamRoom>> domain) {
        if(students <= 0) {
            checkDomainOK(domain, examHalls);
            return;
        }
        if(index >= array.length)
            return;
        examHalls.add(array[index]);
        getDomain(students - array[index].getCapacity(), examHalls, array, index + 1, domain);
        examHalls.remove(array[index]);
        getDomain(students, examHalls, array, index + 1, domain);
    }
    private List<List<ExamRoom>> capacityConstraint(Exam exam, List<ExamRoom> copyAllRooms) {
        ArrayDeque<ExamRoom> examHallDeque = new ArrayDeque<>();
        List<List<ExamRoom>> domain = new LinkedList<>();
        getDomain(exam.getStudents(), examHallDeque, copyAllRooms.toArray(new ExamRoom[0]), 0, domain);
        return domain;
    }
    private void sortDomain(List<RoomsAndTime> toSort) {
        int size = toSort.size();
        for(int i = 0; i < size - 1; i++)
            for(int j = i + 1; j < size; j++) {
                RoomsAndTime rm1 = toSort.get(i);
                RoomsAndTime rm2 = toSort.get(j);
                if ((rm1.getQuality() > rm2.getQuality()) ||
                        ((rm1.getQuality() == rm2.getQuality()) && rm1.getCapacity() > rm2.getCapacity())) {
                    toSort.set(i, rm2);
                    toSort.set(j, rm1);
                }
            }
    }
    public void createExamDomain(Exam exam, List<ExamRoom> allRooms) {
        List<ExamRoom> copyAllRooms = new LinkedList<>(allRooms);

        if(exam.getPC()) removeRoomsPC(copyAllRooms);
        if(exam.getPC() && copyAllRooms.isEmpty()) {
            System.out.println("Unable to find the solution, no enough pcs, terminating the program");
            System.exit(-1);
        }

        List<List<ExamRoom>> setList = capacityConstraint(exam, allRooms);
        if(setList.isEmpty()) {
            System.out.println("Unable to find the solution, no enough space, terminating the program");
            System.exit(-2);
        }

        List<RoomsAndTime> oneDomain = new LinkedList<>();
        for(List<ExamRoom> list : setList)
            for(Time time : App.times)
                oneDomain.add(new RoomsAndTime(time, list));
        sortDomain(oneDomain);
        domain.put(exam, oneDomain);
    }

    public static void addSolution(Map<Exam, RoomsAndTime> solution) {
        LinkedHashMap<Exam, RoomsAndTime> copySolution = new LinkedHashMap<>();
        int cnt = solutions.size();
        double quality = 0;

        for(Map.Entry<Exam, RoomsAndTime> entry : solution.entrySet()) {
            copySolution.put(entry.getKey(), entry.getValue());
            quality += entry.getValue().getQuality();
        }
        if(cnt == 0 || quality < bestQuality) {
            bestQuality = quality;
            bestSolutionIndex = cnt;
        }
        solutions.add(copySolution);
    }

    public List<RoomsAndTime> getExamDomain(Exam exam) { return domain.get(exam); }
    public static Map<Exam, RoomsAndTime> getBestSolution() { return solutions.get(bestSolutionIndex); }

    private String printDomains(List<RoomsAndTime> domains) {
        StringBuilder sb = new StringBuilder();
        int iter = 0;
        for(RoomsAndTime rm : domains)
            if(iter++ == App.domainLimit) break;
            else sb.append(rm).append(" ");
        return sb.toString();
    }
    public void printState(Map<Exam, RoomsAndTime> scheduled, long iteration) {
        System.out.print("> Scheduled |         ");
        System.out.println("exam, rooms and time");
        for(Map.Entry<Exam, RoomsAndTime> entry : scheduled.entrySet())
            System.out.println(entry.getKey().getCode() + ", " + entry.getValue());
        System.out.println();
        System.out.println("> Iterations # " + iteration);
        System.out.print(" State  |                                                ");
        System.out.println("exam, [domain | quality | day:hour:minute]       ");
        System.out.println("-----------------------------------------------------------");
        for(Map.Entry<Exam, List<RoomsAndTime>> entry : domain.entrySet()) {
            System.out.println(entry.getKey().getCode() + printDomains(entry.getValue()));
        }
    }
}
