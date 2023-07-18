package ru.senya.sampleserv.utils;

import lombok.SneakyThrows;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.senya.sampleserv.models.Model;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;


@SuppressWarnings("ALL")
@Service
public class Utils {

    public static final String PATH_FOLDER = "src/main/resources/static/";
    public static String SERVER_IP;
    public static String SERVER_PORT;
    public static final String SERVER_HOST = "http://192.168.1.110:8082";
    private static final int YOLO_CONST = 2;
    String[] yolo9000command = {
            "src/main/resources/libs/darknet/darknet",
            "detector",
            "test",
//            "src/main/resources/libs/darknet/cfg/imagenet1k.data",
            "src/main/resources/libs/darknet/cfg/combine9k.data",
//            "src/main/resources/libs/darknet/cfg/darknet19.cfg",
            "src/main/resources/libs/darknet/cfg/yolo9000.cfg",
            "src/main/config/yolo9000/yolo9000.weights",
            "-dont_show"
    };
    String[] yolov4command = {
            "src/main/resources/libs/darknet/darknet",
            "detector",
            "test",
//            "src/main/resources/libs/darknet/cfg/imagenet1k.data",
            "src/main/resources/libs/darknet/cfg/coco.data",
//            "src/main/resources/libs/darknet/cfg/darknet19.cfg",
            "src/main/resources/libs/darknet/cfg/yolov4.cfg",
            "src/main/config/yolo/yolov4.weights",
            "-dont_show"
    };

    String[] darknetCommand = {
            "src/main/resources/libs/darknet/darknet",
            "classifier",
            "predict",
            "src/main/resources/libs/darknet/cfg/imagenet1k.data",
//            "src/main/resources/libs/darknet/cfg/coco.data",
            "src/main/resources/libs/darknet/cfg/darknet19.cfg",
//            "src/main/resources/libs/darknet/cfg/yolov4.cfg",
            "src/main/config/darknet19/darknet19.weights",
            "-dont_show"
    };

    public static long COUNT = 0L, c2 = 0L;
    private TaskExecutor taskExecutor;

    private final Process
            process1 = new ProcessBuilder(yolo9000command).start(),
            process2 = new ProcessBuilder(yolo9000command).start(),
            process3 = new ProcessBuilder(yolov4command).start(),
            process4 = new ProcessBuilder(yolov4command).start(),
            process5 = new ProcessBuilder(darknetCommand).start(),
            process6 = new ProcessBuilder(darknetCommand).start();

    private final BufferedReader
            errorReader1 = new BufferedReader(new InputStreamReader(process1.getErrorStream())),
            errorReader2 = new BufferedReader(new InputStreamReader(process2.getErrorStream())),
            errorReader3 = new BufferedReader(new InputStreamReader(process3.getErrorStream())),
            errorReader4 = new BufferedReader(new InputStreamReader(process4.getErrorStream())),
            errorReader5 = new BufferedReader(new InputStreamReader(process5.getErrorStream())),
            errorReader6 = new BufferedReader(new InputStreamReader(process6.getErrorStream()));
    private final BufferedReader
            reader1 = new BufferedReader(new InputStreamReader(process1.getInputStream())),
            reader2 = new BufferedReader(new InputStreamReader(process2.getInputStream())),
            reader3 = new BufferedReader(new InputStreamReader(process3.getInputStream())),
            reader4 = new BufferedReader(new InputStreamReader(process4.getInputStream())),
            reader5 = new BufferedReader(new InputStreamReader(process5.getInputStream())),
            reader6 = new BufferedReader(new InputStreamReader(process6.getInputStream()));
    private final BufferedWriter
            writer1 = new BufferedWriter(new OutputStreamWriter(process1.getOutputStream())),
            writer2 = new BufferedWriter(new OutputStreamWriter(process2.getOutputStream())),
            writer3 = new BufferedWriter(new OutputStreamWriter(process3.getOutputStream())),
            writer4 = new BufferedWriter(new OutputStreamWriter(process4.getOutputStream())),
            writer5 = new BufferedWriter(new OutputStreamWriter(process5.getOutputStream())),
            writer6 = new BufferedWriter(new OutputStreamWriter(process6.getOutputStream()));
    private final Process[] yolo9000processes = {process1, process2};
    private final Process[] yolov4processes = {process3, process4};
    private final Process[] darknetProcesses = {process5, process6};

    private final BufferedReader[] yolo9000readers = {reader1, reader2};
    private final BufferedReader[] yolov4readers = {reader3, reader4};
    private final BufferedReader[] darknetReaders = {reader5, reader6};

    private final BufferedReader[] yolo9000errorReaders = {errorReader1, errorReader2};
    private final BufferedReader[] yolov4errorReaders = {errorReader3, errorReader4};
    private final BufferedReader[] darknetErrorReaders = {errorReader5, errorReader6};

