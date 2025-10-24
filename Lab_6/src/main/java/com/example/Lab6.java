package com.example;
import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class Lab6 {

    // Функция f(x) = 1 / |3 + 2*cos(x/2)|
    private static double f(double x) {
        return 1.0 / Math.abs(3.0 + 2.0 * Math.cos(x / 2.0));
    }

    // Метод прямоугольников (средних точек)
    public static double rectangleMethod(Function<Double, Double> func, double a, double b, int n) {
        double h = (b - a) / n;
        double sum = 0.0;

        for (int i = 0; i < n; i++) {
            double xi = a + (i + 0.5) * h;
            sum += func.apply(xi);
        }

        return h * sum;
    }

    // Метод Гаусса
    public static double gaussMethod(Function<Double, Double> func, double a, double b, int m) {
        double[] t = new double[m];
        double[] A = new double[m];

        switch (m) {
            case 3:
                t = new double[]{0.112701665, 0.500000000, 0.887298335};
                A = new double[]{0.277777778, 0.444444444, 0.277777778};
                break;
            case 5:
                t = new double[]{0.046910077, 0.230765345, 0.500000000, 0.769234655, 0.953089923};
                A = new double[]{0.118463443, 0.239314335, 0.284444444, 0.239314335, 0.118463443};
                break;
            case 7:
                t = new double[]{0.025446044, 0.129234407, 0.297077424, 0.500000000,
                        0.702922576, 0.870765593, 0.974553956};
                A = new double[]{0.064742483, 0.139852696, 0.190915025, 0.208979592,
                        0.190915025, 0.139852696, 0.064742483};
                break;
            case 9:
                t = new double[]{0.015919880, 0.081934446, 0.193314284, 0.337873288,
                        0.500000000, 0.662126712, 0.806685716, 0.918065554, 0.984080120};
                A = new double[]{0.040637194, 0.090324080, 0.130305348, 0.156173539,
                        0.165119678, 0.156173539, 0.130305348, 0.090324080, 0.040637194};
                break;
            case 11:
                t = new double[]{0.010885671, 0.056468700, 0.134923997, 0.240451935,
                        0.365228422, 0.500000000, 0.634771578, 0.759548065,
                        0.865076003, 0.943531300, 0.989114329};
                A = new double[]{0.027834284, 0.062790185, 0.093145105, 0.116596882,
                        0.131402272, 0.136462543, 0.131402272, 0.116596882,
                        0.093145105, 0.062790185, 0.027834284};
                break;
            default:
                throw new IllegalArgumentException("Количество узлов m должно быть 3, 5, 7, 9 или 11");
        }

        double sum = 0.0;
        for (int i = 0; i < m; i++) {
            double x = a + (b - a) * t[i];
            sum += A[i] * func.apply(x);
        }

        return (b - a) * sum;
    }

    // Панель для рисования графика функции
    static class FunctionPanel extends JPanel {
        private double a, b;

        public FunctionPanel(double a, double b) {
            this.a = a;
            this.b = b;
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int margin = 60;

            // Вычисление диапазона значений функции
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;
            int numPoints = 200;

            for (int i = 0; i <= numPoints; i++) {
                double x = a + (b - a) * i / numPoints;
                double y = f(x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }

            double yRange = maxY - minY;
            minY -= yRange * 0.1;
            maxY += yRange * 0.1;

            // Рисование осей
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(margin, height - margin, width - margin, height - margin); // Ось X
            g2.drawLine(margin, margin, margin, height - margin); // Ось Y

            // Стрелки осей
            int[] xArrow = {width - margin, width - margin - 10, width - margin - 10};
            int[] yArrow = {height - margin, height - margin - 5, height - margin + 5};
            g2.fillPolygon(xArrow, yArrow, 3);

            int[] xArrow2 = {margin, margin - 5, margin + 5};
            int[] yArrow2 = {margin, margin + 10, margin + 10};
            g2.fillPolygon(xArrow2, yArrow2, 3);

            // Метки на оси X
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            for (int i = 0; i <= 5; i++) {
                double x = a + (b - a) * i / 5;
                int px = margin + (width - 2 * margin) * i / 5;
                g2.drawLine(px, height - margin, px, height - margin + 5);
                g2.drawString(String.format("%.2f", x), px - 15, height - margin + 20);
            }

            // Метки на оси Y
            for (int i = 0; i <= 5; i++) {
                double y = minY + (maxY - minY) * i / 5;
                int py = height - margin - (height - 2 * margin) * i / 5;
                g2.drawLine(margin - 5, py, margin, py);
                g2.drawString(String.format("%.3f", y), margin - 45, py + 5);
            }

            // Рисование сетки
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(1));
            for (int i = 1; i < 5; i++) {
                int px = margin + (width - 2 * margin) * i / 5;
                g2.drawLine(px, margin, px, height - margin);

                int py = height - margin - (height - 2 * margin) * i / 5;
                g2.drawLine(margin, py, width - margin, py);
            }

            // Рисование графика функции
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2));

            for (int i = 0; i < numPoints; i++) {
                double x1 = a + (b - a) * i / numPoints;
                double x2 = a + (b - a) * (i + 1) / numPoints;
                double y1 = f(x1);
                double y2 = f(x2);

                int px1 = margin + (int)((x1 - a) / (b - a) * (width - 2 * margin));
                int py1 = height - margin - (int)((y1 - minY) / (maxY - minY) * (height - 2 * margin));
                int px2 = margin + (int)((x2 - a) / (b - a) * (width - 2 * margin));
                int py2 = height - margin - (int)((y2 - minY) / (maxY - minY) * (height - 2 * margin));

                g2.drawLine(px1, py1, px2, py2);
            }

            // Подписи
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("График функции f(x) = 1/|3+2cos(x/2)|", width / 2 - 150, 30);
            g2.drawString("x", width - margin + 10, height - margin + 5);
            g2.drawString("f(x)", margin - 5, margin - 10);
        }
    }

    // Панель для рисования графика сравнения методов
    static class ComparisonPanel extends JPanel {
        private double[] rectangleResults;
        private int[] nValues;
        private double[] gaussResults;
        private int[] mValues;

        public ComparisonPanel(double[] rectangleResults, int[] nValues,
                               double[] gaussResults, int[] mValues) {
            this.rectangleResults = rectangleResults;
            this.nValues = nValues;
            this.gaussResults = gaussResults;
            this.mValues = mValues;
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int margin = 80;

            // Вычисление диапазона значений
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;

            for (double val : rectangleResults) {
                minY = Math.min(minY, val);
                maxY = Math.max(maxY, val);
            }
            for (double val : gaussResults) {
                minY = Math.min(minY, val);
                maxY = Math.max(maxY, val);
            }

            double yRange = maxY - minY;
            minY -= yRange * 0.1;
            maxY += yRange * 0.1;

            double maxX = Math.max(nValues[nValues.length - 1], mValues[mValues.length - 1]);

            // Рисование осей
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(margin, height - margin, width - margin, height - margin); // Ось X
            g2.drawLine(margin, margin, margin, height - margin); // Ось Y

            // Стрелки осей
            int[] xArrow = {width - margin, width - margin - 10, width - margin - 10};
            int[] yArrow = {height - margin, height - margin - 5, height - margin + 5};
            g2.fillPolygon(xArrow, yArrow, 3);

            int[] xArrow2 = {margin, margin - 5, margin + 5};
            int[] yArrow2 = {margin, margin + 10, margin + 10};
            g2.fillPolygon(xArrow2, yArrow2, 3);

            // Метки на оси X
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            for (int i = 0; i <= 5; i++) {
                double x = maxX * i / 5;
                int px = margin + (width - 2 * margin) * i / 5;
                g2.drawLine(px, height - margin, px, height - margin + 5);
                g2.drawString(String.format("%.0f", x), px - 10, height - margin + 20);
            }

            // Метки на оси Y
            for (int i = 0; i <= 5; i++) {
                double y = minY + (maxY - minY) * i / 5;
                int py = height - margin - (height - 2 * margin) * i / 5;
                g2.drawLine(margin - 5, py, margin, py);
                g2.drawString(String.format("%.6f", y), margin - 70, py + 5);
            }

            // Рисование сетки
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(1));
            for (int i = 1; i < 5; i++) {
                int px = margin + (width - 2 * margin) * i / 5;
                g2.drawLine(px, margin, px, height - margin);

                int py = height - margin - (height - 2 * margin) * i / 5;
                g2.drawLine(margin, py, width - margin, py);
            }

            // Рисование графика метода прямоугольников
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2));
            for (int i = 0; i < nValues.length - 1; i++) {
                int x1 = margin + (int)(nValues[i] / maxX * (width - 2 * margin));
                int y1 = height - margin - (int)((rectangleResults[i] - minY) / (maxY - minY) * (height - 2 * margin));
                int x2 = margin + (int)(nValues[i + 1] / maxX * (width - 2 * margin));
                int y2 = height - margin - (int)((rectangleResults[i + 1] - minY) / (maxY - minY) * (height - 2 * margin));
                g2.drawLine(x1, y1, x2, y2);
            }

            // Точки для метода прямоугольников
            for (int i = 0; i < nValues.length; i++) {
                int x = margin + (int)(nValues[i] / maxX * (width - 2 * margin));
                int y = height - margin - (int)((rectangleResults[i] - minY) / (maxY - minY) * (height - 2 * margin));
                g2.fillOval(x - 4, y - 4, 8, 8);
            }

            // Рисование графика метода Гаусса
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(2));
            for (int i = 0; i < mValues.length - 1; i++) {
                int x1 = margin + (int)(mValues[i] / maxX * (width - 2 * margin));
                int y1 = height - margin - (int)((gaussResults[i] - minY) / (maxY - minY) * (height - 2 * margin));
                int x2 = margin + (int)(mValues[i + 1] / maxX * (width - 2 * margin));
                int y2 = height - margin - (int)((gaussResults[i + 1] - minY) / (maxY - minY) * (height - 2 * margin));
                g2.drawLine(x1, y1, x2, y2);
            }

            // Точки для метода Гаусса
            for (int i = 0; i < mValues.length; i++) {
                int x = margin + (int)(mValues[i] / maxX * (width - 2 * margin));
                int y = height - margin - (int)((gaussResults[i] - minY) / (maxY - minY) * (height - 2 * margin));
                g2.fillOval(x - 4, y - 4, 8, 8);
            }

            // Заголовок
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("Зависимость значения интеграла от числа узлов", width / 2 - 200, 30);
            g2.drawString("Число узлов", width / 2 - 40, height - 20);

            // Повернутая надпись для оси Y
            g2.rotate(-Math.PI / 2);
            g2.drawString("Значение интеграла", -height / 2 - 80, 20);
            g2.rotate(Math.PI / 2);

            // Легенда
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.setColor(Color.BLUE);
            g2.fillRect(width - 250, 60, 20, 10);
            g2.setColor(Color.BLACK);
            g2.drawString("Метод прямоугольников", width - 225, 70);

            g2.setColor(Color.RED);
            g2.fillRect(width - 250, 80, 20, 10);
            g2.setColor(Color.BLACK);
            g2.drawString("Метод Гаусса", width - 225, 90);
        }
    }

    public static void main(String[] args) {
        double a = 0.0;
        double b = Math.PI;

        System.out.println("ЧИСЛЕННОЕ ИНТЕГРИРОВАНИЕ");
        System.out.println("Функция: f(x) = 1 / |3 + 2*cos(x/2)|");
        System.out.printf("Пределы интегрирования: [%.4f, %.4f]\n\n", a, b);

        // Метод прямоугольников
        System.out.println("МЕТОД ПРЯМОУГОЛЬНИКОВ:");
        System.out.println("----------------------------------------------");
        System.out.printf("%-15s %-20s\n", "Число узлов", "Значение интеграла");
        System.out.println("----------------------------------------------");

        int[] nValues = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        double[] rectangleResults = new double[nValues.length];

        for (int i = 0; i < nValues.length; i++) {
            rectangleResults[i] = rectangleMethod(Lab6::f, a, b, nValues[i]);
            System.out.printf("%-15d %-20.10f\n", nValues[i], rectangleResults[i]);
        }

        // Метод Гаусса
        System.out.println("\n\nМЕТОД ГАУССА:");
        System.out.println("----------------------------------------------");
        System.out.printf("%-15s %-20s\n", "Число узлов", "Значение интеграла");
        System.out.println("----------------------------------------------");

        int[] mValues = {3, 5, 7, 9, 11};
        double[] gaussResults = new double[mValues.length];

        for (int i = 0; i < mValues.length; i++) {
            gaussResults[i] = gaussMethod(Lab6::f, a, b, mValues[i]);
            System.out.printf("%-15d %-20.10f\n", mValues[i], gaussResults[i]);
        }

        // Сравнение методов
        System.out.println("\n\nСРАВНЕНИЕ МЕТОДОВ:");
        System.out.println("----------------------------------------------");
        double rectangleResult = rectangleResults[rectangleResults.length - 1];
        double gaussResult = gaussResults[gaussResults.length - 1];

        System.out.printf("Метод прямоугольников (n=100): %.10f\n", rectangleResult);
        System.out.printf("Метод Гаусса (m=11):            %.10f\n", gaussResult);
        System.out.printf("Разница:                        %.10e\n", Math.abs(rectangleResult - gaussResult));

        // Создание окон с графиками
        SwingUtilities.invokeLater(() -> {
            // График функции
            JFrame functionFrame = new JFrame("График функции");
            functionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            functionFrame.add(new FunctionPanel(a, b));
            functionFrame.pack();
            functionFrame.setLocationRelativeTo(null);
            functionFrame.setVisible(true);

            // График сравнения методов
            JFrame comparisonFrame = new JFrame("Сравнение методов интегрирования");
            comparisonFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            comparisonFrame.add(new ComparisonPanel(rectangleResults, nValues, gaussResults, mValues));
            comparisonFrame.pack();
            comparisonFrame.setLocation(100, 100);
            comparisonFrame.setVisible(true);
        });
    }
}
