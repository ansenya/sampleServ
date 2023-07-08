package ru.senya.sampleserv.utils;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import ru.senya.sampleserv.models.Model;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
public class Utils {

    //    public static final String PATH_FOLDER = "src/main/resources/static/";
    public static final String PATH_FOLDER = "/home/senya/Desktop/imgs/";
    public static String SERVER_IP;
    public static String SERVER_PORT;
    public static final String SERVER_HOST = "http://192.168.50.85:8082";
    private static final String path = "src/main/config/yolo/yolov4.names", cfgPath = "src/main/config/yolo/yolov4.cfg", weightsPath = "src/main/config/yolo/yolov4.weights";
    private static final String carPath = "src/main/config/cars_ai/cars.names", carCfgPath = "src/main/config/cars_ai/cars.cfg", carWeightsPath = "src/main/config/cars_ai/cars.weights";
    private static final Net
            network1,
            network2,
            network3,
            network4,
            network5,
            network6,
            network7,
            network8,
            car_net;

    private static final ReentrantLock lock1 = new ReentrantLock(), lock2 = new ReentrantLock(), lock3 = new ReentrantLock(), lock4 = new ReentrantLock(), lock5 = new ReentrantLock(), lock6 = new ReentrantLock(), lock7 = new ReentrantLock(), lock8 = new ReentrantLock(), lock9 = new ReentrantLock(), lock10 = new ReentrantLock(), lock11 = new ReentrantLock(), lock12 = new ReentrantLock(), lock13 = new ReentrantLock(), lock14 = new ReentrantLock(), lock15 = new ReentrantLock(), lock16 = new ReentrantLock(), lock17 = new ReentrantLock(), lock18 = new ReentrantLock(), lock19 = new ReentrantLock(), lock20 = new ReentrantLock(), lock21 = new ReentrantLock(), lock22 = new ReentrantLock(), lock23 = new ReentrantLock(), lock24 = new ReentrantLock();
    private static final List<String> labels, carLabels, outputLayersNames;
    private static final int amountOfClasses, amountOfOutputLayers;
    private static final MatOfInt outputLayersIndexes;
    private static final Net[] nets;
    public static long COUNT = 0L;
    private static float minProbability = 0.5f, threshold = 0.3f;
    private static Mat frame1, frame2, frame3, frame4, frame5, frame6, frame7, frame8;
    private static Mat frameResized1, frameResized2, frameResized3, frameResized4, frameResized5, frameResized6, frameResized7, frameResized8;
    private static Mat additionalFrame1, additionalFrame2, additionalFrame3, additionalFrame4, additionalFrame5, additionalFrame6, additionalFrame7, additionalFrame8;
    private static int height1, height2, height3, height4, height5, height6, height7, height8;
    private static int width1, width2, width3, width4, width5, width6, width7, width8;
    private static MatOfInt indices1, indices2, indices3, indices4, indices5, indices6, indices7, indices8;
    private static List<String> tags1 = new LinkedList<>(), tags2 = new LinkedList<>(), tags3 = new LinkedList<>(), tags4 = new LinkedList<>(), tags5 = new LinkedList<>(), tags6 = new LinkedList<>(), tags7 = new LinkedList<>(), tags8 = new LinkedList<>();
    private static Scalar contrastingColor1, contrastingColor2, contrastingColor3, contrastingColor4, contrastingColor5, contrastingColor6, contrastingColor7, contrastingColor8;
    private static String aiPath1, aiPath2, aiPath3, aiPath4, aiPath5, aiPath6, aiPath7, aiPath8;
    private static String coloredPath1, coloredPath2, coloredPath3, coloredPath4, coloredPath5, coloredPath6, coloredPath7, coloredPath8;


    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.print("OpenCV version: " + Core.VERSION);

        network1 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network2 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network3 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network4 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network5 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network6 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network7 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network8 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        car_net = Dnn.readNetFromDarknet("src/main/config/cars_ai/cars.cfg", "src/main/config/cars_ai/cars.weights");

        frameResized1 = new Mat();
        frameResized2 = new Mat();
        frameResized3 = new Mat();
        frameResized4 = new Mat();
        frameResized5 = new Mat();
        frameResized6 = new Mat();
        frameResized7 = new Mat();
        frameResized8 = new Mat();


        indices1 = new MatOfInt();
        indices2 = new MatOfInt();
        indices3 = new MatOfInt();
        indices4 = new MatOfInt();
        indices5 = new MatOfInt();
        indices6 = new MatOfInt();
        indices7 = new MatOfInt();
        indices8 = new MatOfInt();