    private final BufferedWriter[] yolo9000writers = {writer1, writer2};
    private final BufferedWriter[] yolov4writers = {writer3, writer4};
    private final BufferedWriter[] darknetWriters = {writer5, writer6};

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("OpenCV version: " + Core.VERSION);
    }


    @Autowired
    public Utils(TaskExecutor taskExecutor) throws IOException {
        this.taskExecutor = taskExecutor;
    }

    public void processImages2(Model model, CountDownLatch latch, String path, String uniqueFilename) {
        taskExecutor.execute(() -> {
            int i = (int) ((COUNT++) % yolo9000processes.length);
            System.out.println("i: " + i + "; Count" + (COUNT - 1));
            model.setHexColor(getColor(path));
            model.setAiPath("ai_" + uniqueFilename);
            model.setColoredPath("colored_" + uniqueFilename);
            Mat image = Imgcodecs.imread(path);

            ArrayList<String> tags = new ArrayList<>();
//            tags.addAll(yolo9000(uniqueFilename, image, path, i));
            tags.addAll(yolov4(uniqueFilename, image, path, i));
            tags.addAll(darknet(uniqueFilename, image, path, i));

            model.setTags(tags);

            latch.countDown();
        });
    }


    @SneakyThrows
    private ArrayList<String> yolo9000(String fileName, Mat image, String path, int i) {
        ArrayList<String> tags = new ArrayList<>();

        Process process = yolo9000processes[i];
        BufferedReader reader = yolo9000readers[i];
        BufferedWriter writer = yolo9000writers[i];
        BufferedReader errorReader = yolo9000errorReaders[i];
        String line;

        if (COUNT <= YOLO_CONST) {
            while (true) {
                line = errorReader.readLine();
                if (line == null) {
                    return new ArrayList<>();
                }
                System.out.println(line);
                if (line.contains("layers")) {
                    writer.write(path);
                    writer.newLine();
                    writer.flush();
                    break;
                }
            }
        } else {
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    return new ArrayList<>();
                }
                System.out.println(line);
                if (line.contains("Image")) {
                    writer.write(path);
                    writer.newLine();
                    writer.flush();
                    break;
                }
            }
        }

//        if (COUNT > YOLO_CONST) {
//            for (int j = 0; j < 5; j++) {
//                line = reader.readLine();
//                if (line == null) {
//                    return new ArrayList<>();
//                }
//                System.out.println(line);
//            }
//        } else {
//            for (int j = 0; j < 5; j++) {
//                line = reader.readLine();
//                if (line == null) {
//                    return new ArrayList<>();
//                }
//                System.out.println(line);
//            }
//        }

        while (true) {
            line = reader.readLine();
            if (line == null) {
                return new ArrayList<>();
            }
            if (line.contains("THE_FUCKING_END")) {
                break;
            }
            System.out.println(line);
            tags.add(line);
        }
        line = line.replace(" THE_FUCKING_END", "");
        tags.add(line);

        return tags;
    }

    @SneakyThrows
    private ArrayList<String> yolov4(String fileName, Mat image, String path, int i) {
        ArrayList<String> tags = new ArrayList<>();

        Process process = yolov4processes[i];
        BufferedReader reader = yolov4readers[i];
        BufferedWriter writer = yolov4writers[i];
        BufferedReader errorReader = yolov4errorReaders[i];

        String line;

        if (COUNT <= YOLO_CONST) {
            while (true) {
                line = errorReader.readLine();
                if (line == null) {
                    return new ArrayList<>();
                }
                System.out.println(line);
                if (line.contains("layers")) {
                    writer.write(path);
                    writer.newLine();
                    writer.flush();
                    break;
                }
            }
        } else {
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    return new ArrayList<>();
                }
                System.out.println(line);
                if (line.contains("Image")) {
                    writer.write(path);
                    writer.newLine();
                    writer.flush();
                    break;
                }
            }
        }

        if (COUNT > YOLO_CONST) {
            for (int j = 0; j < 4; j++) {
                line = reader.readLine();
                if (line == null) {
                    return new ArrayList<>();
                }
                System.out.println(line);
            }
        } else {
            for (int j = 0; j < 14; j++) {
                line = reader.readLine();
                if (line == null) {
                    return new ArrayList<>();
                }
                System.out.println(line);
            }
        }

        while (true) {
            line = reader.readLine();
            if (line == null) {
                return new ArrayList<>();
            }
            if (line.contains("THE_FUCKING_END")) {
                break;
            }
            System.out.println(line);
            tags.add(line);
        }
        line = line.replace(" THE_FUCKING_END", "");
        tags.add(line);

        return tags;
    }

    @SneakyThrows
    private ArrayList<String> darknet(String fileName, Mat image, String path, int i) {
        ArrayList<String> tags = new ArrayList<>();

        Process process = darknetProcesses[i];
        BufferedReader reader = darknetReaders[i];
        BufferedWriter writer = darknetWriters[i];
        BufferedReader errorReader = darknetErrorReaders[i];
        String line;

        while (true) {
            line = reader.readLine();
            if (line == null) {
                return new ArrayList<>();
            }
//            System.out.println(line);
            if (line.contains("Image")) {
                writer.write(path);
                writer.newLine();
                writer.flush();
                break;
            }
        }

        for (int j = 0; j < 2; j++) {
            line = reader.readLine();
            if (line == null) {
                return new ArrayList<>();
            }
//            System.out.println(line);
        }

        while (true) {
            line = reader.readLine();
            if (line == null) {
                return new ArrayList<>();
            }
            if (line.contains("THE_FUCKING_END")) {
                break;
            }
//            System.out.println(line);
            tags.add(line);
        }
        line = line.replace(" THE_FUCKING_END", "");
        tags.add(line);

        return tags;
    }


    private String getColor(String path) {
        Mat image = Imgcodecs.imread(path);
        Scalar meanColor = Core.mean(image);
        return scalarToHex(meanColor);
    }

    private String scalarToHex(Scalar scalar) {
        // Получение среднего цвета в формате BGR
        double blue = scalar.val[0];
        double green = scalar.val[1];
        double red = scalar.val[2];

        return String.format("#%02X%02X%02X", (int) red, (int) green, (int) blue);
    }

    private Scalar hexToScalar(String hexColor) {
        // Извлечение компонентов цвета из HEX-кода
        int red = Integer.parseInt(hexColor.substring(1, 3), 16);
        int green = Integer.parseInt(hexColor.substring(3, 5), 16);
        int blue = Integer.parseInt(hexColor.substring(5, 7), 16);

        return new Scalar(blue, green, red);
    }

    public static void init() {
        // очень важная функция для инициализация static
    }
}