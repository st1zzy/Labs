package com.example;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MonteCarloIntegration {

    // Параметры эллипсоида - изменено на public для доступа из других классов
    public static final double A = 2.0;
    public static final double B = 3.0;
    public static final double C = 1.5;

    // Параметры функции ρ(x) - вариант 4
    private static final double[] ALPHA = {-2.0, 1.0, 0.5};
    private static final double[] BETA = {1.0, 2.0, 1.5};
    private static final double[] P = {0.3, 0.2, 0.4};
    private static final double[] Q = {1.0, 1.5, 0.8};

    // Массив количества испытаний
    private static final int[] N_VALUES = {1000, 5000, 10000, 50000, 100000, 500000};

    private static final Random random = new Random();

    // Класс для хранения точки 3D
    static class Point3D {
        double x, y, z;
        boolean inside;

        public Point3D(double x, double y, double z, boolean inside) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.inside = inside;
        }
    }

    // Функция плотности заряда ρ(x1, x2, x3)
    public static double rho(double x1, double x2, double x3) {
        double[] x = {x1, x2, x3};
        double product = 1.0;

        for (int i = 0; i < 3; i++) {
            double diff = Math.abs(x[i] - ALPHA[i]);
            double term = Math.pow(diff, BETA[i]) *
                    Math.exp(-P[i] * Math.pow(diff, Q[i]));
            product *= term;
        }

        return product;
    }

    // Проверка принадлежности точки эллипсоиду
    public static boolean isInEllipsoid(double x1, double x2, double x3) {
        return (x1*x1)/(A*A) + (x2*x2)/(B*B) + (x3*x3)/(C*C) <= 1.0;
    }

    // Вычисление объемного заряда с сохранением точек для визуализации
    public static ResultWithPoints computeChargeWithVisualization(int N, int maxPoints) {
        double x1Min = -A, x1Max = A;
        double x2Min = -B, x2Max = B;
        double x3Min = -C, x3Max = C;

        double volumeW = (x1Max - x1Min) * (x2Max - x2Min) * (x3Max - x3Min);

        int M = 0;
        double sumF = 0.0;
        List<Point3D> points = new ArrayList<>();

        for (int j = 0; j < N; j++) {
            double x1 = x1Min + (x1Max - x1Min) * random.nextDouble();
            double x2 = x2Min + (x2Max - x2Min) * random.nextDouble();
            double x3 = x3Min + (x3Max - x3Min) * random.nextDouble();

            boolean inside = isInEllipsoid(x1, x2, x3);

            if (inside) {
                M++;
                sumF += rho(x1, x2, x3);
            }

            // Сохраняем точки для визуализации (не все, чтобы не перегружать)
            if (points.size() < maxPoints) {
                points.add(new Point3D(x1, x2, x3, inside));
            }
        }

        double volumeV = (M / (double) N) * volumeW;
        double integral = (volumeW / N) * sumF;
        double exactVolume = (4.0 / 3.0) * Math.PI * A * B * C;

        return new ResultWithPoints(N, M, volumeV, integral, exactVolume, points);
    }

    // Вычисление объемного заряда методом Монте-Карло
    public static Result computeCharge(int N) {
        double x1Min = -A, x1Max = A;
        double x2Min = -B, x2Max = B;
        double x3Min = -C, x3Max = C;

        double volumeW = (x1Max - x1Min) * (x2Max - x2Min) * (x3Max - x3Min);

        int M = 0;
        double sumF = 0.0;

        for (int j = 0; j < N; j++) {
            double x1 = x1Min + (x1Max - x1Min) * random.nextDouble();
            double x2 = x2Min + (x2Max - x2Min) * random.nextDouble();
            double x3 = x3Min + (x3Max - x3Min) * random.nextDouble();

            if (isInEllipsoid(x1, x2, x3)) {
                M++;
                sumF += rho(x1, x2, x3);
            }
        }

        double volumeV = (M / (double) N) * volumeW;
        double integral = (volumeW / N) * sumF;
        double exactVolume = (4.0 / 3.0) * Math.PI * A * B * C;

        return new Result(N, M, volumeV, integral, exactVolume);
    }

    // Тестовая задача: вычисление объема эллипсоида
    public static Result computeEllipsoidVolume(int N) {
        double x1Min = -A, x1Max = A;
        double x2Min = -B, x2Max = B;
        double x3Min = -C, x3Max = C;

        double volumeW = (x1Max - x1Min) * (x2Max - x2Min) * (x3Max - x3Min);

        int M = 0;

        for (int j = 0; j < N; j++) {
            double x1 = x1Min + (x1Max - x1Min) * random.nextDouble();
            double x2 = x2Min + (x2Max - x2Min) * random.nextDouble();
            double x3 = x3Min + (x3Max - x3Min) * random.nextDouble();

            if (isInEllipsoid(x1, x2, x3)) {
                M++;
            }
        }

        double volumeV = (M / (double) N) * volumeW;
        double exactVolume = (4.0 / 3.0) * Math.PI * A * B * C;

        return new Result(N, M, volumeV, 0.0, exactVolume);
    }

    // Класс для хранения результатов
    static class Result {
        int N;
        int M;
        double volumeV;
        double integral;
        double exactVolume;

        public Result(int N, int M, double volumeV, double integral, double exactVolume) {
            this.N = N;
            this.M = M;
            this.volumeV = volumeV;
            this.integral = integral;
            this.exactVolume = exactVolume;
        }

        public double getVolumeError() {
            return Math.abs(volumeV - exactVolume);
        }

        public double getRelativeError() {
            return Math.abs(volumeV - exactVolume) / exactVolume * 100;
        }
    }

    // Класс с результатами и точками для визуализации
    static class ResultWithPoints extends Result {
        List<Point3D> points;

        public ResultWithPoints(int N, int M, double volumeV, double integral,
                                double exactVolume, List<Point3D> points) {
            super(N, M, volumeV, integral, exactVolume);
            this.points = points;
        }
    }

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║     ВЫЧИСЛЕНИЕ КРАТНЫХ ИНТЕГРАЛОВ МЕТОДОМ МОНТЕ-КАРЛО                    ║");
        System.out.println("║     Задание: Вычисление объемного заряда в эллипсоиде                    ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════╝\n");

        System.out.printf("Параметры эллипсоида: a = %.2f, b = %.2f, c = %.2f%n", A, B, C);
        System.out.printf("Точный объем эллипсоида: V = (4/3)πabc = %.6f%n%n",
                (4.0/3.0) * Math.PI * A * B * C);

        // Визуализация с небольшим количеством точек для наглядности
        System.out.println("Создание 3D визуализации с 2000 точками...");
        ResultWithPoints visResult = computeChargeWithVisualization(2000, 2000);

        SwingUtilities.invokeLater(() -> {
            create3DVisualization(visResult);
        });

        // ТЕСТ: Вычисление объема эллипсоида
        System.out.println("\n" + "═".repeat(100));
        System.out.println("ТЕСТ: Вычисление объема эллипсоида");
        System.out.println("═".repeat(100));
        System.out.printf("%-12s %-15s %-20s %-20s %-15s%n",
                "N", "M", "Вычисл. объем", "Точный объем", "Отн. ошибка (%)");
        System.out.println("─".repeat(100));

        Result[] testResults = new Result[N_VALUES.length];
        for (int i = 0; i < N_VALUES.length; i++) {
            testResults[i] = computeEllipsoidVolume(N_VALUES[i]);
            Result r = testResults[i];
            System.out.printf("%-12d %-15d %-20.6f %-20.6f %-15.4f%n",
                    r.N, r.M, r.volumeV, r.exactVolume, r.getRelativeError());
        }

        // ОСНОВНАЯ ЗАДАЧА: Вычисление объемного заряда
        System.out.println("\n" + "═".repeat(100));
        System.out.println("ОСНОВНАЯ ЗАДАЧА: Вычисление объемного заряда Q");
        System.out.println("═".repeat(100));
        System.out.printf("%-12s %-15s %-20s %-25s %-15s%n",
                "N", "M", "Объем V", "Объемный заряд Q", "Отн. ошибка V (%)");
        System.out.println("─".repeat(100));

        Result[] chargeResults = new Result[N_VALUES.length];
        for (int i = 0; i < N_VALUES.length; i++) {
            chargeResults[i] = computeCharge(N_VALUES[i]);
            Result r = chargeResults[i];
            System.out.printf("%-12d %-15d %-20.6f %-25.10f %-15.4f%n",
                    r.N, r.M, r.volumeV, r.integral, r.getRelativeError());
        }

        // Сохранение результатов в CSV
        try {
            saveResultsToCSV(testResults, chargeResults);
            System.out.println("\n✅ Результаты сохранены в файл: 'monte_carlo_results.csv'");
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении файла: " + e.getMessage());
        }

        // Построение графика сходимости
        SwingUtilities.invokeLater(() -> {
            createConvergenceGraph(testResults, chargeResults);
        });
    }

    // 3D визуализация
    private static void create3DVisualization(ResultWithPoints result) {
        JFrame frame = new JFrame("3D Визуализация метода Монте-Карло");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 900);

        JPanel mainPanel = new JPanel(new BorderLayout());

        Ellipsoid3DPanel visualPanel = new Ellipsoid3DPanel(result.points);
        mainPanel.add(visualPanel, BorderLayout.CENTER);

        // Панель управления
        JPanel controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(1000, 80));
        controlPanel.setBackground(new Color(240, 240, 240));

        JLabel infoLabel = new JLabel(String.format(
                "Всего точек: %d  |  Внутри эллипсоида: %d (%.1f%%)  |  Снаружи: %d (%.1f%%)",
                result.points.size(),
                result.M, (result.M * 100.0 / result.N),
                result.N - result.M, ((result.N - result.M) * 100.0 / result.N)
        ));
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        controlPanel.add(infoLabel);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Сохранение результатов в CSV
    private static void saveResultsToCSV(Result[] testResults, Result[] chargeResults)
            throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter("monte_carlo_results.csv"));
        writer.println("N,M_test,Volume_test,Exact_volume,Error_test," +
                "M_charge,Volume_charge,Integral_charge,Error_charge");

        for (int i = 0; i < N_VALUES.length; i++) {
            Result t = testResults[i];
            Result c = chargeResults[i];
            writer.printf("%d,%d,%.10f,%.10f,%.10f,%d,%.10f,%.10f,%.10f%n",
                    t.N, t.M, t.volumeV, t.exactVolume, t.getVolumeError(),
                    c.M, c.volumeV, c.integral, c.getVolumeError());
        }
        writer.close();
    }

    // График сходимости
    private static void createConvergenceGraph(Result[] testResults, Result[] chargeResults) {
        JFrame frame = new JFrame("Сходимость метода Монте-Карло");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1200, 700);

        ConvergencePanel panel = new ConvergencePanel(testResults, chargeResults);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// Панель 3D визуализации эллипсоида с точками
