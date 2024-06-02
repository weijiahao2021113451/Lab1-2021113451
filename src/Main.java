import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        // 指定当前文件夹下的1.txt文件路径
        String filePath = "src/1.txt";
        DirectedGraph graph = new DirectedGraph();

        // 读取文件并生成有向图
        try {
            graph.generateGraph(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 打印有向图的邻接表表示
        System.out.println("Original Text:");
        System.out.println(graph.getProcessedText());
        System.out.println("\nAdjacency List:");
        System.out.println(graph);
        visualizeGraph(graph, "graphVisualization");
        //////////////////////////////////////////////////////////////////////////////////////
        // 示例：查询桥接词
        System.out.println("\n////////////////////bridgeWords//////////////////////");
        String filePath3 = "src/3.txt"; // 文件路径
        String word3_1 = "";
        String word3_2 = "";
        // 使用try-with-resources语句自动关闭资源
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath3))) {
            word3_1 = reader.readLine();
            word3_2 = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> bridgeWords = graph.queryBridgeWords(word3_1, word3_2);

        if (bridgeWords != null && !bridgeWords.isEmpty()) {
            // 将bridgeWords列表转换为字符串，并按照要求格式化
            String formattedBridgeWords = String.join(", ", bridgeWords.subList(0, bridgeWords.size() - 1)) +
                    " and " + bridgeWords.get(bridgeWords.size() - 1);

            // 输出结果
            System.out.println("The bridge words from " + word3_1 + " to " + word3_2 + " is: " + formattedBridgeWords + ".");
        } else {
            // 如果bridgeWords为空或者null，输出相应的提示
            System.out.println("No bridge words found between " + word3_1 + " and " + word3_2 + ".");
        }
        /////////////////////////////////////////////////
//
//
//        4.据bridge word生成新文本
        System.out.println("\n//////////////////////newtext//////////////////////");
        String filePath4 = "src/4.txt";
        String userInput = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath4))) {
            // 读取文件的第一行内容
            userInput = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String newText = graph.generateNewTextWithBridgeWords(userInput);
        System.out.println("\nNew Text with Bridge Words:");
        System.out.println(newText);
//        //////////////////////////////////////////////
//       最短距离
        System.out.println("\n//////////////////////shortestPath//////////////////////");
        String filePath5 = "src/5.txt";
        String word5_1 = "";
        String word5_2 = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath5))) {
            word5_1 = reader.readLine();
            word5_2 = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String shortestPath = graph.calcShortestPath(word5_1, word5_2);
        System.out.println(shortestPath);
        Set<String> edges = splitPathIntoEdgeSet(shortestPath);
        visualizeGraphShort(graph, "最短路径展示", edges);
        ////////////////////////////////////
////////////////////////////////////
        //随机游走
        System.out.println("\n/////////////////////randomWalk//////////////////////");
        System.out.println("Press 't' to stop the random walk...");
        // 调用randomWalkAndWriteToFile方法
        String traversalPath = graph.randomWalkAndWriteToFile("path.txt");
        // 打印游走路径
        System.out.println("Traversal Path: " + traversalPath);
    }


    static class DirectedGraph {
        private Map<String, Map<String, Integer>> adjacencyList = new HashMap<>();
        private StringBuilder originalText = new StringBuilder(); // 存储原始文本

        Set<String> shortestPathEdges = new HashSet<>();//用于重建最短路径
        public List<String> queryBridgeWords(String word1, String word2) {
            word1 = word1.toLowerCase();
            word2 = word2.toLowerCase();

            // 检查word1和word2是否在图中
            if (!adjacencyList.containsKey(word1) || !adjacencyList.containsKey(word2)) {

                return null;
            }

            Set<String> bridgeWordsSet = new HashSet<>(); // 使用HashSet来避免重复的桥接词
            for (String adjacentWord : adjacencyList.get(word1).keySet()) {
                if (adjacencyList.containsKey(adjacentWord) && adjacencyList.get(adjacentWord).containsKey(word2)) {
                    bridgeWordsSet.add(adjacentWord);
                }
            }

            // 转换为List并返回
            List<String> bridgeWords = new ArrayList<>(bridgeWordsSet);
            if (bridgeWords.isEmpty()) {
                //System.out.println("No bridge words from word1 to word2!");
            }
            return bridgeWords;
        }
        public void generateGraph(String filePath) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                String last="aeeaaeaa";
                while ((line = reader.readLine()) != null) {
                    // 替换换行符为空格并添加原始文本
                    originalText.append(line.replace("\n", " ")).append("\n");
                    // 处理每一行文本
                    last=processLine(line,last);

                }
            }
        }

        private String processLine(String line,String last) {
            // 移除非字母数字字符  并转换为小写，替换换行符为空格
            String[] words = line.replaceAll("[^a-zA-Z\\s]", " ").toLowerCase().split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                String current = words[i];
                String next = words[i + 1];
                addEdge(current, next, 1);
            }//根据数组增加边
            if(!last.equals("aeeaaeaa")){
               addEdge(last,words[0],1);
            }
            last=words[words.length-1];
            return last;
        }

        private void addEdge(String from, String to, int weight) {
            // 获取当前节点的邻接表，如果不存在则创建一个新的HashMap
            adjacencyList.computeIfAbsent(from, k -> new HashMap<>());
            // 更新边的权重
            adjacencyList.get(from).merge(to, weight, Integer::sum);
        }

        public String getProcessedText() {
            // 移除所有非字母数字字符，并将所有文本转换为小写
            String processedText = originalText.toString().replaceAll("[^a-zA-Z\\s]", "").toLowerCase();
            // 替换一个或多个空格为单个空格，并去除首尾空格
            processedText = processedText.trim().replaceAll("\\s+", " ");
            return processedText;
        }
        public String generateNewTextWithBridgeWords(String input) {
            String[] words = input.split("\\s+");
            List<String> newWords = new ArrayList<>();
            Random rand = new Random();

            for (int i = 0; i < words.length - 1; i++) {
                newWords.add(words[i]);
                List<String> bridgeWords = queryBridgeWords(words[i], words[i + 1]);
                if (bridgeWords != null && !bridgeWords.isEmpty()) {
                    // 随机选择一个bridge word
                    String selectedBridgeWord = bridgeWords.get(rand.nextInt(bridgeWords.size()));
                    newWords.add(selectedBridgeWord);
                }
            }
            newWords.add(words[words.length - 1]); // 添加最后一个词

            return String.join(" ", newWords);//连成一句话
        }

        // Dijkstra算法找到从startWord到endWord的最短路径
        public String calcShortestPath(String word1, String word2) {

            if (!adjacencyList.containsKey(word1) || !adjacencyList.containsKey(word2)) {
                return "One or both of the words are not in the graph.";
            }

            Map<String, Double> distances = new HashMap<>();
            Map<String, String> previous = new HashMap<>();
            Set<String> visited = new HashSet<>(); // 初始化访问集合
            PriorityQueue<Map.Entry<String, Double>> pq = new PriorityQueue<>(Comparator.comparing(Map.Entry::getValue));
            //根据键值排序
            // 初始化所有节点的距离为无穷大，并将起点的距离设置为0
            for (String node : adjacencyList.keySet()) {
                distances.put(node, Double.MAX_VALUE);
            }
            distances.put(word1, 0.0);

            // 将起点加入优先队列
            pq.offer(new AbstractMap.SimpleEntry<>(word1, distances.get(word1)));

            while (!pq.isEmpty()) {
                Map.Entry<String, Double> currentEntry = pq.poll();
                String currentNode = currentEntry.getKey();//获取名称
                double currentDistance = currentEntry.getValue();

              //  System.out.println("Original Text:");
                if (currentNode.equals(word2)) {
                    // 如果当前节点是目标节点，开始重建路径
                    return reconstructPath(previous, word2);
                }

                if (!visited.contains(currentNode)) { // 检查当前节点是否已访问
                    visited.add(currentNode); // 标记当前节点为已访问

                    Map<String, Integer> neighbors = adjacencyList.get(currentNode);//获取节点邻居
                    if (neighbors != null) {
                        for (Map.Entry<String, Integer> edge : neighbors.entrySet()) {//遍历邻居节点
                            String neighbor = edge.getKey();
                            int weight = edge.getValue();
                            double distanceThroughU = currentDistance + weight;
//                           找到了更短的路径
                            if (distances.getOrDefault(neighbor, Double.MAX_VALUE) > distanceThroughU) {
                                distances.put(neighbor, distanceThroughU);

                                previous.put(neighbor, currentNode);//用于重构图像，保证了用新的值覆盖之前的值
                                pq.offer(new AbstractMap.SimpleEntry<>(neighbor, distanceThroughU));//加入邻居

                            }
                        }
                    }
                }
            }

            // 如果没有找到路径
            return "No path exists between " + word1 + " and " + word2;
        }

        private String reconstructPath(Map<String, String> previous, String word2) {
            List<String> path = new ArrayList<>();
            String current = word2;
            while (current != null) {
                path.add(0, current); // 将当前节点添加到路径的开始
                current = previous.get(current); // 移动到前一个节点
            }
            return String.join(" → ", path);
        }
        // 执行随机游走的方法
        public List<String> randomWalk() {
            Map<String, Map<String, Integer>> adjListCopy = new HashMap<>(adjacencyList);
            List<String> traversalPath = new ArrayList<>();
            StringBuilder traversalText = new StringBuilder();
            String currentNode = chooseRandomNode(adjListCopy); // 随机选择起点
            traversalPath.add(currentNode);
            traversalText.append(currentNode).append(" ");

            boolean stop = false;
            while (!stop && adjListCopy.containsKey(currentNode)) {
                Map<String, Integer> edges = adjListCopy.get(currentNode);
                if (edges.isEmpty()) {
                    // 当前节点没有出边，停止游走
                    break;
                }
                String nextNode = chooseRandomNextNode(edges, traversalPath); // 随机选择下一个节点
                if (nextNode != null) {
                    traversalPath.add(nextNode);
                    traversalText.append(nextNode).append(" ");
                    currentNode = nextNode;
                } else {
                    // 遇到了重复的边，停止游走
                    stop = true;
                }
            }
            return traversalPath;
        }
        public String randomWalkAndWriteToFile(String filePath) {
            Map<String, Map<String, Integer>> adjListCopy = new HashMap<>(adjacencyList);
            StringBuilder traversalText = new StringBuilder();
            final String[] currentNode = {chooseRandomNode(adjListCopy)};
            traversalText.append(currentNode[0]).append(" ");

            final boolean[] stop = {false};
            ExecutorService executor = Executors.newSingleThreadExecutor();//一个ExecutorService对象
            Future<?> future = executor.submit(() -> {//异步计算，未设置返回值，将这些提交给线程池进行
                try {
                    while (!stop[0] && adjListCopy.containsKey(currentNode[0])) {
                        Map<String, Integer> edges = adjListCopy.get(currentNode[0]);
                        if (edges.isEmpty()) {
                            break;
                        }
                        System.out.println(currentNode[0]);

                        // 修改参数以接收 List<String> 类型的参数
                      //  List<String> visitedNodes = Arrays.asList(currentNode[0]);
                        String traversalText1=traversalText.toString();
                        List<String> visitedNodes = Arrays.asList(traversalText1);
                        String nextNode = chooseRandomNextNode(edges, visitedNodes);//选择下一个节点
                        if (nextNode != null) {
                            traversalText.append(nextNode).append(" ");//添加路径中
                            currentNode[0] = nextNode;//下一个节点
                        } else {
                            stop[0] = true;
                        }
                        Thread.sleep(3000); // 每秒输出一个节点
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 重置中断状态，介绍
                }
            });

            // 监听键盘输入
            new Thread(() -> {
                try (Scanner scanner = new Scanner(System.in)) { // 使用 try-with-resources 自动关闭 Scanner

                    while (!stop[0]) {
                        if (scanner.hasNextLine()) {
                            String input = scanner.nextLine();
                            if ("t".equalsIgnoreCase(input)) { // 按下 't'（不区分大小写），则停止游走
                                stop[0] = true;
                                future.cancel(true);
                            }
                        }
                        Thread.sleep(100); // 减少CPU使用率
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            try {
                // 等待游走线程完成
                future.get();
            } catch (InterruptedException | ExecutionException | CancellationException e) {
                e.printStackTrace();//执行过程中被中断时抛出//异步任务执行过程中发生了异常//异步任务被取消
            } finally {
                executor.shutdown(); // 关闭执行器服务
                // 游走结束或被停止后写入文件
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write(traversalText.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 返回游走路径字符串
                return traversalText.toString().trim();
            }
        }


        // 从图中随机选择一个节点作为起点
        private String chooseRandomNode(Map<String, Map<String, Integer>> adjListCopy) {
            List<String> nodes = new ArrayList<>(adjListCopy.keySet());
            Random random = new Random();
            return nodes.get(random.nextInt(nodes.size()));
        }

        // 确保不重复
        private String chooseRandomNextNode(Map<String, Integer> edges, List<String> traversalPath) {
            System.out.println("the road is"+ traversalPath );
            List<String> availableNodes = new ArrayList<>(edges.keySet());
            Collections.shuffle(availableNodes); // 打乱列表顺序
            for (String node : availableNodes) {
                if (!traversalPath.contains(node)) {
                    return node;
                }
            }
            return null; // 没有可用的节点或遇到重复边
        }

        // 将遍历的节点写入文件


        //
      //  @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Map<String, Integer>> entry : adjacencyList.entrySet()) {
                sb.append(entry.getKey()).append(": ");
                for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                    sb.append(edge.getKey()).append("(").append(edge.getValue()).append(") ");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }
    public static Set<String> splitPathIntoEdgeSet(String pathString) {
        Set<String> edgeSet = new HashSet<>();
        String[] parts = pathString.split("\\s*→\\s*");
        for (int i = 0; i < parts.length - 1; i++) {
            // 将当前元素和下一个元素拼接
            String combined = parts[i] +"->" +parts[i + 1];
            // 添加到集合中
            edgeSet.add(combined);
        }
        return edgeSet;
    }

    public static void visualizeGraph(DirectedGraph graph, String outputFileName) {
        // 创建一个dot文件
        try (FileWriter writer = new FileWriter(outputFileName + ".dot")) {
            writer.write("digraph G {\n");
            writer.write("\tgraph [rankdir=LR, splines=true, overlap=false];\n"); // 设置图的方向和避免节点重叠
            writer.write("\tnode [shape=box, style=filled, fillcolor=lightgrey, fontsize=12];\n"); // 设置节点样式
            writer.write("\tedge [color=black, fontsize=10];\n"); // 设置边样式

            // 遍历邻接表并写入节点和边
            for (Map.Entry<String, Map<String, Integer>> entry : graph.adjacencyList.entrySet()) {
                String from = entry.getKey();
                for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                    String to = edge.getKey();
                    writer.write(String.format("\t\"%s\" -> \"%s\" [label=\"%d\"];\n", from, to, edge.getValue()));
                }
            }

            writer.write("\tlabel=\"visualizeGraph\";\n"); // 添加图的标题
            writer.write("}\n");
        } catch (IOException e) {
            e.printStackTrace();
            return; // 如果发生错误，提前退出函数
        }

        // 使用Graphviz的dot命令生成图像文件
        String command = String.format("dot -Tpng %s.dot -o %s.png", outputFileName, outputFileName);
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();//创建了一个Process对象，用于执行dot命令。确保执行完成
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void visualizeGraphShort(DirectedGraph graph, String outputFileName, Set<String> shortestPathEdges) {
        System.out.println(shortestPathEdges);
        try (FileWriter writer = new FileWriter(outputFileName + ".dot")) {
            writer.write("digraph G {\n");
            writer.write("\tgraph [rankdir=LR, splines=true, overlap=false];\n");
            writer.write("\tnode [shape=box, style=filled, fillcolor=lightgrey, fontsize=12];\n");
            writer.write("\tedge [color=black, fontsize=10];\n");

            for (Map.Entry<String, Map<String, Integer>> entry : graph.adjacencyList.entrySet()) {
                String from = entry.getKey();
                for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                    String to = edge.getKey();
                    String edgeLabel = String.format("\"%s\" -> \"%s\" [label=\"%d\"", from, to, edge.getValue());
                    // 检查边是否属于最短路径
                    if (shortestPathEdges.contains(from + "->" + to)) {
                        writer.write(edgeLabel + ", color=\"red\"" + "];\n"); // 使用红色高亮最短路径的边
                    } else {
                        writer.write(edgeLabel + ", color=\"black\"" + "];\n"); // 使用默认颜色绘制非最短路径的边
                    }
                }
            }

            writer.write("\tlabel=\"visualizeGraph5 \";\n");
            writer.write("}\n");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 使用Graphviz的dot命令生成图像文件
        String command = String.format("dot -Tpng %s.dot -o %s.png", outputFileName, outputFileName);
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            System.out.println("Graph visualization5 generated ");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}