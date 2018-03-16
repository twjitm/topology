package prefuse.safein.demo;

import prefuse.action.layout.graph.RadialTreeLayout;

import java.io.*;
import java.util.*;

/**
 * @author safein-1
 */
public class Demo {

    public static void main(String[] args) {
        long s1 = System.currentTimeMillis();
        Map<String, Integer> nodes = new HashMap<>();
        nodes.put("1dff", 1);
        nodes.put("2sdff", 2);
        nodes.put("3dfsdf", 3);
        nodes.put("6dfdf", 4);
        nodes.put("5dfdf", 5);
        nodes.put("6frdf", 6);
        nodes.put("7dfdf", 7);
        List<String> edges = new ArrayList<>();
        edges.add("1,2");
        edges.add("2,3");
        edges.add("3,4");
        edges.add("4,5");
        edges.add("5,6");
        edges.add("6,7");
        edges.add("5,4");
        edges.add("6,3");
        TopologyPosition tPosition = new TopologyPosition();

        RadialTreeLayout r = new RadialTreeLayout("graph");
        tPosition.setDiaplay(true);
        tPosition.setLayout(r);
        // Map<String, Object> position = tPosition.run(nodes, edges, 5);

        dataFormat();

    }

    public static List<String> dataFormat() {
        //给每个节点编号
        List<ReadExcelUtils.BlogCommentEntity> list = ReadExcelUtils.read();
        Set treeSet = new TreeSet<String>();
        for (ReadExcelUtils.BlogCommentEntity entity : list) {
            treeSet.add(entity.getRowLable());
            for (ReadExcelUtils.BlogCommentEntity subentity : entity.getBlogCommentEntity()) {
                treeSet.add(subentity.getRowLable());
            }
        }
        //编号节点
        Map<String, Integer> nodes = new HashMap<>();
        Iterator it = treeSet.iterator();
        int i = 0;
        while (it.hasNext()) {
            nodes.put(it.next().toString(), i);
            i++;
        }
        //编号关系
        List<String> edges = new ArrayList<>();
        //edges.add("1,2");
        for (ReadExcelUtils.BlogCommentEntity entity : list) {
            for (ReadExcelUtils.BlogCommentEntity subentity : entity.getBlogCommentEntity()) {
                edges.add(nodes.get(entity.getRowLable()) + "," + nodes.get(subentity.getRowLable()));
            }
        }
        createXmlFile(nodes,edges);
        return edges;
    }


    //生成文件
    public static void createXmlFile(Map<String, Integer> nodes, List<String> edges) {
        //create node context;
        File file = new File("e:\\blog_comment_time_temp.xml");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileWriter writer= null;
        try {
            writer = new FileWriter(file);
            for(Map.Entry<String,Integer>entry:nodes.entrySet()){
                writer.write("<node id=\""+entry.getValue()+"\">\n" +
                        " <data key=\"name\">"+entry.getKey()+"</data>\n" +
                        " <data key=\"gender\">"+entry.getKey()+"</data>\n" +
                        " </node>");
            }
            for(String edge:edges){
                String[] var = edge.split(",");
                writer.write("<edge source=\""+var[0]+"\" target=\""+var[1]+"\"></edge>\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}




