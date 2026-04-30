package org.perfect047.storage.streamvalue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class LockEntry {
    final ReentrantLock lock = new ReentrantLock();
    final Condition condition = lock.newCondition();
}
