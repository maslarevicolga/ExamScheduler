import org.json.simple.parser.ParseException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {

        String examsPath = "C:\\Users\\olgam\\Desktop\\javni_testovi\\rok2.json";
        String roomsPath = "C:\\Users\\olgam\\Desktop\\javni_testovi\\sale2.json";
        String solutionPath = "C:\\Users\\olgam\\Desktop\\javni_testovi\\mySolutionArc2.csv";

        new App(examsPath, roomsPath, solutionPath, false, 5, false, true).solve();

    }

}
