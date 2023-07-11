package ru.senya.sampleserv.utils;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import ru.senya.sampleserv.models.Model;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressWarnings("ALL")
@Service
public class Utils {

    public static final String PATH_FOLDER = "src/main/resources/static/";
    public static String SERVER_IP;
    public static String SERVER_PORT;
    public static final String SERVER_HOST = "http://192.168.1.110:8082";
    private final String path = "src/main/config/yolo/yolov4.names", cfgPath = "src/main/config/yolo/yolov4.cfg", weightsPath = "src/main/config/yolo/yolov4.weights";
    //    private final String path = "src/main/config/darknet19/imagenet.shortnames.list", cfgPath = "src/main/config/darknet19/darknet19.cfg", weightsPath = "src/main/config/darknet19/darknet19.weights";
//    private final String path = "src/main/config/yolo9000/9k.names", cfgPath = "src/main/config/yolo9000/yolo9000.cfg", weightsPath = "src/main/config/yolo9000/yolo9000.weights";
//    private final String carPath = "src/main/config/cars_ai/cars.names", carCfgPath = "src/main/config/cars_ai/cars.cfg", carWeightsPath = "src/main/config/cars_ai/cars.weights";
    private final Net
            network1,
            network2,
            network3,
            network4,
            network5,
            network6,
            network7,
            network8;

    private final ReentrantLock lock1 = new ReentrantLock(), lock2 = new ReentrantLock(), lock3 = new ReentrantLock(), lock4 = new ReentrantLock(), lock5 = new ReentrantLock(), lock6 = new ReentrantLock(), lock7 = new ReentrantLock(), lock8 = new ReentrantLock(), lock9 = new ReentrantLock(), lock10 = new ReentrantLock(), lock11 = new ReentrantLock(), lock12 = new ReentrantLock(), lock13 = new ReentrantLock(), lock14 = new ReentrantLock(), lock15 = new ReentrantLock(), lock16 = new ReentrantLock(), lock17 = new ReentrantLock(), lock18 = new ReentrantLock(), lock19 = new ReentrantLock(), lock20 = new ReentrantLock(), lock21 = new ReentrantLock(), lock22 = new ReentrantLock(), lock23 = new ReentrantLock(), lock24 = new ReentrantLock();
    private final List<String> labels;
    private final int amountOfClasses;
    //    amountOfOutputLayers;
//    private final MatOfInt outputLayersIndexes;
    private final Net[] nets;
    public static long COUNT = 0L, c2 = 0L;
    //    private ExecutorService taskExecutor = Executors.newFixedThreadPool(8);
    private TaskExecutor taskExecutor;

