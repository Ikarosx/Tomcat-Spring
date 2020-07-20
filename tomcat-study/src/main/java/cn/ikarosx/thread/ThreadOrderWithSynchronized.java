package cn.ikarosx.thread;

/**
 * @author Ikarosx
 * @date 2020/7/18 20:54
 */
public class ThreadOrderWithSynchronized {
  private static int a = 1;
  private static final Object LOCK = new Object();
  private static final int MAX_VALUE = 100;

  public static void main(String[] args) throws InterruptedException {

    // Thread 1 2 3 轮流输出到100 输出数字不重复 线程必须保持顺序输出
    Thread threadA =
        new Thread(
            () -> {
              while (a <= MAX_VALUE) {
                synchronized (LOCK) {
                  if (a % 3 == 1) {
                    System.out.println(Thread.currentThread().getName() + "\t" + a++);
                    LOCK.notifyAll();
                  } else {
                    try {
                      LOCK.wait();
                    } catch (InterruptedException e) {
                      e.printStackTrace();
                    }
                  }
                }
              }
            });
    Thread threadB =
        new Thread(
            () -> {
              while (a <= MAX_VALUE) {
                synchronized (LOCK) {
                  if (a % 3 == 2) {
                    System.out.println(Thread.currentThread().getName() + "\t" + a++);
                    LOCK.notifyAll();
                  } else {
                    try {
                      LOCK.wait();
                    } catch (InterruptedException e) {
                      e.printStackTrace();
                    }
                  }
                }
              }
            });
    Thread threadC =
        new Thread(
            () -> {
              while (a <= MAX_VALUE) {
                synchronized (LOCK) {
                  if (a % 3 == 0) {
                    System.out.println(Thread.currentThread().getName() + "\t" + a++);
                    LOCK.notifyAll();
                  } else {
                    try {
                      LOCK.wait();
                    } catch (InterruptedException e) {
                      e.printStackTrace();
                    }
                  }
                }
              }
            });
    threadA.start();
    threadB.start();
    threadC.start();
    threadA.join();
    threadB.join();
    threadC.join();
  }
}
