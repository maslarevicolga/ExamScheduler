import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class JSONReader {

    private final List<Exam> allExams = new LinkedList<>();
    private final List<ExamRoom> allRooms = new LinkedList<>();

    public List<Exam> readJSONExam(String examPath) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject)jsonParser.parse(new FileReader(examPath));
        App.durationInDays = ((Long)jsonObject.get("trajanje_u_danima")).intValue();
        JSONArray jsonArray = (JSONArray)jsonObject.get("ispiti");
        for(Object object : jsonArray) {
            jsonObject = (JSONObject)object;
            parseOneExam(jsonObject);
        }
        return allExams;
    }
    private void parseOneExam(JSONObject jsonObject) {
        String code = (String)jsonObject.get("sifra");
        int students = ((Long)jsonObject.get("prijavljeni")).intValue();
        int pc = ((Long)jsonObject.get("racunari")).intValue();
        JSONArray jsonArray = (JSONArray)jsonObject.get("odseci");
        String str = jsonArray.toString();
        String[] departments = str.substring(1, str.length() - 1).split(",");
        allExams.add(new Exam(code, students, pc == 1, departments));
    }

    public List<ExamRoom> readJSONExamRooms(String roomsPath) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray)jsonParser.parse(new FileReader(roomsPath));
        for(Object object : jsonArray) {
            JSONObject jsonObject = (JSONObject)object;
            String name = (String)jsonObject.get("naziv");
            int capacity = ((Long)jsonObject.get("kapacitet")).intValue();
            int pc = ((Long)jsonObject.get("racunari")).intValue();
            int onDuty = ((Long)jsonObject.get("dezurni")).intValue();
            int etf = ((Long)jsonObject.get("etf")).intValue();
            allRooms.add(new ExamRoom(name, capacity, pc == 1, onDuty, etf == 1));
        }
        return allRooms;
    }
}
