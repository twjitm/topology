package prefuse.safein.demo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public  class TransferData {
   public static Map<String,Object> map = new HashMap<>();
   public  static void putData(String key,String val){
	 /**
	  * �����ֹ�쳣
	  */
	  synchronized (map) {
		  map.remove(key);
	} 
	   map.put(key, val);
   }
   public static Map<String,Object> getData(){
	    return map;
   }
}
