package prefuse.safein.demo;


import org.apache.demo.LayoutTabUtils.AutoPanAction;
import org.apache.demo.LayoutTabUtils.MyFocusControl;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.GroupAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.*;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.filter.FisheyeTreeFilter;
import prefuse.action.layout.*;
import prefuse.action.layout.GridLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.FruchtermanReingoldLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.activity.Activity;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.*;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.force.*;
import prefuse.visual.VisualItem;
import prefuse.visual.sort.TreeDepthItemSorter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TopologyPosition{
	private static JFrame frame;    
	Display display;
	public static Integer FORCELAYOUTACTION=0;
	private Table nodes=new Table(),edges=new Table();
	private Shape sho;
	private static boolean is_display=false;
	static Layout la=new NodeLinkTreeLayout("graph");
	private static Layout la1=new NodeLinkTreeLayout("graph");

	/**
	 *  获得网络图谱位置
	 * @param nodes  节点集Map<device_name,id>
	 * @param edges 关系集List<"tid,sid">
	 * @param layoutType 算法类型 1,节点边缘树算法。2,径向树算法,3弹力模型算法！(默认为节点边缘树算法)
	 * @return 位置的集合 例如Map<"device_name" ,position> map=new HashMap<String, Object>(); map.put("127.0.0.1","100,200") ，List<String>list=new ArrayList<String>(); list.add("1,2")                             
	 */
	public synchronized  static Map<String, Object>  run(Map<String, Integer>nodes,List<String>edges ,int layoutType){
		getTopoloPositionMap(nodes, edges,layoutType);
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	//	Map<String, Object>map=scend(nodes, edges, layoutType);
		return null;
	}
	private static Map<String, Object> scend(Map<String, Integer> nodes2, List<String> edges2,int layoutType) {
		Map<String, Object> map= getTopoloPositionMap(nodes2, edges2, layoutType);
		return map;
	}

	private synchronized static Map<String, Object> getTopoloPositionMap(Map<String, Integer>nodes,List<String>edges ,int layoutType){
		ActionList layout = new ActionList(Activity.INFINITY); 
		Table node=new Table(nodes.size() ,2);
		node.addColumn("id", int.class);
		node.addColumn("device_name", String.class);
		Table edge=new Table(edges.size(),2);
		edge.addColumn("tid",int.class );
		edge.addColumn("sid", int.class);

		//迭代出 节点的key和values
		Iterator device_name=nodes.keySet().iterator();
		String[]ds=new String[nodes.size()]; 
		int j=0;
		while(device_name.hasNext()){
			ds[j]= device_name.next().toString();
			j++;
		}
		for (int i = 0; i < node.getRowCount(); i++) {
			node.set(i, "device_name", ds[i]);
			node.set(i, "id", nodes.get(ds[i]));
		}

		String top[]=new String[edges.size()];
		List<String>tid=new ArrayList<>();
		List<String>sid=new ArrayList<>();
		for (int i = 0; i < edges.size(); i++) {
			String value= edges.get(i);
			top=value.split(",");
			tid.add(top[0].trim());
			sid.add(top[1].trim());
		}
		for (int i = 0; i <edges.size(); i++) {
			edge.set(i, "sid", sid.get(i));
			edge.set(i, "tid", tid.get(i));
		}
		//创建位视图
		Graph graph = new Graph(node, edge, false, "id", "tid", "sid");    //数据存储中心
		Visualization vis = new Visualization(); 
		vis.add("graph", graph); 
		vis.visibleItems();

		//			VisualItem visitem=vis.getVisualItem("graph", node.getTuple(1));
		//			VisualGraph vg=(VisualGraph) vis.getVisualGroup("graph");
		//			  VisualItem nodeI = (VisualItem)vg.getEdge(7).getSourceNode();
		//			         nodeI.setShape(Constants.SHAPE_STAR);
		//			         nodeI.setSize(4);
		//			        nodeI.setFixed(true);
		//			        Graph graph1=   (Graph) vis.getGroup("graph");
		//			       vg.getEdge(graph1.getNode(1), graph1.getNode(147)).;//getEdge(5).;
		//			          edgeI.setSize(2);

		Display display = new Display(vis); 
		ActionList animate = new ActionList(1250);
		MyFocusControl mfc = new MyFocusControl(1, "layout");
		EdgeRenderer e=new EdgeRenderer(Constants.EDGE_TYPE_CURVE);
		DefaultRendererFactory drf = (DefaultRendererFactory) vis.getRendererFactory();
		//layout.add(layout1);
					switch (layoutType) {
					case 0:
						layout=	setforceLayoutAction(layout);
						
						drf.setDefaultEdgeRenderer(new EdgeRenderer(Constants.EDGE_TYPE_LINE));//设置直线
		
						break;
		
					case 1:
						layout=setNodeLinkTreeLayoutAction(layout);
						AutoPanAction autoPan = new AutoPanAction();
						ActionList animate1 = new ActionList(1000);
						animate1.setPacingFunction(new SlowInSlowOutPacer());
						animate1.add(autoPan);
						animate1.add(new QualityControlAnimator());
						animate1.add(new VisibilityAnimator("graph.nodes"));
						animate1.add(new LocationAnimator("graph.nodes"));
						animate1.add(new ColorAnimator("graph.nodes"));
						animate1.add(new RepaintAction());
						vis.putAction("animate", animate1);
						vis.alwaysRunAfter("filter", "animate");
						mfc = new MyFocusControl(1, "layout");
						vis.getDisplay(0).addControlListener(mfc);
		
						drf = (DefaultRendererFactory) vis.getRendererFactory();
						
						e.setHorizontalAlignment1(1);
						drf.setDefaultEdgeRenderer(new EdgeRenderer(Constants.EDGE_TYPE_CURVE));//设置曲线
						break;
					case 2:
						layout=setNodeLinkTreeLayoutAction(layout);
						layout=setRadialTreeLayoutAction(layout);
						animate = new ActionList(1250);
						animate.setPacingFunction(new SlowInSlowOutPacer());
						animate.add(new QualityControlAnimator());
						animate.add(new VisibilityAnimator("graph"));
						animate.add(new PolarLocationAnimator("graph.nodes", "linear"));
						animate.add(new ColorAnimator("graph.nodes"));
						animate.add(new RepaintAction());
						vis.putAction("animate", animate);
						vis.alwaysRunAfter("layout", "animate");
						mfc = new MyFocusControl(1, "layout");
						vis.getDisplay(0).addControlListener(mfc);
						e.setHorizontalAlignment1(1);
						drf.setDefaultEdgeRenderer(new EdgeRenderer(Constants.EDGE_TYPE_CURVE));//设置曲线
						break;
		
					case 3:
						layout=setFruchtermanLayoutAction(layout,vis);
					case 4:
						layout=setNodeLinkTreeLayoutAction(layout);
                          layout=setRandomLayoutAction(layout);
						break;
					default:
						break;
					}
		RectangularShape m_bbox = new Rectangle2D.Double();
		// 读取node表单的device_name 显示内容     

		//显示背景
		LabelRenderer r = new LabelRenderer("id");  
		r.setRoundedCorner(16, 16);   
		r.setImagePosition(Constants.BOTTOM);
		//sho=  r.getRawShape(visitem);
		//System.out.println(sho.getBounds().x);
		vis.setRendererFactory(new DefaultRendererFactory(r));
		System.out.println("layout.getPacingFunction()="+layout.getPacingFunction());
		vis.setInteractive("graph.edges", null,true);
		//NodeItem nodeItem=(NodeItem) vis.getVisualItem("graph", node.getTuple(0));
		int[] palette = new int[] {    
				ColorLib.rgb(255,180,180), ColorLib.rgb(190,190,255)    
		};
		DataColorAction fill = new DataColorAction("graph.nodes", "device_name",    
				Constants.NOMINAL, VisualItem.FILLCOLOR, palette);    
		ColorAction textColor = new ColorAction("graph.nodes",    
				VisualItem.TEXTCOLOR, ColorLib.gray(0));    
		ColorAction edgesColor = new ColorAction("graph.edges",    
				VisualItem.STROKECOLOR, ColorLib.gray(200));    
		ActionList color = new ActionList();    
		color.add(fill);    
		color.add(textColor);   
		color.add(edgesColor);    
		color.add(new ColorAction(edges.toString(), VisualItem.STROKECOLOR, ColorLib.gray(200)));  
		frame= new JFrame("Prefuse");
		layout.add(new TreeRootAction("graph"));
		vis.putAction("color", color);    
		vis.putAction("layout", layout);    
		display.setSize(2000, 2000);  
		display.setVisualization(vis);

		display.setItemSorter(new TreeDepthItemSorter());
		//         (交互 请误删)  
		display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new WheelZoomControl());
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new NeighborHighlightControl());
		// 面板(请误删)
		vis.getDisplay(0).addControlListener(new FocusControl(1, "layout"));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    
		frame.add(display);    
		frame.pack();
		frame.setVisible(getDiaplay());  
		vis.run("color");
		vis.run("layout"); 
		//		//int top=1;
		//		
		//			if (layoutType==3) {
		//				try {
		//					Thread.sleep(10000);
		//				} catch (InterruptedException e) {
		//					e.printStackTrace();
		//				}
		//			}else {
		//				for (int i = 0; i < 2; i++) {
		//					int nodeId=0;
		//					nodeId=nodes.get(ds[i]);
		//					edges.add(nodeId+","+(nodeId-1));
		//					//getTopoloPositionMap(nodes, edges, layoutType);	
		//				}
		//			}
		//			int tag = 0;
		//		while(true && tag == 0){
		//			if(TransferData.getData().size()==(nodes.size()))
		//			{
		//				tag=1; 
		//			};
		//		}
		//Map<String, Object>params=new HashMap<>();
		//			for (int i = 0; i < nodes.size(); i++) {
		//				String path=ds[i].replaceAll("/", "");
		//				String context=readFile("data/position/"+path+".txt");//(String) TransferData.getData().get(ds[i]);
		//				//System.out.println(ds[i]+"的坐标"+context);
		//				params.put(ds[i], context);
		//				//System.out.println(ds[i]+"="+TransferData.getData().get(ds[i]));
		//			}
		return TransferData.getData();
	}


	
	public void  setLayout(Layout la){
		this.la=la;
	}

	public  Layout getLayout(){
		return la;
	}

	 /**
	 * 是否需要显示（调试的时候用）
	 * @param display
	 */
	 public void  setDiaplay(boolean display){
		this.is_display=display;
	}
	public static boolean getDiaplay(){
		return is_display;
	}
	

	/**
	 * 力导向布局选项卡
	 */
	
	private  static ActionList setforceLayoutAction(ActionList layout){
		ForceSimulator	m_fsim=	new ForceSimulator();
		ForceItem forcee1=new ForceItem();
		ForceItem forcee2=(ForceItem) forcee1.clone();
		m_fsim = new ForceSimulator();
		m_fsim.addForce(new NBodyForce());
		m_fsim.addForce(new SpringForce());
		m_fsim.addForce(new DragForce());
		//				    NBodyForce nBodyForce = new NBodyForce();
		//			        SpringForce springForce = new SpringForce();
		//				    DragForce dragForce = new DragForce();
		//			        nBodyForce.setParameter( 0, (float) -9.758 );
		//				    nBodyForce.setParameter( 1, (float) -1.0 );
		//			        nBodyForce.setParameter( 2, (float) 0.9 );
		//			        dragForce.setParameter( 0, (float) 0.00645 );
		//			        springForce.setParameter( 0, (float) 0.00001 );
		//			        springForce.setParameter( 1, (float) 160.0 );
		//				    m_fsim.addForce( nBodyForce );
		//				    m_fsim.addForce( springForce );
		//			       m_fsim.addForce( dragForce );
		m_fsim.addSpring(forcee1, forcee2, 1, 2);
		//		
		ForceDirectedLayout forceDirectedLayout=new ForceDirectedLayout("graph");
		//forceDirectedLayout.set
		layout.add(new ForceDirectedLayout("graph",true,false));
		layout.add(new CollapsedSubtreeLayout("graph"));
		layout.add(new RepaintAction());
		return layout;

	}
	/**
	 * 圆形布局选项卡
	 */
	public static ActionList setCircleLayoutAction(ActionList layout){
		layout.add(new CircleLayout("graph",500));
		layout.add(new RepaintAction());
		return layout;

	}


	/**
	 * 随机布局选项卡
	 */
	public static ActionList setRandomLayoutAction(ActionList layout){
		layout.add(new FisheyeTreeFilter("graph", 80));//添加一个鱼眼树形过滤器    设置初始界面显示几层节点
		layout.add(new RandomLayout("graph"));
		layout.add(new RepaintAction());
		return layout;

	}

	/**
	 * 网状布局选项卡     可能涉及到数据结构是否符合网状布局的情况
	 */
	 GridLayout  grid=new  GridLayout("graph");
	public static ActionList setGridLayoutAction(ActionList layout){
		 GridLayout  grid=new  GridLayout("graph");
		layout.add(new GridLayout("graph"));
		layout.add(new RepaintAction());
		return layout;
	}
	/**
	 * 设置网格布局
	 * @param gridLayout
	 */
	public void setGridLayout(GridLayout gridLayout){
		this.grid=gridLayout;
	}
	private GridLayout getGridLayout(){
		return grid;
	}

	
	
	/**
	 * Fruchterman Reingold布局选项卡
	 */
	FruchtermanReingoldLayout reingoldLayout=new FruchtermanReingoldLayout("graph");
	public static ActionList setFruchtermanLayoutAction(ActionList layout,Visualization vis){
		FruchtermanReingoldLayout f=new FruchtermanReingoldLayout("graph",20);
		// ForceDirectedLayout d=new ForceDirectedLayout("graph"); 
		f.setEnabled(true);
		//f.calcPositions(n,nodeItem);
		//	f.setPacingFunction(new ThereAndBackPacer());
		layout.add(new TreeRootAction("graph"));
		layout.add(f);
		layout.add(new RepaintAction());
		//		Point2D anchor = f.getLayoutAnchor();//得到根节点
		//		Iterator iter = vis.visibleItems("graph");
		//		while (iter.hasNext()) {
		//			VisualItem item = (VisualItem) iter.next();
		//			item.setX(anchor.getX());
		//			item.setY(anchor.getY());   //所有节点坐标都是中心点坐标
		//		   System.out.println(item.getStartX()+"-----------"+item.getY());
		//		}
		return layout;

	}
	public void setFruchtermanReingoldLayout(FruchtermanReingoldLayout reingoldLayout){
		this.reingoldLayout=reingoldLayout;
	}
	private FruchtermanReingoldLayout getFruchtermanReingoldLayout(){
		return reingoldLayout;
		
	}
	
	
	/**
	 * 辐射树状布局选项卡
	 */
	RadialTreeLayout radialTreeLayout=new RadialTreeLayout("graph",200);
	public static ActionList setRadialTreeLayoutAction(ActionList layout){
		layout.add(new TreeRootAction("graph"));
		RadialTreeLayout r=new RadialTreeLayout("graph",100);
		
		r.setRadiusIncrement(1000);
		//		TupleSet focus=vis.getGroup("graph");//设置根节点
		//		Iterator iter = focus.tuples();
		//		r.setLayoutRoot((NodeItem) iter.next());
		layout.add(r);
		layout.add(new CollapsedSubtreeLayout("graph"));

		layout.add(new RepaintAction());
		return layout;

	}
	/**
	 * 辐射树状布局
	 * @param radialTreeLayout
	 */
	public void setRadialTreeLayout(RadialTreeLayout radialTreeLayout){
		this.radialTreeLayout=radialTreeLayout;
	}
	private RadialTreeLayout getRadialTreeLayout(){
		return radialTreeLayout;
	}
	
	/**
	 * 节点分级展示选项卡
	 */
	NodeLinkTreeLayout nodelinktree=new NodeLinkTreeLayout("graph");
	public static ActionList setNodeLinkTreeLayoutAction(ActionList layout){
		layout.add(new FisheyeTreeFilter("graph", 80));//添加一个鱼眼树形过滤器    设置初始界面显示几层节点
		layout.add(new FontAction("graph.nodes", FontLib.getFont("微软雅黑", 16)));
		NodeLinkTreeLayout nodelinktree=new NodeLinkTreeLayout("graph");
		nodelinktree.setDepthSpacing(500);
		nodelinktree.setBreadthSpacing(30);
		layout.add(nodelinktree);
		layout.add(new CollapsedSubtreeLayout("graph", Constants.ORIENT_LEFT_RIGHT));
		layout.add(new RepaintAction());
		return layout;

	}
	/**
	 * 树形布局
	 * @param nodeLinkTree
	 */
	public void setNodeLinkTreeLayout(NodeLinkTreeLayout nodeLinkTree){
		this.nodelinktree=nodeLinkTree;
	}
	private NodeLinkTreeLayout getNodeLinkTreeLayout(){
		return nodelinktree;
	}


public static class TreeRootAction extends GroupAction {
		public TreeRootAction(String graphGroup) {
			super(graphGroup);
		}
		public void run(double frac) {
			TupleSet focus = m_vis.getGroup(Visualization.FOCUS_ITEMS);//如果没有点被选中或选中为空则跳出run函数
			if ( focus==null || focus.getTupleCount() == 0 ) return;

			Graph g = (Graph)m_vis.getGroup(m_group);
			Node f = null;
			Iterator tuples = focus.tuples();
			while (tuples.hasNext() && !g.containsTuple(f=(Node)tuples.next()))//迭代选中的焦点，如果选中焦点不是上次的焦点，则置f为空，并跳出run函数
			{
				f = null;
			}
			if ( f == null ) return;
			g.getSpanningTree(f).getSpanningTree().setDirected(true);;//返回一个生成树
		}
	}
}