class Ellipsoid3DPanel extends JPanel {
    private List<MonteCarloIntegration.Point3D> points;
    private double angleX = 0.3;
    private double angleY = 0.5;
    private double angleZ = 0;
    private double scale = 80;

    private int lastMouseX, lastMouseY;

    public Ellipsoid3DPanel(List<MonteCarloIntegration.Point3D> points) {
        this.points = points;
        setBackground(Color.WHITE);

        // Мышь для вращения
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;

                angleY += dx * 0.01;
                angleX += dy * 0.01;

                lastMouseX = e.getX();
                lastMouseY = e.getY();

                repaint();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        // Колесо мыши для масштабирования
        addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) {
                scale *= 1.1;
            } else {
                scale /= 1.1;
            }
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2 - 40;

        // Заголовок
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(Color.BLACK);
        g2d.drawString("3D Визуализация: Метод Монте-Карло для эллипсоида", 20, 30);

        // Инструкции
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Мышь: вращение | Колесо: масштаб", 20, 55);

        // Рисуем оси координат
        g2d.setStroke(new BasicStroke(2.0f));
        drawAxes(g2d, centerX, centerY);

        // Рисуем каркас эллипсоида
        drawEllipsoidWireframe(g2d, centerX, centerY);

        // Рисуем точки
        drawPoints(g2d, centerX, centerY);

