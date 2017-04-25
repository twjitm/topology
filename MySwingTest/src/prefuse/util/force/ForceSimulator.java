package prefuse.util.force;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Manages a simulation of physical forces acting on bodies. To create a
 * custom ForceSimulator, add the desired {@link Force} functions and choose an
 * appropriate {@link Integrator}.管理作用于身体的物理力的模拟。创建一个自定义forcesimulator，添加所需的力的功能和选择合适的积分。
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ForceSimulator {

    private ArrayList items;
    private ArrayList springs;
    private Force[] iforces;
    private Force[] sforces;
    private int iflen, sflen;
    private Integrator integrator;
    private float speedLimit = 1.0f;
    
    /**
     * Create a new, empty ForceSimulator. A RungeKuttaIntegrator is used
     * by default.
     */
    public ForceSimulator() {
        this(new RungeKuttaIntegrator());
    }

    /**
     * Create a new, empty ForceSimulator.
     * @param integr the Integrator to use
     */
    public ForceSimulator(Integrator integr) {
        integrator = integr;
        iforces = new Force[5];
        sforces = new Force[5];
        iflen = 0;
        sflen = 0;
        items = new ArrayList();
        springs = new ArrayList();
    }

    /**
     * Get the speed limit, or maximum velocity value allowed by this
     * simulator.
     * @return the "speed limit" maximum velocity value
     */
    public float getSpeedLimit() {
        return speedLimit;
    }
    
    /**
     * Set the speed limit, or maximum velocity value allowed by this
     * simulator.设置此模拟器所允许的速度限制，或允许的最大速度值。
     * @param limit the "speed limit" maximum velocity value to use
     */
    public void setSpeedLimit(float limit) {
        speedLimit = limit;
    }
    
    /**
     * Get the Integrator used by this simulator.
     * @return the Integrator
     */
    public Integrator getIntegrator() {
        return integrator;
    }
    
    /**
     * Set the Integrator used by this simulator.设置此模拟器所使用的集成器
     * @param intgr the Integrator to use
     */
    public void setIntegrator(Integrator intgr) {
        integrator = intgr;
    }
    
    /**
     * Clear this simulator, removing all ForceItem and Spring instances清除此模拟器，该模拟器的去除所有的forceitem和弹簧的实例。
     * for the simulator.
     */
    public void clear() {
        items.clear();
        Iterator siter = springs.iterator();
        Spring.SpringFactory f = Spring.getFactory();
        while ( siter.hasNext() )
            f.reclaim((Spring)siter.next());
        springs.clear();
    }
    
    /**
     * Add a new Force function to the simulator.
     * 向模拟器中添加一个新的力函数s
     * @param f the Force function to add
     */
    public void addForce(Force f) {
        if ( f.isItemForce() ) {
            if ( iforces.length == iflen ) {
                // resize necessary
                Force[] newf = new Force[iflen+10];
                System.arraycopy(iforces, 0, newf, 0, iforces.length);
                iforces = newf;
            }
            iforces[iflen++] = f;
        }
        if ( f.isSpringForce() ) {
            if ( sforces.length == sflen ) {
                // resize necessary
                Force[] newf = new Force[sflen+10];
                System.arraycopy(sforces, 0, newf, 0, sforces.length);
                sforces = newf;
            }
            sforces[sflen++] = f;
        }
    }
    
    /**
     * Get an array of all the Force functions used in this simulator.
     * 获取此模拟器中使用的所有力函数的数组
     * @return an array of Force functions
     */
    public Force[] getForces() {
        Force[] rv = new Force[iflen+sflen];
        System.arraycopy(iforces, 0, rv, 0, iflen);
        System.arraycopy(sforces, 0, rv, iflen, sflen);
        return rv;
    }
    
    /**
     * Add a ForceItem to the simulation.
     * 添加一个forceitem的仿真。
     * @param item the ForceItem to add
     */
    public void addItem(ForceItem item) {
        items.add(item);
    }
    
    /**
     * Remove a ForceItem to the simulation.
     * @param item the ForceItem to remove
     */
    public boolean removeItem(ForceItem item) {
        return items.remove(item);
    }

    /**
     * Get an iterator over all registered ForceItems.
     * @return an iterator over the ForceItems.
     */
    public Iterator getItems() {
        return items.iterator();
    }
    
    /**
     * Add a Spring to the simulation.
     * @param item1 the first endpoint of the spring
     * t弹簧的第一个终点
     * @param item2 the second endpoint of the spring
     * 弹簧的第二个终点
     * @return the Spring added to the simulation
     */
    public Spring addSpring(ForceItem item1, ForceItem item2) {
        return addSpring(item1, item2, -1.f, -1.f);
    }
    
    /**
     * Add a Spring to the simulation.
     * @param item1 the first endpoint of the spring
     * @param item2 the second endpoint of the spring
     * @param length the spring length
     * @return the Spring added to the simulation
     */
    public Spring addSpring(ForceItem item1, ForceItem item2, float length) {
        return addSpring(item1, item2, -1.f, length);
    }
    
    /**
     * Add a Spring to the simulation.
     * @param item1 the first endpoint of the spring第一个点
     * @param item2 the second endpoint of the spring第二个点
     * @param coeff the spring coefficient系数的弹簧系数
     * @param length the spring length//弹簧的长度
     * @return the Spring added to the simulation
     */
    public Spring addSpring(ForceItem item1, ForceItem item2, float coeff, float length) {
        if ( item1 == null || item2 == null )
            throw new IllegalArgumentException("ForceItems must be non-null");
        Spring s = Spring.getFactory().getSpring(item1, item2, coeff, length);
        springs.add(s);
        return s;
    }
    
    /**
     * Get an iterator over all registered Springs.
     * @return an iterator over the Springs.
     */
    public Iterator getSprings() {
        return springs.iterator();
    }
    
    /**
     * Run the simulator for one timestep.
     * @param timestep the span of the timestep for which to run the simulator
     */
    public void runSimulator(long timestep) {
        accumulate();
        integrator.integrate(this, timestep);//执行的是RungeKuttaIntegrator
    }
    
    /**
     * Accumulate all forces acting on the items in this simulation
     */
    public void accumulate() {
        for ( int i = 0; i < iflen; i++ )
            iforces[i].init(this);
        for ( int i = 0; i < sflen; i++ )
            sforces[i].init(this);
        Iterator itemIter = items.iterator();
        while ( itemIter.hasNext() ) {
            ForceItem item = (ForceItem)itemIter.next();
            item.force[0] = 0.0f; item.force[1] = 0.0f;
            for ( int i = 0; i < iflen; i++ )
                iforces[i].getForce(item);
        }
        Iterator springIter = springs.iterator();
        while ( springIter.hasNext() ) {
            Spring s = (Spring)springIter.next();
            for ( int i = 0; i < sflen; i++ ) {
                sforces[i].getForce(s);
            }
        }
    }
    
} // end of class ForceSimulator