    @Autowired
    public Utils(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;

        network1 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network2 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network3 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network4 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network5 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network6 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network7 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network8 = Dnn.readNetFromDarknet(cfgPath, weightsPath);


        nets = new Net[]{network1, network2, network3, network4, network5, network6, network7, network8};


        // Инициализация работы на видеокарте
        if (Core.getBuildInformation().contains("CUDA")) {
            for (Net net : nets) {
                net.setPreferableBackend(Dnn.DNN_BACKEND_CUDA);
                net.setPreferableTarget(Dnn.DNN_TARGET_CUDA_FP16);
            }
        } else {
            for (Net net : nets) {
                net.setPreferableBackend(Dnn.DNN_BACKEND_DEFAULT);
                net.setPreferableTarget(Dnn.DNN_TARGET_CPU);
            }
        }

        // Классы
        labels = labels(path);
        //carLabels = labels(carPath);
        amountOfClasses = labels.size();

        // Извлекаем наименования выходных слоев.
//        outputLayersNames = getOutputLayerNames(nets[0]);

        // Извлекаем индексы выходных слоев.
//        outputLayersIndexes = nets[0].getUnconnectedOutLayers();
//        amountOfOutputLayers = outputLayersIndexes.toArray().length;
    }


    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.print("OpenCV version: " + Core.VERSION);
//        cleanStatic();
    }


    public void processImages(Model model, CountDownLatch latch, String path, String uniqueFilename) {
        int i = (int) (COUNT++ % (nets.length));
        taskExecutor.execute(() -> {
            processImage(model, i, uniqueFilename);
            latch.countDown();
        });
    }

    public void processImages2(Model model, CountDownLatch latch, String path, String uniqueFilename) {
        int i = (int) (COUNT++ % (nets.length));
        taskExecutor.execute(() -> {
            model.setHexColor(getColor(path));
            model.setAiPath("ai_" + uniqueFilename);
            model.setColoredPath("colored_" + uniqueFilename);
            model.setTags(processWithCmd(path, model, uniqueFilename));
            latch.countDown();
        });
    }

    @Async
    private void processImage(Model model, int index, String filename) {
        String path = PATH_FOLDER + filename;
        model.setHexColor(getColor(path));
        model.setTags(processWithYolo(path, model, index));
        model.setAiPath("ai_" + filename);
        model.setColoredPath("colored_" + filename);
    }

    @SuppressWarnings("UnusedAssignment")
    private String[] processWithYolo(String pathToImage, Model model, int index) {
        // Инициализируем переменные.
        Mat frame, frameResized = new Mat(), additionalFrame;
        MatOfInt indices = new MatOfInt();
        float minProbability = 0.3f, threshold = 0.3f;
        int height, width;
        String color = model.getHexColor();
        Scalar contrastingColor = getContrastingColor(hexToScalar(getContrastingHex(color)));
        String aiPath, coloredPath, fileName;
        List<String> tags = new LinkedList<>();
        Net net = nets[index];


        fileName = pathToImage.split("/")[4];

        aiPath = "ai_" + fileName;
        coloredPath = "colored_" + fileName;

        // Читаем изображение
        frame = Imgcodecs.imread(pathToImage);
        additionalFrame = frame.clone();

        height = frame.height();
        width = frame.width();

        // Изменяем размер кадра для уменьшения нагрузки на нейронную сеть.
        // Изменяем размер кадра для уменьшения нагрузки на нейронную сеть.
        int resizedHeight = 32 * (width / 32);
        int resizedWidth = 32 * (height / 32);
        int k = 64;

        while (resizedHeight * resizedWidth > 500_000) {
            resizedWidth = 32 * (resizedWidth / k);
            resizedHeight = 32 * (resizedHeight / k);
        }

        Imgproc.resize(frame, frameResized, new Size(resizedWidth, resizedHeight), 0, 0, Imgproc.INTER_LINEAR);

        // Подаём blob на вход нейронной сети.
        Mat blob = Dnn.blobFromImage(frameResized, 1 / 255.0);
        net.setInput(blob);

        // Извлекаем данные с выходных слоев нейронной сети.
        List<Mat> outputFromNetwork = new ArrayList<>();
        List<String> outputLayersNames = new ArrayList<>();

        // Извлекаем наименования выходных слоев.
        outputLayersNames = getOutputLayerNames(net);

        net.forward(outputFromNetwork, outputLayersNames);

        // Обнаруживаем объекты на изображении.
        List<Integer> classIndexes = new ArrayList<>();

        List<Float> confidencesList = new ArrayList<>();
        MatOfFloat confidences = new MatOfFloat();

        List<Rect2d> boundingBoxesList = new ArrayList<>();

        MatOfRect2d boundingBoxes = new MatOfRect2d();

        // Проходим через все предсказания из выходных слоёв по очереди.
        // В цикле проходим через слои:
        outputFromNetwork.parallelStream().forEach(output -> {
            for (int i = 0; i < output.rows(); i++) {
                Mat scores = output.row(i).colRange(5, output.cols());
                Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                Point classPoint = mm.maxLoc;
                double confidence = mm.maxVal;

                // Фильтруем предсказания по порогу уверенности.
                if (confidence > minProbability) {
                    int centerX = (int) (output.row(i).get(0, 0)[0] * width);
                    int centerY = (int) (output.row(i).get(0, 1)[0] * height);
                    int boxWidth = (int) (output.row(i).get(0, 2)[0] * width);
                    int boxHeight = (int) (output.row(i).get(0, 3)[0] * height);
                    int left = centerX - boxWidth / 2;
                    int top = centerY - boxHeight / 2;

                    classIndexes.add((int) classPoint.x);
                    confidencesList.add((float) confidence);
                    boundingBoxesList.add(new Rect2d(left, top, boxWidth, boxHeight));
                }

            }
        });

        // Применяем алгоритм подавления немаксимумов.
        boundingBoxes.fromList(boundingBoxesList);
        confidences.fromList(confidencesList);
        Dnn.NMSBoxes(boundingBoxes, confidences, minProbability, threshold, indices);
        Rect newRect = null;

        // Если алгоритм выявил ограничительные рамки,
        if (indices.size().height > 0) {
            List<Integer> indicesList = indices.toList();
            List<Rect2d> boundingList = boundingBoxes.toList();

            // то наносим выявленные рамки на изображения.
            for (int i = 0; i < indicesList.size(); i++) {

                // Ограничительная рамка
                Rect rect = new Rect(
                        Math.abs((int) boundingList.get(indicesList.get(i)).x), // не знаю почему, но иногда координата может быть отрицательной, и тогда код ломается
                        Math.abs((int) boundingList.get(indicesList.get(i)).y),
                        (int) boundingList.get(indicesList.get(i)).width,
                        (int) boundingList.get(indicesList.get(i)).height
                );
                rect = checkRect(rect, frame);

                // Извлекаем индекс выявленгого класса (объекта на изображении).
                int classIndex = classIndexes.get(indices.toList().get(i));

                // Форматируем строку для нанесения на изображение:
                // Выявленный клас: вероятность
                String label = labels.get(classIndex) + " " + String.format("%.2f", confidences.toList().get(i));

                // Инициализируем точку для нанесения текста.
                Point textPoint = new Point(
                        (int) boundingList.get(indices.toList().get(i)).x,
                        (int) boundingList.get(indices.toList().get(i)).y - 10
                );


                label += " " + getTagColor(frame.submat(getSmallRect(rect, 0.4f)));

//                if (label.contains("car")) {
//                    Mat croppedFrame = new Mat(additionalFrame, rect);
//                    label = label.replace("car", getCarBrand(car_nets[index], croppedFrame, color));
//                }

                // Наносим ограничительную рамку.
                Imgproc.rectangle(frame, rect, contrastingColor, 2);

                // Наносим текст на изображение.
                Imgproc.putText(frame, label, textPoint, Imgproc.FONT_HERSHEY_COMPLEX, 1.2, contrastingColor, 2);

                tags.add(label);
            }
        }

        Imgcodecs.imwrite(PATH_FOLDER + aiPath, frame);

        frame.setTo(hexToScalar(color));
        Imgcodecs.imwrite(PATH_FOLDER + "colored_" + fileName, frame);

        // Освобождение ресурсов для объектов Mat
        frame.release();
        frameResized.release();
        additionalFrame.release();
        indices.release();
        blob.release();

        System.gc();

        return tags.toArray(new String[tags.size()]);
    }

    private String[] processWithCmd(String path, Model model, String filename) {
        String darknetPath = "/home/senya/projects/yolo_train/darknet/darknet";

//        String configPath = "src/main/config/yolo/yolov4.cfg";
//        String weightsPath = "src/main/config/yolo/yolov4.weights";
        String configPath = "src/main/config/yolo9000/yolo9000.cfg";
        String weightsPath = "src/main/config/yolo9000/yolo9000.weights";

        Scalar contrastingColor = getContrastingColor(hexToScalar(getContrastingHex(model.getHexColor())));


        String[] command = {
                darknetPath,
                "detector",
                "test",
                "/home/senya/projects/yolo_train/darknet/cfg/combine9k.data",
//                "src/main/config/yolo/coco.data",
                configPath,
                weightsPath,
                "-dont_show",
                "-ext_output",
                path
        };

        ArrayList<String> labels = new ArrayList<>();

        try {
            Process process = new ProcessBuilder(command).start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            String line;
            while (true) {
                Mat image = Imgcodecs.imread(path);
                line = reader.readLine();
                if (line == null) break;
                if (line.contains("%")) {
                    line = line.replaceAll("\\s", "");
                    String[] strings = getPattern(line);
                    double confidence = Integer.parseInt(strings[1]) / 100.0;
                    if (confidence > 0.5) {
                        String tag = strings[0];
                        labels.add(tag + ": " + confidence);

                        int x = Integer.parseInt(strings[2]);
                        int y = Integer.parseInt(strings[3]);
                        int width = Integer.parseInt(strings[4]);
                        int height = Integer.parseInt(strings[5]);

//                         Ограничительная рамка
                        Rect rect = new Rect(
                                Math.abs(x), // не знаю почему, но иногда координата может быть отрицательной, и тогда код ломается
                                Math.abs(y),
                                (int) width,
                                (int) height
                        );


                        Imgproc.rectangle(image, rect, contrastingColor);

                    }
                }
                Imgcodecs.imwrite(PATH_FOLDER + "ai_" + filename, image);
            }

            return labels.toArray(new String[labels.size()]);
//            int exitCode = process.waitFor();
//            System.out.println("Darknet process exited with code: " + exitCode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getCarBrand(Net net, Mat frame, String color) {

        // Инициализируем переменные.
        Mat frameResized = new Mat(), gray;
        float minProbability = 0.5f, threshold = 0.3f;
        int height, width, k = 64;
        int resizedHeight, resizedWidth;
        MatOfInt indices = new MatOfInt();
        String tag = new String();

        height = frame.height();
        width = frame.width();


        // Изменяем размер кадра для уменьшения нагрузки на нейронную сеть.
        Imgproc.resize(frame, frameResized, new Size(32 * (width / 32), 32 * (height / 32)));

        resizedHeight = frameResized.height();
        resizedWidth = frameResized.width();

        while (resizedHeight * resizedWidth > 105_000) {
            resizedWidth = 32 * (resizedWidth / k);
            resizedHeight = 32 * (resizedHeight / k);
        }

        Imgproc.resize(frame, frameResized, new Size(resizedWidth, resizedHeight));

        // Подаём blob на вход нейронной сети.
        net.setInput(Dnn.blobFromImage(frameResized, 1 / 255.0));

        // Извлекаем данные с выходных слоев нейронной сети.
        List<Mat> outputFromNetwork = new ArrayList<>();
        List<String> outputLayersNames = new ArrayList<>();
        net.forward(outputFromNetwork, outputLayersNames);

        // Обнаруживаем объекты на изображении.
        List<Integer> classIndexes = new ArrayList<>();

        List<Float> confidencesList = new ArrayList<>();
        MatOfFloat confidences = new MatOfFloat();

        List<Rect2d> boundingBoxesList = new ArrayList<>();
        MatOfRect2d boundingBoxes = new MatOfRect2d();

        // Проходим через все предсказания из выходных слоёв по очереди.
        // В цикле проходим через слои:
        for (Mat output : outputFromNetwork) {
            // Проходимся по всем предсказаниям.
            for (int i = 0; i < output.rows(); i++) {
                Mat scores = output.row(i).colRange(5, output.cols());
                Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                Point classPoint = mm.maxLoc;
                double confidence = mm.maxVal;

                // Фильтруем предсказания по порогу уверенности.
                if (confidence > minProbability) {
                    int centerX = (int) (output.row(i).get(0, 0)[0] * width);
                    int centerY = (int) (output.row(i).get(0, 1)[0] * height);
                    int boxWidth = (int) (output.row(i).get(0, 2)[0] * width);
                    int boxHeight = (int) (output.row(i).get(0, 3)[0] * height);
                    int left = centerX - boxWidth / 2;
                    int top = centerY - boxHeight / 2;

                    classIndexes.add((int) classPoint.x);
                    confidencesList.add((float) confidence);
                    boundingBoxesList.add(new Rect2d(left, top, boxWidth, boxHeight));
                }
            }
        }

        // Применяем алгоритм подавления немаксимумов.
        boundingBoxes.fromList(boundingBoxesList);
        confidences.fromList(confidencesList);
        Dnn.NMSBoxes(boundingBoxes, confidences, minProbability, threshold, indices);
        Rect newRect = null;

        // Если алгоритм выявил ограничительные рамки,
        if (indices.size().height > 0) {
            List<Integer> indicesList = indices.toList();
            int classIndex = classIndexes.get(indices.toList().get(0));
            //String label = carLabels.get(classIndex);
            //tag = label;
        }

        return tag.isEmpty() ? "car" : tag;
    }

    private Rect getSmallRect(Rect rect, double scaleFactor) {
        int newWidth = (int) (rect.width * scaleFactor);
        int newHeight = (int) (rect.height * scaleFactor);

        // Центрирование новой рамки относительно исходной
        int newX = rect.x + (rect.width - newWidth) / 2;
        int newY = rect.y + (rect.height - newHeight) / 2;

        return new Rect(newX, newY, newWidth, newHeight); // Новый уменьшенный rect (чтобы цвет тега определить)
    }

    @Async
    private Rect checkRect(Rect rect, Mat frame) {
        int frameWidth = frame.width();
        int frameHeight = frame.height();

        // Проверка, чтобы rect не выходил за пределы frame
        int x = Math.max(rect.x, 0);
        int y = Math.max(rect.y, 0);
        int mWidth = Math.min(rect.width, frameWidth - x);
        int mHeight = Math.min(rect.height, frameHeight - y);

        // Создание нового прямоугольника, учитывающего проверку границ
        return new Rect(x, y, mWidth, mHeight);
    }


    private String[] getPattern(String s) {
        s = s.replace("left_x", "");
        s = s.replace("top_y", "");
        s = s.replace("width", "");
        s = s.replace("height", "");
        s = s.replace("(", "");
        s = s.replace(")", "");
        s = s.replace("%", "");
        return s.split(":");
    }

    private String getTagColor(Mat frame) {
        Scalar meanColor = Core.mean(frame);
        return scalarToHex(meanColor);
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

    private Scalar getContrastingColor(Scalar rgbColor) {
        // Преобразование цвета из RGB в HSV
        Mat rgbMat = new Mat(1, 1, CvType.CV_8UC3, rgbColor);
        Mat hsvMat = new Mat();
        Imgproc.cvtColor(rgbMat, hsvMat, Imgproc.COLOR_RGB2HSV);

        // Получение оттенка, насыщенности и значения
        double[] hsvValues = hsvMat.get(0, 0);
        double hue = hsvValues[0];
        double saturation = hsvValues[1];
        double value = hsvValues[2];

        // Инвертирование оттенка на 180 градусов
        double oppositeHue = (hue + 180) % 180;

        // Преобразование обратного цвета из HSV в RGB
        Mat oppositeHsvMat = new Mat(1, 1, CvType.CV_8UC3, new Scalar(oppositeHue, saturation, value));
        Mat oppositeRgbMat = new Mat();
        Imgproc.cvtColor(oppositeHsvMat, oppositeRgbMat, Imgproc.COLOR_HSV2RGB);

        // Получение обратного цвета в формате RGB
        double[] oppositeRgbValues = oppositeRgbMat.get(0, 0);

        return new Scalar(oppositeRgbValues[2], oppositeRgbValues[1], oppositeRgbValues[0]);
    }

    private String getContrastingHex(String hexColor) {
        // Извлечение компонентов цвета из HEX-кода
        int red = Integer.parseInt(hexColor.substring(1, 3), 16);
        int green = Integer.parseInt(hexColor.substring(3, 5), 16);
        int blue = Integer.parseInt(hexColor.substring(5, 7), 16);

        // Вычисление яркости цвета
        int brightness = (red * 299 + green * 587 + blue * 114) / 1000;

        // Определение цвета шрифта на основе яркости
        String contrastingColor;
        if (brightness < 128) {
            contrastingColor = "#FFFFFF"; // Белый цвет шрифта на темном фоне
        } else {
            contrastingColor = "#000000"; // Черный цвет шрифта на светлом фоне
        }

        return contrastingColor;
    }

    // Функция для парсинга файла yolov4.names.
    private List<String> labels(String path) {
        List<String> labels = new ArrayList<>();
        try {
            Scanner scnLabels = new Scanner(new File(path));
            while (scnLabels.hasNext()) {
                String label = scnLabels.nextLine();
                labels.add(label);
            }
        } catch (IOException ignored) {
        }

        return labels;
    }

    // Функция для генерации цветов
    private Scalar[] generateColors(int amountOfClasses) {
        Scalar[] colors = new Scalar[amountOfClasses];
        Random random = new Random();
        for (int i = 0; i < amountOfClasses; i++) {
            int r = random.nextInt(256);
            int g = random.nextInt(256);
            int b = random.nextInt(256);
            colors[i] = new Scalar(r, g, b);
        }
        return colors;
    }

    // Метод для извлечения наименований выходных слоев.
    private List<String> getOutputLayerNames(Net network) {
        List<String> layersNames = network.getLayerNames();
        List<String> outputLayersNames = new ArrayList<>();
        List<Integer> unconnectedLayersIndexes = network.getUnconnectedOutLayers().toList();
        for (int i : unconnectedLayersIndexes) {
            outputLayersNames.add(layersNames.get(i - 1));
        }
        return outputLayersNames;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void cleanStatic() {
        File folder = new File(PATH_FOLDER);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }

    public static void init() {

        // очень важная функция для инициализация static
    }
}