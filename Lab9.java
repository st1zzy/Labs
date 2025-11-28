package com.example;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Lab9 extends JFrame {

    public Lab9() {
        setTitle("Лабораторная работа 9: Трехслойные схемы");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Вкладка 1: Уравнение колебания (Схема Крест)
        tabbedPane.addTab("Уравнение колебания (Волновое)", new WaveEquationPanel());

        // Вкладка 2: Уравнение теплопроводности (Схема Дюфорта-Франкля)
        tabbedPane.addTab("Уравнение теплопроводности (Дюфорта-Франкля)", new HeatEquationPanel());

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Lab9().setVisible(true);
        });
    }
}

// ==========================================================
// Абстрактный класс для отрисовки графиков и управления циклом
// ==========================================================
abstract class SimulationPanel extends JPanel implements ActionListener {
    protected Timer timer;
    protected double[] x;      // Сетка по X
    protected double[] u_prev; // Слой j-1
    protected double[] u_curr; // Слой j
    protected double[] u_next; // Слой j+1
    protected int N = 100;     // Количество узлов по пространству
    protected double L = 1.0;  // Длина отрезка
    protected double h;        // Шаг по пространству
    protected double tau;      // Шаг по времени
    protected int timeStep = 0;

    public SimulationPanel() {
        this.h = L / N;
        this.x = new double[N + 1];
        for (int i = 0; i <= N; i++) x[i] = i * h;

        u_prev = new double[N + 1];
        u_curr = new double[N + 1];
        u_next = new double[N + 1];

        // Кнопка перезапуска
        JButton restartBtn = new JButton("Перезапустить");
        restartBtn.addActionListener(e -> resetSimulation());
        this.add(restartBtn);

        initialize();
        timer = new Timer(30, this); // ~33 FPS
        timer.start();
    }

    protected abstract void initialize();
    protected abstract void calculateNextLayer();

    protected void resetSimulation() {
        timeStep = 0;
        initialize();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        calculateNextLayer();

        // Сдвиг слоев: curr -> prev, next -> curr
        System.arraycopy(u_curr, 0, u_prev, 0, u_curr.length);
        System.arraycopy(u_next, 0, u_curr, 0, u_next.length);

        timeStep++;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h_panel = getHeight();
        int padding = 50;

        // Рисуем оси
        g2.drawLine(padding, h_panel - padding, w - padding, h_panel - padding); // X
        g2.drawLine(padding, padding, padding, h_panel - padding); // Y

        g2.drawString("Время t = " + String.format("%.3f", timeStep * tau), w - 150, 30);

        // Масштабирование
        double yMin = -1.2;
        double yMax = 1.2;
        double yScale = (h_panel - 2 * padding) / (yMax - yMin);
        double xScale = (double) (w - 2 * padding) / N;

        // Отрисовка графика u_curr
        g2.setColor(Color.BLUE);
        g2.setStroke(new BasicStroke(2));

        for (int i = 0; i < N; i++) {
            int x1 = padding + (int) (i * xScale);
            int y1 = h_panel - padding - (int) ((u_curr[i] - yMin) * yScale);
            int x2 = padding + (int) ((i + 1) * xScale);
            int y2 = h_panel - padding - (int) ((u_curr[i + 1] - yMin) * yScale);
            g2.drawLine(x1, y1, x2, y2);
        }
    }
}

// ==========================================================
// РЕШЕНИЕ 1: Уравнение колебаний (Волновое)
// Реализация формул (2), (5) и условия устойчивости (7)
// ==========================================================
class WaveEquationPanel extends SimulationPanel {

    @Override
    protected void initialize() {
        // Условие устойчивости: tau <= h (для a=1).
        // Источник [30]: корней не превосходят 1, если sigma <= 1.
        tau = h * 0.5; // Берем с запасом

        // Начальное условие: u(x,0) = sin(pi * x)
        // Начальная скорость: du/dt(x,0) = 0
        for (int i = 0; i <= N; i++) {
            u_prev[i] = Math.sin(Math.PI * x[i]); // Слой j=0
        }

        // Аппроксимация второго слоя (j=1) с учетом начальной скорости
        // Источник [22]: u_1 = u_0 + tau*phi + (tau^2/2) * u_xx
        double r = (tau * tau) / (h * h);
        for (int i = 1; i < N; i++) {
            // Вторая производная u_xx ~ (u_{i+1} - 2u_i + u_{i-1}) / h^2
            double u_xx = (u_prev[i + 1] - 2 * u_prev[i] + u_prev[i - 1]) / (h * h);
            // Так как начальная скорость (phi) = 0:
            u_curr[i] = u_prev[i] + 0.5 * (tau * tau) * u_xx;
        }
        // Граничные условия (закрепленные концы)
        u_curr[0] = 0;
        u_curr[N] = 0;
    }

    @Override
    protected void calculateNextLayer() {
        // Явная трехслойная схема "Крест"
        // Источник [9]: u^{j+1} выражается через u^j и u^{j-1}
        double r = (tau * tau) / (h * h); // Число Куранта в квадрате

        for (int i = 1; i < N; i++) {
            u_next[i] = 2 * u_curr[i] - u_prev[i] + r * (u_curr[i + 1] - 2 * u_curr[i] + u_curr[i - 1]);
        }

        // Граничные условия
        u_next[0] = 0;
        u_next[N] = 0;
    }
}

// ==========================================================
// РЕШЕНИЕ 2: Уравнение теплопроводности
// Схема Дюфорта-Франкля (Ромб)
// Источник [40-43]
// ==========================================================
class HeatEquationPanel extends SimulationPanel {

    @Override
    protected void initialize() {
        // Для теплопроводности tau может быть больше h^2, но схема Дюфорта-Франкля
        // абсолютно устойчива [41], поэтому выбираем удобный шаг.
        tau = 0.0005;

        // Начальное условие: "горб" температуры в центре
        for (int i = 0; i <= N; i++) {
            if (x[i] > 0.4 && x[i] < 0.6)
                u_prev[i] = 1.0;
            else
                u_prev[i] = 0.0;
        }
        u_prev[0] = 0; u_prev[N] = 0;

        // Для старта схемы Дюфорта-Франкля нужны два слоя (j и j-1).
        // Слой j=1 рассчитаем по простой явной схеме (Ричардсона/Эйлера) для старта.
        double sigma = tau / (h * h);
        for (int i = 1; i < N; i++) {
            u_curr[i] = u_prev[i] + sigma * (u_prev[i + 1] - 2 * u_prev[i] + u_prev[i - 1]);
        }
        u_curr[0] = 0; u_curr[N] = 0;
    }

    @Override
    protected void calculateNextLayer() {
        // Схема Ромб (Дюфорта-Франкля)
        // Источник [40]: (u^{j+1} - u^{j-1})/(2tau) = (u_{i+1}^j - (u^{j+1} + u^{j-1}) + u_{i-1}^j) / h^2
        // Выражаем u^{j+1}:
        double lambda = (2 * tau) / (h * h);

        for (int i = 1; i < N; i++) {
            double term1 = (1 - lambda) * u_prev[i];
            double term2 = lambda * (u_curr[i + 1] + u_curr[i - 1]);
            double denominator = 1 + lambda;

            u_next[i] = (term1 + term2) / denominator;
        }

        // Граничные условия (температура на концах стержня = 0)
        u_next[0] = 0;
        u_next[N] = 0;
    }
}