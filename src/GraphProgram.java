import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/*
34. Неориентированный граф называется двудольным, если его можно раскрасить в два
цвета так, что концы любого ребра будут разного цвета. Составить алгоритм
проверки, является ли заданный граф двудольным (число действий не превосходит N
+ M).
 */
public class GraphProgram extends JFrame {
    private GraphPanel graphPanel;
    private JButton selectGraphButton;
    private JButton ifBipartiteGraphButton;

    public GraphProgram() {
        setTitle("Двудольный?");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        graphPanel = new GraphPanel();
        selectGraphButton = new JButton("Выбрать граф");
        ifBipartiteGraphButton = new JButton("Является ли заданный граф двудольным?");
        JFileChooser fileChooserOpen;


        fileChooserOpen = new JFileChooser();
        fileChooserOpen.setCurrentDirectory(new File("."));
        FileFilter filter = new FileNameExtensionFilter("Text files", "txt");
        fileChooserOpen.addChoosableFileFilter(filter);

        JFileChooser finalFileChooserOpen = fileChooserOpen;

        selectGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (finalFileChooserOpen.showOpenDialog(graphPanel) == JFileChooser.APPROVE_OPTION) {
                    int[][] adjacencyMatrix = ArrayUtils.readIntArray2FromFile(finalFileChooserOpen.getSelectedFile().getPath());
                    graphPanel.setGraph(adjacencyMatrix);
                    graphPanel.resetPathHighlight();
                    /*
                    из этого файла считывается двумерный массив целых чисел, который представляет матрицу смежности графа.
                    Этот массив передается в метод setGraph() объекта graphPanel, который отображает граф на экране.
                    Также вызывается метод resetPathHighlight(), который сбрасывает подсветку пути на графе.
                     */
                }
            }
        });

        ifBipartiteGraphButton.addActionListener(new ActionListener() { // чекает двудольный ли граф
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isBipartite = graphPanel.isBipartiteGraph(); //записывается в boolean двудольный ли граф
                JOptionPane.showMessageDialog(GraphProgram.this,
                        "Is the graph bipartite: " + isBipartite,
                        "Answer",
                        JOptionPane.INFORMATION_MESSAGE);
                graphPanel.highlightShortestPath(); //подсвечивает кратчайший путь на графе.
                // (у меня должен быть метод расскраски на две доли)
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(selectGraphButton);
        buttonPanel.add(ifBipartiteGraphButton);

        add(graphPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GraphProgram();
            }
        });
    }
}

class GraphPanel extends JPanel {
    private List<Node> nodes;
    private List<Edge> edges;
    private int[][] adjacencyMatrix;  //массив adjacencyMatrix является матрицей смежности графа.
    private List<Integer> redZeroPart;// те вершины, которые дожны быть красными (и равнми 0 в моём методе проверки на двудольные графы)

