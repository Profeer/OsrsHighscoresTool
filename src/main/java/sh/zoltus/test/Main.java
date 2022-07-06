package sh.zoltus.test;

import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    static int days = 365;
    static int extraPerDay = 55;

    static int total = 0;

    public static List<String> failed = new ArrayList<>();
    public static List<String> data = new ArrayList<>();

    @SneakyThrows
    public static void main(String[] args) {
        //fetchPlayers(); // 11.1.2022a
        doStuff();
    }

    @SneakyThrows
    private static void fetchPlayers() {
        for (String name : Members.members) {
            try {
                URL url = new URL("https://secure.runescape.com/m=hiscore_oldschool/index_lite.ws?player=" + name);
                URLConnection con = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String jsonString = in.lines().collect(Collectors.joining("\n"));
                System.out.println("Fetched " + name);
                data.add(name + ":" + jsonString);
                //OSRSPlayer p = new OSRSPlayer(jsonString);
                in.close();
                //sending the actual Thread of execution to sleep X milliseconds
                Thread.sleep(50);
            } catch (InterruptedException | IOException ignored) {
                failed.add(name);
            }
        }
        FileUtils.writeLines(new File("data.txt"), data, "|");
        FileUtils.writeLines(new File("failed.txt"), failed, "|");
    }


    private static List<HiscoreResult> results = new ArrayList<>();
    @SneakyThrows
    public static void doStuff() {
        List<String> dataLines = FileUtils.readLines(new File("data.txt"));
        String dataJson = StringUtils.join(dataLines, "\n");
        String[] dataJsonArr = dataJson.split("\\|");

        for (String jsonDatum : dataJsonArr) {
            String[] jsonSplit = jsonDatum.split(":");
            //0 is name : 1 is data
            String name = jsonSplit[0];
            String scoreLine = jsonSplit[1];
            results.add(parseResponse(name, scoreLine));
        }
        HiscoreSkill skill = HiscoreSkill.COMBAT;

        //Most order
        sortByMost(skill)
                .forEach(result -> System.out.println(result.getPlayer() + " -> " + skill.name() + " : " + result.getSkill(skill).level()));


        System.out.println(skill.name() + " average: " + getAverage(skill));
    }


    private static double getAverage(HiscoreSkill Skill) {
        return results.stream().mapToDouble(x -> x.getSkill(Skill).level()).average().orElse(0);
    }

    private static List<HiscoreResult> sortByMost(HiscoreSkill skill) {
        return results.stream()
                .filter(result -> result.getSkill(skill).level() > 0)//
                .sorted(Collections.reverseOrder((o1, o2) -> Double.compare(o2.getSkill(skill).level(), o1.getSkill(skill).level())))
                .collect(Collectors.toList());
    }



    private static HiscoreResult parseResponse(String username, String responseStr) throws IOException {
        CSVParser parser = CSVParser.parse(responseStr, CSVFormat.DEFAULT);

        HiscoreResultBuilder hiscoreBuilder = new HiscoreResultBuilder(username);
        int count = 0;

        for (CSVRecord record : parser.getRecords()) {
            if (count++ >= HiscoreSkill.values().length) {
                break; // rest is other things?
            }
            // rank, level, experience
            int rank = Integer.parseInt(record.get(0));
            int level = Integer.parseInt(record.get(1));

            // items that are not skills do not have an experience parameter
            long experience = -1;
            if (record.size() == 3) {
                experience = Long.parseLong(record.get(2));
            }
            Skill skill = new Skill(rank, level, experience);
            hiscoreBuilder.setNextSkill(skill);
        }
        return hiscoreBuilder.build();
    }
}
