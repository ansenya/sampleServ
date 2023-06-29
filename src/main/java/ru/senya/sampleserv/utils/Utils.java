package ru.senya.sampleserv.utils;

import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import ru.senya.sampleserv.models.Model;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Utils {

    public static final String PATH_FOLDER = "src/main/resources/static/";
    public static String SERVER_IP;
    public static String SERVER_PORT;
    public static String SERVER_HOST = "localhost:8082";
    private static final String path = "src/main/config/coco.names", cfgPath = "src/main/config/yolov4.cfg", weightsPath = "src/main/config/yolov4.weights";
    private static Net network;
    private static List<String> labels, outputLayersNames;
    private static int amountOfClasses, amountOfOutputLayers;
    private static MatOfInt outputLayersIndexes;


    static {
        // Инициализация сети
        network = Dnn.readNetFromDarknet(cfgPath, weightsPath);

        // Инициализация работы на видеокарте
        // Если OpenCV был собран без поддержки CUDA, то строчки нужно закомментировать
        network.setPreferableBackend(Dnn.DNN_BACKEND_CUDA);
        network.setPreferableTarget(Dnn.DNN_TARGET_CUDA);

        // Классы
        labels = labels(path);
        amountOfClasses = labels.size();

        // Извлекаем наименования выходных слоев.
        outputLayersNames = getOutputLayerNames(network);

        // Извлекаем индексы выходных слоев.
        outputLayersIndexes = network.getUnconnectedOutLayers();
        amountOfOutputLayers = outputLayersIndexes.toArray().length;
    }

    public static void processImage(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model));
    }

    private static String getTags(String image, String color, String fileName, Model model) {

        // Инициализируем переменные.
        Mat frame, frameResized = new Mat(), coloredFrame;
        float minProbability = 0.5f, threshold = 0.3f;
        int height, width;
        MatOfInt indices = new MatOfInt();
        StringBuilder tags = new StringBuilder();
        Scalar contrastingColor = getContrastingColor(hexToScalar(getContrastingHex(color)));

        // Читаем изображение
        frame = Imgcodecs.imread(image);
        height = frame.height();
        width = frame.width();

        // Изменяем размер кадра для уменьшения нагрузки на нейронную сеть.
        Imgproc.resize(frame, frameResized, new Size(256, 256));

        // Подаём blob на вход нейронной сети.
        network.setInput(Dnn.blobFromImage(frameResized, 1 / 255.0));

        // Извлекаем данные с выходных слоев нейронной сети.
        List<Mat> outputFromNetwork = new ArrayList<>();
        network.forward(outputFromNetwork, outputLayersNames);


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
                        (int) boundingList.get(indicesList.get(i)).x,
                        (int) boundingList.get(indicesList.get(i)).y,
                        (int) boundingList.get(indicesList.get(i)).width,
                        (int) boundingList.get(indicesList.get(i)).height
                );

                // Извлекаем индекс выявленгого класса (объекта на изображении).
                int classIndex = classIndexes.get(indices.toList().get(i));

                // Наносим ограничительную рамку.
                Imgproc.rectangle(frame, rect, contrastingColor, 2);


                // Форматируем строку для нанесения на изображение:
                // Выявленный клас: вероятность
                String label = labels.get(classIndex) + ": " + String.format("%.2f", confidences.toList().get(i));

                // Инициализируем точку для нанесения текста.
                Point textPoint = new Point(
                        (int) boundingList.get(indices.toList().get(i)).x,
                        (int) boundingList.get(indices.toList().get(i)).y - 10
                );

                double scaleFactor = 0.5; // Множитель уменьшения

                int newWidth = (int) (rect.width * scaleFactor); // Новая ширина
                int newHeight = (int) (rect.height * scaleFactor); // Новая высота

                // Центрирование новой рамки относительно исходной
                int newX = rect.x + (rect.width - newWidth) / 2;
                int newY = rect.y + (rect.height - newHeight) / 2;

                newRect = new Rect(newX, newY, newWidth, newHeight); // Новый уменьшенный rect

                label += " " + getTagColor(frame.submat(newRect));
                // Наносим текст на изображение.
                Imgproc.putText(frame, label, textPoint, Imgproc.FONT_HERSHEY_COMPLEX, 1.2, contrastingColor, 2);
                tags.append(label).append("; ");
            }
        }

        Imgcodecs.imwrite(PATH_FOLDER + "ai_" + fileName, frame);
        model.setAiPath("http://" + SERVER_HOST + "/get/" + "ai_" + fileName);

        frame.setTo(hexToScalar(model.getHexColor()));
        Imgcodecs.imwrite(PATH_FOLDER + "colored_" + fileName, frame);
        model.setColoredPath("http://" + SERVER_HOST + "/get/" + "colored_" + fileName);

        return tags.toString();
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

    // Функция для парсинга файла coco.names.
    private static List<String> labels(String path) {
        List<String> labels = new ArrayList<>();
        try {
            Scanner scnLabels = new Scanner(new File(path));
            while (scnLabels.hasNext()) {
                String label = scnLabels.nextLine();
                labels.add(label);
            }
        } catch (IOException e) {
            e.printStackTrace();
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

}
