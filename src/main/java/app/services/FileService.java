package app.services;

import app.dao.SubjectManager;
import app.dao.TestManager;
import app.models.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.regex.Pattern;

import app.models.Topic;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class FileService {
    public static String readFileContent(File file) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;

            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        }
        return contentBuilder.toString();
    }

    public static boolean writeTestToFile(String content, Test test) {
        // Get the Downloads directory
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");

        // Create the target file in Downloads
        File file = new File(downloadsDir, test.getName().replace(" ", "_").replace("/", "_") + ".docx");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            XWPFDocument document = createDocxFile(content, test);

            document.write(fileOutputStream);
            fileOutputStream.close();
            document.close();
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] - Error processing the file: " + e.getMessage());
            return false;
        }
    }

    private static XWPFDocument createDocxFile(String content, Test test) throws SQLException {
        XWPFDocument document = new XWPFDocument();

        XWPFParagraph mainTitle = document.createParagraph();
        mainTitle.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun mainTitleRun = mainTitle.createRun();
        mainTitleRun.setText(test.getName());
        mainTitleRun.setBold(true);
        mainTitleRun.setFontSize(20);
        mainTitleRun.setColor("0F4761");
        mainTitleRun.setFontFamily("Aptos Display");

        XWPFParagraph subjectTitle = document.createParagraph();
        subjectTitle.setAlignment(ParagraphAlignment.CENTER);

        String subject = SubjectManager.getSubject(test.getPrompt().getTopics().get(0)).getName();

        XWPFRun subjectTitleRun = subjectTitle.createRun();
        subjectTitleRun.setText(subject);
        subjectTitleRun.setFontSize(16);
        subjectTitleRun.setColor("0F4761");
        subjectTitleRun.setFontFamily("Aptos Display");

        XWPFParagraph topicsTitle = document.createParagraph();
        topicsTitle.setAlignment(ParagraphAlignment.CENTER);

        StringBuilder topics = new StringBuilder();

        for (Topic topic : test.getPrompt().getTopics()) {
            topics.append(topic.getName());

            if (!test.getPrompt().getTopics().get(test.getPrompt().getTopics().size() - 1).equals(topic)) {
                topics.append(", ");
            }
        }

        XWPFRun topicsTitleRun = topicsTitle.createRun();
        topicsTitleRun.setText(topics.toString());
        topicsTitleRun.setFontSize(14);
        topicsTitleRun.setColor("0F4761");
        topicsTitleRun.setFontFamily("Aptos Display");

        XWPFParagraph difficulty = document.createParagraph();
        difficulty.setAlignment(ParagraphAlignment.RIGHT);

        XWPFRun difficultyRun = difficulty.createRun();
        difficultyRun.setText("Obtížnost: " + test.getDifficulty().getName());
        difficultyRun.setFontSize(11);
        difficultyRun.setItalic(true);
        difficultyRun.setFontFamily("Aptos");

        XWPFParagraph timeLimit = document.createParagraph();
        timeLimit.setAlignment(ParagraphAlignment.RIGHT);

        XWPFRun timeLimitRun = timeLimit.createRun();
        timeLimitRun.setText("Časový limit: " + test.getTimeLimitInMinutes() + " minut");
        timeLimitRun.setFontSize(11);
        timeLimitRun.setItalic(true);
        timeLimitRun.setFontFamily("Aptos");

        XWPFParagraph maxPoints = document.createParagraph();
        maxPoints.setAlignment(ParagraphAlignment.RIGHT);

        String maxPointsLine = "Maximalní počet bodů: " + TestManager.calculateMaxPoints(test.getId());
        maxPointsLine = maxPointsLine.replace("\n", "");

        XWPFRun maxPointsRun = maxPoints.createRun();
        maxPointsRun.setText(maxPointsLine);
        maxPointsRun.setFontSize(11);
        maxPointsRun.setItalic(true);
        maxPointsRun.setFontFamily("Aptos");

        // Remove last line from the test content
        content = content.substring(0, content.lastIndexOf("\n"));

        Pattern questionPattern = Pattern.compile("^\\d+\\.\\s+.*");
        boolean inAnswers = false;
        boolean isFirstPage = true;

        for (String line : content.split("\n")) {
            line = line.trim();

            if (TestManager.lineContainsAnyOf(TestManager.requiredSections, line) || line.isEmpty() || line.contains("```")) {
                continue; // Skip unnecessary lines
            }

            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setAlignment(line.startsWith("Body:") ? ParagraphAlignment.RIGHT : ParagraphAlignment.LEFT);

            XWPFRun run = paragraph.createRun();

            if (line.startsWith("Správné odpovědi:")) {
                inAnswers = true;
                line = line.trim();

                run.setFontSize(13);
                run.setColor("0F4761");
                run.setFontFamily("Aptos Display");

                if (!isFirstPage) {
                    paragraph.setPageBreak(true);
                }
            }

            run.setFontSize(11);
            run.setFontFamily("Aptos");
            run.setText(line);

            // Bold questions and answers
            if (questionPattern.matcher(line).matches() || (inAnswers && !line.startsWith("Vysvětlení:") && !line.startsWith("Správné odpovědi:") && !line.isEmpty())) {
                run.setBold(true);
            }

            // Italic poins and explanations
            if (line.startsWith("Body:") || (inAnswers && line.startsWith("Vysvětlení:"))) {
                run.setItalic(true);
            }

            isFirstPage = false;
        }

        return document;
    }
}