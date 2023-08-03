package ru.senya.sampleserv.utils;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.senya.sampleserv.models.Model;
import ru.senya.sampleserv.models.TagModel;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;


@SuppressWarnings("ALL")
@Service
public class Utils {

    public static final String PATH_FOLDER = "src/main/resources/static/";
    public static String SERVER_IP;
    public static String SERVER_PORT;
    public static final String SERVER_HOST = "http://192.168.1.110:8082";
    private static final int YOLO_CONST = 1;
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

    ArrayList<String> yolov4labels = convertToList("src/main/resources/libs/darknet/cfg/coco.names");
    ArrayList<String> yolov4ruLabels = convertToList("src/main/config/yolo/yolov4.names");

    ArrayList<String> yolo9000labels = convertToList("src/main/resources/libs/darknet/cfg/9k.names");
    ArrayList<String> yolo9000ruLabels = convertToList("src/main/config/yolo9000/9k.names");

    ArrayList<String> efNetEnTags = convertToList("src/main/config/efNet/labelsEn.txt");
    ArrayList<String> efNetRuTags = convertToList("src/main/config/efNet/labelsRu.txt");

    public static long COUNT = 0L, c2 = 0L;
    private TaskExecutor taskExecutor;

    private final Process
            process1 = new ProcessBuilder(yolo9000command).start(),
            process3 = new ProcessBuilder(yolov4command).start(),
            process5 = new ProcessBuilder(darknetCommand).start();

    private final BufferedReader
            errorReader1 = new BufferedReader(new InputStreamReader(process1.getErrorStream())),
            errorReader3 = new BufferedReader(new InputStreamReader(process3.getErrorStream())),
            errorReader5 = new BufferedReader(new InputStreamReader(process5.getErrorStream()));
    private final BufferedReader
            reader1 = new BufferedReader(new InputStreamReader(process1.getInputStream())),
            reader3 = new BufferedReader(new InputStreamReader(process3.getInputStream())),
            reader5 = new BufferedReader(new InputStreamReader(process5.getInputStream()));
    private final BufferedWriter
            writer1 = new BufferedWriter(new OutputStreamWriter(process1.getOutputStream())),
            writer3 = new BufferedWriter(new OutputStreamWriter(process3.getOutputStream())),
            writer5 = new BufferedWriter(new OutputStreamWriter(process5.getOutputStream()));
    private final Process[] yolo9000processes = {process1};
    private final Process[] yolov4processes = {process3};
    private final Process[] darknetProcesses = {process5};

    private final BufferedReader[] yolo9000readers = {reader1};
    private final BufferedReader[] yolov4readers = {reader3};
    private final BufferedReader[] darknetReaders = {reader5};

    private final BufferedReader[] yolo9000errorReaders = {errorReader1};
    private final BufferedReader[] yolov4errorReaders = {errorReader3};
    private final BufferedReader[] darknetErrorReaders = {errorReader5};