        nets = new Net[]{network1, network2, network3, network4, network5, network6, network7, network8, car_net};


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
        carLabels = labels(carPath);
        amountOfClasses = labels.size();

        // Извлекаем наименования выходных слоев.
        outputLayersNames = getOutputLayerNames(nets[0]);

        // Извлекаем индексы выходных слоев.
        outputLayersIndexes = nets[0].getUnconnectedOutLayers();
        amountOfOutputLayers = outputLayersIndexes.toArray().length;

        cleanStatic();
    }


    public static void processImages(long COUNT, Model model, CountDownLatch latch, String path, String uniqueFilename) {
        int i = (int) (COUNT % nets.length);
        switch (i) {
            case 0 -> new Thread(() -> {
                lock1.lock();
                try {
                    processImage1(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock1.unlock();
                }
            }).start();
            case 1 -> new Thread(() -> {
                lock2.lock();
                try {
                    processImage2(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock2.unlock();
                }
            }).start();
            case 2 -> new Thread(() -> {
                lock3.lock();
                try {
                    processImage3(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock3.unlock();
                }
            }).start();
            case 3 -> new Thread(() -> {
                lock4.lock();
                try {
                    processImage4(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock4.unlock();
                }
            }).start();
            case 4 -> new Thread(() -> {
                lock5.lock();
                try {
                    processImage5(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock5.unlock();
                }
            }).start();
            case 5 -> new Thread(() -> {
                lock6.lock();
                try {
                    processImage6(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock6.unlock();
                }
            }).start();
            case 6 -> new Thread(() -> {
                lock7.lock();
                try {
                    processImage7(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock7.unlock();
                }
            }).start();
            case 7 -> new Thread(() -> {
                lock8.lock();
                try {
                    processImage8(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock8.unlock();
                }
            }).start();
        }
    }

    public static void processImage1(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, nets[0], frame1, frameResized1, additionalFrame1, minProbability, threshold, height1, width1, indices1, tags1, contrastingColor1, aiPath1, coloredPath1));
        model.setAiPath("ai_" + fileName);
        model.setColoredPath("colored_" + fileName);
    }

    public static void processImage2(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, nets[1], frame2, frameResized2, additionalFrame2, minProbability, threshold, height2, width2, indices2, tags2, contrastingColor2, aiPath2, coloredPath2));
        model.setAiPath("ai_" + fileName);
        model.setColoredPath("colored_" + fileName);
    }

    public static void processImage3(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, nets[2], frame3, frameResized3, additionalFrame3, minProbability, threshold, height3, width3, indices3, tags3, contrastingColor3, aiPath3, coloredPath3));
        model.setAiPath("ai_" + fileName);
        model.setColoredPath("colored_" + fileName);
    }

    public static void processImage4(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, nets[3], frame4, frameResized4, additionalFrame4, minProbability, threshold, height4, width4, indices4, tags4, contrastingColor4, aiPath4, coloredPath4));
        model.setAiPath("ai_" + fileName);
        model.setColoredPath("colored_" + fileName);
    }

    public static void processImage5(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, nets[4], frame5, frameResized5, additionalFrame5, minProbability, threshold, height5, width5, indices5, tags5, contrastingColor5, aiPath5, coloredPath5));
        model.setAiPath("ai_" + fileName);
        model.setColoredPath("colored_" + fileName);
    }

    public static void processImage6(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, nets[5], frame6, frameResized6, additionalFrame6, minProbability, threshold, height6, width6, indices6, tags6, contrastingColor6, aiPath6, coloredPath6));
        model.setAiPath("ai_" + fileName);
        model.setColoredPath("colored_" + fileName);
    }

    public static void processImage7(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, nets[6], frame7, frameResized7, additionalFrame7, minProbability, threshold, height7, width7, indices7, tags7, contrastingColor7, aiPath7, coloredPath7));
        model.setAiPath("ai_" + fileName);
        model.setColoredPath("colored_" + fileName);
    }

    public static void processImage8(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, nets[7], frame8, frameResized8, additionalFrame8, minProbability, threshold, height8, width8, indices8, tags8, contrastingColor8, aiPath8, coloredPath8));
        model.setAiPath("ai_" + fileName);
        model.setColoredPath("colored_" + fileName);
    }


    @SuppressWarnings("UnusedAssignment")
    private static String[] getTags(String image, String color, String fileName, Net net,
                                    Mat frame, Mat frameResized, Mat additionalFrame,
                                    float minProbability, float threshold,
                                    int height, int width,
                                    MatOfInt indices, List<String> tags,
                                    Scalar contrastingColor, String aiPath, String coloredPath) {

        try {
            // Освобождение ресурсов для объектов Mat
            frame.release();
            frameResized.release();
            additionalFrame.release();

            indices.release();

            // Очистка списка tags
            tags.clear();
        } catch (NullPointerException ignored) {
        }


        contrastingColor = getContrastingColor(hexToScalar(getContrastingHex(color)));
        aiPath = "ai_" + fileName;
        coloredPath = "colored_" + fileName;

        // Читаем изображение
        frame = Imgcodecs.imread(image);
        additionalFrame = frame.clone();

        height = frame.height();
        width = frame.width();

        // Изменяем размер кадра для уменьшения нагрузки на нейронную сеть.
        Imgproc.resize(frame, frameResized, new Size(512, 512));

        // Подаём blob на вход нейронной сети.
        net.setInput(Dnn.blobFromImage(frameResized, 1 / 255.0));

        // Извлекаем данные с выходных слоев нейронной сети.
        List<Mat> outputFromNetwork = new ArrayList<>();
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

                double scaleFactor = 0.4; // Множитель уменьшения

                int newWidth = (int) (rect.width * scaleFactor);
                int newHeight = (int) (rect.height * scaleFactor);

                // Центрирование новой рамки относительно исходной
                int newX = rect.x + (rect.width - newWidth) / 2;
                int newY = rect.y + (rect.height - newHeight) / 2;

                newRect = new Rect(newX, newY, newWidth, newHeight); // Новый уменьшенный rect (чтобы цвет тега определить)

                label += " " + getTagColor(frame.submat(newRect));

                if (label.contains("car")) {
                    Mat croppedFrame = new Mat(additionalFrame, rect);
                    label = label.replace("car", getCarBrand(car_net, croppedFrame, color));
                }

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

        return tags.toArray(new String[tags.size()]);
    }

    private static synchronized String getCarBrand(Net net, Mat frame, String color) {

        // Инициализируем переменные.
        Mat frameResized = new Mat(), gray;
        float minProbability = 0.5f, threshold = 0.3f;
        int height, width, k = 64;
        MatOfInt indices = new MatOfInt();
        String tag = new String();

        height = frame.height();
        width = frame.width();


        // Изменяем размер кадра для уменьшения нагрузки на нейронную сеть.
        Imgproc.resize(frame, frameResized, new Size(32 * (width / 32), 32 * (height / 32)));
        while (frameResized.width() * frameResized.height() > 105_000) {
            Imgproc.resize(frame, frameResized, new Size(32 * (frameResized.width() / k), 32 * (frameResized.height() / k)));
        }

        // Подаём blob на вход нейронной сети.
        net.setInput(Dnn.blobFromImage(frameResized, 1 / 255.0));

        // Извлекаем данные с выходных слоев нейронной сети.
        List<Mat> outputFromNetwork = new ArrayList<>();
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
            String label = carLabels.get(classIndex);
            tag = label;
        }
        if (tag.isEmpty()) {
//            Imgcodecs.imwrite("problem"+(COUNT++)+".jpg", frame);
            return "car";
        }
        return tag;
    }

    private static String getPattern(String str) {
        Pattern pattern = Pattern.compile("\\{([^}]*)\\}");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            String extractedText = matcher.group(1);
            return extractedText;
        }
        return "";
    }

    private static String getTagColor(Mat frame) {
        Scalar meanColor = Core.mean(frame);
        return scalarToHex(meanColor);
    }

    private static String getColor(String path) {
        // Загрузка изображения
        Mat image = Imgcodecs.imread(path);

        // Вычисление среднего значения всех пикселей
        Scalar meanColor = Core.mean(image);

        // Вывод среднего цвета
        return scalarToHex(meanColor);
    }

    private static String scalarToHex(Scalar scalar) {
        // Получение среднего цвета в формате BGR
        double blue = scalar.val[0];
        double green = scalar.val[1];
        double red = scalar.val[2];

        return String.format("#%02X%02X%02X", (int) red, (int) green, (int) blue);
    }

    private static Scalar hexToScalar(String hexColor) {
        // Извлечение компонентов цвета из HEX-кода
        int red = Integer.parseInt(hexColor.substring(1, 3), 16);
        int green = Integer.parseInt(hexColor.substring(3, 5), 16);
        int blue = Integer.parseInt(hexColor.substring(5, 7), 16);

        return new Scalar(blue, green, red);
    }

    private static Scalar getContrastingColor(Scalar rgbColor) {
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

    private static String getContrastingHex(String hexColor) {
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
    private static List<String> labels(String path) {
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
    private static Scalar[] generateColors(int amountOfClasses) {
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
    private static List<String> getOutputLayerNames(Net network) {
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
