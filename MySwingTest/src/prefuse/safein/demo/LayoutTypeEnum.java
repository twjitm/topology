package prefuse.safein.demo;


public enum LayoutTypeEnum {
	  NODELINKTREELAYOUT(1,"节点边缘树算法"),
	    RADIALTREELAYOUT(2,"径向树算法"),
		FORCEDIRECTEDLAYOUT(3,"定向弹力模型算法"),
	    FRUCHTERMANREINGOLDLAYOUT(4,"莱因戈尔德算法");
	//莱因戈尔德

	private  int  value;
	private  String description;
	LayoutTypeEnum(int value,String description){
		this.setValue(value);
		this.setDescription(description);
	}

	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	  public static LayoutTypeEnum valueOf(int n){
		  	switch (n) {
				case 1:
					return LayoutTypeEnum.NODELINKTREELAYOUT ;
				case 2:
					return LayoutTypeEnum.RADIALTREELAYOUT ;
				case 3:
					return LayoutTypeEnum.FORCEDIRECTEDLAYOUT;
				case 4:
					return LayoutTypeEnum.FRUCHTERMANREINGOLDLAYOUT;
				
				default:
					return null;
				}
		  }
}
