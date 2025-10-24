package com.example;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class NewtonInterpolationSecondOrder {
    // Функция из задачи: y(x) = (cos(pi * x^(1/m)))^k
    public static double f(double x, double k, double m) {
        double val = Math.cos(Math.PI * Math.pow(x, 1.0 / m));
        return Math.pow(val, k);
    }

    // Построение узлов и значений функции
    // n - число интервалов на [0,1], number of nodes = n+1
    public static Result runInterpolation(int n, double k, double m) {
        int nodes = n + 1;
        double h = 1.0 / n;
        double[] x = new double[nodes];
        double[] y = new double[nodes];

        for (int i = 0; i < nodes; i++) {
            x[i] = i * h;
            y[i] = f(x[i], k, m);
        }

        // Первые конечные разности Δy[i] = y[i+1] - y[i], размер n
        double[] delta1 = new double[n];
        for (int i = 0; i < n; i++) delta1[i] = y[i + 1] - y[i];

        // Вторые конечные разности Δ^2 y[i] = Δy[i+1] - Δy[i], размер n-1
        double[] delta2 = new double[Math.max(0, n - 1)];
        for (int i = 0; i < n - 1; i++) delta2[i] = delta1[i + 1] - delta1[i];

        // Интерполируем в серединах отрезков: x_mid = x[i] + h/2, i=0..n-2 (требуется Δ^2 y[i])
        int midCount = Math.max(0, n - 1);
        double[] xMid = new double[midCount];
        double[] pMid = new double[midCount];
        double[] yTrue = new double[midCount];
        double[] absErr = new double[midCount];

        for (int i = 0; i < midCount; i++) {
            double xm = x[i] + h / 2.0;
            double t = (xm - x[i]) / h; // = 0.5
            // P = y_i + t*Δy_i + t*(t-1)/2 * Δ^2 y_i
            double P = y[i] + t * delta1[i] + (t * (t - 1.0) / 2.0) * delta2[i];
            double yt = f(xm, k, m);
            xMid[i] = xm;
            pMid[i] = P;
            yTrue[i] = yt;
            absErr[i] = Math.abs(P - yt);
        }

        // Оценки ошибок
        double epsMax = 0.0;
        double mse = 0.0;
        for (int i = 0; i < midCount; i++) {
            if (absErr[i] > epsMax) epsMax = absErr[i];
            mse += absErr[i] * absErr[i];
        }
        if (midCount > 0) mse /= midCount;
        double rmse = Math.sqrt(mse);

        return new Result(n, h, x, y, delta1, delta2, xMid, pMid, yTrue, absErr, epsMax, mse, rmse);
    }

    // Структура для результата
    public static class Result {
        public final int n;
        public final double h;
        public final double[] x, y, delta1, delta2;
        public final double[] xMid, pMid, yTrue, absErr;
        public final double epsMax, mse, rmse;

        public Result(int n, double h, double[] x, double[] y, double[] delta1, double[] delta2,
                      double[] xMid, double[] pMid, double[] yTrue, double[] absErr,
                      double epsMax, double mse, double rmse) {
            this.n = n;
            this.h = h;
            this.x = x;
            this.y = y;
            this.delta1 = delta1;
            this.delta2 = delta2;
            this.xMid = xMid;
            this.pMid = pMid;
            this.yTrue = yTrue;
            this.absErr = absErr;
            this.epsMax = epsMax;
            this.mse = mse;
            this.rmse = rmse;
        }
    }

    // Печать подробного отчета для одного запуска
    public static void printReport(Result r) {
        System.out.println("---------- n = " + r.n + " (узлов " + (r.n + 1) + "), h = " + r.h + " ----------");
        System.out.println("Узлы x_j и y_j:");
        for (int i = 0; i < r.x.length; i++) {
            System.out.printf("x[%d]=%.6f\t y[%d]=%.10f%n", i, r.x[i], i, r.y[i]);
        }

        System.out.println("\nПервые конечные разности Δy:");
        for (int i = 0; i < r.delta1.length; i++) {
            System.out.printf("Δy[%d]=%.10e%n", i, r.delta1[i]);
        }

        System.out.println("\nВторые конечные разности Δ^2 y:");
        for (int i = 0; i < r.delta2.length; i++) {
            System.out.printf("Δ^2 y[%d]=%.10e%n", i, r.delta2[i]);
        }

        System.out.println("\nИнтерполяция в точках с полуцелыми индексами (середины отрезков):");
        System.out.println("i\t x_mid\t\t P(x)\t\t y_true\t\t |err|");
        for (int i = 0; i < r.xMid.length; i++) {
            System.out.printf("%d\t %.6f\t %.10f\t %.10f\t %.10e%n",
                    i, r.xMid[i], r.pMid[i], r.yTrue[i], r.absErr[i]);
        }

        System.out.println("\nПогрешности:");
        System.out.printf("ε_max = %.10e%n", r.epsMax);
        System.out.printf("MSE = %.10e%n", r.mse);
        System.out.printf("RMSE = ε_m = %.10e%n", r.rmse);
        System.out.println("-----------------------------------------------------\n");
    }

    // Класс для отрисовки графика
    static class GraphPanel extends JPanel {
        private Result result;
        private double k, m;

        public GraphPanel(Result result, double k, double m) {
            this.result = result;
            this.k = k;
            this.m = m;
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
            int padding = 50;
            int labelPadding = 25;

            // Найдем минимум и максимум для масштабирования
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;

            for (int i = 0; i < result.y.length; i++) {
                minY = Math.min(minY, result.y[i]);
                maxY = Math.max(maxY, result.y[i]);
            }
            for (int i = 0; i < result.yTrue.length; i++) {
                minY = Math.min(minY, result.yTrue[i]);
                maxY = Math.max(maxY, result.yTrue[i]);
            }

            // Добавим небольшой запас
            double yRange = maxY - minY;
            minY -= yRange * 0.1;
            maxY += yRange * 0.1;

            // Рисуем оси
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(padding, height - padding, width - padding, height - padding); // X ось
            g2.drawLine(padding, padding, padding, height - padding); // Y ось

            // Подписи осей
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString("x", width - padding + 10, height - padding + 5);
            g2.drawString("y(x)", padding - 10, padding - 10);

            // Заголовок
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            String title = String.format("Интерполяция Ньютона 2-го порядка (n=%d)", result.n);
            FontMetrics metrics = g2.getFontMetrics();
            int titleWidth = metrics.stringWidth(title);
            g2.drawString(title, (width - titleWidth) / 2, 30);

            // Рисуем метки на осях
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            for (int i = 0; i <= 10; i++) {
                double xVal = i / 10.0;
                int x = padding + (int)((width - 2 * padding) * xVal);
                g2.drawLine(x, height - padding, x, height - padding + 5);
                g2.drawString(String.format("%.1f", xVal), x - 10, height - padding + 20);

                double yVal = minY + (maxY - minY) * i / 10.0;
                int y = height - padding - (int)((height - 2 * padding) * i / 10.0);
                g2.drawLine(padding - 5, y, padding, y);
                g2.drawString(String.format("%.2f", yVal), padding - 40, y + 5);
            }

            // Рисуем исходную функцию как плавную кривую (синяя линия)
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2f));

            GeneralPath path = new GeneralPath();
            boolean firstPoint = true;

            for (double x = 0; x <= 1.0; x += 0.002) {
                double y = f(x, k, m);
                int px = padding + (int)((width - 2 * padding) * x);
                int py = height - padding - (int)((height - 2 * padding) * (y - minY) / (maxY - minY));

                if (firstPoint) {
                    path.moveTo(px, py);
                    firstPoint = false;
                } else {
                    path.lineTo(px, py);
                }
            }

            g2.draw(path);

            // Рисуем узлы интерполяции (красные точки)
            g2.setColor(Color.RED);
            for (int i = 0; i < result.x.length; i++) {
                int px = padding + (int)((width - 2 * padding) * result.x[i]);
                int py = height - padding - (int)((height - 2 * padding) * (result.y[i] - minY) / (maxY - minY));
                g2.fillOval(px - 4, py - 4, 8, 8);
            }

            // Рисуем интерполированные точки (зеленые точки)
            g2.setColor(new Color(0, 150, 0));
            for (int i = 0; i < result.xMid.length; i++) {
                int px = padding + (int)((width - 2 * padding) * result.xMid[i]);
                int py = height - padding - (int)((height - 2 * padding) * (result.pMid[i] - minY) / (maxY - minY));
                g2.fillOval(px - 4, py - 4, 8, 8);
            }

            // Легенда
            int legendX = width - padding - 180;
            int legendY = padding + 20;
            g2.setFont(new Font("Arial", Font.PLAIN, 12));

            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(legendX, legendY, legendX + 30, legendY);
            g2.setColor(Color.BLACK);
            g2.drawString("Исходная функция", legendX + 35, legendY + 5);

            g2.setColor(Color.RED);
            g2.fillOval(legendX + 10, legendY + 15, 8, 8);
            g2.setColor(Color.BLACK);
            g2.drawString("Узлы интерполяции", legendX + 35, legendY + 23);

            g2.setColor(new Color(0, 150, 0));
            g2.fillOval(legendX + 10, legendY + 35, 8, 8);
            g2.setColor(Color.BLACK);
            g2.drawString("Интерполяция P(x)", legendX + 35, legendY + 43);
        }
    }

    // Метод для отображения графика
    public static void displayChart(Result r, double k, double m) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("График функции и интерполяции (n=" + r.n + ")");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new GraphPanel(r, k, m));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Интерполяция Ньютона 2-го порядка на [0,1] (узлы равноотстоящие).");
        System.out.print("Введите k (степень косинуса, double, например 2): ");
        double k = sc.nextDouble();
        System.out.print("Введите m (показатель корня в аргументе косинуса, double, например 1): ");
        double m = sc.nextDouble();

        System.out.print("Введите через пробел несколько значений n (число интервалов) для исследования (например: 4 8 16 32): ");
        sc.nextLine();
        String line = sc.nextLine().trim();
        if (line.isEmpty()) {
            System.out.println("Нет значений n — завершаем.");
            return;
        }
        String[] tokens = line.split("\\s+");
        int[] ns = Arrays.stream(tokens).mapToInt(Integer::parseInt).toArray();

        // Для каждого n запускаем и печатаем результат
        System.out.println("\nЗапуск для заданных n...");
        for (int n : ns) {
            if (n < 2) {
                System.out.println("n должно быть >= 2 (нужно хотя бы 3 узла для Δ^2). Пропускаем n=" + n);
                continue;
            }
            Result r = runInterpolation(n, k, m);
            printReport(r);

            // Отображаем график для каждого n
            displayChart(r, k, m);
        }
        String yn = sc.next().trim().toLowerCase();
        }
    }