        // Легенда
        drawLegend(g2d, width);
    }

    private void drawAxes(Graphics2D g2d, int centerX, int centerY) {
        // Оси X, Y, Z
        Point2D axisX = project3D(3, 0, 0, centerX, centerY);
        Point2D axisY = project3D(0, 3, 0, centerX, centerY);
        Point2D axisZ = project3D(0, 0, 3, centerX, centerY);
        Point2D origin = project3D(0, 0, 0, centerX, centerY);

        // Ось X (красная)
        g2d.setColor(new Color(200, 0, 0));
        g2d.drawLine((int)origin.x, (int)origin.y, (int)axisX.x, (int)axisX.y);
        g2d.drawString("X", (int)axisX.x + 5, (int)axisX.y);

        // Ось Y (зеленая)
        g2d.setColor(new Color(0, 150, 0));
        g2d.drawLine((int)origin.x, (int)origin.y, (int)axisY.x, (int)axisY.y);
        g2d.drawString("Y", (int)axisY.x + 5, (int)axisY.y);

        // Ось Z (синяя)
        g2d.setColor(new Color(0, 0, 200));
        g2d.drawLine((int)origin.x, (int)origin.y, (int)axisZ.x, (int)axisZ.y);
        g2d.drawString("Z", (int)axisZ.x + 5, (int)axisZ.y);
    }

    private void drawEllipsoidWireframe(Graphics2D g2d, int centerX, int centerY) {
        g2d.setColor(new Color(100, 100, 100, 100));
        g2d.setStroke(new BasicStroke(1.0f));

        int segments = 30;

        // Рисуем меридианы и параллели эллипсоида
        for (int i = 0; i <= segments; i++) {
            double theta = Math.PI * i / segments;

            List<Point2D> meridian = new ArrayList<>();
            for (int j = 0; j <= segments; j++) {
                double phi = 2 * Math.PI * j / segments;
                double x = MonteCarloIntegration.A * Math.sin(theta) * Math.cos(phi);
                double y = MonteCarloIntegration.B * Math.sin(theta) * Math.sin(phi);
                double z = MonteCarloIntegration.C * Math.cos(theta);
                meridian.add(project3D(x, y, z, centerX, centerY));
            }

            for (int j = 0; j < meridian.size() - 1; j++) {
                Point2D p1 = meridian.get(j);
                Point2D p2 = meridian.get(j + 1);
                g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
            }
        }

        for (int i = 0; i <= segments; i++) {
            double phi = 2 * Math.PI * i / segments;

            List<Point2D> parallel = new ArrayList<>();
            for (int j = 0; j <= segments; j++) {
                double theta = Math.PI * j / segments;
                double x = MonteCarloIntegration.A * Math.sin(theta) * Math.cos(phi);
                double y = MonteCarloIntegration.B * Math.sin(theta) * Math.sin(phi);
                double z = MonteCarloIntegration.C * Math.cos(theta);
                parallel.add(project3D(x, y, z, centerX, centerY));
            }

            for (int j = 0; j < parallel.size() - 1; j++) {
                Point2D p1 = parallel.get(j);
                Point2D p2 = parallel.get(j + 1);
                g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
            }
        }
    }

    private void drawPoints(Graphics2D g2d, int centerX, int centerY) {
        for (MonteCarloIntegration.Point3D p : points) {
            Point2D projected = project3D(p.x, p.y, p.z, centerX, centerY);

            if (p.inside) {
                g2d.setColor(new Color(0, 200, 0, 180)); // Зеленый - внутри
            } else {
                g2d.setColor(new Color(255, 0, 0, 120)); // Красный - снаружи
            }

            int size = p.inside ? 3 : 2;
            g2d.fillOval((int)projected.x - size/2, (int)projected.y - size/2, size, size);
        }
    }

    private void drawLegend(Graphics2D g2d, int width) {
        int legendX = width - 200;
        int legendY = 100;

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.BLACK);
        g2d.drawString("Легенда:", legendX, legendY);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        // Точки внутри
        g2d.setColor(new Color(0, 200, 0));
        g2d.fillOval(legendX, legendY + 10, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Внутри эллипсоида", legendX + 20, legendY + 20);

        // Точки снаружи
        g2d.setColor(new Color(255, 0, 0));
        g2d.fillOval(legendX, legendY + 35, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Снаружи эллипсоида", legendX + 20, legendY + 45);

        // Каркас
        g2d.setColor(new Color(100, 100, 100));
        g2d.drawLine(legendX, legendY + 60, legendX + 10, legendY + 60);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Каркас эллипсоида", legendX + 20, legendY + 65);
    }

    // Проекция 3D точки на 2D с вращением
    private Point2D project3D(double x, double y, double z, int centerX, int centerY) {
        // Вращение вокруг оси X
        double y1 = y * Math.cos(angleX) - z * Math.sin(angleX);
        double z1 = y * Math.sin(angleX) + z * Math.cos(angleX);

        // Вращение вокруг оси Y
        double x2 = x * Math.cos(angleY) + z1 * Math.sin(angleY);
        double z2 = -x * Math.sin(angleY) + z1 * Math.cos(angleY);

        // Проекция на плоскость
        double screenX = centerX + x2 * scale;
        double screenY = centerY - y1 * scale;

        return new Point2D(screenX, screenY);
    }

    static class Point2D {
        double x, y;
        Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}

// Класс для отрисовки графика сходимости
class ConvergencePanel extends JPanel {
    private MonteCarloIntegration.Result[] testResults;
    private MonteCarloIntegration.Result[] chargeResults;

    public ConvergencePanel(MonteCarloIntegration.Result[] testResults,
                            MonteCarloIntegration.Result[] chargeResults) {
        this.testResults = testResults;
        this.chargeResults = chargeResults;
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 80;

        double minN = Math.log10(testResults[0].N);
        double maxN = Math.log10(testResults[testResults.length - 1].N);

        double maxError = 0;
        for (int i = 0; i < testResults.length; i++) {
            maxError = Math.max(maxError, testResults[i].getVolumeError());
            maxError = Math.max(maxError, chargeResults[i].getVolumeError());
        }
        maxError *= 1.2;

        // Оси
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(padding, height - padding, width - padding, height - padding);
        g2d.drawLine(padding, padding, padding, height - padding);

        // Заголовок
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Сходимость метода Монте-Карло", width / 2 - 150, 30);

        // Подписи осей
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Число испытаний N", width / 2 - 70, height - padding + 50);

        Graphics2D g2dRotate = (Graphics2D) g2d.create();
        g2dRotate.rotate(-Math.PI / 2);
        g2dRotate.drawString("Абсолютная погрешность объема", -height / 2 - 100, padding - 50);
        g2dRotate.dispose();

        // Метки на оси X
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        for (int i = 0; i < testResults.length; i++) {
            int N = testResults[i].N;
            double logN = Math.log10(N);
            int xPos = padding + (int)((width - 2 * padding) * (logN - minN) / (maxN - minN));

            g2d.drawLine(xPos, height - padding, xPos, height - padding + 5);
            String label = String.format("%d", N);
            g2d.drawString(label, xPos - 20, height - padding + 20);
        }

        // Метки на оси Y
        for (int i = 0; i <= 10; i++) {
            int yPos = height - padding - (int)((height - 2 * padding) * i / 10.0);
            g2d.drawLine(padding - 5, yPos, padding, yPos);
            String label = String.format("%.3f", maxError * i / 10.0);
            g2d.drawString(label, padding - 50, yPos + 5);
        }

        // График погрешности (тест)
        g2d.setColor(new Color(0, 0, 255));
        g2d.setStroke(new BasicStroke(2.5f));
        for (int i = 0; i < testResults.length - 1; i++) {
            int x1 = getXPos(testResults[i].N, minN, maxN, width, padding);
            int y1 = getYPos(testResults[i].getVolumeError(), maxError, height, padding);
            int x2 = getXPos(testResults[i + 1].N, minN, maxN, width, padding);
            int y2 = getYPos(testResults[i + 1].getVolumeError(), maxError, height, padding);
            g2d.drawLine(x1, y1, x2, y2);
        }

        for (int i = 0; i < testResults.length; i++) {
            int xPos = getXPos(testResults[i].N, minN, maxN, width, padding);
            int yPos = getYPos(testResults[i].getVolumeError(), maxError, height, padding);
            g2d.fillOval(xPos - 5, yPos - 5, 10, 10);
        }

        // График погрешности (заряд)
        g2d.setColor(new Color(255, 0, 0));
        for (int i = 0; i < chargeResults.length - 1; i++) {
            int x1 = getXPos(chargeResults[i].N, minN, maxN, width, padding);
            int y1 = getYPos(chargeResults[i].getVolumeError(), maxError, height, padding);
            int x2 = getXPos(chargeResults[i + 1].N, minN, maxN, width, padding);
            int y2 = getYPos(chargeResults[i + 1].getVolumeError(), maxError, height, padding);
            g2d.drawLine(x1, y1, x2, y2);
        }

        for (int i = 0; i < chargeResults.length; i++) {
            int xPos = getXPos(chargeResults[i].N, minN, maxN, width, padding);
            int yPos = getYPos(chargeResults[i].getVolumeError(), maxError, height, padding);
            g2d.fillOval(xPos - 5, yPos - 5, 10, 10);
        }

        // Легенда
        int legendX = width - padding - 250;
        int legendY = padding + 40;
        g2d.setFont(new Font("Arial", Font.PLAIN, 13));

        g2d.setColor(new Color(0, 0, 255));
        g2d.fillOval(legendX, legendY - 5, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Тест: объем эллипсоида", legendX + 20, legendY + 5);

        g2d.setColor(new Color(255, 0, 0));
        g2d.fillOval(legendX, legendY + 20, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Основная задача: объемный заряд", legendX + 20, legendY + 30);
    }

    private int getXPos(int N, double minN, double maxN, int width, int padding) {
        double logN = Math.log10(N);
        return padding + (int)((width - 2 * padding) * (logN - minN) / (maxN - minN));
    }

    private int getYPos(double error, double maxError, int height, int padding) {
        return height - padding - (int)((height - 2 * padding) * error / maxError);
    }
}
