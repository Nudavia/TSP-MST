import java.io.*;
import java.util.*;

public class TSP {
    public static void main(String[] args) throws IOException {
        Solution S = new Solution();
        S.Kruskal();
        S.Cut();
        S.MakeRing();
        S.ShowHamiltonian();
        System.out.println(S.SumWeight());
    }
}

class Solution {
    public static final double MAX_WEIGHT = 9999;
    public double[][] Matrix = null;
    public int[] Degree = null;
    public ArrayList<Edge> Edges = new ArrayList<Edge>();
    public ArrayList<Edge> MST = new ArrayList<Edge>();
    public ArrayList<Edge> Hamiltonian = new ArrayList<Edge>();
    public Set<Integer> D1 = new HashSet<Integer>();
    public Set<Integer> D0 = new HashSet<Integer>();
    public Unset HamiU;//用却确定Hamiltonian的连通情况

    //构造函数
    public Solution() throws IOException {
        Initialize("data/edge.txt");
    }

    //读取文件，初始化边集和距离矩阵
    public void Initialize(String filepath) throws IOException {
        //读取txt文件
        String filePath = filepath;
        FileInputStream fin = new FileInputStream(filePath);
        InputStreamReader reader = new InputStreamReader(fin);
        BufferedReader buffReader = new BufferedReader(reader);
        //将字符串按空字符分割成字符串数组
        String[] strTmp = buffReader.readLine().split("\\s+");
        int nodeCount = Integer.parseInt(strTmp[0]);
        int edgeCount = Integer.parseInt(strTmp[1]);
        Degree = new int[nodeCount];
        Matrix = new double[nodeCount][nodeCount];
        for (int i = 0; i < Matrix.length; ++i) {
            Arrays.fill(Matrix[i], MAX_WEIGHT);
            Matrix[i][i] = 0;
        }
        //读边集
        String strLine;
        while ((strLine = buffReader.readLine()) != null) {
            String[] edgeStr = strLine.split("\\s+");
            int node1 = Integer.parseInt(edgeStr[0]);
            int node2 = Integer.parseInt(edgeStr[1]);
            double weight = Double.parseDouble(edgeStr[2]);
            Edges.add(new Edge(node1, node2, weight));
            Matrix[node1][node2] = Matrix[node2][node1] = weight;
            ++Degree[node1];
            ++Degree[node2];
        }
        buffReader.close();
    }

    //Kruskal算法获取最小生成树
    public void Kruskal() {
        Arrays.fill(Degree, 0);
        PriorityQueue<Edge> minheap = new PriorityQueue<Edge>(Edges.size(), new Comparator<Edge>() {
            @Override
            public int compare(Edge e1, Edge e2) {
                return e1.weight - e2.weight > 0 ? 1 : -1;
            }
        });
        Unset U = new Unset(Degree.length);
        for (Edge e : Edges) {
            minheap.add(e);
        }
        while (!minheap.isEmpty()) {
            Edge e = minheap.peek();
            if (U.Find(e.node1) != U.Find((e.node2))) {
                MST.add(e);
                U.Merge(e.node1, e.node2);
                ++Degree[e.node1];
                ++Degree[e.node2];
            }
            minheap.poll();
        }
    }

    //将最小生成树减支至各个结点的度为0,1,2
    public void Cut() {
        HamiU=new Unset(Degree.length) ;
        for (int i = MST.size() - 1; i >= 0; --i) {
            boolean needcut = false;
            if (Degree[MST.get(i).node1] > 2) {
                needcut = true;
                --Degree[MST.get(i).node1];
                --Degree[MST.get(i).node2];
            }
            if (needcut == false && Degree[MST.get(i).node2] > 2) {
                needcut = true;
                --Degree[MST.get(i).node1];
                --Degree[MST.get(i).node2];
            }
            if (!needcut) {
                Edge e=new Edge(MST.get(i));
                Hamiltonian.add(e);
                HamiU.Merge(e.node1,e.node2);
            }
        }
        for (int i = 0; i < Degree.length; ++i) {
            if (Degree[i] == 0)
                D0.add(i);
            else if (Degree[i] == 1)
                D1.add(i);
        }
    }

