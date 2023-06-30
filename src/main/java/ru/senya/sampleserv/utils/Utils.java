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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class Utils {

    public static final String PATH_FOLDER = "src/main/resources/static/";
    public static String SERVER_IP;
    public static String SERVER_PORT;
    public static String SERVER_HOST = "192.168.50.85:8082";
    private static final String path = "src/main/config/coco.names", cfgPath = "src/main/config/yolov4.cfg", weightsPath = "src/main/config/yolov4.weights";
    private static final Net network1,
            network2,
            network3,
            network4,
            network5,
            network6,
            network7,
            network8,
            network9,
            network10,
            network11,
            network12,
            network13,
            network14,
            network15,
            network16;

    private static final ReentrantLock lock1 = new ReentrantLock(), lock2 = new ReentrantLock(), lock3 = new ReentrantLock(), lock4 = new ReentrantLock(), lock5 = new ReentrantLock(), lock6 = new ReentrantLock(), lock7 = new ReentrantLock(), lock8 = new ReentrantLock(), lock9 = new ReentrantLock(), lock10 = new ReentrantLock(), lock11 = new ReentrantLock(), lock12 = new ReentrantLock(), lock13 = new ReentrantLock(), lock14 = new ReentrantLock(), lock15 = new ReentrantLock(), lock16 = new ReentrantLock(), lock17 = new ReentrantLock(), lock18 = new ReentrantLock(), lock19 = new ReentrantLock(), lock20 = new ReentrantLock(), lock21 = new ReentrantLock(), lock22 = new ReentrantLock(), lock23 = new ReentrantLock(), lock24 = new ReentrantLock();
    private static final List<String> labels, outputLayersNames;
    private static final int amountOfClasses, amountOfOutputLayers;
    private static final MatOfInt outputLayersIndexes;
    private static final Net[] nets;
    public static long COUNT = 0L;

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
        network9 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network10 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network11 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network12 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network13 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network14 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network15 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
        network16 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
//        network17 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
//        network18 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
//        network19 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
//        network20 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
//        network21 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
//        network22 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
//        network23 = Dnn.readNetFromDarknet(cfgPath, weightsPath);
//        network24 = Dnn.readNetFromDarknet(cfgPath, weightsPath);

        nets = new Net[]{network1, network2, network3, network4, network5, network6, network7, network8, network9, network10, network11, network12, network13, network14, network15, network16};


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
        labels = labels();
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
            case 8 -> new Thread(() -> {
                lock9.lock();
                try {
                    processImage9(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock9.unlock();
                }
            }).start();
            case 9 -> new Thread(() -> {
                lock10.lock();
                try {
                    processImage10(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock10.unlock();
                }
            }).start();
            case 10 -> new Thread(() -> {
                lock11.lock();
                try {
                    processImage11(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock11.unlock();
                }
            }).start();
            case 11 -> new Thread(() -> {
                lock12.lock();
                try {
                    processImage12(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock12.unlock();
                }
            }).start();
            case 12 -> new Thread(() -> {
                lock13.lock();
                try {
                    processImage13(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock13.unlock();
                }
            }).start();
            case 13 -> new Thread(() -> {
                lock14.lock();
                try {
                    processImage14(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock14.unlock();
                }
            }).start();
            case 14 -> new Thread(() -> {
                lock15.lock();
                try {
                    processImage15(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock15.unlock();
                }
            }).start();
            case 15 -> new Thread(() -> {
                lock16.lock();
                try {
                    processImage16(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock16.unlock();
                }
            }).start();
            case 16 -> new Thread(() -> {
                lock17.lock();
                try {
                    processImage17(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock17.unlock();
                }
            }).start();
            case 17 -> new Thread(() -> {
                lock18.lock();
                try {
                    processImage18(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock18.unlock();
                }
            }).start();
            case 18 -> new Thread(() -> {
                lock19.lock();
                try {
                    processImage19(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock19.unlock();
                }
            }).start();
            case 19 -> new Thread(() -> {
                lock20.lock();
                try {
                    processImage20(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock20.unlock();
                }
            }).start();
            case 20 -> new Thread(() -> {
                lock21.lock();
                try {
                    processImage21(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock21.unlock();
                }
            }).start();
            case 21 -> new Thread(() -> {
                lock22.lock();
                try {
                    processImage22(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock22.unlock();
                }
            }).start();
            case 22 -> new Thread(() -> {
                lock23.lock();
                try {
                    processImage23(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock23.unlock();
                }
            }).start();
            case 23 -> new Thread(() -> {
                lock24.lock();
                try {
                    processImage24(model, path, uniqueFilename);
                    latch.countDown();
                } finally {
                    lock24.unlock();
                }
            }).start();

        }
    }

    public static void processImage1(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[0]));
    }

    public static void processImage2(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[1]));
    }

    public static void processImage3(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[2]));
    }

    public static void processImage4(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[3]));
    }

    public static void processImage5(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[4]));
    }

    public static void processImage6(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[5]));
    }

    public static void processImage7(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[6]));
    }

    public static void processImage8(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[7]));
    }

    public static void processImage9(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[8]));
    }

    public static void processImage10(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[9]));
    }

    public static void processImage11(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[10]));
    }

    public static void processImage12(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[11]));
    }

    public static void processImage13(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[12]));
    }

    public static void processImage14(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[13]));
    }

    public static void processImage15(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[14]));
    }

    public static void processImage16(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[15]));
    }

    public static void processImage17(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[16]));
    }

    public static void processImage18(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[17]));
    }

    public static void processImage19(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[18]));
    }

    public static void processImage20(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[19]));
    }

    public static void processImage21(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[20]));
    }

    public static void processImage22(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[21]));
    }

    public static void processImage23(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[22]));
    }

    public static void processImage24(Model model, String src, String fileName) {
        model.setHexColor(getColor(src));
        model.setTags(getTags(src, model.getHexColor(), fileName, model, nets[23]));
    }


    private static String getTags(String image, String color, String fileName, Model model, Net net) {

        // Инициализируем переменные.
        Mat frame, frameResized = new Mat(), gray;
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
        Imgproc.resize(frame, frameResized, new Size(320, 320));

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
//                // Ограничительная рамка
                Rect rect = new Rect(
                        (int) boundingList.get(indicesList.get(i)).x,
                        (int) boundingList.get(indicesList.get(i)).y,
                        (int) boundingList.get(indicesList.get(i)).width,
                        (int) boundingList.get(indicesList.get(i)).height
                );
//
//                // Извлекаем индекс выявленгого класса (объекта на изображении).
                int classIndex = classIndexes.get(indices.toList().get(i));
//
//                // Наносим ограничительную рамку.
                Imgproc.rectangle(frame, rect, contrastingColor, 2);
//
//
//                // Форматируем строку для нанесения на изображение:
//                // Выявленный клас: вероятность
                String label = labels.get(classIndex) + ": " + String.format("%.2f", confidences.toList().get(i));
//
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
//                 Наносим текст на изображение.
                Imgproc.putText(frame, label, textPoint, Imgproc.FONT_HERSHEY_COMPLEX, 1.2, contrastingColor, 2);
                tags.append(label).append("; ");
            }
        }

        Imgcodecs.imwrite(PATH_FOLDER + "ai_" + fileName, frame);
        model.setAiPath("http://" + SERVER_HOST + "/get/" + "ai_" + fileName);

        frame.setTo(hexToScalar(model.getHexColor()));
        Imgcodecs.imwrite(PATH_FOLDER + "colored_" + fileName, frame);
        model.setColoredPath("http://" + SERVER_HOST + "/get/" + "colored_" + fileName);
        tags.append(Arrays.stream(nets).toList().indexOf(net));
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
    private static List<String> labels() {
        List<String> labels = new ArrayList<>();
        try {
            Scanner scnLabels = new Scanner(new File(Utils.path));
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
    }
}
