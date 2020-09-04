package network.freecoin.notary.core.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TronNotaryAddressPool {

  private Set<String> addressSet;
  ReadWriteLock setLock;

  public TronNotaryAddressPool() {
    this.addressSet = new HashSet<>();
    this.setLock = new ReentrantReadWriteLock();
  }

  public boolean contain(String addr) {
    setLock.readLock().lock();
    try {
      return addressSet.contains(addr);
    } finally {
      setLock.readLock().unlock();
    }
  }

  public void add(List<String> addrList) {
    setLock.writeLock().lock();
    try {
      addressSet.addAll(addrList);
    } finally {
      setLock.writeLock().unlock();
    }
  }
}