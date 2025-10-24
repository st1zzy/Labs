package com.example;
import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class NumericalDifferentiation {

    // Параметры
    private static final int N = 50; // количество интервалов (можно менять: 20, 50, 100)
    private static final double H = 1.0 / N; // шаг сетки

    // Исходная функция: y(x) = cos(π*x²/2)
    public static double function(double x) {
        return Math.cos(Math.PI * x * x / 2.0);
    }

    // Точная первая производная: y'(x) = -π*x*sin(π*x²/2)
    public static double firstDerivativeExact(double x) {
        return -Math.PI * x * Math.sin(Math.PI * x * x / 2.0);
    }

    // Точная вторая производная: y''(x) = -π*sin(π*x²/2) - π²*x²*cos(π*x²/2)
    public static double secondDerivativeExact(double x) {
        return -Math.PI * Math.sin(Math.PI * x * x / 2.0)
                - Math.PI * Math.PI * x * x * Math.cos(Math.PI * x * x / 2.0);
    }

    public static void main(String[] args) {
        // Блок 2: Начальные данные
        double[] x = new double[N + 1];
        double[] y = new double[N + 1];

        // Блок 3-4-5: Цикл вычисления узлов и значений функции
        for (int j = 0; j <= N; j++) {
            x[j] = j * H;
            y[j] = function(x[j]);
        }

        // Массивы для производных
        double[] yFirstExact = new double[N + 1];
        double[] ySecondExact = new double[N + 1];
        double[] yFirstApprox = new double[N + 1];
        double[] ySecondApprox = new double[N + 1];

        // Блок 6-7-8: Цикл вычисления производных
        // Первая производная
        yFirstApprox[0] = (-3*y[0] + 4*y[1] - y[2]) / (2*H);
        for (int j = 1; j < N; j++) {
            yFirstApprox[j] = (y[j+1] - y[j-1]) / (2*H);
        }
        yFirstApprox[N] = (3*y[N] - 4*y[N-1] + y[N-2]) / (2*H);

        // Вторая производная
        ySecondApprox[0] = (2*y[0] - 5*y[1] + 4*y[2] - y[3]) / (H*H);
        for (int j = 1; j < N; j++) {
            ySecondApprox[j] = (y[j+1] - 2*y[j] + y[j-1]) / (H*H);
        }
        ySecondApprox[N] = (2*y[N] - 5*y[N-1] + 4*y[N-2] - y[N-3]) / (H*H);

        // Точные значения и погрешности
        double[] errorFirst = new double[N + 1];
        double[] errorSecond = new double[N + 1];

        for (int j = 0; j <= N; j++) {
            yFirstExact[j] = firstDerivativeExact(x[j]);
            ySecondExact[j] = secondDerivativeExact(x[j]);
            errorFirst[j] = Math.abs(yFirstExact[j] - yFirstApprox[j]);
            errorSecond[j] = Math.abs(ySecondExact[j] - ySecondApprox[j]);
        }

        // Блок 9: Вычисление среднеквадратичной погрешности
        double epsilon1Max = 0, epsilon2Max = 0;
        int j1Max = 0, j2Max = 0;
        double sumSquareFirst = 0, sumSquareSecond = 0;

        for (int j = 0; j <= N; j++) {
            if (errorFirst[j] > epsilon1Max) {
                epsilon1Max = errorFirst[j];
                j1Max = j;
            }
            if (errorSecond[j] > epsilon2Max) {
                epsilon2Max = errorSecond[j];
                j2Max = j;
            }
            sumSquareFirst += errorFirst[j] * errorFirst[j];
            sumSquareSecond += errorSecond[j] * errorSecond[j];
        }

        double epsilon1Rms = Math.sqrt(sumSquareFirst / (N + 1));
        double epsilon2Rms = Math.sqrt(sumSquareSecond / (N + 1));

        // Блок 10: Вывод результатов
        printResults(x, y, yFirstExact, yFirstApprox, errorFirst,
                ySecondExact, ySecondApprox, errorSecond,
                epsilon1Max, j1Max, epsilon1Rms,
                epsilon2Max, j2Max, epsilon2Rms);

        // Сохранение в CSV
        try {
            saveResultsToCSV(x, y, yFirstExact, yFirstApprox, errorFirst,
                    ySecondExact, ySecondApprox, errorSecond);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении файла: " + e.getMessage());
        }

        // Построение графиков
        SwingUtilities.invokeLater(() -> {
            createCombinedGraph(x, y, yFirstExact, yFirstApprox, ySecondExact, ySecondApprox);
        });
    }

    // Метод вывода результатов
    private static void printResults(double[] x, double[] y,
                                     double[] yFirstExact, double[] yFirstApprox, double[] errorFirst,
                                     double[] ySecondExact, double[] ySecondApprox, double[] errorSecond,
                                     double epsilon1Max, int j1Max, double epsilon1Rms,
                                     double epsilon2Max, int j2Max, double epsilon2Rms) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║       ЧИСЛЕННОЕ ДИФФЕРЕНЦИРОВАНИЕ                         ║");
        System.out.println("║       Функция: y(x) = cos(πx²/2)                         ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println("\n📊 ПАРАМЕТРЫ:");
        System.out.println("   n = " + N);
        System.out.println("   h = " + H);

        System.out.println("\n" + "═".repeat(60));
        System.out.println("📈 РЕЗУЛЬТАТЫ ДЛЯ ПЕРВОЙ ПРОИЗВОДНОЙ:");
        System.out.println("═".repeat(60));
        System.out.printf("   Максимальная погрешность ε₁ₘₐₓ = %.6e%n", epsilon1Max);
        System.out.printf("   Узел с максимальной погрешностью: j₁ₘₐₓ = %d (x = %.4f)%n", j1Max, x[j1Max]);
        System.out.printf("   Среднеквадратичная погрешность ε₁ᵣₘₛ = %.6e%n", epsilon1Rms);

        System.out.println("\n" + "═".repeat(60));
        System.out.println("📉 РЕЗУЛЬТАТЫ ДЛЯ ВТОРОЙ ПРОИЗВОДНОЙ:");
        System.out.println("═".repeat(60));
        System.out.printf("   Максимальная погрешность ε₂ₘₐₓ = %.6e%n", epsilon2Max);
        System.out.printf("   Узел с максимальной погрешностью: j₂ₘₐₓ = %d (x = %.4f)%n", j2Max, x[j2Max]);
        System.out.printf("   Среднеквадратичная погрешность ε₂ᵣₘₛ = %.6e%n", epsilon2Rms);

        System.out.println("\n" + "═".repeat(120));
        System.out.println("📋 ТАБЛИЦА ЗНАЧЕНИЙ (первые 11 узлов):");
        System.out.println("═".repeat(120));
        System.out.printf("%-4s %-8s %-10s %-12s %-12s %-12s %-12s %-12s %-12s%n",
                "j", "x", "y", "y'_exact", "y'_approx", "ε₁", "y''_exact", "y''_approx", "ε₂");
        System.out.println("─".repeat(120));

        for (int j = 0; j <= Math.min(10, N); j++) {
            System.out.printf("%-4d %-8.4f %-10.6f %-12.6f %-12.6f %-12.6e %-12.6f %-12.6f %-12.6e%n",
                    j, x[j], y[j], yFirstExact[j], yFirstApprox[j], errorFirst[j],
                    ySecondExact[j], ySecondApprox[j], errorSecond[j]);
        }
        System.out.println("═".repeat(120));
        System.out.println("\n✅ Полные результаты сохранены в файл: 'numerical_differentiation_results.csv'");
        System.out.println("📊 График с функцией и производными отображен в окне\n");
    }

    // Метод сохранения в CSV
    private static void saveResultsToCSV(double[] x, double[] y,
                                         double[] yFirstExact, double[] yFirstApprox, double[] errorFirst,
                                         double[] ySecondExact, double[] ySecondApprox, double[] errorSecond)
            throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter("numerical_differentiation_results.csv"));
        writer.println("j,x,y,y_first_exact,y_first_approx,error_first,y_second_exact,y_second_approx,error_second");

        for (int j = 0; j <= N; j++) {
            writer.printf("%d,%.10f,%.10f,%.10f,%.10f,%.10e,%.10f,%.10f,%.10e%n",
                    j, x[j], y[j], yFirstExact[j], yFirstApprox[j], errorFirst[j],
                    ySecondExact[j], ySecondApprox[j], errorSecond[j]);
        }
        writer.close();
    }

    // Метод создания комбинированного графика (все на одном рисунке)
    private static void createCombinedGraph(double[] x, double[] y,
                                            double[] yFirstExact, double[] yFirstApprox,
                                            double[] ySecondExact, double[] ySecondApprox) {
        JFrame frame = new JFrame("Численное дифференцирование: y(x), y'(x), y''(x)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        CombinedGraphPanel panel = new CombinedGraphPanel(x, y, yFirstExact, yFirstApprox,
                ySecondExact, ySecondApprox);
        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// Класс для отрисовки комбинированного графика
class CombinedGraphPanel extends JPanel {
    private double[] x;
    private double[] y;
    private double[] yFirstExact, yFirstApprox;
    private double[] ySecondExact, ySecondApprox;

    public CombinedGraphPanel(double[] x, double[] y,
                              double[] yFirstExact, double[] yFirstApprox,
                              double[] ySecondExact, double[] ySecondApprox) {
        this.x = x;
        this.y = y;
        this.yFirstExact = yFirstExact;
        this.yFirstApprox = yFirstApprox;
        this.ySecondExact = ySecondExact;
        this.ySecondApprox = ySecondApprox;
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

        // Найти min и max значения для масштабирования
        double minX = x[0];
        double maxX = x[x.length - 1];
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        // Учитываем все массивы для масштабирования
        for (int i = 0; i < x.length; i++) {
            minY = Math.min(minY, y[i]);
            maxY = Math.max(maxY, y[i]);
            minY = Math.min(minY, yFirstExact[i]);
            maxY = Math.max(maxY, yFirstExact[i]);
            minY = Math.min(minY, ySecondExact[i]);
            maxY = Math.max(maxY, ySecondExact[i]);
        }

        double rangeY = maxY - minY;
        minY -= rangeY * 0.1;
        maxY += rangeY * 0.1;

        // Рисовать оси
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(padding, height - padding, width - padding, height - padding); // X ось
        g2d.drawLine(padding, padding, padding, height - padding); // Y ось

        // Метки осей
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("x", width / 2 - 10, height - padding + 40);

        Graphics2D g2dRotate = (Graphics2D) g2d.create();
        g2dRotate.rotate(-Math.PI / 2);
        g2dRotate.drawString("Значение", -height / 2 - 30, padding - 50);
        g2dRotate.dispose();

        // Рисовать деления на осях
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = 0; i <= 10; i++) {
            // Деления на оси X
            int xPos = padding + (int) ((width - 2 * padding) * i / 10.0);
            int yPos = height - padding;
            g2d.drawLine(xPos, yPos, xPos, yPos + 5);
            String label = String.format("%.1f", minX + (maxX - minX) * i / 10.0);
            g2d.drawString(label, xPos - 15, yPos + 20);

            // Деления на оси Y
            yPos = height - padding - (int) ((height - 2 * padding) * i / 10.0);
            g2d.drawLine(padding - 5, yPos, padding, yPos);
            label = String.format("%.2f", minY + (maxY - minY) * i / 10.0);
            g2d.drawString(label, padding - 50, yPos + 5);
        }

        // Заголовок
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Функция y(x) = cos(πx²/2) и её производные", width / 2 - 200, 30);

        // Рисовать графики
        g2d.setStroke(new BasicStroke(2.5f));

        // График основной функции y(x)
        g2d.setColor(new Color(0, 0, 255)); // Синий
        drawCurve(g2d, x, y, minX, maxX, minY, maxY, width, height, padding);

        // График первой производной y'(x) - точная
        g2d.setColor(new Color(255, 0, 0)); // Красный
        drawCurve(g2d, x, yFirstExact, minX, maxX, minY, maxY, width, height, padding);

        // График первой производной y'(x) - приближенная
        g2d.setColor(new Color(255, 100, 100)); // Светло-красный
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{5}, 0)); // Пунктир
        drawCurve(g2d, x, yFirstApprox, minX, maxX, minY, maxY, width, height, padding);

        // График второй производной y''(x) - точная
        g2d.setColor(new Color(0, 150, 0)); // Зеленый
        g2d.setStroke(new BasicStroke(2.5f));
        drawCurve(g2d, x, ySecondExact, minX, maxX, minY, maxY, width, height, padding);

        // График второй производной y''(x) - приближенная
        g2d.setColor(new Color(100, 200, 100)); // Светло-зеленый
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{5}, 0)); // Пунктир
        drawCurve(g2d, x, ySecondApprox, minX, maxX, minY, maxY, width, height, padding);

        // Легенда
        int legendX = width - padding - 200;
        int legendY = padding + 20;
        g2d.setFont(new Font("Arial", Font.PLAIN, 13));

        // y(x)
        g2d.setColor(new Color(0, 0, 255));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawLine(legendX, legendY, legendX + 30, legendY);
        g2d.setColor(Color.BLACK);
        g2d.drawString("y(x)", legendX + 40, legendY + 5);

        // y'(x) точная
        legendY += 25;
        g2d.setColor(new Color(255, 0, 0));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawLine(legendX, legendY, legendX + 30, legendY);
        g2d.setColor(Color.BLACK);
        g2d.drawString("y'(x) точная", legendX + 40, legendY + 5);

        // y'(x) приближенная
        legendY += 25;
        g2d.setColor(new Color(255, 100, 100));
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{5}, 0));
        g2d.drawLine(legendX, legendY, legendX + 30, legendY);
        g2d.setColor(Color.BLACK);
        g2d.drawString("y'(x) приближ.", legendX + 40, legendY + 5);

        // y''(x) точная
        legendY += 25;
        g2d.setColor(new Color(0, 150, 0));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawLine(legendX, legendY, legendX + 30, legendY);
        g2d.setColor(Color.BLACK);
        g2d.drawString("y''(x) точная", legendX + 40, legendY + 5);

        // y''(x) приближенная
        legendY += 25;
        g2d.setColor(new Color(100, 200, 100));
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{5}, 0));
        g2d.drawLine(legendX, legendY, legendX + 30, legendY);
        g2d.setColor(Color.BLACK);
        g2d.drawString("y''(x) приближ.", legendX + 40, legendY + 5);
    }

    // Вспомогательный метод для рисования кривой
    private void drawCurve(Graphics2D g2d, double[] xArray, double[] yArray,
                           double minX, double maxX, double minY, double maxY,
                           int width, int height, int padding) {
        for (int i = 0; i < xArray.length - 1; i++) {
            int x1 = padding + (int) ((width - 2 * padding) * (xArray[i] - minX) / (maxX - minX));
            int y1 = height - padding - (int) ((height - 2 * padding) * (yArray[i] - minY) / (maxY - minY));
            int x2 = padding + (int) ((width - 2 * padding) * (xArray[i + 1] - minX) / (maxX - minX));
            int y2 = height - padding - (int) ((height - 2 * padding) * (yArray[i + 1] - minY) / (maxY - minY));
            g2d.drawLine(x1, y1, x2, y2);
        }
    }
}
