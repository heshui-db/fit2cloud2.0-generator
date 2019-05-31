package

#{packagePath}.job;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import org.springframework.stereotype.Component;

@Component
public class SyncJob {
    @QuartzScheduled(cron = "${cron.expression.demo}")
    public void sync() {
        System.out.println("todo");
    }
}
