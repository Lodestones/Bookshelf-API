package gg.lode.bookshelfapi.api;

import org.bukkit.scheduler.BukkitTask;

public interface TaskHandle {

    void cancel();

    boolean isCancelled();

    static TaskHandle of(BukkitTask task) {
        return new TaskHandle() {
            @Override
            public void cancel() {
                task.cancel();
            }

            @Override
            public boolean isCancelled() {
                return task.isCancelled();
            }
        };
    }

    static TaskHandle of(io.papermc.paper.threadedregions.scheduler.ScheduledTask task) {
        return new TaskHandle() {
            @Override
            public void cancel() {
                task.cancel();
            }

            @Override
            public boolean isCancelled() {
                return task.isCancelled();
            }
        };
    }
}