    public GraphPanel() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        adjacencyMatrix = null;
        redZeroPart = new ArrayList<>();
    }

    public int[][] getGraph() {
        return adjacencyMatrix;
    }

    public void setGraph(int[][] graph) {
        adjacencyMatrix = graph;
        int numNodes = graph.length;

        nodes.clear();
        edges.clear();

        int nodeSize = 30;
        int padding = 50;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        double angleIncrement = 2 * Math.PI / numNodes;
        double currentAngle = 0;

        // Создаем узлы и рассчитываем их позиции по окружности
        for (int i = 0; i < numNodes; i++) {
            int x = (int) (centerX + Math.cos(currentAngle) * (centerX - padding));
            int y = (int) (centerY + Math.sin(currentAngle) * (centerY - padding));

            Node node = new Node(x, y, nodeSize, i);
            nodes.add(node);

            currentAngle += angleIncrement;
        }

        // Создаем ребра на основе матрицы смежности
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                if (graph[i][j] > 0) {
                    Edge edge = new Edge(nodes.get(i), nodes.get(j), graph[i][j]);
                    edges.add(edge);
                }
            }
        }
    }

    public boolean isBipartiteGraph() { // главный метод чекает на то, двудольный ли граф
        if (adjacencyMatrix == null) { //массив adjacencyMatrix является матрицей смежности графа.
            return false;
        }

        redZeroPart.clear();


        int n = adjacencyMatrix.length; //строки - кол-во вершин графа
        int[] colors = new int[n];
        Arrays.fill(colors, -1); //заполнения массива цветов -1.

        for (int i = 0; i < n; i++) {
            if (colors[i] == -1) {
                colors[i] = 0;
                redZeroPart.add(i); // добавляем вершину в shortestPath

                Queue<Integer> queue = new LinkedList<>();
                queue.offer(i); // добавления элемента в конец очереди.

                while (!queue.isEmpty()) { //пока очередь не пустая
                    int u = queue.poll(); //poll-удаления и возврат элемента из начала очереди
                    //извлекается вершина u
                    for (int v = 0; v < n; v++) {
                        if (adjacencyMatrix[u][v] == 1) { //для каждой вершины v, смежной с u, проверяется ее цвет
                            if (colors[v] == -1) {        //Если v еще не окрашена, то ей присваивается цвет, отличный от цвета u
                                colors[v] = 1 - colors[u];
                                 if((1 - colors[u] )== 0){ redZeroPart.add(v);}
                                queue.offer(v);
                            } else if (colors[v] == colors[u]) { //Если же v уже окрашена и ее цвет совпадает с цветом u, то граф не является двудольным и возвращается значение false.
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    public void highlightShortestPath() {
       /* // Подсветка кратчайшего пути на графе
        resetPathHighlight();

        for (int i = 1; i < redZeroPart.size(); i++) {
            int nodeA = redZeroPart.get(i - 1);
            int nodeB = redZeroPart.get(i);

            // Подсветка узлов
            nodes.get(nodeA).setHighlighted(true);
            nodes.get(nodeB).setHighlighted(true);

            // Подсветка ребер
            for (Edge edge : edges) {
                if ((edge.getNodeA().getId() == nodeA && edge.getNodeB().getId() == nodeB)
                        || (edge.getNodeA().getId() == nodeB && edge.getNodeB().getId() == nodeA)) {
                    edge.setHighlighted(true);
                    break;
                }
            }
        }

        // Перерисовка графа для отображения подсветки
        repaint();

        */ //даши

        /* //мой но не работает чтот
        resetPathHighlight();

        for (int i = 0; i < redZeroPart.size(); i++) {
            int nodeId = redZeroPart.get(i);

            nodes.get(nodeId).setHighlighted(true);
        }

        repaint();

         */

        // Подсветка кратчайшего пути на графе
        resetPathHighlight();

        for (int i = 0; i < redZeroPart.size(); i++) {
            int node = redZeroPart.get(i);

            // Подсветка узла
            nodes.get(node).setHighlighted(true);

            // Подсветка ребер
            if (i > 0) {
                int prevNode = redZeroPart.get(i - 1);
                for (Edge edge : edges) {
                    if ((edge.getNodeA().getId() == node && edge.getNodeB().getId() == prevNode)
                            || (edge.getNodeA().getId() == prevNode && edge.getNodeB().getId() == node)) {
                        edge.setHighlighted(true);
                        break;
                    }
                }
            }
        }

        // Перерисовка графа для отображения подсветки
        repaint();
    }

    public void resetPathHighlight() {
        // Сброс подсветки пути на графе
        for (Node node : nodes) {
            node.setHighlighted(false);
        }

        for (Edge edge : edges) {
            edge.setHighlighted(false);
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // Отрисовка ребер
        for (Edge edge : edges) {
            if (edge.isHighlighted()) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(Color.BLACK);
            }
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(edge.getNodeA().getX(), edge.getNodeA().getY(), edge.getNodeB().getX(), edge.getNodeB().getY());
        }

        // Отрисовка узлов
        for (Node node : nodes) {
            if (node.isHighlighted()) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(Color.BLUE);
            }
            g2d.fillOval(node.getX() - node.getSize() / 2, node.getY() - node.getSize() / 2, node.getSize(), node.getSize());
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(Integer.toString(node.getId()), node.getX() - 5, node.getY() + 5);
        }
    }
}

class Node {
    private int x;
    private int y;
    private int size;
    private int id;
    private boolean highlighted;

    public Node(int x, int y, int size, int id) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.id = id;
        this.highlighted = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    public int getId() {
        return id;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }
}

class Edge {
    private Node nodeA;
    private Node nodeB;
    private int weight;
    private boolean highlighted;

    public Edge(Node nodeA, Node nodeB, int weight) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.weight = weight;
        this.highlighted = false;
    }

    public Node getNodeA() {
        return nodeA;
    }

    public Node getNodeB() {
        return nodeB;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }
}