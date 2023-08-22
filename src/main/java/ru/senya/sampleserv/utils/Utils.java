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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.senya.sampleserv.models.Model;
import ru.senya.sampleserv.models.ResponseModel;
import ru.senya.sampleserv.models.TagModel;
import ru.senya.sampleserv.models.TokenModel;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@SuppressWarnings("ALL")
@Service
public class Utils {

    public static final String PATH_FOLDER = "src/main/resources/static/";
    public static String SERVER_IP;
    public static String SERVER_PORT;
    public static final String SERVER_HOST = "http://192.168.1.110:8082";
    private static final int YOLO_CONST = 1;

    private static String yandexToken;
    private static String url = "https://llm.api.cloud.yandex.net/llm/v1alpha/chat";
    private static String x_folder_id = "b1g919216h0usde9431e";
    String[] yolo9000command = {
            "src/main/resources/libs/darknet/darknet",
            "detector",
            "test",
            "src/main/resources/libs/darknet/cfg/combine9k.data",
            "src/main/resources/libs/darknet/cfg/yolo9000.cfg",
            "src/main/config/yolo9000/yolo9000.weights",
            "-dont_show"
    };
    String[] yolov4command = {
            "src/main/resources/libs/darknet/darknet",
            "detector",
            "test",
            "src/main/resources/libs/darknet/cfg/coco.data",
            "src/main/resources/libs/darknet/cfg/yolov4.cfg",
            "src/main/config/yolo/yolov4.weights",
            "-dont_show"
    };

    String[] darknetCommand = {
            "src/main/resources/libs/darknet/darknet",
            "classifier",
            "predict",
            "src/main/resources/libs/darknet/cfg/imagenet1k.data",
            "src/main/resources/libs/darknet/cfg/darknet19.cfg",
            "src/main/config/darknet19/darknet19.weights",
            "-dont_show"
    };

    private ArrayList<String> yolov4labels = convertToList("src/main/resources/libs/darknet/cfg/coco.names");
    private ArrayList<String> yolov4ruLabels = convertToList("src/main/config/yolo/yolov4.names");

    private ArrayList<String> yolo9000labels = convertToList("src/main/resources/libs/darknet/cfg/9k.names");
    private ArrayList<String> yolo9000ruLabels = convertToList("src/main/config/yolo9000/9k.names");

    private ArrayList<String> efNetEnTags = convertToList("src/main/config/efNet/labelsEn.txt");
    private ArrayList<String> efNetRuTags = convertToList("src/main/config/efNet/labelsRu.txt");

    public static long COUNT = 0L, c2 = 0L;
    private TaskExecutor taskExecutor;

    private final Process
            yolo9000process = new ProcessBuilder(yolo9000command).start(),
            yolov4process = new ProcessBuilder(yolov4command).start(),
            darknetProcess = new ProcessBuilder(darknetCommand).start();

    private final BufferedReader
            errorReader9000 = new BufferedReader(new InputStreamReader(yolo9000process.getErrorStream())),
            errorReaderV4 = new BufferedReader(new InputStreamReader(yolov4process.getErrorStream())),
            errorReaderDarknet = new BufferedReader(new InputStreamReader(darknetProcess.getErrorStream()));
    private final BufferedReader
            reader9000 = new BufferedReader(new InputStreamReader(yolo9000process.getInputStream())),
            readerV4 = new BufferedReader(new InputStreamReader(yolov4process.getInputStream())),
            darknetReader = new BufferedReader(new InputStreamReader(darknetProcess.getInputStream()));
    private final BufferedWriter
            writer9000 = new BufferedWriter(new OutputStreamWriter(yolo9000process.getOutputStream())),
            writerV4 = new BufferedWriter(new OutputStreamWriter(yolov4process.getOutputStream())),
            writerDarknet = new BufferedWriter(new OutputStreamWriter(darknetProcess.getOutputStream()));
    private final Process[] yolo9000processes = {yolo9000process};
    private final Process[] yolov4processes = {yolov4process};
    private final Process[] darknetProcesses = {darknetProcess};

    private final BufferedReader[] yolo9000readers = {reader9000};
    private final BufferedReader[] yolov4readers = {readerV4};
    private final BufferedReader[] darknetReaders = {darknetReader};

    private final BufferedReader[] yolo9000errorReaders = {errorReader9000};
    private final BufferedReader[] yolov4errorReaders = {errorReaderV4};
    private final BufferedReader[] darknetErrorReaders = {errorReaderDarknet};

