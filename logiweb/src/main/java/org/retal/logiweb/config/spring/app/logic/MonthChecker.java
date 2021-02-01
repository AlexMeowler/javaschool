package org.retal.logiweb.config.spring.app.logic;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.apache.log4j.Logger;
import org.retal.logiweb.service.logic.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class MonthChecker extends Thread {
  @Autowired
  private UserService userService;

  private Calendar calendar = new GregorianCalendar();

  private static final long SLEEP_TIME_MINUTES = 1;
  
  private static final Logger log = Logger.getLogger(MonthChecker.class);

  @Override
  public void run() {
    while (!isInterrupted()) {
      Calendar now = new GregorianCalendar();
      if (now.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) {
        log.debug("Month changed");
        userService.setUsersWorkedHoursToZero();
      }
      calendar = now;
      try {
        sleep(SLEEP_TIME_MINUTES * 60 * 1000);
      } catch (InterruptedException e) {
        log.info("Trying to stop month checker");
        interrupt();
      }
    }
  }
}