    //将各个连通分量连成环
    public void MakeRing() throws IOException {
        //处理独立点
        while (!D0.isEmpty()) {
            int closest = -1;
            double minWeight = MAX_WEIGHT;
            int select = D0.iterator().next();
            for (int i = 0; i < Degree.length; ++i) {
                if (Degree[i] < 2 && i != select && Matrix[select][i] < minWeight) {
                    closest = i;
                    minWeight = Matrix[select][i];
                }
            }
            if (closest != -1) {
                Edge e=new Edge(select, closest, minWeight);
                Hamiltonian.add(e);
                HamiU.Merge(e.node1,e.node2);
                D1.add(select);
                D0.remove(select);
                if (Degree[closest] == 0) {
                    D1.add(closest);
                    D0.remove(closest);
                } else {
                    D1.remove(closest);
                }
                ++Degree[select];
                ++Degree[closest];
            }
        }
        //处理度为1的点,注意不要和已经相连的点在连，仅仅通过度为1还不足以判断，需要通过并查集
        while (!D1.isEmpty()) {
            int closest = -1;
            double minWeight = MAX_WEIGHT;
            int select = D1.iterator().next();
            for (int i = 0; i < Degree.length; ++i) {
                if (Degree[i] == 1 && i != select && Matrix[select][i] < minWeight) {
                    int tmp=closest;
                    closest = i;
                    if(HamiU.Find(select)!=HamiU.Find(closest)||D1.size()==2) {//注意最后2个度为1的点就不需要并查集来判断了
                        minWeight = Matrix[select][i];
                    }else {
                        closest=tmp;
                    }
                }
            }
            if (closest != -1) {
                Edge e=new Edge(select, closest, minWeight);
                Hamiltonian.add(e);
                HamiU.Merge(e.node1,e.node2);
                D1.remove(select);
                D1.remove(closest);
                ++Degree[select];
                ++Degree[closest];
            }
        }
    }

    //返回最短代价
    public double SumWeight() {
        double sum = 0;
        for (Edge e : Hamiltonian) {
            sum += e.weight;
        }
        return sum;
    }

    //打印度为0的集合
    public void ShowD0() {
        System.out.println("Solution.ShowD0");
        for (int num : D0) {
            System.out.println(num);
        }
        System.out.println();
    }

    //打印度为1的结点
    public void ShowD1() {
        System.out.println("Solution.ShowD1");
        for (int num : D1) {
            System.out.println(num);
        }
        System.out.println();
    }

    //打印距离矩阵
    public void ShowMatrix() {
        System.out.println("Solution.ShowMatrix");
        for (double[] row : Matrix) {
            for (double w : row) {
                if (Math.abs(w - MAX_WEIGHT) < 1e-2)
                    System.out.printf("%-10.2f   ", -1.0);
                else
                    System.out.printf("%-10.2f   ", w);
            }
            System.out.println();
        }
        System.out.println();
    }

    //打印属于哈密顿的边（MakeRing之后）
    public void ShowHamiltonian() throws IOException {
        System.out.println("Solution.ShowHamiltonian");
        BufferedWriter out = new BufferedWriter(new FileWriter("result.txt"));
        for (Edge e : Hamiltonian) {
            System.out.println(e.node1 + " 到 " + e.node2 + " 的权重为 " + e.weight);
            out.write(e.node1+"  "+e.node2+"\n");
        }
        out.close();
        System.out.println();
    }

    //打印边集
    public void ShowEdges() {
        System.out.println("Solution.ShowEdges");
        for (Edge e : Edges) {
            System.out.println(e.node1 + " 到 " + e.node2 + " 的权重为 " + e.weight);
        }
        System.out.println();
    }

    //打印最小生成树
    public void ShowMST() {
        System.out.println("Solution.ShowMST");
        for (Edge e : MST) {
            System.out.println(e.node1 + " 到 " + e.node2 + " 的权重为 " + e.weight);
        }
        System.out.println();
    }

    //打印当前各个结点的度
    public void ShowDegree() {
        System.out.println("Solution.ShowDegree");
        for (int i = 0; i < Degree.length; ++i) {
            System.out.println(i + " 的度为: " + Degree[i]);
        }
        System.out.println();
    }
}


class Edge {
    public int node1, node2;
    public double weight;

    public Edge(int n1, int n2, double w) {
        node1 = n1;
        node2 = n2;
        weight = w;
    }

    //边集构造函数
    public Edge(Edge e) {
        node1 = e.node1;
        node2 = e.node2;
        weight = e.weight;
    }
}