    private final BufferedWriter[] yolo9000writers = {writer9000};
    private final BufferedWriter[] yolov4writers = {writerV4};
    private final BufferedWriter[] darknetWriters = {writerDarknet};

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("OpenCV version: " + Core.VERSION);
    }


    @Autowired
    public Utils(TaskExecutor taskExecutor) throws IOException {
        this.taskExecutor = taskExecutor;
        resetToken();
    }

    public void process(Model model, CountDownLatch latch, String path, String uniqueFilename) {
        taskExecutor.execute(() -> {
            int i = (int) ((COUNT++) % yolo9000processes.length);

            // Получение и установка цвета изображения
            String hexColor = getColor(path);
            model.setHexColor(hexColor);
            model.setIntColor(Integer.parseInt(hexColor.replace("#", ""), 16));
            model.setScalarColor(hexToScalar(hexColor).val);

            // Загрузка изображения и установка его размеров
            Mat image = Imgcodecs.imread(path);
            model.setImageWidth(image.width());
            model.setImageHeight(image.height());

            // Создание списков для тегов на английском и русском языках
            ArrayList<String> enTags = new ArrayList<>();
            ArrayList<String> ruTags = new ArrayList<>();

            // Получение тегов с помощью различных методов
            ArrayList<String> efNetTags = null;
            try {
                efNetTags = getEfNetTags(path);
            } catch (ResourceAccessException e) {
                latch.countDown();
                return;
            }
            ArrayList<String> v4tags = yolov4(uniqueFilename, image, path, i);
            ArrayList<String> v9000tags = yolo9000(uniqueFilename, image, path, i);

            // Преобразование и добавление тегов на русском языке
            for (String tag : efNetTags) {
                int index = efNetEnTags.indexOf(tag.split(":")[0].trim());
                if (index >= 0) {
                    ruTags.add(efNetRuTags.get(index) + " : " + Double.parseDouble(tag.split(":")[1].trim()));
                }
            }
            for (String tag : v4tags) {
                int index = yolov4labels.indexOf(tag.split(":")[0].trim());
                if (index >= 0) {
                    String ruTag = yolov4ruLabels.get(index) + " : " + Double.parseDouble(tag.split(":")[1]);
                    ruTags.add(ruTag);
                }
            }
            for (String tag : v9000tags) {
                int index = yolo9000labels.indexOf(tag.split(":")[0].trim());
                if (index >= 0) {
                    ruTags.add(yolo9000ruLabels.get(index) + " : " + Double.parseDouble(tag.split(":")[1]));
                }
            }

            // Объединение тегов на английском
            enTags.addAll(efNetTags);
            enTags.addAll(v4tags);
            enTags.addAll(v9000tags);

            // Установка тегов в модель
            model.setEnTags(cleanTags(enTags));
            model.setRuTags(cleanTags(ruTags));

            String generatedText = null;

            try {
                generatedText = getGeneratedText(model.toJson());
            } catch (HttpClientErrorException e) {
                resetToken();
                latch.countDown();
                return;
            }

            model.setText(generatedText);


            // Уменьшение счетчика CountDownLatch
            model.setSuccess(true);
            latch.countDown();
        });
    }

    private void resetToken() {
        String url = "https://iam.api.cloud.yandex.net/iam/v1/tokens";
        RestTemplate restTemplate = new RestTemplate();
        Gson gson = new Gson();

        String body = "{\n" +
                "    \"yandexPassportOauthToken\": \"y0_AgAAAAArbTLAAATuwQAAAADpLndikDIdjcsCRBafZ_UeQXn2eR5-gRk\"\n" +
                "}";
        System.out.println(body);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        TokenModel model = gson.fromJson(response.getBody(), TokenModel.class);

        System.out.println(model.getIamToken());
        yandexToken = model.getIamToken();
    }

    @SneakyThrows
    private ArrayList<String> yolo9000(String fileName, Mat image, String path, int i) {
        ArrayList<String> tags = new ArrayList<>();

        Process process = yolo9000processes[i];
        BufferedReader reader = yolo9000readers[i];
        BufferedWriter writer = yolo9000writers[i];
        BufferedReader errorReader = yolo9000errorReaders[i];
        String line;

        // Определение начальной строки для ввода пути к изображению
        String enterImagePathLine = COUNT <= YOLO_CONST ? "layers" : "Enter Image Path";

        // Ожидание строки для ввода пути
        while (true) {
            line = COUNT <= YOLO_CONST ? errorReader.readLine() : reader.readLine();
            if (line == null) {
                return new ArrayList<>();
            }
            if (line.contains(enterImagePathLine)) {
                writer.write(path);
                writer.newLine();
                writer.flush();
                break;
            }
        }

        // Пропуск необходимого количества строк
        int linesToSkip = COUNT <= YOLO_CONST ? 9 : 2;
        for (int j = 0; j < linesToSkip; j++) {
            line = reader.readLine();
            if (line == null) {
                return new ArrayList<>();
            }
            // Возможно, здесь стоит добавить вывод строк в лог
        }

        // Считывание и обработка результатов
        while (true) {
            line = reader.readLine();
            if (line == null) {
                return new ArrayList<>();
            }
            if (line.contains("THE_FUCKING_END")) {
                if (line.contains("NO_DATA")) return new ArrayList<>();
                break;
            }

            // Обработка строки с тегом
            try {
                String[] split = line.split(":");
                String object = split[0].trim();
                double probability = Integer.parseInt(split[1].trim().substring(0, 2)) / 100.0;
                line = object + ": " + probability;
            } catch (Exception e) {
                // Обработка ошибки, если парсинг не удался
                // Здесь возможно стоит добавить вывод строки в лог
            }

            tags.add(line);
        }

        // Удаление метки "THE_FUCKING_END" и добавление в список
        line = line.replace(" THE_FUCKING_END", "");
        String[] split = line.split(":");
        String object = split[0].trim();
        double probability = Integer.parseInt(split[1].trim().substring(0, 2)) / 100.0;
        line = object + ": " + probability;
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

        // Определение начальной строки для ввода пути к изображению
        String enterImagePathLine = COUNT <= YOLO_CONST ? "layers" : "Image";

        // Ожидание строки для ввода пути
        while (true) {
            line = COUNT <= YOLO_CONST ? errorReader.readLine() : reader.readLine();
            if (line == null) {
                return new ArrayList<>();
            }
            if (line.contains(enterImagePathLine)) {
                writer.write(path);
                writer.newLine();
                writer.flush();
                break;
            }
        }

        // Пропуск необходимого количества строк
        int linesToSkip = COUNT <= YOLO_CONST ? 14 : 4;
        for (int j = 0; j < linesToSkip; j++) {
            line = reader.readLine();
            if (line == null) {
                return new ArrayList<>();
            }
            // Возможно, здесь стоит добавить вывод строк в лог
        }

        // Считывание и обработка результатов
        while (true) {
            line = reader.readLine();
            if (line == null) {
                return new ArrayList<>();
            }
            if (line.contains("THE_FUCKING_END")) {
                if (line.contains("NO_DATA")) return new ArrayList<>();
                break;
            }

            // Обработка строки с тегом
            try {
                String[] split = line.split(":");
                String object = split[0].trim();
                double probability = Integer.parseInt(split[1].trim().substring(0, 2)) / 100.0;
                line = object + ": " + probability;
            } catch (Exception e) {
                // Обработка ошибки, если парсинг не удался
                return new ArrayList<>();
            }

            tags.add(line);
        }

        // Удаление метки "THE_FUCKING_END" и добавление в список
        line = line.replace(" THE_FUCKING_END", "");
        String[] split = line.split(":");
        String object = split[0].trim();
        double probability = Integer.parseInt(split[1].trim().substring(0, 2)) / 100.0;
        line = object + ": " + probability;

        tags.add(line);

        return tags;
    }

    public ArrayList<String> getEfNetTags(String path) {
        // Замените на URL сервера, к которому вы хотите отправить запрос
        String url = "http://localhost:7000/getTags";

        // Создание заголовков с типом контента application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Создание тела запроса как MultiValueMap, содержащий путь к файлу
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("path", "/home/senya/IdeaProjects/sampleServ/" + path);

        // Создание HttpEntity с заголовками и телом
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // Создание объекта RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Отправка POST-запроса
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, String.class);

        // Получение тела ответа
        String response = responseEntity.getBody();

        // Парсинг JSON-ответа с использованием Gson
        Gson gson = new Gson();
        TagModel tagModel = gson.fromJson(response, TagModel.class);

        return tagModel.getTags();
    }

    private ArrayList<String> cleanTags(ArrayList<String> tags) {
        // Создание HashMap для хранения тегов и вероятностей
        HashMap<String, Double> tagMap = new HashMap<>();

        // Обработка каждого тега в списке
        for (String tag : tags) {
            String[] split = tag.split(":");
            String object = split[0].trim();
            double probability = Double.parseDouble(split[1].trim());

            // Обновление вероятности, если объект уже существует
            tagMap.putIfAbsent(object, 0.0);
            tagMap.computeIfPresent(object, (key, value) -> Math.max(value, probability));
        }

        // Фильтрация и сортировка тегов по вероятности
        ArrayList<String> filteredTags = tagMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.05)
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.toCollection(ArrayList::new));

        // Извлечение чистых тегов (без вероятности)
        ArrayList<String> cleanTags = filteredTags.stream()
                .map(tag -> tag.split(":")[0])
                .collect(Collectors.toCollection(ArrayList::new));

        return cleanTags;
    }

    private String getGeneratedText(String text) {
        RestTemplate template = new RestTemplate();
        Gson gson = new Gson();


        String body = "{\n" +
                "  \"model\": \"general\",\n" +
                "  \"generationOptions\": {\n" +
                "    \"partialResults\": false,\n" +
                "    \"temperature\"" + ": 0.28," +
                "    \"maxTokens\": 7500\n" +
                "  },\n" +
                "  \"instructionText\": \"" + "ты пишешь текст описание фотографии для фотостока. " + text + "\"" +
                "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-folder-id", "b1g919216h0usde9431e");
        headers.set("Authorization", "Bearer " + yandexToken);


        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = template.postForEntity(url, request, String.class);
        ResponseModel model = gson.fromJson(response.getBody(), ResponseModel.class);

        return model.getResult().getMessage().getText();
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

    private static ArrayList<String> convertToList(String fileName) {
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

    public static void init() {
        // очень важная функция для инициализация static
    }
}