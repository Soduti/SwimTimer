package se.soduti.products.swimtimer;

/**
 * Created by Larsi on 2016-02-22.
 * IFragment
 */
public interface IFragment {
    void fragmentBecameVisible(int previousFragment);
    void fragmentPageScrolled(int scrollState);
    void addLane();
    void removeLane();
   // void fragmentPageScrollStateChanged(int scrollState);
}