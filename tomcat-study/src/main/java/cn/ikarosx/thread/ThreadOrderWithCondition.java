package cn.ikarosx.thread;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ikaros
 * @date 2020/07/18 22:26
 */
public class ThreadOrderWithCondition {
  private static final ReentrantLock LOCK = new ReentrantLock();
  private static final Condition A = LOCK.newCondition();
  private static final Condition B = LOCK.newCondition();
  private static final Condition C = LOCK.newCondition();
  private static int a = 1;
  private static final int MAX_VALUE = 100;

  public static void main(String[] args) throws InterruptedException {
    Thread ta =
        new Thread(
            () -> {
              while (a <= MAX_VALUE) {
                LOCK.lock();
                if (a % 3 == 1) {
                  System.out.println(Thread.currentThread().getName() + "\t" + a++);
                  B.signal();
                } else {
                  try {
                    A.await();
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                  }
                }
                LOCK.unlock();
              }
            });
    Thread tb =
        new Thread(
            () -> {
              while (a <= MAX_VALUE) {
                LOCK.lock();
                if (a % 3 == 2) {
                  System.out.println(Thread.currentThread().getName() + "\t" + a++);
                  C.signal();
                } else {
                  try {
                    B.await();
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                  }
                }
                LOCK.unlock();
              }
            });
    Thread tc =
        new Thread(
            () -> {
              while (a <= MAX_VALUE) {
                LOCK.lock();
                if (a % 3 == 0) {
                  System.out.println(Thread.currentThread().getName() + "\t" + a++);
                  A.signal();
                } else {
                  try {
                    C.await();
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                  }
                }
                LOCK.unlock();
              }
            });
    ta.start();
    tb.start();
    tc.start();
    ta.join();
    tb.join();
    tc.join();
  }
}
