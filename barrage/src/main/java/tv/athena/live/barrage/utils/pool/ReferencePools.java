package tv.athena.live.barrage.utils.pool;

import java.util.Vector;

/**
 * Created by donghao on 2018/3/14.
 *
 * 采用引用计数方式实现的对象池，一般用于比较复杂，多线程以及嵌套等方式的对象重复使用
 *
 * Example:
 *
 public class PooledObject extends Reuseable{
 static int count = 0;
 int index = 0;
 public PooledObject(){
 index = ++count;
 }
 public String toString(){
 return "PooledObject["+index+"]";
 }

 }

 class PooledObjectFactory implements ReuseablePoolFactory{
@Override
public Reuseable createReuseableObject() {
return new PooledObject();
}
}

 public class PoolTest{

 public void test(String[] args){
 PooledObjectFactory pooledObjectFactory = new PooledObjectFactory();
 ReuseablePool reuseablePool = new ReuseablePool(pooledObjectFactory);
 reuseablePool.setMaxPoolSize(5);

 PooledObject obj1 = (PooledObject) reuseablePool.obtainReuseable();
 System.out.println(obj1 + " is Checked out!");

 PooledObject obj2 = obj1;
 obj2.addRef();

 obj1.release();
 obj1 = (PooledObject) reuseablePool.obtainReuseable();

 System.out.println(obj1 + " is Checked out!");
 obj2.release();

 PooledObject obj3 = (PooledObject) reuseablePool.obtainReuseable();
 System.out.println(obj3 + " is Checked out!");
 }

 }

 运行结果应该是
 PooledObject[5] is Checked out!
 PooledObject[4] is Checked out!
 PooledObject[5] is Checked out!

 每一个引用在自己生命周期维护自己的引用计数，保持前后对应，有一条addRef，必然对应一个release

 *
 */

public class ReferencePools {

    /**
     * 可回收对象抽象类
     */
    public abstract class Reuseable{

        private int mRef = 0;   //引用数量

        private ReuseablePool mOwner = null;    //对应的对象池

        public synchronized void addRef(){
            mRef++;
        }

        public synchronized void release(){
            if(--mRef == 0){
                mOwner.releaseObject(this);
            }
        }

    }

    public static class ReuseablePool{

        private int mMaxSize = 0;

        private Vector<Reuseable> mObjPool = new Vector<Reuseable>();

        private ReuseablePoolFactory mReuseablePoolFactory = null;

        public ReuseablePool(ReuseablePoolFactory factory){
            mReuseablePoolFactory = factory;
        }

        public synchronized void releaseObject(Reuseable obj){
            if(obj != null && obj.mOwner == this){
                mObjPool.add(obj);
            }
        }

        public synchronized Reuseable obtainReuseable(){
            if(mObjPool.size() == 0){
                return null;
            }

            int idx = mObjPool.size() - 1;
            Reuseable obj = mObjPool.get(idx);
            obj.mRef = 1;
            mObjPool.remove(obj);
            return obj;
        }

        public void setMaxPoolSize(int maxSize){
            mMaxSize = maxSize;
            for(int i = 0; i < maxSize; i++){
                Reuseable object = mReuseablePoolFactory.createReuseableObject();
                object.mOwner = this;
                mObjPool.add(object);
            }
        }

    }

    /**
     * 抽象工厂生产Reuseable的接口
     */
    public interface ReuseablePoolFactory{
        Reuseable createReuseableObject();
    }




}