    private final BufferedWriter[] yolo9000writers = {writer1};
    private final BufferedWriter[] yolov4writers = {writer3};
    private final BufferedWriter[] darknetWriters = {writer5};

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("OpenCV version: " + Core.VERSION);
    }


    @Autowired
    public Utils(TaskExecutor taskExecutor) throws IOException {
        this.taskExecutor = taskExecutor;
    }

    ArrayList<String> getEfNetTags(String path) {
        // Replace with the URL of the server you want to send the request to
        String url = "http://localhost:7000/getTags";

        // Create headers with content type as multipart/form-data
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request body as a MultiValueMap containing the image file
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("file", new FileSystemResource(new File(path)));
        body.add("path", "/home/senya/IdeaProjects/sampleServ/" + path);

        // Create the HttpEntity with headers and body
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // Create the RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Send the POST request
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        // Get the response body
        String response = responseEntity.getBody();
//
        Gson gson = new Gson();

        return gson.fromJson(response, TagModel.class).getTags();
    }

    public void processImages2(Model model, CountDownLatch latch, String path, String uniqueFilename) {
        taskExecutor.execute(() -> {
            int i = (int) ((COUNT++) % yolo9000processes.length);
            model.setHexColor(getColor(path));
            model.setIntColor(Integer.parseInt(model.getHexColor().replace("#", ""), 16));
            model.setScalarColor(hexToScalar(model.getHexColor()).val);
            Mat image = Imgcodecs.imread(path);

            model.setImageWidth(image.width());
            model.setImageHeight(image.height());


            HashMap<String, Double> tagMap = new HashMap<>();
            ExecutorService executorService = Executors.newFixedThreadPool(3);
            List<Callable<ArrayList<String>>> callableList = new ArrayList<>();

            ArrayList<String> enTags = new ArrayList<>();
            ArrayList<String> ruTags = new ArrayList<>();

            ArrayList<String> efNetTags = getEfNetTags(path);
            ArrayList<String> v4tags = yolov4(uniqueFilename, image, path, i);
            ArrayList<String> v9000tags = yolo9000(uniqueFilename, image, path, i);
//            ArrayList<String> darknetTags = darknet(uniqueFilename, image, path, i);

            for (String tag : efNetTags) {
                ruTags.add(efNetRuTags.get(efNetEnTags.indexOf(tag.split(":")[0].trim())) + " : " + Double.parseDouble((String) tag.split(":")[1].trim()));
            }
            for (String tag : v4tags) {
                String ruTag = yolov4ruLabels.get(yolov4labels.indexOf(tag.split(":")[0].trim())) + " : " + Double.parseDouble((String) tag.split(":")[1]);
                ruTags.add(ruTag);
            }
            for (String tag : v9000tags) {
                ruTags.add(yolo9000ruLabels.get(yolo9000labels.indexOf(tag.split(":")[0].trim())) + " : " + Double.parseDouble((String) tag.split(":")[1].trim()));
            }
//            for (String tag: darknetTags){
//                ruTags.add(yolov4ruLabels.get(yolov4labels.indexOf(tag.split(":")[0].trim()))+" : "+ Double.parseDouble((String) tag.split(":")[1].trim().subSequence(0, 2)) / 100f);
//            }


            enTags.addAll(efNetTags);
            enTags.addAll(v4tags);
            enTags.addAll(v9000tags);
//            enTags.addAll(darknetTags);


            model.setEnTags(cleanTags(enTags));

            model.setRuTags(cleanTags(ruTags));


            latch.countDown();
        });
    }

    ArrayList<String> cleanTags(ArrayList<String> tags) {
        HashMap<String, Double> tagMap = new HashMap<>();
        for (String tag : tags) {
            String[] split = tag.split(":");
            String object = split[0].trim();
            double probability = Double.parseDouble(split[1].trim());

            if (tagMap.containsKey(object)) {
                double currentProbability = tagMap.get(object);
                if (probability > currentProbability) {
                    tagMap.put(object, probability);
                }
            } else {
                tagMap.put(object, probability);
            }
        }

        ArrayList<String> filteredTags = new ArrayList<>();
        for (Map.Entry<String, Double> entry : tagMap.entrySet()) {
            String object = entry.getKey();
            double probability = entry.getValue();
            if (probability > 0.05) {
                String tag = object + ": " + probability;
                filteredTags.add(tag);
            }
        }

        filteredTags.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Double value1 = Double.valueOf(o1.split(":")[1].trim());
                Double value2 = Double.valueOf(o2.split(":")[1].trim());

                if (value1.equals(value2)) {
                    return 0;
                } else if (value1 > value2) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        ArrayList<String> tags2 = new ArrayList<>();

        for (String s : filteredTags) {
            tags2.add(s.split(":")[0]);
        }
        return tags2;
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
//                System.out.println(line);
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
//                System.out.println(line);
                if (line.contains("Enter Image Path")) {
                    writer.write(path);
                    writer.newLine();
                    writer.flush();
                    break;
                }
            }
        }

        if (COUNT > YOLO_CONST) {
            for (int j = 0; j < 2; j++) {
                line = reader.readLine();
                if (line == null) {
                    return new ArrayList<>();
                }
//                if (line.contains("Image"))
//                    System.out.println(line);
//                else
//                    System.out.println(line);
            }
        } else {
            for (int j = 0; j < 9; j++) {
                line = reader.readLine();
                if (line == null) {
                    return new ArrayList<>();
                }
//                if (line.contains("Image"))
//                    System.out.println(line);
//                else
//                    System.out.println(line);
            }
        }

        while (true) {
            line = reader.readLine();
            if (line == null) {
                return new ArrayList<>();
            }
            if (line.contains("THE_FUCKING_END")) {
                if (line.contains("NO_DATA")) return new ArrayList<>();
                break;
            }
//            System.out.println(line);
            try {
                line = line.split(":")[0] + ": " + Integer.parseInt((String) line.split(":")[1].trim().subSequence(0, 2)) / 100f;
            } catch (Exception e) {
                // System.out.println(line);
            }

            tags.add(line);
        }
        line = line.split(":")[0] + ": " + Integer.parseInt((String) line.split(":")[1].trim().subSequence(0, 2)) / 100f;
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
                //System.out.println(line);
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
                //System.out.println(line);
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
                //System.out.println(line);
            }
        } else {
            for (int j = 0; j < 14; j++) {
                line = reader.readLine();
                if (line == null) {
                    return new ArrayList<>();
                }
                //System.out.println(line);
            }
        }

        while (true) {
            line = reader.readLine();
            if (line == null) {
                return new ArrayList<>();
            }
            if (line.contains("THE_FUCKING_END")) {
                if (line.contains("NO_DATA")) return new ArrayList<>();
                break;
            }
//            System.out.println(line);
            try {
                line = line.split(":")[0] + ": " + Integer.parseInt((String) line.split(":")[1].trim().subSequence(0, 2)) / 100f;
            } catch (Exception e) {
                return new ArrayList<>();
            }

            tags.add(line);
        }
        line = line.replace(" THE_FUCKING_END", "");
        line = line.split(":")[0] + ": " + Integer.parseInt((String) line.split(":")[1].trim().subSequence(0, 2)) / 100f;

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
                if (line.contains("NO_DATA")) return new ArrayList<>();
                break;
            }
//            System.out.println(line);
//            line = line.split(":")[0] + "_darknet" + ": " + Integer.parseInt((String) line.split(":")[1].trim().subSequence(0, 2)) / 100f;
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

    public static ArrayList<String> convertToList(String fileName) {
        ArrayList<String> list = new ArrayList<>();
        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                list.add(line);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void init() {
        // очень важная функция для инициализация static
    }
}