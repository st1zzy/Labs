package com.example;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Scanner;

public class LeastSquaresApproximation {
    // Функция для аппроксимации
    public static double f(double x, int q) {
        return Math.log(1 + Math.pow(x, q));
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Введите q (1, 2 или 3): ");
        int q = sc.nextInt();

        System.out.print("Введите n (число узлов, например 10): ");
        int n = sc.nextInt();

        double a = 0.0, b = 1.0; // интервал аппроксимации
        double[] x = new double[n + 1];
        double[] y = new double[n + 1];
        double h = (b - a) / n;

        // Заполняем массивы x и y
        for (int i = 0; i <= n; i++) {
            x[i] = a + i * h;
            y[i] = f(x[i], q);
        }

        // Считаем суммы для системы
        double Sx = 0, Sx2 = 0, Sx3 = 0, Sx4 = 0;
        double Sy = 0, Sxy = 0, Sx2y = 0;
        for (int i = 0; i <= n; i++) {
            Sx += x[i];
            Sx2 += x[i] * x[i];
            Sx3 += x[i] * x[i] * x[i];
            Sx4 += x[i] * x[i] * x[i] * x[i];
            Sy += y[i];
            Sxy += x[i] * y[i];
            Sx2y += x[i] * x[i] * y[i];
        }

        // Составляем матрицу и правую часть
        double[][] A = {
                {n + 1, Sx, Sx2},
                {Sx, Sx2, Sx3},
                {Sx2, Sx3, Sx4}
        };
        double[] B = {Sy, Sxy, Sx2y};

        // Решаем систему методом Крамера
        double detA = determinant3x3(A);
        double[] coeffs = new double[3];
        for (int i = 0; i < 3; i++) {
            double[][] Ai = replaceColumn(A, B, i);
            coeffs[i] = determinant3x3(Ai) / detA;
        }

        System.out.printf("Аппроксимирующий многочлен: φ(x) = %.6f + %.6f x + %.6f x^2\n", coeffs[0], coeffs[1], coeffs[2]);

        // Оценка погрешности
        double mse = 0;
        for (int i = 0; i <= n; i++) {
            double approx = coeffs[0] + coeffs[1] * x[i] + coeffs[2] * x[i] * x[i];
            mse += Math.pow(approx - y[i], 2);
        }
        mse /= (n + 1);
        System.out.printf("Среднеквадратичная ошибка: %.8f\n", mse);

        // Визуализация
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Аппроксимация методом наименьших квадратов");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new GraphPanel(x, y, coeffs, q));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Определитель 3x3
    public static double determinant3x3(double[][] m) {
        return m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
                - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
                + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);
    }

    // Заменить столбец в матрице
    public static double[][] replaceColumn(double[][] A, double[] B, int col) {
        double[][] res = new double[3][3];
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                res[i][j] = (j == col) ? B[i] : A[i][j];
        return res;
    }

    // Класс для отрисовки графика
    static class GraphPanel extends JPanel {
        double[] x, y, coeffs;
        int q;
        public GraphPanel(double[] x, double[] y, double[] coeffs, int q) {
            this.x = x;
            this.y = y;
            this.coeffs = coeffs;
            this.q = q;
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
            int padding = 60;

            // Найти min/max по Y для масштабирования
            double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
            for (double xi = 0; xi <= 1.0; xi += 0.01) {
                double yf = f(xi, q);
                double ya = coeffs[0] + coeffs[1] * xi + coeffs[2] * xi * xi;
                minY = Math.min(minY, Math.min(yf, ya));
                maxY = Math.max(maxY, Math.max(yf, ya));
            }
            minY -= 0.1 * (maxY - minY);
            maxY += 0.1 * (maxY - minY);

            // Оси
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(padding, height - padding, width - padding, height - padding); // X
            g2.drawLine(padding, padding, padding, height - padding); // Y

            // Подписи
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString("x", width - padding + 10, height - padding + 5);
            g2.drawString("y", padding - 20, padding - 10);

            // Метки
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            for (int i = 0; i <= 10; i++) {
                double xVal = i / 10.0;
                int xPix = padding + (int)((width - 2 * padding) * xVal);
                g2.drawLine(xPix, height - padding, xPix, height - padding + 5);
                g2.drawString(String.format("%.1f", xVal), xPix - 10, height - padding + 20);
            }
            for (int i = 0; i <= 10; i++) {
                double yVal = minY + (maxY - minY) * i / 10.0;
                int yPix = height - padding - (int)((height - 2 * padding) * (yVal - minY) / (maxY - minY));
                g2.drawLine(padding - 5, yPix, padding, yPix);
                g2.drawString(String.format("%.2f", yVal), padding - 45, yPix + 5);
            }

            // График исходной функции (синяя линия)
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2f));
            GeneralPath path = new GeneralPath();
            boolean first = true;
            for (double xi = 0; xi <= 1.0; xi += 0.002) {
                double yf = f(xi, q);
                int xPix = padding + (int)((width - 2 * padding) * xi);
                int yPix = height - padding - (int)((height - 2 * padding) * (yf - minY) / (maxY - minY));
                if (first) {
                    path.moveTo(xPix, yPix);
                    first = false;
                } else {
                    path.lineTo(xPix, yPix);
                }
            }
            g2.draw(path);

            // График аппроксимирующего многочлена (красная линия)
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(2f));
            path = new GeneralPath();
            first = true;
            for (double xi = 0; xi <= 1.0; xi += 0.002) {
                double ya = coeffs[0] + coeffs[1] * xi + coeffs[2] * xi * xi;
                int xPix = padding + (int)((width - 2 * padding) * xi);
                int yPix = height - padding - (int)((height - 2 * padding) * (ya - minY) / (maxY - minY));
                if (first) {
                    path.moveTo(xPix, yPix);
                    first = false;
                } else {
                    path.lineTo(xPix, yPix);
                }
            }
            g2.draw(path);

            // Узловые точки (черные кружки)
            g2.setColor(Color.BLACK);
            for (int i = 0; i < x.length; i++) {
                int xPix = padding + (int)((width - 2 * padding) * x[i]);
                int yPix = height - padding - (int)((height - 2 * padding) * (y[i] - minY) / (maxY - minY));
                g2.fillOval(xPix - 4, yPix - 4, 8, 8);
            }

            // Легенда
            int legendX = width - padding - 200;
            int legendY = padding + 20;
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.setColor(Color.BLUE);
            g2.drawLine(legendX, legendY, legendX + 30, legendY);
            g2.setColor(Color.BLACK);
            g2.drawString("Исходная функция", legendX + 35, legendY + 5);

            g2.setColor(Color.RED);
            g2.drawLine(legendX, legendY + 20, legendX + 30, legendY + 20);
            g2.setColor(Color.BLACK);
            g2.drawString("Аппроксимация", legendX + 35, legendY + 25);

            g2.setColor(Color.BLACK);
            g2.fillOval(legendX + 10, legendY + 35, 8, 8);
            g2.drawString("Узлы", legendX + 35, legendY + 43);
        }
    }
}
