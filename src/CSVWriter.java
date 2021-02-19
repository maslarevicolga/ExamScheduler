import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CSVWriter {

    public static void writeToFile(String stepsPath, StringBuilder sb) throws IOException {
        FileWriter writer = new FileWriter(stepsPath);
        writer.write(sb.toString());
        writer.close();
    }

    public void writeSolution(String solutionPath, List<ExamRoom> allRooms) throws IOException {
        com.opencsv.CSVWriter writer = new com.opencsv.CSVWriter(
                new FileWriter(solutionPath), ',', com.opencsv.CSVWriter.NO_QUOTE_CHARACTER,
                com.opencsv.CSVWriter.DEFAULT_ESCAPE_CHARACTER, com.opencsv.CSVWriter.DEFAULT_LINE_END);
        int sizeAllRooms = allRooms.size();
        Map<Exam, RoomsAndTime> bestSolution = Assignment.getBestSolution();
        String[] roomsString = new String[sizeAllRooms];
        for(int index = 0; index < sizeAllRooms; index++)
            roomsString[index] = allRooms.get(index).getName();
        for(int day = 1; day <= App.durationInDays; day++) {
            String[] header = new String[sizeAllRooms + 1];
            header[0] = "Dan " + day;
            System.arraycopy(roomsString, 0, header, 1, sizeAllRooms);
            writer.writeNext(header);
            for (int min : App.inMinutes) {
                String[] oneDataRow = new String[sizeAllRooms + 1];
                oneDataRow[0]  = String.format("%02d", (min / 60)) + ":" + String.format("%02d", (min % 60));
                for(int index = 0; index < sizeAllRooms; index++) {
                    boolean found = false;
                    for(Map.Entry<Exam, RoomsAndTime> entry : bestSolution.entrySet()) {
                        Exam exam = entry.getKey();
                        RoomsAndTime assigned = entry.getValue();
                        if((assigned.getTime().getDay() == day) && (assigned.getTime().getInMinutes() == min)
                                && assigned.getRooms().contains(allRooms.get(index))) {
                            found = true;
                            oneDataRow[index + 1] = exam.getCode();
                            break;
                        }
                    }
                    if(!found) oneDataRow[index + 1] = "X";
                }
                writer.writeNext(oneDataRow);
            }
            if(day + 1 <= App.durationInDays) writer.writeNext(new String[]{"\n"});
        }
        writer.close();
    }
}
