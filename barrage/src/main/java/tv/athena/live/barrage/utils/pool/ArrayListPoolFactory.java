package tv.athena.live.barrage.utils.pool;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by donghao on 2018/3/12.
 *
 * ArrayList pools, one test
 */

public class ArrayListPoolFactory extends AbsPoolFactory<ArrayList> {

//    private static class TestPoolFactoryHolder{
//        private static final ArrayListPoolFactory instance = new ArrayListPoolFactory();
//    }

    public ArrayListPoolFactory(){
    }

    public ArrayListPoolFactory(int maxSize){
        super(maxSize);
    }

//    public static final ArrayListPoolFactory getDefaultInstance(){
//        return TestPoolFactoryHolder.instance;
//    }

    @Override
    protected ArrayList createObject() {
        Log.d("pool", "createObject new ArrayList()");
        return new ArrayList();
    }

    @Override
    protected void resetObject(ArrayList obj) {
        if( obj != null) {
            obj.clear();
        }
    }
}
